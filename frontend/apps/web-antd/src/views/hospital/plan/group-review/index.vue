<script lang="ts" setup>
import type { PageParam } from '@vben/request';

import ReviewWorkbench from '../_shared/review-workbench.vue';

import {
  approvePlanGroupReview,
  getPlanGroupReview,
  getPlanGroupReviewPage,
  rejectPlanGroupReview,
} from '#/api/hospital/plan-group-review';

type ReviewWorkbenchProps = InstanceType<typeof ReviewWorkbench>['$props'];

const api: NonNullable<ReviewWorkbenchProps['api']> = {
  approve: approvePlanGroupReview,
  getDetail: getPlanGroupReview,
  getPage: (params) => getPlanGroupReviewPage(params as PageParam),
  reject: rejectPlanGroupReview,
};

const config: NonNullable<ReviewWorkbenchProps['config']> = {
  actorLabel: '审核医生',
  actorPlaceholder: '请选择审核医生',
  approveAuth: 'hospital:plan-group-review:approve',
  approvedIndex: 9,
  bizLabel: '审核单号',
  commentLabel: '审核意见',
  commentPlaceholder: '请输入审核意见',
  pageCode: 'plan-group-review',
  rejectAuth: 'hospital:plan-group-review:reject',
  resultLabel: '审核结果',
  reviewingIndex: 8,
  subtitle: '选择左侧审核单后，在右侧查看计划资料、流程进度并完成组长审核。',
  title: '组长审核',
  workflowKey: 'radiotherapy-gz',
};
</script>

<template>
  <ReviewWorkbench :api="api" :config="config" />
</template>
