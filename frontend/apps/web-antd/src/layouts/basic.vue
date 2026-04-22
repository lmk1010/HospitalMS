<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import type { SystemTenantApi } from '#/api/system/tenant';

import { computed, onMounted, ref } from 'vue';

import { useAccess } from '@vben/access';
import { AuthenticationLoginExpiredModal } from '@vben/common-ui';
import { isTenantEnable, useTabs } from '@vben/hooks';
import { AntdProfileOutlined } from '@vben/icons';
import {
  BasicLayout,
  LockScreen,
  Notification,
  TenantDropdown,
  UserDropdown,
} from '@vben/layouts';
import { preferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { formatDateTime } from '@vben/utils';

import { message } from 'ant-design-vue';

import {
  getUnreadNotifyMessageCount,
  getUnreadNotifyMessageList,
  updateAllNotifyMessageRead,
  updateNotifyMessageRead,
} from '#/api/system/notify/message';
import { getSimpleTenantList } from '#/api/system/tenant';
import { $t } from '#/locales';
import { router } from '#/router';
import { useAuthStore } from '#/store';
import LoginForm from '#/views/_core/authentication/login.vue';

const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { hasAccessByCodes } = useAccess();
const { closeOtherTabs } = useTabs();

const notifications = ref<NotificationItem[]>([]);
const unreadCount = ref(0);
const showDot = computed(() => unreadCount.value > 0);

const menus = computed(() => [
  {
    handler: () => {
      router.push({ name: 'Profile' });
    },
    icon: AntdProfileOutlined,
    text: $t('ui.widgets.profile'),
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

async function handleNotificationGetUnreadCount() {
  unreadCount.value = await getUnreadNotifyMessageCount();
}

async function handleNotificationGetList() {
  const list = await getUnreadNotifyMessageList();
  notifications.value = list.map((item) => ({
    avatar: preferences.app.defaultAvatar,
    date: formatDateTime(item.createTime) as string,
    isRead: false,
    id: item.id,
    message: item.templateContent,
    title: item.templateNickname,
  }));
}

function handleNotificationViewAll() {
  router.push({
    name: 'MyNotifyMessage',
  });
}

async function handleNotificationMakeAll() {
  await updateAllNotifyMessageRead();
  unreadCount.value = 0;
  notifications.value = [];
}

async function handleNotificationClear() {
  await handleNotificationMakeAll();
}

async function handleNotificationRead(item: NotificationItem) {
  if (!item.id) {
    return;
  }
  await updateNotifyMessageRead([item.id]);
  await handleNotificationGetUnreadCount();
  notifications.value = notifications.value.filter((n) => n.id !== item.id);
}

function handleNotificationOpen(open: boolean) {
  if (!open) {
    return;
  }
  handleNotificationGetList();
  handleNotificationGetUnreadCount();
}

const tenants = ref<SystemTenantApi.Tenant[]>([]);
const tenantEnable = computed(
  () => hasAccessByCodes(['system:tenant:visit']) && isTenantEnable(),
);

async function handleGetTenantList() {
  if (tenantEnable.value) {
    tenants.value = await getSimpleTenantList();
  }
}

async function handleTenantChange(tenant: SystemTenantApi.Tenant) {
  if (!tenant || !tenant.id) {
    message.error('切换租户失败');
    return;
  }
  accessStore.setVisitTenantId(tenant.id as number);
  await closeOtherTabs();
  window.location.reload();
}

onMounted(async () => {
  await Promise.all([
    handleNotificationGetUnreadCount(),
    handleGetTenantList(),
  ]);
});
</script>

<template>
  <BasicLayout>
    <template #user-dropdown>
      <UserDropdown
        :avatar="avatar"
        :menus="menus"
        :text="userStore.userInfo?.nickname"
        @logout="handleLogout"
      />
    </template>

    <template #notification>
      <Notification
        :count="unreadCount"
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNotificationClear"
        @make-all="handleNotificationMakeAll"
        @open="handleNotificationOpen"
        @read="handleNotificationRead"
        @view-all="handleNotificationViewAll"
      />
    </template>

    <template v-if="tenantEnable" #tenant-dropdown>
      <TenantDropdown
        :tenants="tenants"
        :visit-tenant-id="accessStore.visitTenantId"
        @change="handleTenantChange"
      />
    </template>

    <template #lock-screen>
      <LockScreen />
    </template>
  </BasicLayout>

  <AuthenticationLoginExpiredModal
    v-model:open="accessStore.loginExpired"
    :avatar="avatar"
  >
    <LoginForm />
  </AuthenticationLoginExpiredModal>
</template>
