<script lang="ts" setup>
import type { PageParam } from '@vben/request';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import EntryWorkbench from '../_shared/entry-workbench.vue';

import {
  deletePlanApply,
  getPlanApply,
  getPlanApplyPage,
  submitPlanApply,
  updatePlanApply,
} from '#/api/hospital/plan-apply';

import Form from './modules/form.vue';

type EntryWorkbenchProps = InstanceType<typeof EntryWorkbench>['$props'];

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const workbenchRef = ref<InstanceType<typeof EntryWorkbench>>();

const api: NonNullable<EntryWorkbenchProps['api']> = {
  getDetail: getPlanApply,
  getPage: (params) => getPlanApplyPage(params as PageParam),
  remove: deletePlanApply,
  save: updatePlanApply,
  submit: submitPlanApply,
};

const config: NonNullable<EntryWorkbenchProps['config']> = {
  actorLabel: '申请医生',
  bizLabel: '申请单号',
  createAuth: 'hospital:plan-apply:create',
  createText: '新增计划申请',
  deleteAuth: 'hospital:plan-apply:delete',
  emptyText: '暂无计划申请',
  kind: 'apply',
  listTitle: '计划申请',
  pageCode: 'plan-apply',
  submitAuth: 'hospital:plan-apply:submit',
  subtitle: '选中左侧申请单后，在右侧完成申请维护、查看流程并提交。',
  title: '计划申请',
  updateAuth: 'hospital:plan-apply:update',
  workflowKey: 'radiotherapy-gz',
};

function handleCreate() {
  formModalApi.setData(null).open();
}

function handleSuccess() {
  workbenchRef.value?.reload();
}
</script>

<template>
  <div class="h-full">
    <FormModal @success="handleSuccess" />
    <EntryWorkbench
      ref="workbenchRef"
      :api="api"
      :config="config"
      :on-create="handleCreate"
    />
  </div>
</template>
