<script lang="ts" setup>
import type { BpmModelApi } from '#/api/bpm/model';
import type {
  HospitalWorkflowKey,
  HospitalWorkflowStep,
} from '#/views/hospital/_shared/workflow';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import dayjs from 'dayjs';

import { getModel, getModelList } from '#/api/bpm/model';
import { router } from '#/router';
import { getWorkflowStepsFromModelPath } from '#/views/hospital/_shared/workflow';

interface Props {
  title: string;
  subtitle: string;
  summary: string;
  modelKey: string;
  formPath: string;
  workflowKey: HospitalWorkflowKey;
}
const props = defineProps<Props>();
const loading = ref(false);
const model = ref<BpmModelApi.Model>();
const steps = ref<HospitalWorkflowStep[]>([]);
const deploymentTime = computed(() =>
  model.value?.processDefinition?.deploymentTime
    ? dayjs(model.value.processDefinition.deploymentTime).format(
        'YYYY-MM-DD HH:mm',
      )
    : '--',
);

async function loadData() {
  loading.value = true;
  try {
    const modelList = await getModelList();
    const target = modelList.find(
      (item) =>
        item.key === props.modelKey ||
        item.formCustomCreatePath === props.formPath,
    );
    if (target?.id) model.value = await getModel(String(target.id));
    steps.value = await getWorkflowStepsFromModelPath(
      props.workflowKey,
      props.formPath,
    );
  } finally {
    loading.value = false;
  }
}
function openDesigner() {
  if (!model.value?.id) return;
  router.push({
    name: 'BpmModelUpdate',
    params: { id: model.value.id, type: 'update' },
  });
}
onMounted(() => void loadData());
</script>

<template>
  <Page auto-content-height>
    <div class="wf-page">
      <div class="wf-head">
        <div>
          <div class="wf-title">{{ title }}</div>
          <div class="wf-subtitle">{{ subtitle }}</div>
        </div>
        <a-space>
          <a-tag color="processing">
            {{ model?.category || 'HOSPITAL_WORKFLOW' }}
          </a-tag>
          <a-button type="primary" :disabled="!model?.id" @click="openDesigner">
            打开 BPM 设计器
          </a-button>
        </a-space>
      </div>
      <a-spin :spinning="loading">
        <div class="wf-grid">
          <section class="wf-card">
            <div class="wf-card__title">流程节点</div>
            <div v-for="(item, index) in steps" :key="item.key" class="wf-step">
              <div class="wf-step__index">{{ index + 1 }}</div>
              <div class="wf-step__body">
                <div class="wf-step__title">{{ item.title }}</div>
                <div class="wf-step__desc">{{ item.desc }}</div>
              </div>
            </div>
            <a-empty v-if="steps.length === 0" description="暂无流程节点" />
          </section>
          <section class="wf-card">
            <div class="wf-card__title">流程信息</div>
            <div class="wf-meta">
              <label>流程名称</label><span>{{ model?.name || title }}</span>
            </div>
            <div class="wf-meta">
              <label>流程标识</label><span>{{ model?.key || modelKey }}</span>
            </div>
            <div class="wf-meta">
              <label>发布版本</label>
              <span>V{{ model?.processDefinition?.version || 1 }}</span>
            </div>
            <div class="wf-meta">
              <label>发布时间</label><span>{{ deploymentTime }}</span>
            </div>
            <div class="wf-meta">
              <label>表单路径</label><span>{{ formPath }}</span>
            </div>
            <div class="wf-summary">{{ summary }}</div>
          </section>
        </div>
      </a-spin>
    </div>
  </Page>
</template>

<style scoped>
.wf-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.wf-head,
.wf-grid {
  display: flex;
  gap: 12px;
}
.wf-head {
  justify-content: space-between;
  align-items: center;
}
.wf-grid {
  align-items: flex-start;
}
.wf-card {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  background: #fff;
  padding: 16px;
}
.wf-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}
.wf-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #6b7280;
}
.wf-card__title,
.wf-step__title {
  font-weight: 600;
  color: #111827;
}
.wf-card__title {
  margin-bottom: 12px;
}
.wf-step {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed #e5e7eb;
}
.wf-step:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.wf-step__index {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex: none;
}
.wf-step__desc,
.wf-summary,
.wf-meta {
  font-size: 13px;
  color: #4b5563;
}
.wf-step__desc {
  margin-top: 4px;
  line-height: 1.6;
}
.wf-meta {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px dashed #eef2f7;
}
.wf-meta label {
  color: #6b7280;
  flex: none;
}
.wf-meta span {
  text-align: right;
  color: #111827;
  word-break: break-all;
}
.wf-summary {
  margin-top: 12px;
  line-height: 1.7;
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px;
}
</style>
