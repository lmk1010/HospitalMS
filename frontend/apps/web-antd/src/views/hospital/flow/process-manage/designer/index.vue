<script lang="ts" setup>
import type { BpmCategoryApi } from '#/api/bpm/category';
import type { BpmUserGroupApi } from '#/api/bpm/userGroup';
import type { SystemDeptApi } from '#/api/system/dept';
import type { SystemPostApi } from '#/api/system/post';
import type { SystemRoleApi } from '#/api/system/role';
import type { SystemUserApi } from '#/api/system/user';
import type { SimpleFlowNode } from '#/views/bpm/components/simple-process-design';

import { computed, provide, ref, watch } from 'vue';

import { confirm, Page } from '@vben/common-ui';
import {
  BpmAutoApproveType,
  BpmModelFormType,
  BpmModelType,
  BpmNodeTypeEnum,
} from '@vben/constants';
import { useTabs } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { useUserStore } from '@vben/stores';
import { handleTree } from '@vben/utils';

import {
  Button,
  Col,
  Form,
  Input,
  message,
  Row,
  Select,
  Spin,
} from 'ant-design-vue';

import { getCategorySimpleList } from '#/api/bpm/category';
import { getForm } from '#/api/bpm/form';
import {
  createModel,
  deployModel,
  getModel,
  updateModel,
} from '#/api/bpm/model';
import { getUserGroupSimpleList } from '#/api/bpm/userGroup';
import { getSimpleDeptList } from '#/api/system/dept';
import { getSimplePostList } from '#/api/system/post';
import { getSimpleRoleList } from '#/api/system/role';
import { getSimpleUserList } from '#/api/system/user';
import { router } from '#/router';
import SimpleProcessModel from '#/views/bpm/components/simple-process-design/components/simple-process-model.vue';
import { NodeId } from '#/views/bpm/components/simple-process-design/consts';

import NodeConfigPanel from './node-config-panel.vue';

import '#/views/bpm/components/simple-process-design/styles/simple-process-designer.scss';

defineOptions({ name: 'HospitalWorkflowDesigner' });

const props = withDefaults(
  defineProps<{
    id?: number | string;
    nodeId?: number | string;
    type?: 'copy' | 'update';
  }>(),
  {
    id: undefined,
    nodeId: undefined,
    type: 'update',
  },
);

const HOSPITAL_CATEGORY_NAME_MAP: Record<string, string> = {
  HOSPITAL_FEE: '费用管理',
  HOSPITAL_POSITION: '定位管理',
  HOSPITAL_TREATMENT: '治疗管理',
  HOSPITAL_WORKFLOW: '放疗业务流程',
};

const tabs = useTabs();
const userStore = useUserStore();
const loading = ref(true);
const designerReady = ref(false);
const designerOptionsLoaded = ref(false);
const saveLoading = ref(false);
const deployLoading = ref(false);
const canvasRef = ref<any>();
const formRef = ref();
const categoryList = ref<BpmCategoryApi.Category[]>([]);
const processNodeTree = ref<SimpleFlowNode>();
const selectedNode = ref<SimpleFlowNode>();
const designerConfigMode = ref('sidebar');
const formFields = ref<string[]>([]);
const roleOptions = ref<SystemRoleApi.Role[]>([]);
const postOptions = ref<SystemPostApi.Post[]>([]);
const userOptions = ref<SystemUserApi.User[]>([]);
const deptOptions = ref<SystemDeptApi.Dept[]>([]);
const deptTreeOptions = ref<SystemDeptApi.Dept[]>([]);
const userGroupOptions = ref<BpmUserGroupApi.UserGroup[]>([]);
const tasks = ref<any[]>([]);
const processInstance = ref<Record<string, any>>({});

const actionType = computed(() =>
  props.id ? props.type || 'update' : 'create',
);
const currentUserId = computed(() => userStore.userInfo?.id);
const formType = computed(() => formData.value.formType);
const startUserIds = computed(() => formData.value.startUserIds ?? []);
const startDeptIds = computed(() => formData.value.startDeptIds ?? []);
const categoryOptions = computed(() => {
  return categoryList.value.map((item) => ({
    ...item,
    displayName:
      HOSPITAL_CATEGORY_NAME_MAP[item.code] || item.name || item.code,
  }));
});

function buildDefaultFormData() {
  return {
    id: undefined,
    key: '',
    name: '',
    icon: undefined,
    description: '',
    category: 'HOSPITAL_WORKFLOW',
    type: BpmModelType.SIMPLE,
    formType: BpmModelFormType.CUSTOM,
    formId: undefined,
    formCustomCreatePath: '',
    formCustomViewPath: '',
    visible: true,
    startUserIds: [],
    startDeptIds: [],
    managerUserIds: currentUserId.value ? [currentUserId.value] : [],
    allowCancelRunningProcess: true,
    allowWithdrawTask: false,
    processIdRule: {
      enable: false,
      prefix: '',
      infix: '',
      postfix: '',
      length: 5,
    },
    autoApprovalType: BpmAutoApproveType.NONE,
    titleSetting: {
      enable: false,
      title: '',
    },
    summarySetting: {
      enable: false,
      summary: [],
    },
    simpleModel: undefined,
  };
}

const formData = ref<any>(buildDefaultFormData());

provide('formFields', formFields);
provide('formType', formType);
provide('roleList', roleOptions);
provide('postList', postOptions);
provide('userList', userOptions);
provide('deptList', deptOptions);
provide('userGroupList', userGroupOptions);
provide('deptTree', deptTreeOptions);
provide('startUserIds', startUserIds);
provide('startDeptIds', startDeptIds);
provide('tasks', tasks);
provide('processInstance', processInstance);
provide('processNodeTree', processNodeTree);
provide('selectedNode', selectedNode);
provide('designerConfigMode', designerConfigMode);
provide('modelFormData', formData);

const rules: any = {
  key: [
    { required: true, message: '请输入流程标识', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (!value) return Promise.resolve();
        if (!/^[a-z_][-\w.$]*$/i.test(value)) {
          return Promise.reject(
            new Error(
              '只能包含字母、数字、下划线、连字符和点号，且必须以字母或下划线开头',
            ),
          );
        }
        return Promise.resolve();
      },
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入流程名称', trigger: 'blur' }],
};

function createDefaultProcessNode(): SimpleFlowNode {
  return {
    childNode: {
      id: NodeId.END_EVENT_NODE_ID,
      name: '结束',
      type: BpmNodeTypeEnum.END_EVENT_NODE,
    },
    id: NodeId.START_USER_NODE_ID,
    name: '发起人',
    showText: '提交业务申请',
    type: BpmNodeTypeEnum.START_USER_NODE,
  };
}

function normalizeFormData(model: any) {
  const defaults = buildDefaultFormData();
  return {
    ...defaults,
    ...model,
    type: BpmModelType.SIMPLE,
    formType: model?.formType ?? defaults.formType,
    visible: model?.visible ?? defaults.visible,
    startUserIds: model?.startUserIds ?? [],
    startDeptIds: model?.startDeptIds ?? [],
    managerUserIds:
      model?.managerUserIds?.length > 0
        ? model.managerUserIds
        : defaults.managerUserIds,
    allowCancelRunningProcess:
      model?.allowCancelRunningProcess ?? defaults.allowCancelRunningProcess,
    allowWithdrawTask: model?.allowWithdrawTask ?? defaults.allowWithdrawTask,
    processIdRule: model?.processIdRule ?? defaults.processIdRule,
    autoApprovalType: model?.autoApprovalType ?? defaults.autoApprovalType,
    titleSetting: model?.titleSetting ?? defaults.titleSetting,
    summarySetting: model?.summarySetting ?? defaults.summarySetting,
  };
}

async function loadDesignerOptions() {
  if (designerOptionsLoaded.value) {
    return;
  }
  const [roles, posts, users, depts, userGroups] = await Promise.all([
    getSimpleRoleList(),
    getSimplePostList(),
    getSimpleUserList(),
    getSimpleDeptList(),
    getUserGroupSimpleList(),
  ]);
  roleOptions.value = roles;
  postOptions.value = posts;
  userOptions.value = users;
  deptOptions.value = depts;
  deptTreeOptions.value = handleTree(depts) as SystemDeptApi.Dept[];
  userGroupOptions.value = userGroups;
  designerOptionsLoaded.value = true;
}

async function loadFormFields() {
  if (!formData.value.formId) {
    formFields.value = [];
    return;
  }
  const form = await getForm(formData.value.formId);
  formFields.value = form?.fields ?? [];
}

function findNodeById(
  node: SimpleFlowNode | undefined,
  nodeId?: string,
): SimpleFlowNode | undefined {
  if (!node || !nodeId) {
    return undefined;
  }
  if (node.id === nodeId) {
    return node;
  }
  for (const item of node.conditionNodes || []) {
    const matched = findNodeById(item, nodeId);
    if (matched) {
      return matched;
    }
  }
  return findNodeById(node.childNode, nodeId);
}

function syncSelectedNode() {
  if (!processNodeTree.value) {
    selectedNode.value = undefined;
    return;
  }
  if (!selectedNode.value?.id) {
    selectedNode.value = processNodeTree.value;
    return;
  }
  selectedNode.value =
    findNodeById(processNodeTree.value, selectedNode.value.id) ||
    processNodeTree.value;
}

function applyRouteNodeFocus() {
  if (!processNodeTree.value || !props.nodeId) {
    return;
  }
  const focusNode = findNodeById(processNodeTree.value, String(props.nodeId));
  if (focusNode) {
    selectedNode.value = focusNode;
  }
}

async function initData() {
  let ready = false;
  designerReady.value = false;
  processNodeTree.value = undefined;
  selectedNode.value = undefined;
  loading.value = true;
  try {
    const [categories] = await Promise.all([
      getCategorySimpleList(),
      loadDesignerOptions(),
    ]);
    categoryList.value = categories;
    if (!props.id) {
      formData.value = buildDefaultFormData();
      await loadFormFields();
      processNodeTree.value = createDefaultProcessNode();
      syncSelectedNode();
      applyRouteNodeFocus();
      ready = true;
      return;
    }
    const detail = await getModel(String(props.id));
    const nextFormData = normalizeFormData(detail);
    if (actionType.value === 'copy') {
      nextFormData.id = undefined;
      nextFormData.name = nextFormData.name
        ? `${nextFormData.name}副本`
        : nextFormData.name;
      nextFormData.key = nextFormData.key
        ? `${nextFormData.key}_copy`
        : nextFormData.key;
    }
    formData.value = nextFormData;
    await loadFormFields();
    processNodeTree.value =
      nextFormData.simpleModel || createDefaultProcessNode();
    syncSelectedNode();
    applyRouteNodeFocus();
    ready = true;
  } finally {
    designerReady.value = ready;
    loading.value = false;
  }
}

function handleCanvasSave(node?: SimpleFlowNode) {
  if (!node) {
    return;
  }
  processNodeTree.value = node;
  syncSelectedNode();
}

async function validateBeforeSubmit() {
  await formRef.value?.validate();
  if (!formData.value.managerUserIds?.length) {
    throw new Error('流程管理员不能为空');
  }
  if (!processNodeTree.value) {
    throw new Error('请先设计流程');
  }
  const currentFlowData = await canvasRef.value?.getCurrentFlowData?.();
  if (!currentFlowData) {
    throw new Error('请完善流程节点配置');
  }
  processNodeTree.value = currentFlowData;
  syncSelectedNode();
}

function buildSubmitData() {
  return {
    id: formData.value.id,
    key: formData.value.key?.trim(),
    name: formData.value.name?.trim(),
    icon: formData.value.icon,
    description: formData.value.description,
    category: formData.value.category,
    type: BpmModelType.SIMPLE,
    formType: formData.value.formType,
    formId: formData.value.formId,
    formCustomCreatePath: formData.value.formCustomCreatePath,
    formCustomViewPath: formData.value.formCustomViewPath,
    visible: formData.value.visible,
    startUserIds: formData.value.startUserIds,
    startDeptIds: formData.value.startDeptIds,
    managerUserIds: formData.value.managerUserIds,
    allowCancelRunningProcess: formData.value.allowCancelRunningProcess,
    allowWithdrawTask: formData.value.allowWithdrawTask,
    processIdRule: formData.value.processIdRule,
    autoApprovalType: formData.value.autoApprovalType,
    titleSetting: formData.value.titleSetting,
    summarySetting: formData.value.summarySetting,
    processBeforeTriggerSetting: formData.value.processBeforeTriggerSetting,
    processAfterTriggerSetting: formData.value.processAfterTriggerSetting,
    taskBeforeTriggerSetting: formData.value.taskBeforeTriggerSetting,
    taskAfterTriggerSetting: formData.value.taskAfterTriggerSetting,
    printTemplateSetting: formData.value.printTemplateSetting,
    simpleModel: processNodeTree.value,
  };
}

async function persistModel(showMessage = true) {
  await validateBeforeSubmit();
  const payload = buildSubmitData();
  if (formData.value.id && actionType.value !== 'copy') {
    await updateModel(payload as any);
    if (showMessage) message.success('保存成功');
    return formData.value.id;
  }
  const newId = await createModel(payload as any);
  formData.value.id = newId;
  if (showMessage) {
    message.success(actionType.value === 'copy' ? '复制成功' : '保存成功');
  }
  if (actionType.value === 'copy') {
    await router.replace({
      name: 'HospitalWorkflowDesigner',
      query: { id: String(newId), type: 'update' },
    });
  }
  return newId;
}

async function handleSave() {
  try {
    saveLoading.value = true;
    await persistModel(true);
  } catch (error: any) {
    message.warning(error?.message || '保存失败');
  } finally {
    saveLoading.value = false;
  }
}

async function handleDeploy() {
  try {
    if (!formData.value.id) {
      await confirm('是否确认发布该流程？');
    }
    deployLoading.value = true;
    const modelId = await persistModel(false);
    await deployModel(modelId as any);
    message.success('发布成功');
  } catch (error: any) {
    message.warning(error?.message || '发布失败');
  } finally {
    deployLoading.value = false;
  }
}

function handleBack() {
  tabs.closeCurrentTab();
  router.push({ path: '/hospital-flow/process-manage' });
}

watch(
  processNodeTree,
  () => {
    syncSelectedNode();
  },
  { deep: true },
);

watch(
  () => [props.id, props.type],
  () => {
    void initData();
  },
  { immediate: true },
);

watch(
  () => props.nodeId,
  () => {
    applyRouteNodeFocus();
  },
);
</script>

<template>
  <Page auto-content-height>
    <div class="mx-auto">
      <div
        class="absolute inset-x-0 top-0 z-10 flex h-12 items-center border-b bg-card px-5"
      >
        <div class="flex w-72 items-center overflow-hidden">
          <IconifyIcon
            icon="lucide:arrow-left"
            class="size-5 flex-shrink-0 cursor-pointer"
            @click="handleBack"
          />
          <span
            class="ml-2 truncate text-base font-medium"
            :title="formData.name || '医院流程设计'"
          >
            {{ formData.name || '医院流程设计' }}
          </span>
        </div>
        <div
          class="text-text-secondary flex flex-1 items-center justify-center text-sm"
        >
          左侧画布 · 右侧固定配置面板
        </div>
        <div class="flex w-72 items-center justify-end gap-2">
          <Button type="primary" :loading="deployLoading" @click="handleDeploy">
            发 布
          </Button>
          <Button
            type="primary"
            ghost
            :loading="saveLoading"
            @click="handleSave"
          >
            保 存
          </Button>
        </div>
      </div>

      <Spin :spinning="loading">
        <div class="hospital-workflow-designer mt-12 px-4 pb-4 pt-3">
          <div class="designer-form-card border bg-white px-4 py-3">
            <Form
              ref="formRef"
              :model="formData"
              :rules="rules"
              layout="vertical"
            >
              <Row :gutter="12">
                <Col :span="6">
                  <Form.Item label="流程名称" name="name" class="mb-1">
                    <Input
                      v-model:value="formData.name"
                      size="small"
                      placeholder="请输入流程名称"
                    />
                  </Form.Item>
                </Col>
                <Col :span="6">
                  <Form.Item label="流程标识" name="key" class="mb-1">
                    <Input
                      v-model:value="formData.key"
                      :disabled="!!formData.id"
                      size="small"
                      placeholder="请输入流程标识"
                    />
                  </Form.Item>
                </Col>
                <Col :span="6">
                  <Form.Item label="流程分类" name="category" class="mb-1">
                    <Select
                      v-model:value="formData.category"
                      size="small"
                      placeholder="请选择流程分类"
                    >
                      <Select.Option
                        v-for="item in categoryOptions"
                        :key="item.code"
                        :value="item.code"
                      >
                        {{ item.displayName }}
                      </Select.Option>
                    </Select>
                  </Form.Item>
                </Col>
                <Col :span="6">
                  <Form.Item label="流程说明" name="description" class="mb-1">
                    <Input
                      v-model:value="formData.description"
                      size="small"
                      placeholder="请输入流程说明"
                    />
                  </Form.Item>
                </Col>
              </Row>
            </Form>
          </div>

          <div class="designer-shell mt-3 overflow-hidden border bg-white">
            <div class="designer-canvas">
              <SimpleProcessModel
                v-if="designerReady && processNodeTree"
                ref="canvasRef"
                :flow-node="processNodeTree"
                :readonly="false"
                :default-scale="80"
                @save="handleCanvasSave"
              />
            </div>
            <aside class="designer-side">
              <NodeConfigPanel :node="selectedNode" />
            </aside>
          </div>
        </div>
      </Spin>
    </div>
  </Page>
</template>

<style scoped>
.hospital-workflow-designer {
  min-height: calc(100vh - 96px);
}

.designer-form-card :deep(.ant-form-item) {
  margin-bottom: 10px;
}

.designer-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  height: calc(100vh - 188px);
  min-height: calc(100vh - 188px);
  max-height: calc(100vh - 188px);
  align-items: stretch;
  overflow: hidden;
}

.designer-canvas {
  min-width: 0;
  overflow: auto;
  background: #fafafa
    url('../../../bpm/components/simple-process-design/styles/svg/simple-process-bg.svg')
    0 0 / 22px 22px repeat;
}

.designer-canvas :deep(.simple-process-model-container) {
  min-height: 100%;
  height: 100%;
}

.designer-canvas :deep(.simple-process-model) {
  min-height: 100%;
}

.designer-side {
  display: flex;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
  border-left: 1px solid rgb(5 5 5 / 6%);
  background: #fff;
}

.designer-side :deep(.node-config-panel) {
  height: auto;
  min-height: 100%;
  flex: 1;
  overflow: visible;
  padding: 10px 10px 18px !important;
}

.designer-side :deep(.ant-form-item) {
  margin-bottom: 8px;
}

.designer-side :deep(.ant-form-item-label > label) {
  font-size: 11px;
  color: #4b5563;
}

.designer-side :deep(.ant-input),
.designer-side :deep(.ant-input-number),
.designer-side :deep(.ant-picker),
.designer-side :deep(.ant-select-selector),
.designer-side :deep(.ant-tree-select-selector) {
  min-height: 28px;
  font-size: 11px;
}

.designer-side :deep(.ant-input),
.designer-side :deep(.ant-input-affix-wrapper),
.designer-side :deep(.ant-select-selection-item),
.designer-side :deep(.ant-select-selection-placeholder),
.designer-side :deep(.ant-picker-input > input),
.designer-side :deep(.ant-input-number-input) {
  font-size: 11px;
}

.designer-side :deep(.ant-radio-button-wrapper),
.designer-side :deep(.ant-radio-wrapper) {
  font-size: 11px;
}

.designer-side :deep(.ant-radio-button-wrapper) {
  height: 26px;
  line-height: 24px;
  padding-inline: 8px;
}

.designer-side :deep(.text-base) {
  font-size: 13px;
}

@media (max-width: 1440px) {
  .designer-shell {
    grid-template-columns: minmax(0, 1fr) 336px;
  }
}

@media (max-width: 1280px) {
  .designer-shell {
    grid-template-columns: minmax(0, 1fr) 320px;
  }
}
</style>
