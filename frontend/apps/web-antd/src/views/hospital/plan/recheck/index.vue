<script lang="ts" setup>
import type { PageParam } from '@vben/request';

import ReviewWorkbench from '../_shared/review-workbench.vue';

import {
  approvePlanRecheck,
  getPlanRecheck,
  getPlanRecheckPage,
  rejectPlanRecheck,
} from '#/api/hospital/plan-recheck';

type ReviewWorkbenchProps = InstanceType<typeof ReviewWorkbench>['$props'];

const api: NonNullable<ReviewWorkbenchProps['api']> = {
  approve: approvePlanRecheck,
  getDetail: getPlanRecheck,
  getPage: (params) => getPlanRecheckPage(params as PageParam),
  reject: rejectPlanRecheck,
};

const config: NonNullable<ReviewWorkbenchProps['config']> = {
  actorLabel: '复核人',
  actorPlaceholder: '请选择复核人',
  approveAuth: 'hospital:plan-recheck:approve',
  approvedIndex: 11,
  bizLabel: '复核单号',
  commentLabel: '复核意见',
  commentPlaceholder: '请输入复核意见',
  pageCode: 'plan-recheck',
  rejectAuth: 'hospital:plan-recheck:reject',
  resultLabel: '复核结果',
  reviewingIndex: 10,
  subtitle: '选择左侧复核单后，在右侧查看计划资料、流程进度并完成计划复核。',
  title: '计划复核',
  workflowKey: 'radiotherapy-gz',
};
</script>

<template>
  <ReviewWorkbench :api="api" :config="config" />
</template>
