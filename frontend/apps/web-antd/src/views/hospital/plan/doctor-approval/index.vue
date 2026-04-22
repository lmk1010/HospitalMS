<script lang="ts" setup>
import type { PageParam } from '@vben/request';

import ReviewWorkbench from '../_shared/review-workbench.vue';

import {
  approvePlanDoctorApproval,
  getPlanDoctorApproval,
  getPlanDoctorApprovalPage,
  rejectPlanDoctorApproval,
} from '#/api/hospital/plan-doctor-approval';

type ReviewWorkbenchProps = InstanceType<typeof ReviewWorkbench>['$props'];

const api: NonNullable<ReviewWorkbenchProps['api']> = {
  approve: approvePlanDoctorApproval,
  getDetail: getPlanDoctorApproval,
  getPage: (params) => getPlanDoctorApprovalPage(params as PageParam),
  reject: rejectPlanDoctorApproval,
};

const config: NonNullable<ReviewWorkbenchProps['config']> = {
  actorLabel: '核准医生',
  actorPlaceholder: '请选择核准医生',
  approveAuth: 'hospital:plan-doctor-approval:approve',
  approvedIndex: 10,
  bizLabel: '核准单号',
  commentLabel: '核准意见',
  commentPlaceholder: '请输入核准意见',
  pageCode: 'plan-doctor-approval',
  rejectAuth: 'hospital:plan-doctor-approval:reject',
  resultLabel: '核准结果',
  reviewingIndex: 9,
  subtitle: '选择左侧核准单后，在右侧查看计划资料、流程进度并完成医师核准。',
  title: '医师核准',
  workflowKey: 'radiotherapy-gz',
};
</script>

<template>
  <ReviewWorkbench :api="api" :config="config" />
</template>
