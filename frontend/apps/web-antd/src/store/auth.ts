import type { AuthPermissionInfo, Recordable, UserInfo } from '@vben/types';

import type { AuthApi } from '#/api';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import dayjs from 'dayjs';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { notification } from 'ant-design-vue';
import { defineStore } from 'pinia';

import {
  getAuthPermissionInfoApi,
  loginApi,
  logoutApi,
  refreshTokenApi,
  register,
  smsLogin,
  socialLogin,
} from '#/api';
import { $t } from '#/locales';

const SESSION_REFRESH_ADVANCE_MS = 30_000;

let sessionRefreshPromise: null | Promise<string> = null;
let sessionRefreshTimer: null | ReturnType<typeof setTimeout> = null;

function unwrapApiPayload<T>(payload: any): T {
  if (payload && typeof payload === 'object') {
    const nestedPayload = payload.data;
    if (
      nestedPayload &&
      typeof nestedPayload === 'object' &&
      'code' in nestedPayload
    ) {
      return (nestedPayload.data ?? null) as T;
    }
    if ('code' in payload) {
      return (payload.data ?? null) as T;
    }
  }
  return (payload ?? null) as T;
}

function normalizeExpiresAt(expiresTime: unknown): null | number {
  if (!expiresTime) {
    return null;
  }
  if (typeof expiresTime === 'number') {
    return expiresTime > 1e12 ? expiresTime : expiresTime * 1000;
  }
  if (expiresTime instanceof Date) {
    return expiresTime.getTime();
  }
  if (Array.isArray(expiresTime)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = expiresTime;
    const timestamp = new Date(
      Number(year),
      Number(month) - 1,
      Number(day),
      Number(hour),
      Number(minute),
      Number(second),
    ).getTime();
    return Number.isNaN(timestamp) ? null : timestamp;
  }
  const timestamp = dayjs(expiresTime as string).valueOf();
  return Number.isNaN(timestamp) ? null : timestamp;
}

function clearSessionRefreshTimer() {
  if (sessionRefreshTimer != null) {
    clearTimeout(sessionRefreshTimer);
    sessionRefreshTimer = null;
  }
}

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();

  const loginLoading = ref(false);

  function scheduleSessionRefresh() {
    clearSessionRefreshTimer();
    if (!accessStore.accessToken || !accessStore.refreshToken) {
      return;
    }

    const expiresAt = accessStore.accessTokenExpiresAt;
    const delay = expiresAt
      ? Math.max(expiresAt - Date.now() - SESSION_REFRESH_ADVANCE_MS, 0)
      : 0;

    sessionRefreshTimer = setTimeout(() => {
      void handleScheduledRefresh();
    }, delay);
  }

  function syncLoginTokens(loginResult?: null | Partial<AuthApi.LoginResult>) {
    if (!loginResult?.accessToken) {
      return;
    }
    accessStore.setAccessToken(loginResult.accessToken);
    accessStore.setRefreshToken(loginResult.refreshToken ?? accessStore.refreshToken);
    accessStore.setAccessTokenExpiresAt(normalizeExpiresAt(loginResult.expiresTime));
    scheduleSessionRefresh();
  }

  async function refreshSessionToken() {
    if (sessionRefreshPromise) {
      return sessionRefreshPromise;
    }

    const refreshToken = accessStore.refreshToken as string;
    if (!refreshToken) {
      throw new Error('Refresh token is null!');
    }

    sessionRefreshPromise = (async () => {
      const response = await refreshTokenApi(refreshToken);
      const loginResult = unwrapApiPayload<AuthApi.LoginResult | null>(response);
      if (!loginResult?.accessToken) {
        throw response?.data ?? new Error('Refresh token failed');
      }
      syncLoginTokens({
        accessToken: loginResult.accessToken,
        expiresTime: loginResult.expiresTime,
        refreshToken: loginResult.refreshToken || refreshToken,
      });
      if (accessStore.loginExpired) {
        accessStore.setLoginExpired(false);
      }
      return loginResult.accessToken;
    })();

    try {
      return await sessionRefreshPromise;
    } finally {
      sessionRefreshPromise = null;
    }
  }

  async function handleScheduledRefresh() {
    clearSessionRefreshTimer();
    if (!accessStore.accessToken || !accessStore.refreshToken) {
      return;
    }
    try {
      await refreshSessionToken();
    } catch {
      await logout();
    }
  }

  function initSessionMonitor() {
    if (!accessStore.accessToken) {
      clearSessionRefreshTimer();
      return;
    }
    scheduleSessionRefresh();
  }

  /**
   * 异步处理登录操作
   * Asynchronously handle the login process
   * @param type 登录类型
   * @param params 登录表单数据
   * @param onSuccess 登录成功后的回调函数
   */
  async function authLogin(
    type: 'mobile' | 'register' | 'social' | 'username',
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    let userInfo: null | UserInfo = null;
    try {
      let loginResult: AuthApi.LoginResult;
      loginLoading.value = true;
      switch (type) {
        case 'mobile': {
          loginResult = await smsLogin(params as AuthApi.SmsLoginParams);
          break;
        }
        case 'register': {
          loginResult = await register(params as AuthApi.RegisterParams);
          break;
        }
        case 'social': {
          loginResult = await socialLogin(params as AuthApi.SocialLoginParams);
          break;
        }
        default: {
          loginResult = await loginApi(params);
        }
      }
      syncLoginTokens(loginResult);

      if (loginResult.accessToken) {
        const fetchUserInfoResult = await fetchUserInfo();
        userInfo = fetchUserInfoResult?.user ?? null;

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else if (userInfo) {
          onSuccess
            ? await onSuccess?.()
            : await router.push(
                userInfo.homePath || preferences.app.defaultHomePath,
              );
        }

        if (userInfo?.nickname) {
          notification.success({
            description: `${$t('authentication.loginSuccessDesc')}:${userInfo.nickname}`,
            duration: 3,
            message: $t('authentication.loginSuccess'),
          });
        }
      }
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  async function logout(redirect: boolean = true) {
    clearSessionRefreshTimer();
    try {
      const accessToken = accessStore.accessToken as string;
      if (accessToken) {
        await logoutApi(accessToken);
      }
    } catch {
      // 不做任何处理
    }
    resetAllStores();
    accessStore.setAccessTokenExpiresAt(null);
    accessStore.setLoginExpired(false);

    await router.replace({
      path: LOGIN_PATH,
      query: redirect
        ? {
            redirect: encodeURIComponent(router.currentRoute.value.fullPath),
          }
        : {},
    });
  }

  async function fetchUserInfo() {
    const authPermissionInfo = unwrapApiPayload<AuthPermissionInfo | null>(
      await getAuthPermissionInfoApi(),
    );
    if (!authPermissionInfo) {
      accessStore.setAccessMenus([]);
      accessStore.setAccessCodes([]);
      return null;
    }
    userStore.setUserInfo(authPermissionInfo.user);
    userStore.setUserRoles(Array.from(authPermissionInfo.roles ?? []));
    accessStore.setAccessMenus(
      Array.isArray(authPermissionInfo.menus) ? authPermissionInfo.menus : [],
    );
    accessStore.setAccessCodes(
      Array.from(authPermissionInfo.permissions ?? []),
    );
    return authPermissionInfo;
  }

  function $reset() {
    clearSessionRefreshTimer();
    loginLoading.value = false;
  }

  return {
    $reset,
    authLogin,
    fetchUserInfo,
    initSessionMonitor,
    loginLoading,
    logout,
    refreshSessionToken,
    syncLoginTokens,
  };
});
