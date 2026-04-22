<script lang="ts" setup>
import type { PageParam } from '@vben/request';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import EntryWorkbench from '../_shared/entry-workbench.vue';

import {
  deletePlanDesign,
  getPlanDesign,
  getPlanDesignPage,
  submitPlanDesign,
  updatePlanDesign,
} from '#/api/hospital/plan-design';

import Form from './modules/form.vue';

type EntryWorkbenchProps = InstanceType<typeof EntryWorkbench>['$props'];

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const workbenchRef = ref<InstanceType<typeof EntryWorkbench>>();

const api: NonNullable<EntryWorkbenchProps['api']> = {
  getDetail: getPlanDesign,
  getPage: (params) => getPlanDesignPage(params as PageParam),
  remove: deletePlanDesign,
  save: updatePlanDesign,
  submit: submitPlanDesign,
};

const config: NonNullable<EntryWorkbenchProps['config']> = {
  actorLabel: '设计人员',
  bizLabel: '设计单号',
  createAuth: 'hospital:plan-design:create',
  createText: '新增计划设计',
  deleteAuth: 'hospital:plan-design:delete',
  emptyText: '暂无计划设计',
  kind: 'design',
  listTitle: '计划设计',
  pageCode: 'plan-design',
  submitAuth: 'hospital:plan-design:submit',
  subtitle: '选中左侧设计单后，在右侧完成设计维护、查看流程并提交。',
  title: '计划设计',
  updateAuth: 'hospital:plan-design:update',
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
