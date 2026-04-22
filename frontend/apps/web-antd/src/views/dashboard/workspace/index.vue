<script lang="ts" setup>
import type {
  WorkbenchProjectItem,
  WorkbenchQuickNavItem,
  WorkbenchTodoItem,
  WorkbenchTrendItem,
} from '@vben/common-ui';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  AnalysisChartCard,
  WorkbenchHeader,
  WorkbenchProject,
  WorkbenchQuickNav,
  WorkbenchTodo,
  WorkbenchTrends,
} from '@vben/common-ui';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';

import AnalyticsVisitsSource from '../analytics/analytics-visits-source.vue';

const userStore = useUserStore();
const router = useRouter();

const projectItems: WorkbenchProjectItem[] = [
  {
    color: '#1677ff',
    content: '挂号、门诊、住院、医技一体化协同',
    date: '2026-04-19',
    group: '核心业务',
    icon: 'healthicons:hospital-outline',
    title: '门诊住院协同',
    url: '/system/user',
  },
  {
    color: '#13c2c2',
    content: '流程申请、审批、抄送、催办统一管理',
    date: '2026-04-19',
    group: '工作流',
    icon: 'carbon:workflow-automation',
    title: 'BPM 审批中心',
    url: '/bpm',
  },
  {
    color: '#52c41a',
    content: '患者信息、预约记录、治疗计划统一归档',
    date: '2026-04-19',
    group: '病案管理',
    icon: 'mdi:file-account-outline',
    title: '患者全生命周期',
    url: '/system/dept',
  },
  {
    color: '#fa8c16',
    content: '权限、字典、通知、日志、操作审计全覆盖',
    date: '2026-04-19',
    group: '平台底座',
    icon: 'mdi:shield-check-outline',
    title: '系统治理',
    url: '/system/role',
  },
];

const quickNavItems: WorkbenchQuickNavItem[] = [
  {
    color: '#1677ff',
    icon: 'healthicons:outpatient-outline',
    title: '门诊工作台',
    url: '/dashboard/workspace',
  },
  {
    color: '#52c41a',
    icon: 'mdi:account-group-outline',
    title: '患者管理',
    url: '/system/user',
  },
  {
    color: '#722ed1',
    icon: 'carbon:workflow-automation',
    title: '流程审批',
    url: '/bpm',
  },
  {
    color: '#fa8c16',
    icon: 'mdi:calendar-check-outline',
    title: '预约排班',
    url: '/infra/job',
  },
  {
    color: '#13c2c2',
    icon: 'mdi:bell-outline',
    title: '消息通知',
    url: '/system/notify/my',
  },
  {
    color: '#eb2f96',
    icon: 'mdi:chart-line',
    title: '运营统计',
    url: '/dashboard/analytics',
  },
];

const todoItems = ref<WorkbenchTodoItem[]>([
  {
    completed: false,
    content: '完善门诊预约、检查申请、治疗执行等关键流程节点。',
    date: '2026-04-19 09:00:00',
    title: '门诊流程梳理',
  },
  {
    completed: false,
    content: '确认本地库、Redis、BPM 流程定义与审批角色配置。',
    date: '2026-04-19 10:00:00',
    title: '基础环境巡检',
  },
  {
    completed: false,
    content: '新增患者、治疗计划、收费结算等医院业务菜单。',
    date: '2026-04-19 11:00:00',
    title: '业务菜单建设',
  },
  {
    completed: false,
    content: '统一首页、登录页、关于页的医院品牌和文案风格。',
    date: '2026-04-19 13:30:00',
    title: '品牌视觉收口',
  },
]);

const trendItems: WorkbenchTrendItem[] = [
  {
    avatar: 'svg:avatar-1',
    content: '完成 <a>本地环境初始化</a>，数据库与缓存已连通。',
    date: '刚刚',
    title: '实施工程师',
  },
  {
    avatar: 'svg:avatar-2',
    content: '提交 <a>BPM 审批中心</a> 首版流程配置方案。',
    date: '30分钟前',
    title: '流程管理员',
  },
  {
    avatar: 'svg:avatar-3',
    content: '更新 <a>患者管理</a> 菜单与权限模型。',
    date: '1小时前',
    title: '产品经理',
  },
  {
    avatar: 'svg:avatar-4',
    content: '完成 <a>登录页品牌替换</a> 与推广信息清理。',
    date: '2小时前',
    title: '前端开发',
  },
];

function navTo(nav: WorkbenchProjectItem | WorkbenchQuickNavItem) {
  if (nav.url?.startsWith('/')) {
    router.push(nav.url).catch(() => {});
  }
}
</script>

<template>
  <div class="p-5">
    <WorkbenchHeader
      :avatar="userStore.userInfo?.avatar || preferences.app.defaultAvatar"
    >
      <template #title>
        您好，{{ userStore.userInfo?.nickname }}，欢迎进入上海XX医院管理平台。
      </template>
      <template #description>
        今日重点：患者接诊、流程审批、排班协同与运行监控。
      </template>
    </WorkbenchHeader>

    <div class="mt-5 flex flex-col lg:flex-row">
      <div class="mr-4 w-full lg:w-3/5">
        <WorkbenchProject :items="projectItems" title="重点模块" @click="navTo" />
        <WorkbenchTrends :items="trendItems" class="mt-5" title="实施动态" />
      </div>
      <div class="w-full lg:w-2/5">
        <WorkbenchQuickNav
          :items="quickNavItems"
          class="mt-5 lg:mt-0"
          title="快捷入口"
          @click="navTo"
        />
        <WorkbenchTodo :items="todoItems" class="mt-5" title="今日待办" />
        <AnalysisChartCard class="mt-5" title="访问来源">
          <AnalyticsVisitsSource />
        </AnalysisChartCard>
      </div>
    </div>
  </div>
</template>
