<script lang="ts" setup>
import type { BpmModelApi } from '#/api/bpm/model';
import type { HospitalCustomFormApi } from '#/api/hospital/custom-form';

import { onActivated, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  Button,
  InputSearch,
  message,
  Popconfirm,
  Space,
  Switch,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  deleteModel,
  deployModel,
  getModelList,
  updateModelState,
} from '#/api/bpm/model';
import { getCustomFormSimpleList } from '#/api/hospital/custom-form';
import { router } from '#/router';
import { HOSPITAL_FORM_PAGE_OPTIONS } from '#/views/hospital/flow/form-manage/data';

defineOptions({ name: 'HospitalWorkflowManage' });

const loading = ref(false);
const keyword = ref('');
const rows = ref<BpmModelApi.Model[]>([]);
const customForms = ref<HospitalCustomFormApi.CustomForm[]>([]);
const formCountMap = ref<Record<string, number>>({});
const columns = [
  { title: '流程名称', dataIndex: 'name', key: 'name', width: 220 },
  { title: '流程标识', dataIndex: 'key', key: 'key', width: 240 },
  { title: '流程分类', key: 'category', width: 120 },
  { title: '节点数', key: 'nodeCount', width: 80 },
  { title: '表单数', key: 'formCount', width: 80 },
  { title: '版本', key: 'version', width: 80 },
  { title: '状态', key: 'status', width: 120 },
  { title: '更新时间', key: 'updateTime', width: 180 },
  { title: '操作', key: 'actions', width: 300, fixed: 'right' },
];

function isHospitalModel(row: BpmModelApi.Model) {
  return (
    row.key?.startsWith('hospital_') || row.category?.startsWith('HOSPITAL_')
  );
}

function countProcessNodes(node?: any): number {
  if (!node) return 0;
  let selfCount = 0;
  if (
    node.type !== 3 &&
    node.type !== 4 &&
    node.name &&
    node.id !== 'StartUserNode'
  ) {
    selfCount = 1;
  }
  const childCount = countProcessNodes(node.childNode);
  const branchCount = Array.isArray(node.conditionNodes)
    ? node.conditionNodes.reduce(
        (sum: number, item: any) => sum + countProcessNodes(item),
        0,
      )
    : 0;
  return selfCount + childCount + branchCount;
}

type ProcessStep = {
  bizCategory?: string;
  formId?: number;
  id: string;
  name: string;
  pageCode?: string;
  pagePath?: string;
};

function collectProcessSteps(node?: any, steps: ProcessStep[] = []) {
  if (!node) return steps;
  if (
    node.type !== 3 &&
    node.type !== 4 &&
    node.name &&
    node.id !== 'StartUserNode'
  ) {
    steps.push({
      bizCategory: node.hospitalSetting?.bizCategory,
      formId: node.hospitalSetting?.formId,
      id: node.id,
      name: node.name,
      pageCode: node.hospitalSetting?.pageCode,
      pagePath: node.hospitalSetting?.pagePath,
    });
  }
  (node.conditionNodes || []).forEach((item: any) =>
    collectProcessSteps(item, steps),
  );
  collectProcessSteps(node.childNode, steps);
  return steps;
}

function getPageLabel(pageCode?: string) {
  return (
    HOSPITAL_FORM_PAGE_OPTIONS.find((item) => item.value === pageCode)?.label ||
    pageCode ||
    '--'
  );
}

function getStepForm(row: BpmModelApi.Model, step: ProcessStep) {
  if (step.formId) {
    const byId = customForms.value.find((item) => item.id === step.formId);
    if (byId) {
      return byId;
    }
  }
  return customForms.value.find((item) => {
    return (
      `${item.processKey || ''}` === `${row.key || ''}` &&
      `${item.nodeKey || ''}` === `${step.id || ''}` &&
      (!step.pageCode || `${item.pageCode || ''}` === `${step.pageCode || ''}`)
    );
  });
}

function getProcessSteps(row: BpmModelApi.Model) {
  return collectProcessSteps((row as any).simpleModel).map((step) => ({
    ...step,
    form: getStepForm(row, step),
  }));
}

async function loadList() {
  loading.value = true;
  try {
    const [list, forms] = await Promise.all([
      getModelList(keyword.value.trim() || undefined),
      getCustomFormSimpleList(),
    ]);
    const nextRows = list.filter((row) => isHospitalModel(row));
    const nextFormCountMap: Record<string, number> = {};
    (forms || []).forEach((item) => {
      const key = `${item.processKey || ''}`;
      if (!key) return;
      nextFormCountMap[key] = (nextFormCountMap[key] || 0) + 1;
    });
    customForms.value = forms || [];
    formCountMap.value = nextFormCountMap;
    rows.value = nextRows;
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  router.push({ name: 'HospitalWorkflowDesigner' });
}
function handleEdit(row: BpmModelApi.Model) {
  router.push({
    name: 'HospitalWorkflowDesigner',
    query: { id: row.id, type: 'update' },
  });
}
function handleCopy(row: BpmModelApi.Model) {
  router.push({
    name: 'HospitalWorkflowDesigner',
    query: { id: row.id, type: 'copy' },
  });
}
function handleBindForm(row: BpmModelApi.Model) {
  router.push({
    path: '/hospital-flow/form-manage',
    query: { processKey: row.key, processName: row.name },
  });
}
async function handleDeploy(row: BpmModelApi.Model) {
  await deployModel(row.id as any);
  message.success(`已发布 ${row.name}`);
  loadList();
}
async function handleDelete(row: BpmModelApi.Model) {
  await deleteModel(row.id as any);
  message.success(`已删除 ${row.name}`);
  loadList();
}
async function handleToggle(row: BpmModelApi.Model, checked: boolean) {
  await updateModelState(row.id as any, checked ? 1 : 2);
  message.success(`${checked ? '已启用' : '已停用'} ${row.name}`);
  loadList();
}
function getStatus(row: BpmModelApi.Model) {
  if (!row.processDefinition)
    return { checked: false, text: '未发布', toggle: false };
  const checked = row.processDefinition.suspensionState === 1;
  return { checked, text: checked ? '启用' : '停用', toggle: true };
}
function getCategoryText(row: BpmModelApi.Model) {
  return (
    {
      HOSPITAL_WORKFLOW: '放疗流程',
      HOSPITAL_POSITION: '定位管理',
      HOSPITAL_TREATMENT: '治疗管理',
      HOSPITAL_FEE: '费用管理',
    }[row.category || ''] ||
    row.category ||
    '--'
  );
}
function getNodeCount(row: BpmModelApi.Model) {
  return countProcessNodes((row as any).simpleModel);
}
function getFormCount(row: BpmModelApi.Model) {
  return formCountMap.value[row.key || ''] || 0;
}
function getUpdateTime(row: BpmModelApi.Model) {
  return (
    row.processDefinition?.deploymentTime || row.updateTime || row.createTime
  );
}

onMounted(loadList);
onActivated(loadList);
</script>

<template>
  <Page auto-content-height>
    <div class="bg-white px-4 py-3">
      <div class="mb-3 flex items-center justify-between gap-3">
        <div class="text-base font-semibold">流程管理</div>
        <Space :size="8">
          <InputSearch
            v-model:value="keyword"
            allow-clear
            placeholder="请输入流程名称关键字"
            style="width: 240px"
            @search="loadList"
          />
          <Button type="primary" @click="handleCreate">新建</Button>
        </Space>
      </div>
      <Table
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="{ pageSize: 20, showSizeChanger: false }"
        :scroll="{ x: 1380 }"
        row-key="id"
        size="small"
      >
        <template #expandedRowRender="{ record }">
          <div class="process-step-list">
            <div
              v-for="(step, index) in getProcessSteps(record)"
              :key="step.id"
              class="process-step-card"
            >
              <div class="process-step-badge">{{ index + 1 }}</div>
              <div class="process-step-name">{{ step.name }}</div>
              <div class="process-step-meta">
                {{ step.bizCategory || '--' }}
              </div>
              <div class="process-step-meta">
                页面：{{ getPageLabel(step.pageCode) }}
              </div>
              <div class="process-step-meta">
                表单：{{ step.form?.name || '未绑定' }}
              </div>
            </div>
          </div>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'key'">
            <span class="font-mono text-[12px] text-gray-600">{{
              record.key
            }}</span>
          </template>
          <template v-else-if="column.key === 'category'">
            <Tag color="blue">{{ getCategoryText(record) }}</Tag>
          </template>
          <template v-else-if="column.key === 'nodeCount'">
            {{ getNodeCount(record) }}
          </template>
          <template v-else-if="column.key === 'formCount'">
            {{ getFormCount(record) }}
          </template>
          <template v-else-if="column.key === 'version'">
            V{{ record.processDefinition?.version || 0 }}
          </template>
          <template v-else-if="column.key === 'status'">
            <div class="flex items-center gap-2">
              <Switch
                v-if="getStatus(record).toggle"
                :checked="getStatus(record).checked"
                size="small"
                @change="(checked) => handleToggle(record, checked as boolean)"
              /><Tag
                :color="getStatus(record).checked ? 'processing' : 'default'"
              >
                {{ getStatus(record).text }}
              </Tag>
            </div>
          </template>
          <template v-else-if="column.key === 'updateTime'">
            {{ formatDateTime(getUpdateTime(record)) || '--' }}
          </template>
          <template v-else-if="column.key === 'actions'">
            <Space :size="2" wrap>
              <Button type="link" size="small" @click="handleEdit(record)">
                设计
              </Button>
              <Button
                v-if="!record.processDefinition"
                type="link"
                size="small"
                @click="handleDeploy(record)"
              >
                发布
              </Button>
              <Button type="link" size="small" @click="handleBindForm(record)">
                关联表单
              </Button>
              <Button type="link" size="small" @click="handleCopy(record)">
                复制
              </Button>
              <Popconfirm
                :title="`确认删除 ${record.name} 吗？`"
                @confirm="handleDelete(record)"
              >
                <Button danger type="link" size="small">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>

<style scoped>
.process-step-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 4px 0;
}

.process-step-card {
  position: relative;
  min-width: 180px;
  max-width: 220px;
  padding: 12px 12px 10px 16px;
  border: 1px solid rgb(5 5 5 / 8%);
  border-radius: 10px;
  background: linear-gradient(180deg, #fff 0%, #f8fbff 100%);
}

.process-step-badge {
  position: absolute;
  top: -8px;
  left: 10px;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
}

.process-step-name {
  margin-top: 2px;
  color: #111827;
  font-size: 13px;
  font-weight: 600;
}

.process-step-meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.4;
}
</style>
