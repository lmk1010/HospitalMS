<script lang="ts" setup>
import type { PageParam } from '@vben/request';

import VerifyWorkbench from '../_shared/verify-workbench.vue';

import {
  getPlanVerify,
  getPlanVerifyPage,
  rejectPlanVerify,
  verifyPlan,
} from '#/api/hospital/plan-verify';

type VerifyWorkbenchProps = InstanceType<typeof VerifyWorkbench>['$props'];

const api: NonNullable<VerifyWorkbenchProps['api']> = {
  approve: verifyPlan,
  getDetail: getPlanVerify,
  getPage: (params) => getPlanVerifyPage(params as PageParam),
  reject: rejectPlanVerify,
};

const config: NonNullable<VerifyWorkbenchProps['config']> = {
  actorLabel: '验证人',
  actorPlaceholder: '请选择验证人',
  approveAuth: 'hospital:plan-verify:verify',
  approveText: '验证通过',
  approvedIndex: 12,
  bizLabel: '验证单号',
  commentLabel: '报告地址',
  commentPlaceholder: '请输入验证报告地址',
  pageCode: 'plan-verify',
  deviceLabel: '验证设备',
  devicePlaceholder: '请输入验证设备名称',
  rejectAuth: 'hospital:plan-verify:reject',
  rejectText: '验证不通过',
  resultLabel: '验证结果',
  reviewingIndex: 11,
  subtitle: '选择左侧验证单后，在右侧查看计划资料、流程进度并完成计划验证。',
  title: '计划验证',
  workflowKey: 'radiotherapy-gz',
};
</script>

<template>
  <VerifyWorkbench :api="api" :config="config" />
</template>
