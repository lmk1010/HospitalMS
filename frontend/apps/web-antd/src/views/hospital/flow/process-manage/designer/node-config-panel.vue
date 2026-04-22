<script lang="ts" setup>
import type { Ref } from 'vue';

import type { BpmModelApi } from '#/api/bpm/model';
import type { HospitalCustomFormApi } from '#/api/hospital/custom-form';
import type { SystemDeptApi } from '#/api/system/dept';
import type { SystemUserApi } from '#/api/system/user';
import type { SimpleFlowNode } from '#/views/bpm/components/simple-process-design';
import type {
  CopyTaskFormType,
  UserTaskFormType,
} from '#/views/bpm/components/simple-process-design/helpers';

import { computed, inject, onMounted, ref, watch } from 'vue';

import { BpmModelFormType, BpmNodeTypeEnum } from '@vben/constants';
import { cloneDeep } from '@vben/utils';

import {
  Button,
  DatePicker,
  Divider,
  Empty,
  Form,
  Input,
  InputNumber,
  message,
  Radio,
  Select,
  Switch,
  Tag,
  TreeSelect,
} from 'ant-design-vue';

import { getModelList } from '#/api/bpm/model';
import { getCustomFormSimpleList } from '#/api/hospital/custom-form';
import { router } from '#/router';
import UserTaskListener from '#/views/bpm/components/simple-process-design/components/nodes-config/modules/user-task-listener.vue';
import {
  convertTimeUnit,
  getApproveTypeText,
} from '#/views/bpm/components/simple-process-design/components/nodes-config/utils';
import {
  APPROVE_METHODS,
  APPROVE_TYPE,
  ApproveMethodType,
  ApproveType,
  ASSIGN_EMPTY_HANDLER_TYPES,
  ASSIGN_START_USER_HANDLER_TYPES,
  AssignEmptyHandlerType,
  CANDIDATE_STRATEGY,
  CandidateStrategy,
  DEFAULT_BUTTON_SETTING,
  DELAY_TYPE,
  DelayTypeEnum,
  FieldPermissionType,
  MULTI_LEVEL_DEPT,
  OPERATION_BUTTON_NAME,
  REJECT_HANDLER_TYPES,
  RejectHandlerType,
  TIME_UNIT_TYPES,
  TIMEOUT_HANDLER_TYPES,
  TimeoutHandlerType,
  TimeUnitType,
  TRANSACTOR_DEFAULT_BUTTON_SETTING,
  TRIGGER_TYPES,
} from '#/views/bpm/components/simple-process-design/consts';
import {
  useFormFieldsPermission,
  useNodeForm,
} from '#/views/bpm/components/simple-process-design/helpers';
import {
  HOSPITAL_FORM_BIZ_CATEGORY_OPTIONS,
  HOSPITAL_FORM_PAGE_OPTIONS,
  resolvePagePathByCode,
} from '#/views/hospital/flow/form-manage/data';

defineOptions({ name: 'HospitalWorkflowNodeConfigPanel' });

const props = defineProps<{
  node?: SimpleFlowNode;
}>();

type HospitalNodeTimeoutRule = {
  action: TimeoutHandlerType;
  duration: number;
  id: string;
  maxRemindCount?: number;
  unit: TimeUnitType;
};

type HospitalNodeSetting = {
  actionButtonIds?: number[];
  autoSkip?: boolean;
  bizCategory?: string;
  buttonRoleIds?: number[];
  childProcessKey?: string;
  documentFormIds?: number[];
  editRoleIds?: number[];
  endNode?: boolean;
  flowRecordPageCode?: string;
  formId?: number;
  functionSystemCode?: string;
  pageCode?: string;
  pagePath?: string;
  previousNodeId?: string;
  printSubmitterSignature?: boolean;
  secondaryConfirmButtonIds?: number[];
  timeoutRules?: HospitalNodeTimeoutRule[];
};

type ExtendedFlowNode = SimpleFlowNode & {
  hospitalSetting?: HospitalNodeSetting;
};

const currentNode = ref<ExtendedFlowNode>();

const startUserIds = inject<Ref<number[]>>('startUserIds', ref([]));
const startDeptIds = inject<Ref<number[]>>('startDeptIds', ref([]));
const processNodeTree = inject<Ref<SimpleFlowNode | undefined>>(
  'processNodeTree',
  ref(),
);
const userOptions = inject<Ref<SystemUserApi.User[]>>('userList', ref([]));
const deptOptions = inject<Ref<SystemDeptApi.Dept[]>>('deptList', ref([]));
const modelFormData = inject<Ref<Record<string, any>>>(
  'modelFormData',
  ref({}),
);

const {
  configForm: tempUserConfigForm,
  deptTreeOptions,
  postOptions,
  roleOptions,
  userGroupOptions,
  handleCandidateParam,
  parseCandidateParam,
  getShowText,
} = useNodeForm(BpmNodeTypeEnum.USER_TASK_NODE);
const userConfigForm = tempUserConfigForm as Ref<UserTaskFormType>;

const {
  configForm: tempCopyConfigForm,
  handleCandidateParam: handleCopyCandidateParam,
  parseCandidateParam: parseCopyCandidateParam,
  getShowText: getCopyShowText,
} = useNodeForm(BpmNodeTypeEnum.COPY_TASK_NODE);
const copyConfigForm = tempCopyConfigForm as Ref<CopyTaskFormType>;

const {
  formType: modelFormType,
  fieldsPermissionConfig,
  getNodeConfigFormFields,
} = useFormFieldsPermission(FieldPermissionType.READ);

const approveType = ref(ApproveType.USER);
const syncing = ref(false);
const timeoutUnit = ref(TimeUnitType.HOUR);
const buttonSettings = ref<any[]>([]);
const delayConfig = ref({
  dateTime: '',
  delayType: DelayTypeEnum.FIXED_TIME_DURATION,
  timeDuration: 1,
  timeUnit: TimeUnitType.HOUR,
});
const hospitalNodeForm = ref(createDefaultHospitalNodeForm());
const customFormOptions = ref<HospitalCustomFormApi.CustomForm[]>([]);
const childProcessOptions = ref<BpmModelApi.Model[]>([]);
const basicSectionRef = ref<HTMLElement>();
const actionSectionRef = ref<HTMLElement>();
const fieldsSectionRef = ref<HTMLElement>();
const listenerSectionRef = ref<HTMLElement>();

function scrollToSection(section: 'action' | 'basic' | 'fields' | 'listener') {
  const sectionMap = {
    action: actionSectionRef.value,
    basic: basicSectionRef.value,
    fields: fieldsSectionRef.value,
    listener: listenerSectionRef.value,
  };
  sectionMap[section]?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

const isStartNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.START_USER_NODE;
});
const isUserNode = computed(() => {
  return [
    BpmNodeTypeEnum.TRANSACTOR_NODE,
    BpmNodeTypeEnum.USER_TASK_NODE,
  ].includes(currentNode.value?.type as BpmNodeTypeEnum);
});
const isPureApproveNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.USER_TASK_NODE;
});
const isCopyNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.COPY_TASK_NODE;
});
const isDelayNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.DELAY_TIMER_NODE;
});
const isEndNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.END_EVENT_NODE;
});
const isTriggerNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.TRIGGER_NODE;
});
const isRouterNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.ROUTER_BRANCH_NODE;
});
const isChildProcessNode = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.CHILD_PROCESS_NODE;
});
const nodeActorText = computed(() => {
  return currentNode.value?.type === BpmNodeTypeEnum.TRANSACTOR_NODE
    ? '办理人'
    : '审批人';
});
const usesManualDescription = computed(() => {
  return (
    isStartNode.value ||
    isUserNode.value ||
    (!isCopyNode.value && !isDelayNode.value)
  );
});

const candidateStrategyOptions = computed(() => {
  return CANDIDATE_STRATEGY.filter((item) => {
    return ![
      CandidateStrategy.FORM_DEPT_LEADER,
      CandidateStrategy.FORM_USER,
    ].includes(item.value as CandidateStrategy);
  });
});

const copyCandidateStrategyOptions = computed(() => {
  return CANDIDATE_STRATEGY.filter((item) => {
    return ![
      CandidateStrategy.FORM_DEPT_LEADER,
      CandidateStrategy.FORM_USER,
      CandidateStrategy.START_USER,
    ].includes(item.value as CandidateStrategy);
  });
});

const userCandidatePreview = computed(() => {
  if (!isUserNode.value) return '';
  if (approveType.value !== ApproveType.USER) {
    return getApproveTypeText(approveType.value);
  }
  return getShowText() || `未设置${nodeActorText.value}`;
});

const copyCandidatePreview = computed(() => {
  if (!isCopyNode.value) return '';
  return getCopyShowText() || '未设置抄送人';
});

const delayPreview = computed(() => {
  if (!isDelayNode.value) return '';
  if (delayConfig.value.delayType === DelayTypeEnum.FIXED_TIME_DURATION) {
    const unitText = TIME_UNIT_TYPES.find((item) => {
      return item.value === delayConfig.value.timeUnit;
    })?.label;
    return `延迟${delayConfig.value.timeDuration}${unitText || ''}`;
  }
  return delayConfig.value.dateTime
    ? `延迟至${delayConfig.value.dateTime.replace('T', ' ')}`
    : '未设置延迟时间';
});

const timeoutPreview = computed(() => {
  if (!isUserNode.value || !userConfigForm.value?.timeoutHandlerEnable) {
    return '未启用';
  }
  const actionText =
    TIMEOUT_HANDLER_TYPES.find((item) => {
      return item.value === userConfigForm.value.timeoutHandlerType;
    })?.label || '未设置';
  const unitText =
    TIME_UNIT_TYPES.find((item) => {
      return item.value === timeoutUnit.value;
    })?.label || '';
  const remindText =
    userConfigForm.value.timeoutHandlerType === TimeoutHandlerType.REMINDER
      ? `，最多提醒 ${userConfigForm.value.maxRemindCount || 1} 次`
      : '';
  return `超过 ${userConfigForm.value.timeDuration || 0}${unitText} 未处理时${actionText}${remindText}`;
});

const assignEmptyPreview = computed(() => {
  if (!isUserNode.value) return '';
  const actionText =
    ASSIGN_EMPTY_HANDLER_TYPES.find((item) => {
      return item.value === userConfigForm.value.assignEmptyHandlerType;
    })?.label || '未设置';
  if (
    userConfigForm.value.assignEmptyHandlerType ===
    AssignEmptyHandlerType.ASSIGN_USER
  ) {
    return `${actionText}：${getUserNicknames(
      userConfigForm.value.assignEmptyHandlerUserIds || [],
    )}`;
  }
  return actionText;
});

const nodeTypeText = computed(() => {
  if (isStartNode.value) return '发起人';
  if (isUserNode.value) return '审批节点';
  if (isCopyNode.value) return '抄送节点';
  if (isDelayNode.value) return '延迟节点';
  if (isTriggerNode.value) return '触发器';
  if (isRouterNode.value) return '路由分支';
  if (isChildProcessNode.value) return '子流程';
  if (isEndNode.value) return '结束节点';
  return '流程节点';
});

const triggerTypeText = computed(() => {
  const triggerType = currentNode.value?.triggerSetting?.type;
  return (
    TRIGGER_TYPES.find((item) => item.value === triggerType)?.label || '未配置'
  );
});

const routerPreview = computed(() => {
  const count = currentNode.value?.routerGroups?.length || 0;
  return count > 0 ? `已配置 ${count} 条路由分支` : '暂未配置路由分支';
});

const showFieldPermission = computed(() => {
  return (
    modelFormType.value === BpmModelFormType.NORMAL &&
    (isStartNode.value || isUserNode.value || isCopyNode.value) &&
    fieldsPermissionConfig.value.length > 0
  );
});

const childProcessPreview = computed(() => {
  const setting = currentNode.value?.childProcessSetting;
  if (!setting?.calledProcessDefinitionName) {
    return '暂未绑定子流程';
  }
  return `调用子流程：${setting.calledProcessDefinitionName}`;
});

const buttonSummaryText = computed(() => {
  if (!isUserNode.value) return '';
  const enabledCount = buttonSettings.value.filter(
    (item) => item.enable,
  ).length;
  return `已启用 ${enabledCount}/${buttonSettings.value.length} 个操作按钮`;
});

const nodeOrderNo = computed(() => {
  return previousNodeOptions.value.length + 1;
});

const previousNodeOptions = computed(() => {
  const path =
    findPathToNode(processNodeTree.value, currentNode.value?.id) || [];
  return path.filter((item) => {
    return (
      item.id !== currentNode.value?.id &&
      ![BpmNodeTypeEnum.END_EVENT_NODE].includes(item.type)
    );
  });
});

const actionButtonOptions = computed(() => {
  return buttonSettings.value.map((item) => ({
    label:
      item.displayName ||
      OPERATION_BUTTON_NAME.get(item.id) ||
      `按钮${item.id}`,
    value: item.id,
  }));
});

const pageOptions = computed(() => {
  return HOSPITAL_FORM_PAGE_OPTIONS.map((item) => ({
    label: item.label,
    value: item.value,
  }));
});
const selectedPagePath = computed(
  () =>
    resolvePagePathByCode(hospitalNodeForm.value.pageCode) ||
    hospitalNodeForm.value.pagePath ||
    '',
);
const matchedNodeForm = computed(() => {
  const processKey = `${modelFormData.value?.key || ''}`;
  const nodeId = `${currentNode.value?.id || ''}`;
  const pageCode = `${hospitalNodeForm.value.pageCode || ''}`;
  if (!pageCode) return undefined;
  if (hospitalNodeForm.value.formId) {
    return customFormOptions.value.find(
      (item) => item.id === hospitalNodeForm.value.formId,
    );
  }
  return customFormOptions.value.find((item) => {
    return (
      `${item.processKey || ''}` === processKey &&
      `${item.nodeKey || ''}` === nodeId &&
      `${item.pageCode || ''}` === pageCode
    );
  });
});

const returnTaskOptions = computed(() => {
  const path = findPathToNode(processNodeTree.value, currentNode.value?.id);
  if (!path) {
    return [];
  }
  return path.filter((item) => {
    return (
      item.id !== currentNode.value?.id &&
      [
        BpmNodeTypeEnum.START_USER_NODE,
        BpmNodeTypeEnum.TRANSACTOR_NODE,
        BpmNodeTypeEnum.USER_TASK_NODE,
      ].includes(item.type)
    );
  });
});

function findPathToNode(
  node: SimpleFlowNode | undefined,
  targetId?: string,
  path: SimpleFlowNode[] = [],
): SimpleFlowNode[] | undefined {
  if (!node || !targetId) {
    return undefined;
  }
  const nextPath = [...path, node];
  if (node.id === targetId) {
    return nextPath;
  }
  for (const item of node.conditionNodes || []) {
    const found = findPathToNode(item, targetId, nextPath);
    if (found) {
      return found;
    }
  }
  return findPathToNode(node.childNode, targetId, nextPath);
}

function createTimeoutRule(): HospitalNodeTimeoutRule {
  return {
    action: TimeoutHandlerType.REMINDER,
    duration: 6,
    id: `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    maxRemindCount: 1,
    unit: TimeUnitType.HOUR,
  };
}

function createDefaultHospitalNodeForm(): HospitalNodeSetting {
  return {
    actionButtonIds: [],
    autoSkip: false,
    bizCategory: undefined,
    buttonRoleIds: [],
    childProcessKey: undefined,
    documentFormIds: [],
    editRoleIds: [],
    endNode: false,
    flowRecordPageCode: undefined,
    formId: undefined,
    functionSystemCode: undefined,
    pageCode: undefined,
    pagePath: undefined,
    previousNodeId: undefined,
    printSubmitterSignature: true,
    secondaryConfirmButtonIds: [],
    timeoutRules: [],
  };
}

const ALWAYS_SKIP_EXPRESSION = String.raw`\${true}`;

function sameNumberArray(source: number[] = [], target: number[] = []) {
  if (source.length !== target.length) return false;
  const sortedSource = source.toSorted((left, right) => left - right);
  const sortedTarget = target.toSorted((left, right) => left - right);
  return sortedSource.every((item, index) => item === sortedTarget[index]);
}

function parseIsoDuration(duration?: string) {
  if (!duration || !duration.startsWith('PT')) return undefined;
  return {
    duration: Number.parseInt(duration.slice(2, -1), 10) || 1,
    unit: convertTimeUnit(duration.slice(-1)),
  };
}

function buildHospitalTimeoutRules(node?: ExtendedFlowNode) {
  const savedRules = node?.hospitalSetting?.timeoutRules;
  if (savedRules?.length) {
    return savedRules.map((item) => ({
      ...createTimeoutRule(),
      ...item,
      maxRemindCount:
        item.action === TimeoutHandlerType.REMINDER
          ? (item.maxRemindCount ?? 1)
          : undefined,
    }));
  }
  if (node?.timeoutHandler?.enable) {
    const parsed = parseIsoDuration(node.timeoutHandler.timeDuration);
    if (parsed) {
      return [
        {
          ...createTimeoutRule(),
          action: node.timeoutHandler.type || TimeoutHandlerType.REMINDER,
          duration: parsed.duration,
          maxRemindCount: node.timeoutHandler.maxRemindCount || 1,
          unit: parsed.unit,
        },
      ];
    }
  }
  return [];
}

async function loadHospitalExtraOptions() {
  const [formResult, modelResult] = await Promise.allSettled([
    getCustomFormSimpleList(),
    getModelList(),
  ]);
  customFormOptions.value =
    formResult.status === 'fulfilled' ? formResult.value || [] : [];
  childProcessOptions.value =
    modelResult.status === 'fulfilled' ? modelResult.value || [] : [];
  if (currentNode.value) {
    syncHospitalNode(currentNode.value);
  }
}

function syncHospitalNode(node?: ExtendedFlowNode) {
  const setting = node?.hospitalSetting;
  const matchedForm = customFormOptions.value.find((item) => {
    return (
      `${item.processKey || ''}` === `${modelFormData.value?.key || ''}` &&
      `${item.nodeKey || ''}` === `${node?.id || ''}` &&
      (!setting?.pageCode ||
        `${item.pageCode || ''}` === `${setting.pageCode || ''}`)
    );
  });
  hospitalNodeForm.value = {
    ...createDefaultHospitalNodeForm(),
    ...setting,
    actionButtonIds: setting?.actionButtonIds?.length
      ? [...setting.actionButtonIds]
      : getDefaultButtons(node)
          .filter((item) => item.enable)
          .map((item) => item.id),
    autoSkip: setting?.autoSkip ?? !!node?.skipExpression,
    buttonRoleIds: [...(setting?.buttonRoleIds || [])],
    documentFormIds: [...(setting?.documentFormIds || [])],
    editRoleIds: [...(setting?.editRoleIds || [])],
    formId: setting?.formId || matchedForm?.id,
    pageCode: setting?.pageCode || matchedForm?.pageCode,
    pagePath:
      setting?.pagePath ||
      matchedForm?.pagePath ||
      resolvePagePathByCode(setting?.pageCode || matchedForm?.pageCode),
    secondaryConfirmButtonIds: [...(setting?.secondaryConfirmButtonIds || [])],
    timeoutRules: buildHospitalTimeoutRules(node),
  };
}

function addTimeoutRule() {
  hospitalNodeForm.value.timeoutRules = [
    ...(hospitalNodeForm.value.timeoutRules || []),
    createTimeoutRule(),
  ];
}

function removeTimeoutRule(ruleId: string) {
  hospitalNodeForm.value.timeoutRules = (
    hospitalNodeForm.value.timeoutRules || []
  ).filter((item) => item.id !== ruleId);
}

function updateSettingIdList(
  source: number[] = [],
  targetId: number,
  checked: boolean,
) {
  const values = new Set(source);
  if (checked) {
    values.add(targetId);
  } else {
    values.delete(targetId);
  }
  return [...values];
}

function isActionEnabled(buttonId: number) {
  return (hospitalNodeForm.value.actionButtonIds || []).includes(buttonId);
}

function isSecondaryConfirmEnabled(buttonId: number) {
  return (hospitalNodeForm.value.secondaryConfirmButtonIds || []).includes(
    buttonId,
  );
}

function toggleActionButton(buttonId: number, checked: boolean) {
  hospitalNodeForm.value.actionButtonIds = updateSettingIdList(
    hospitalNodeForm.value.actionButtonIds || [],
    buttonId,
    checked,
  );
  if (!checked) {
    hospitalNodeForm.value.secondaryConfirmButtonIds = (
      hospitalNodeForm.value.secondaryConfirmButtonIds || []
    ).filter((item) => item !== buttonId);
  }
}

function toggleSecondaryConfirm(buttonId: number, checked: boolean) {
  if (!isActionEnabled(buttonId)) {
    return;
  }
  hospitalNodeForm.value.secondaryConfirmButtonIds = updateSettingIdList(
    hospitalNodeForm.value.secondaryConfirmButtonIds || [],
    buttonId,
    checked,
  );
}

function applyHospitalNode() {
  const node = currentNode.value;
  if (syncing.value || !node) {
    return;
  }
  const nextSetting: HospitalNodeSetting = {
    ...hospitalNodeForm.value,
    actionButtonIds: [...(hospitalNodeForm.value.actionButtonIds || [])],
    buttonRoleIds: [...(hospitalNodeForm.value.buttonRoleIds || [])],
    documentFormIds: [...(hospitalNodeForm.value.documentFormIds || [])],
    editRoleIds: [...(hospitalNodeForm.value.editRoleIds || [])],
    pagePath: selectedPagePath.value || undefined,
    secondaryConfirmButtonIds: [
      ...(hospitalNodeForm.value.secondaryConfirmButtonIds || []),
    ],
    timeoutRules: (hospitalNodeForm.value.timeoutRules || []).map((item) => ({
      ...item,
      maxRemindCount:
        item.action === TimeoutHandlerType.REMINDER
          ? (item.maxRemindCount ?? 1)
          : undefined,
    })),
  };
  node.hospitalSetting = nextSetting;

  if (isUserNode.value) {
    const enabledIds = buttonSettings.value
      .filter((item) => item.enable)
      .map((item) => item.id);
    if (!sameNumberArray(enabledIds, nextSetting.actionButtonIds || [])) {
      buttonSettings.value = buttonSettings.value.map((item) => ({
        ...item,
        enable: (nextSetting.actionButtonIds || []).includes(item.id),
      }));
    }

    if (nextSetting.autoSkip) {
      userConfigForm.value.skipExpression =
        userConfigForm.value.skipExpression || ALWAYS_SKIP_EXPRESSION;
    } else if (userConfigForm.value.skipExpression === ALWAYS_SKIP_EXPRESSION) {
      userConfigForm.value.skipExpression = '';
    }

    const firstRule = nextSetting.timeoutRules?.[0];
    if (firstRule) {
      userConfigForm.value.timeoutHandlerEnable = true;
      userConfigForm.value.timeoutHandlerType = firstRule.action;
      userConfigForm.value.timeDuration = firstRule.duration;
      userConfigForm.value.maxRemindCount = firstRule.maxRemindCount || 1;
      timeoutUnit.value = firstRule.unit;
    }
  }
}

function openNodePage() {
  if (!selectedPagePath.value) {
    message.warning('请先选择节点页面');
    return;
  }
  router.push(selectedPagePath.value);
}

function handleCreateNodeForm() {
  if (!modelFormData.value?.id) {
    message.warning('请先保存流程，再配置节点表单');
    return;
  }
  if (!currentNode.value?.id) {
    message.warning('请先选择流程节点');
    return;
  }
  if (!hospitalNodeForm.value.pageCode) {
    message.warning('请先配置节点页面');
    return;
  }
  router.push({
    name: 'HospitalCustomFormDesigner',
    query: {
      backTo: 'workflow-designer',
      focusNodeId: currentNode.value.id,
      pageCode: hospitalNodeForm.value.pageCode,
      processKey: modelFormData.value?.key,
      processName: modelFormData.value?.name,
      nodeKey: currentNode.value.id,
      nodeName: currentNode.value.name,
      type: 'create',
      workflowId: modelFormData.value.id,
      workflowType: 'update',
    },
  });
}

function handleEditNodeForm() {
  if (!modelFormData.value?.id) {
    message.warning('请先保存流程，再配置节点表单');
    return;
  }
  const formId = matchedNodeForm.value?.id || hospitalNodeForm.value.formId;
  if (!formId) {
    message.warning('当前节点还未绑定表单');
    return;
  }
  router.push({
    name: 'HospitalCustomFormDesigner',
    query: {
      backTo: 'workflow-designer',
      focusNodeId: currentNode.value?.id,
      id: formId,
      pageCode: hospitalNodeForm.value.pageCode,
      processKey: modelFormData.value?.key,
      processName: modelFormData.value?.name,
      nodeKey: currentNode.value?.id,
      nodeName: currentNode.value?.name,
      type: 'edit',
      workflowId: modelFormData.value.id,
      workflowType: 'update',
    },
  });
}

function clearCandidateFields(configForm: CopyTaskFormType | UserTaskFormType) {
  configForm.deptIds = [];
  configForm.deptLevel = 1;
  configForm.expression = '';
  configForm.formDept = '';
  configForm.formUser = '';
  configForm.postIds = [];
  configForm.roleIds = [];
  configForm.userGroups = [];
  configForm.userIds = [];
}

function getDefaultButtons(node?: SimpleFlowNode) {
  return cloneDeep(
    node?.type === BpmNodeTypeEnum.TRANSACTOR_NODE
      ? TRANSACTOR_DEFAULT_BUTTON_SETTING
      : DEFAULT_BUTTON_SETTING,
  );
}

function normalizeButtonDisplayName(button: any) {
  button.displayName ||= OPERATION_BUTTON_NAME.get(button.id) || '';
}

function resetUserConfigForm() {
  userConfigForm.value = {
    approveMethod: ApproveMethodType.SEQUENTIAL_APPROVE,
    approveRatio: 100,
    assignEmptyHandlerType: AssignEmptyHandlerType.APPROVE,
    assignEmptyHandlerUserIds: [],
    assignStartUserHandlerType: 1,
    buttonsSetting: [],
    candidateStrategy: CandidateStrategy.USER,
    deptIds: [],
    deptLevel: 1,
    expression: '',
    maxRemindCount: 1,
    postIds: [],
    reasonRequire: false,
    rejectHandlerType: RejectHandlerType.FINISH_PROCESS,
    returnNodeId: '',
    roleIds: [],
    signEnable: false,
    skipExpression: '',
    taskAssignListener: buildEmptyListener(),
    taskAssignListenerEnable: false,
    taskAssignListenerPath: '',
    taskCompleteListener: buildEmptyListener(),
    taskCompleteListenerEnable: false,
    taskCompleteListenerPath: '',
    taskCreateListener: buildEmptyListener(),
    taskCreateListenerEnable: false,
    taskCreateListenerPath: '',
    timeDuration: 6,
    timeoutHandlerEnable: false,
    timeoutHandlerType: TimeoutHandlerType.REMINDER,
    userGroups: [],
    userIds: [],
  } as UserTaskFormType;
  timeoutUnit.value = TimeUnitType.HOUR;
  buttonSettings.value = getDefaultButtons(currentNode.value);
}

function resetCopyConfigForm() {
  copyConfigForm.value = {
    candidateStrategy: CandidateStrategy.USER,
    deptIds: [],
    deptLevel: 1,
    expression: '',
    postIds: [],
    roleIds: [],
    userGroups: [],
    userIds: [],
  } as CopyTaskFormType;
}

function buildEmptyListener() {
  return {
    body: [],
    header: [],
  };
}

function syncFieldsPermission(node?: SimpleFlowNode) {
  if (
    modelFormType.value === BpmModelFormType.NORMAL &&
    (isStartNode.value || isUserNode.value || isCopyNode.value)
  ) {
    getNodeConfigFormFields(node?.fieldsPermission);
    return;
  }
  fieldsPermissionConfig.value = [];
}

function resetDelayConfig() {
  delayConfig.value = {
    dateTime: '',
    delayType: DelayTypeEnum.FIXED_TIME_DURATION,
    timeDuration: 1,
    timeUnit: TimeUnitType.HOUR,
  };
}

function syncUserNode(node?: SimpleFlowNode) {
  resetUserConfigForm();
  approveType.value = (node?.approveType as ApproveType) || ApproveType.USER;
  if (!node || !isUserNode.value) {
    return;
  }
  buttonSettings.value =
    cloneDeep(node.buttonsSetting) || getDefaultButtons(node);
  if (approveType.value !== ApproveType.USER) {
    return;
  }
  userConfigForm.value.candidateStrategy =
    (node.candidateStrategy as CandidateStrategy) || CandidateStrategy.USER;
  parseCandidateParam(
    userConfigForm.value.candidateStrategy,
    node.candidateParam || undefined,
  );
  userConfigForm.value.approveMethod =
    (node.approveMethod as ApproveMethodType) ||
    ApproveMethodType.SEQUENTIAL_APPROVE;
  userConfigForm.value.approveRatio = node.approveRatio || 100;
  userConfigForm.value.rejectHandlerType =
    node.rejectHandler?.type || RejectHandlerType.FINISH_PROCESS;
  userConfigForm.value.returnNodeId = node.rejectHandler?.returnNodeId || '';
  userConfigForm.value.timeoutHandlerEnable =
    node.timeoutHandler?.enable ?? false;
  userConfigForm.value.timeoutHandlerType =
    node.timeoutHandler?.type || TimeoutHandlerType.REMINDER;
  userConfigForm.value.maxRemindCount =
    node.timeoutHandler?.maxRemindCount || 1;
  if (node.timeoutHandler?.timeDuration?.startsWith('PT')) {
    userConfigForm.value.timeDuration = Number.parseInt(
      node.timeoutHandler.timeDuration.slice(2, -1),
    );
    timeoutUnit.value = convertTimeUnit(
      node.timeoutHandler.timeDuration.slice(-1),
    );
  }
  userConfigForm.value.assignEmptyHandlerType =
    node.assignEmptyHandler?.type || AssignEmptyHandlerType.APPROVE;
  userConfigForm.value.assignEmptyHandlerUserIds =
    node.assignEmptyHandler?.userIds || [];
  userConfigForm.value.assignStartUserHandlerType =
    node.assignStartUserHandlerType || 1;
  userConfigForm.value.signEnable = node.signEnable ?? false;
  userConfigForm.value.reasonRequire = node.reasonRequire ?? false;
  userConfigForm.value.skipExpression = node.skipExpression || '';
  getNodeConfigFormFields(node.fieldsPermission);
  userConfigForm.value.taskCreateListenerEnable =
    node.taskCreateListener?.enable ?? false;
  userConfigForm.value.taskCreateListenerPath =
    node.taskCreateListener?.path || '';
  userConfigForm.value.taskCreateListener = {
    body: node.taskCreateListener?.body ?? [],
    header: node.taskCreateListener?.header ?? [],
  };
  userConfigForm.value.taskAssignListenerEnable =
    node.taskAssignListener?.enable ?? false;
  userConfigForm.value.taskAssignListenerPath =
    node.taskAssignListener?.path || '';
  userConfigForm.value.taskAssignListener = {
    body: node.taskAssignListener?.body ?? [],
    header: node.taskAssignListener?.header ?? [],
  };
  userConfigForm.value.taskCompleteListenerEnable =
    node.taskCompleteListener?.enable ?? false;
  userConfigForm.value.taskCompleteListenerPath =
    node.taskCompleteListener?.path || '';
  userConfigForm.value.taskCompleteListener = {
    body: node.taskCompleteListener?.body ?? [],
    header: node.taskCompleteListener?.header ?? [],
  };
}

function syncCopyNode(node?: SimpleFlowNode) {
  resetCopyConfigForm();
  if (!node || !isCopyNode.value) {
    return;
  }
  getNodeConfigFormFields(node.fieldsPermission);
  copyConfigForm.value.candidateStrategy =
    (node.candidateStrategy as CandidateStrategy) || CandidateStrategy.USER;
  parseCopyCandidateParam(
    copyConfigForm.value.candidateStrategy,
    node.candidateParam || undefined,
  );
}

function syncDelayNode(node?: SimpleFlowNode) {
  resetDelayConfig();
  if (!node || !isDelayNode.value || !node.delaySetting) {
    return;
  }
  delayConfig.value.delayType =
    node.delaySetting.delayType || DelayTypeEnum.FIXED_TIME_DURATION;
  if (delayConfig.value.delayType === DelayTypeEnum.FIXED_TIME_DURATION) {
    const strTimeDuration = node.delaySetting.delayTime || '';
    if (strTimeDuration.startsWith('PT')) {
      delayConfig.value.timeDuration = Number.parseInt(
        strTimeDuration.slice(2, -1),
      );
      delayConfig.value.timeUnit = convertTimeUnit(strTimeDuration.slice(-1));
    }
    return;
  }
  delayConfig.value.dateTime = node.delaySetting.delayTime || '';
}

function syncFromNode(node?: ExtendedFlowNode) {
  syncing.value = true;
  try {
    syncFieldsPermission(node);
    syncUserNode(node);
    syncCopyNode(node);
    syncDelayNode(node);
    syncHospitalNode(node);
  } finally {
    syncing.value = false;
  }
}

function getTimeoutIsoTimeDuration() {
  let strTimeDuration = 'PT';
  if (timeoutUnit.value === TimeUnitType.MINUTE) {
    strTimeDuration += `${userConfigForm.value.timeDuration}M`;
  }
  if (timeoutUnit.value === TimeUnitType.HOUR) {
    strTimeDuration += `${userConfigForm.value.timeDuration}H`;
  }
  if (timeoutUnit.value === TimeUnitType.DAY) {
    strTimeDuration += `${userConfigForm.value.timeDuration}D`;
  }
  return strTimeDuration;
}

function applyUserNode() {
  const node = currentNode.value;
  if (syncing.value || !node || !isUserNode.value) {
    return;
  }
  node.approveType = approveType.value;
  node.buttonsSetting = cloneDeep(buttonSettings.value);
  if (approveType.value !== ApproveType.USER) {
    node.showText = getApproveTypeText(approveType.value);
    return;
  }
  node.candidateStrategy = userConfigForm.value.candidateStrategy;
  node.candidateParam = handleCandidateParam();
  node.approveMethod = userConfigForm.value.approveMethod;
  node.approveRatio =
    userConfigForm.value.approveMethod === ApproveMethodType.APPROVE_BY_RATIO
      ? userConfigForm.value.approveRatio
      : undefined;
  node.rejectHandler = isPureApproveNode.value
    ? {
        type:
          userConfigForm.value.rejectHandlerType ||
          RejectHandlerType.FINISH_PROCESS,
        returnNodeId:
          userConfigForm.value.rejectHandlerType ===
          RejectHandlerType.RETURN_USER_TASK
            ? userConfigForm.value.returnNodeId
            : undefined,
      }
    : undefined;
  node.timeoutHandler = isPureApproveNode.value
    ? {
        enable: !!userConfigForm.value.timeoutHandlerEnable,
        type: userConfigForm.value.timeoutHandlerEnable
          ? userConfigForm.value.timeoutHandlerType
          : undefined,
        timeDuration: userConfigForm.value.timeoutHandlerEnable
          ? getTimeoutIsoTimeDuration()
          : undefined,
        maxRemindCount:
          userConfigForm.value.timeoutHandlerEnable &&
          userConfigForm.value.timeoutHandlerType ===
            TimeoutHandlerType.REMINDER
            ? userConfigForm.value.maxRemindCount
            : undefined,
      }
    : undefined;
  node.assignEmptyHandler = {
    type:
      userConfigForm.value.assignEmptyHandlerType ||
      AssignEmptyHandlerType.APPROVE,
    userIds:
      userConfigForm.value.assignEmptyHandlerType ===
      AssignEmptyHandlerType.ASSIGN_USER
        ? userConfigForm.value.assignEmptyHandlerUserIds
        : undefined,
  };
  node.assignStartUserHandlerType = isPureApproveNode.value
    ? userConfigForm.value.assignStartUserHandlerType
    : undefined;
  node.signEnable = isPureApproveNode.value
    ? !!userConfigForm.value.signEnable
    : undefined;
  node.reasonRequire = isPureApproveNode.value
    ? !!userConfigForm.value.reasonRequire
    : undefined;
  node.skipExpression =
    userConfigForm.value.skipExpression?.trim() || undefined;
  node.fieldsPermission = cloneDeep(fieldsPermissionConfig.value);
  node.taskCreateListener = {
    body: userConfigForm.value.taskCreateListener?.body,
    enable: !!userConfigForm.value.taskCreateListenerEnable,
    header: userConfigForm.value.taskCreateListener?.header,
    path: userConfigForm.value.taskCreateListenerEnable
      ? userConfigForm.value.taskCreateListenerPath?.trim()
      : undefined,
  };
  node.taskAssignListener = {
    body: userConfigForm.value.taskAssignListener?.body,
    enable: !!userConfigForm.value.taskAssignListenerEnable,
    header: userConfigForm.value.taskAssignListener?.header,
    path: userConfigForm.value.taskAssignListenerEnable
      ? userConfigForm.value.taskAssignListenerPath?.trim()
      : undefined,
  };
  node.taskCompleteListener = {
    body: userConfigForm.value.taskCompleteListener?.body,
    enable: !!userConfigForm.value.taskCompleteListenerEnable,
    header: userConfigForm.value.taskCompleteListener?.header,
    path: userConfigForm.value.taskCompleteListenerEnable
      ? userConfigForm.value.taskCompleteListenerPath?.trim()
      : undefined,
  };
}

function applyCopyNode() {
  const node = currentNode.value;
  if (syncing.value || !node || !isCopyNode.value) {
    return;
  }
  node.candidateStrategy = copyConfigForm.value.candidateStrategy;
  node.candidateParam = handleCopyCandidateParam();
  node.fieldsPermission = cloneDeep(fieldsPermissionConfig.value);
  node.showText = copyCandidatePreview.value;
}

function getDelayIsoTimeDuration() {
  let strTimeDuration = 'PT';
  if (delayConfig.value.timeUnit === TimeUnitType.MINUTE) {
    strTimeDuration += `${delayConfig.value.timeDuration}M`;
  }
  if (delayConfig.value.timeUnit === TimeUnitType.HOUR) {
    strTimeDuration += `${delayConfig.value.timeDuration}H`;
  }
  if (delayConfig.value.timeUnit === TimeUnitType.DAY) {
    strTimeDuration += `${delayConfig.value.timeDuration}D`;
  }
  return strTimeDuration;
}

function applyDelayNode() {
  const node = currentNode.value;
  if (syncing.value || !node || !isDelayNode.value) {
    return;
  }
  node.showText = delayPreview.value;
  if (delayConfig.value.delayType === DelayTypeEnum.FIXED_TIME_DURATION) {
    node.delaySetting = {
      delayTime: getDelayIsoTimeDuration(),
      delayType: delayConfig.value.delayType,
    };
    return;
  }
  node.delaySetting = {
    delayTime: delayConfig.value.dateTime,
    delayType: delayConfig.value.delayType,
  };
}

function getUserNicknames(userIds: number[]) {
  return (userIds || [])
    .map((userId) => {
      return userOptions.value.find((item) => item.id === userId)?.nickname;
    })
    .filter(Boolean)
    .join('、');
}

function getDeptNames(deptIds: number[]) {
  return (deptIds || [])
    .map((deptId) => {
      return deptOptions.value.find((item) => item.id === deptId)?.name;
    })
    .filter(Boolean)
    .join('、');
}

onMounted(() => {
  void loadHospitalExtraOptions();
});

watch(
  hospitalNodeForm,
  () => {
    applyHospitalNode();
  },
  { deep: true },
);

watch(
  () => hospitalNodeForm.value.actionButtonIds,
  (value) => {
    if (syncing.value) {
      return;
    }
    hospitalNodeForm.value.secondaryConfirmButtonIds = (
      hospitalNodeForm.value.secondaryConfirmButtonIds || []
    ).filter((item) => {
      return (value || []).includes(item);
    });
  },
  { deep: true },
);

watch(
  () => [
    userConfigForm.value.timeoutHandlerEnable,
    userConfigForm.value.timeoutHandlerType,
    userConfigForm.value.timeDuration,
    userConfigForm.value.maxRemindCount,
    timeoutUnit.value,
  ],
  () => {
    if (syncing.value || !isUserNode.value) {
      return;
    }
    if (!userConfigForm.value.timeoutHandlerEnable) {
      if ((hospitalNodeForm.value.timeoutRules || []).length <= 1) {
        hospitalNodeForm.value.timeoutRules = [];
      }
      return;
    }
    const currentRules = hospitalNodeForm.value.timeoutRules || [];
    const firstRule = {
      ...(currentRules[0] || createTimeoutRule()),
      action:
        userConfigForm.value.timeoutHandlerType || TimeoutHandlerType.REMINDER,
      duration: userConfigForm.value.timeDuration || 1,
      maxRemindCount:
        userConfigForm.value.timeoutHandlerType === TimeoutHandlerType.REMINDER
          ? userConfigForm.value.maxRemindCount || 1
          : undefined,
      unit: timeoutUnit.value,
    };
    hospitalNodeForm.value.timeoutRules = [firstRule, ...currentRules.slice(1)];
  },
  { deep: true },
);

watch(
  () => props.node,
  (node) => {
    currentNode.value = node;
    syncFromNode(node);
  },
  { immediate: true },
);

watch(
  () => userConfigForm.value?.candidateStrategy,
  (value, oldValue) => {
    if (syncing.value || value === undefined || value === oldValue) {
      return;
    }
    clearCandidateFields(userConfigForm.value);
  },
);

watch(
  () => copyConfigForm.value?.candidateStrategy,
  (value, oldValue) => {
    if (syncing.value || value === undefined || value === oldValue) {
      return;
    }
    clearCandidateFields(copyConfigForm.value);
  },
);

watch(
  [approveType, userConfigForm],
  () => {
    applyUserNode();
  },
  { deep: true },
);

watch(
  buttonSettings,
  () => {
    if (!syncing.value) {
      const enabledIds = buttonSettings.value
        .filter((item) => item.enable)
        .map((item) => item.id);
      if (
        !sameNumberArray(
          enabledIds,
          hospitalNodeForm.value.actionButtonIds || [],
        )
      ) {
        hospitalNodeForm.value.actionButtonIds = enabledIds;
      }
    }
    applyUserNode();
  },
  { deep: true },
);

watch(
  copyConfigForm,
  () => {
    applyCopyNode();
  },
  { deep: true },
);

watch(
  fieldsPermissionConfig,
  () => {
    if (
      syncing.value ||
      !currentNode.value ||
      !(isStartNode.value || isUserNode.value || isCopyNode.value)
    ) {
      return;
    }
    currentNode.value.fieldsPermission = cloneDeep(
      fieldsPermissionConfig.value,
    );
  },
  { deep: true },
);

watch(
  delayConfig,
  () => {
    applyDelayNode();
  },
  { deep: true },
);

watch(
  returnTaskOptions,
  (options) => {
    if (
      userConfigForm.value.rejectHandlerType ===
        RejectHandlerType.RETURN_USER_TASK &&
      userConfigForm.value.returnNodeId &&
      !options.some((item) => item.id === userConfigForm.value.returnNodeId)
    ) {
      userConfigForm.value.returnNodeId = options[options.length - 1]?.id || '';
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="node-config-panel min-h-full px-2 py-2">
    <template v-if="currentNode">
      <div class="mb-3 flex items-center justify-between gap-2">
        <div class="min-w-0">
          <div class="truncate text-sm font-semibold text-gray-900">
            节点配置
          </div>
          <div class="truncate text-[11px] text-gray-500">
            当前节点：{{ currentNode.name }}
          </div>
        </div>
        <Tag color="processing">
          {{ nodeTypeText }}
        </Tag>
      </div>

      <div v-if="isUserNode" class="panel-section-nav">
        <button
          type="button"
          class="panel-section-button"
          @click="scrollToSection('basic')"
        >
          基础设置
        </button>
        <button
          type="button"
          class="panel-section-button"
          @click="scrollToSection('action')"
        >
          动作设置
        </button>
        <button
          v-if="showFieldPermission"
          type="button"
          class="panel-section-button"
          @click="scrollToSection('fields')"
        >
          字段权限
        </button>
        <button
          type="button"
          class="panel-section-button"
          @click="scrollToSection('listener')"
        >
          监听器
        </button>
      </div>

      <Form layout="vertical" size="small">
        <div ref="basicSectionRef" class="panel-section-anchor"></div>

        <div class="config-section compact-section">
          <div class="config-section-title">基本设置</div>
          <div class="config-static-row">
            <span>序号</span>
            <strong>{{ nodeOrderNo }}</strong>
          </div>

          <Form.Item label="结束节点">
            <Switch v-model:checked="hospitalNodeForm.endNode" size="small" />
          </Form.Item>

          <Form.Item label="节点名称">
            <Input
              v-model:value="currentNode.name"
              :maxlength="20"
              placeholder="请输入节点名称"
              show-count
            />
          </Form.Item>

          <Form.Item label="所属分类">
            <Select
              v-model:value="hospitalNodeForm.bizCategory"
              placeholder="请选择分类"
            >
              <Select.Option
                v-for="item in HOSPITAL_FORM_BIZ_CATEGORY_OPTIONS"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="节点页面">
            <Select
              v-model:value="hospitalNodeForm.pageCode"
              allow-clear
              option-filter-prop="label"
              placeholder="请选择节点页面"
              show-search
            >
              <Select.Option
                v-for="item in pageOptions"
                :key="item.value"
                :value="item.value"
                :label="item.label"
              >
                {{ item.label }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="页面路径">
            <div class="config-readonly text-[11px] text-gray-700">
              {{ selectedPagePath || '未配置页面路径' }}
            </div>
            <div class="mt-2 flex gap-2">
              <Button size="small" @click="openNodePage">打开页面</Button>
              <Button
                size="small"
                type="primary"
                ghost
                @click="handleCreateNodeForm"
              >
                新建表单
              </Button>
              <Button size="small" type="primary" @click="handleEditNodeForm">
                编辑表单
              </Button>
            </div>
          </Form.Item>

          <Form.Item label="前置流程">
            <Select
              v-model:value="hospitalNodeForm.previousNodeId"
              allow-clear
              placeholder="请选择前置流程"
            >
              <Select.Option
                v-for="item in previousNodeOptions"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="子流程">
            <Select
              v-model:value="hospitalNodeForm.childProcessKey"
              allow-clear
              option-filter-prop="label"
              placeholder="请选择"
              show-search
            >
              <Select.Option
                v-for="item in childProcessOptions"
                :key="item.key"
                :value="item.key"
                :label="item.name"
              >
                {{ item.name }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="自动跳过">
            <Switch v-model:checked="hospitalNodeForm.autoSkip" size="small" />
          </Form.Item>

          <div class="config-static-meta">内部节点：{{ currentNode.id }}</div>
        </div>

        <div class="config-section compact-section">
          <div class="config-section-title-row">
            <div class="config-section-title">任务超时设置</div>
            <Button size="small" type="link" @click="addTimeoutRule">
              + 添加规则
            </Button>
          </div>

          <div
            v-if="hospitalNodeForm.timeoutRules?.length"
            class="timeout-rule-list"
          >
            <div
              v-for="rule in hospitalNodeForm.timeoutRules"
              :key="rule.id"
              class="timeout-rule-card"
            >
              <div class="timeout-rule-grid">
                <InputNumber
                  v-model:value="rule.duration"
                  :min="1"
                  class="w-full"
                />
                <Select v-model:value="rule.unit">
                  <Select.Option
                    v-for="item in TIME_UNIT_TYPES"
                    :key="item.value"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </Select.Option>
                </Select>
                <Select v-model:value="rule.action">
                  <Select.Option
                    v-for="item in TIMEOUT_HANDLER_TYPES"
                    :key="item.value"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </Select.Option>
                </Select>
                <Button
                  size="small"
                  type="text"
                  @click="removeTimeoutRule(rule.id)"
                >
                  删除
                </Button>
              </div>
              <div
                v-if="rule.action === TimeoutHandlerType.REMINDER"
                class="timeout-rule-extra"
              >
                <span>提醒次数</span>
                <InputNumber
                  v-model:value="rule.maxRemindCount"
                  :min="1"
                  :max="10"
                />
              </div>
            </div>
          </div>

          <div v-else class="config-empty-line">暂未配置超时规则</div>

          <Form.Item label="表单选择">
            <Select
              v-model:value="hospitalNodeForm.formId"
              allow-clear
              option-filter-prop="label"
              placeholder="请选择表单"
              show-search
            >
              <Select.Option
                v-for="item in customFormOptions"
                :key="item.id"
                :value="item.id"
                :label="item.name"
              >
                {{ item.name }}
              </Select.Option>
            </Select>
            <div class="mt-1 text-[11px] text-gray-500">
              当前绑定：{{ matchedNodeForm?.name || '未绑定节点表单' }}
            </div>
          </Form.Item>

          <Form.Item label="流转记录">
            <Select
              v-model:value="hospitalNodeForm.flowRecordPageCode"
              allow-clear
              option-filter-prop="label"
              placeholder="请选择"
              show-search
            >
              <Select.Option
                v-for="item in pageOptions"
                :key="item.value"
                :value="item.value"
                :label="item.label"
              >
                {{ item.label }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item label="功能系统">
            <Select
              v-model:value="hospitalNodeForm.functionSystemCode"
              allow-clear
              placeholder="请选择"
            >
              <Select.Option
                v-for="item in HOSPITAL_FORM_BIZ_CATEGORY_OPTIONS"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </Select.Option>
            </Select>
          </Form.Item>
        </div>

        <template v-if="isUserNode">
          <div ref="actionSectionRef" class="panel-section-anchor"></div>
          <div class="config-section compact-section">
            <div class="config-section-title-row">
              <div class="config-section-title">动作设置</div>
              <div class="config-section-caption">
                已启用 {{ hospitalNodeForm.actionButtonIds?.length || 0 }} 项
              </div>
            </div>

            <Form.Item label="动作能力" class="action-matrix-item">
              <div class="action-matrix">
                <div class="action-matrix-head">
                  <span>动作</span>
                  <span>启用</span>
                  <span>校验</span>
                </div>
                <div
                  v-for="item in actionButtonOptions"
                  :key="item.value"
                  class="action-matrix-row"
                >
                  <span class="truncate text-gray-700">{{ item.label }}</span>
                  <div class="action-matrix-cell">
                    <Switch
                      :checked="isActionEnabled(item.value)"
                      size="small"
                      @update:checked="
                        (checked) => toggleActionButton(item.value, !!checked)
                      "
                    />
                  </div>
                  <div class="action-matrix-cell">
                    <Switch
                      :checked="isSecondaryConfirmEnabled(item.value)"
                      :disabled="!isActionEnabled(item.value)"
                      size="small"
                      @update:checked="
                        (checked) =>
                          toggleSecondaryConfirm(item.value, !!checked)
                      "
                    />
                  </div>
                </div>
              </div>
            </Form.Item>

            <Form.Item label="按钮角色">
              <Select
                v-model:value="hospitalNodeForm.buttonRoleIds"
                mode="multiple"
                :max-tag-count="1"
                option-filter-prop="label"
                placeholder="请选择角色"
                show-search
              >
                <Select.Option
                  v-for="item in roleOptions"
                  :key="item.id"
                  :value="item.id"
                  :label="item.name"
                >
                  {{ item.name }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item label="修改权限">
              <Select
                v-model:value="hospitalNodeForm.editRoleIds"
                mode="multiple"
                :max-tag-count="1"
                option-filter-prop="label"
                placeholder="请选择角色"
                show-search
              >
                <Select.Option
                  v-for="item in roleOptions"
                  :key="`edit_${item.id}`"
                  :value="item.id"
                  :label="item.name"
                >
                  {{ item.name }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item label="生成文档">
              <Select
                v-model:value="hospitalNodeForm.documentFormIds"
                mode="multiple"
                :max-tag-count="1"
                option-filter-prop="label"
                placeholder="请选择文档模板"
                show-search
              >
                <Select.Option
                  v-for="item in customFormOptions"
                  :key="`doc_${item.id}`"
                  :value="item.id"
                  :label="item.name"
                >
                  {{ item.name }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item label="打印提交人签名">
              <Switch
                v-model:checked="hospitalNodeForm.printSubmitterSignature"
                size="small"
              />
            </Form.Item>
          </div>
        </template>

        <Divider class="config-divider">BPM执行规则</Divider>

        <Form.Item v-if="usesManualDescription" label="节点说明">
          <Input.TextArea
            v-model:value="currentNode.showText"
            :auto-size="{ minRows: 2, maxRows: 4 }"
            :placeholder="
              isUserNode ? '请输入节点说明，展示在左侧卡片中' : '请输入节点说明'
            "
          />
        </Form.Item>

        <template v-if="isStartNode">
          <Form.Item label="发起范围">
            <div class="config-readonly text-sm text-gray-700">
              <template
                v-if="startUserIds?.length === 0 && startDeptIds?.length === 0"
              >
                全部成员可发起
              </template>
              <template v-else-if="startUserIds?.length">
                指定人员：{{ getUserNicknames(startUserIds) }}
              </template>
              <template v-else>
                指定部门：{{ getDeptNames(startDeptIds) }}
              </template>
            </div>
          </Form.Item>
        </template>

        <template v-else-if="isUserNode">
          <Form.Item label="审批类型">
            <Radio.Group v-model:value="approveType" button-style="solid">
              <Radio.Button
                v-for="item in APPROVE_TYPE"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </Radio.Button>
            </Radio.Group>
          </Form.Item>

          <template v-if="approveType === ApproveType.USER">
            <Form.Item :label="`${nodeActorText}设置`">
              <Select
                v-model:value="userConfigForm.candidateStrategy"
                placeholder="请选择处理人设置"
              >
                <Select.Option
                  v-for="item in candidateStrategyOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              v-if="userConfigForm.candidateStrategy === CandidateStrategy.USER"
              label="指定成员"
            >
              <Select
                v-model:value="userConfigForm.userIds"
                mode="multiple"
                option-filter-prop="label"
                placeholder="请选择成员"
              >
                <Select.Option
                  v-for="item in userOptions"
                  :key="item.id"
                  :value="item.id"
                  :label="item.nickname"
                >
                  {{ item.nickname }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              v-if="userConfigForm.candidateStrategy === CandidateStrategy.ROLE"
              label="指定角色"
            >
              <Select v-model:value="userConfigForm.roleIds" mode="multiple">
                <Select.Option
                  v-for="item in roleOptions"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.name }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              v-if="userConfigForm.candidateStrategy === CandidateStrategy.POST"
              label="指定岗位"
            >
              <Select v-model:value="userConfigForm.postIds" mode="multiple">
                <Select.Option
                  v-for="item in postOptions"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.name }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              v-if="
                userConfigForm.candidateStrategy ===
                CandidateStrategy.USER_GROUP
              "
              label="用户组"
            >
              <Select v-model:value="userConfigForm.userGroups" mode="multiple">
                <Select.Option
                  v-for="item in userGroupOptions"
                  :key="item.id"
                  :value="item.id"
                >
                  {{ item.name }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              v-if="
                [
                  CandidateStrategy.DEPT_MEMBER,
                  CandidateStrategy.DEPT_LEADER,
                  CandidateStrategy.MULTI_LEVEL_DEPT_LEADER,
                ].includes(
                  userConfigForm.candidateStrategy as CandidateStrategy,
                )
              "
              label="指定部门"
            >
              <TreeSelect
                v-model:value="userConfigForm.deptIds"
                :tree-data="deptTreeOptions"
                :field-names="{
                  label: 'name',
                  value: 'id',
                  children: 'children',
                }"
                multiple
                tree-checkable
                show-search
                tree-default-expand-all
                placeholder="请选择部门"
              />
            </Form.Item>

            <Form.Item
              v-if="
                [
                  CandidateStrategy.DEPT_LEADER,
                  CandidateStrategy.MULTI_LEVEL_DEPT_LEADER,
                  CandidateStrategy.START_USER_DEPT_LEADER,
                  CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER,
                ].includes(
                  userConfigForm.candidateStrategy as CandidateStrategy,
                )
              "
              label="负责人层级"
            >
              <Select v-model:value="userConfigForm.deptLevel">
                <Select.Option
                  v-for="item in MULTI_LEVEL_DEPT"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              v-if="
                userConfigForm.candidateStrategy ===
                CandidateStrategy.EXPRESSION
              "
              label="流程表达式"
            >
              <Input.TextArea
                v-model:value="userConfigForm.expression"
                :auto-size="{ minRows: 2, maxRows: 4 }"
                placeholder="请输入流程表达式"
              />
            </Form.Item>

            <Form.Item label="多人审批方式">
              <Select v-model:value="userConfigForm.approveMethod">
                <Select.Option
                  v-for="item in APPROVE_METHODS"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              v-if="
                userConfigForm.approveMethod ===
                ApproveMethodType.APPROVE_BY_RATIO
              "
              label="通过比例(%)"
            >
              <InputNumber
                v-model:value="userConfigForm.approveRatio"
                :min="1"
                :max="100"
                class="w-full"
              />
            </Form.Item>

            <Form.Item label="执行规则预览">
              <div class="config-readonly text-sm text-gray-700">
                {{ userCandidatePreview }}
              </div>
            </Form.Item>

            <Divider class="config-divider">拒绝规则</Divider>

            <template v-if="isPureApproveNode">
              <Form.Item label="拒绝动作">
                <Radio.Group v-model:value="userConfigForm.rejectHandlerType">
                  <div class="vertical-radio-list">
                    <Radio
                      v-for="item in REJECT_HANDLER_TYPES"
                      :key="item.value"
                      :value="item.value"
                    >
                      {{ item.label }}
                    </Radio>
                  </div>
                </Radio.Group>
              </Form.Item>

              <Form.Item
                v-if="
                  userConfigForm.rejectHandlerType ===
                  RejectHandlerType.RETURN_USER_TASK
                "
                label="驳回节点"
              >
                <Select
                  v-model:value="userConfigForm.returnNodeId"
                  placeholder="请选择驳回节点"
                >
                  <Select.Option
                    v-for="item in returnTaskOptions"
                    :key="item.id"
                    :value="item.id"
                  >
                    {{ item.name }}
                  </Select.Option>
                </Select>
              </Form.Item>
            </template>

            <Divider class="config-divider">超时处理</Divider>

            <template v-if="isPureApproveNode">
              <Form.Item label="启用超时">
                <Switch v-model:checked="userConfigForm.timeoutHandlerEnable" />
              </Form.Item>

              <template v-if="userConfigForm.timeoutHandlerEnable">
                <Form.Item label="执行动作">
                  <Radio.Group
                    v-model:value="userConfigForm.timeoutHandlerType"
                  >
                    <div class="vertical-radio-list two-columns">
                      <Radio
                        v-for="item in TIMEOUT_HANDLER_TYPES"
                        :key="item.value"
                        :value="item.value"
                      >
                        {{ item.label }}
                      </Radio>
                    </div>
                  </Radio.Group>
                </Form.Item>

                <Form.Item label="超时时间">
                  <div class="flex items-center gap-2">
                    <span class="text-xs text-gray-500">超过</span>
                    <InputNumber
                      v-model:value="userConfigForm.timeDuration"
                      :min="1"
                      class="flex-1"
                    />
                    <Select v-model:value="timeoutUnit" class="w-24">
                      <Select.Option
                        v-for="item in TIME_UNIT_TYPES"
                        :key="item.value"
                        :value="item.value"
                      >
                        {{ item.label }}
                      </Select.Option>
                    </Select>
                    <span class="text-xs text-gray-500">未处理</span>
                  </div>
                </Form.Item>

                <Form.Item
                  v-if="
                    userConfigForm.timeoutHandlerType ===
                    TimeoutHandlerType.REMINDER
                  "
                  label="提醒次数"
                >
                  <InputNumber
                    v-model:value="userConfigForm.maxRemindCount"
                    :min="1"
                    :max="10"
                    class="w-full"
                  />
                </Form.Item>
              </template>
            </template>

            <Form.Item label="超时规则预览">
              <div class="config-readonly text-sm text-gray-700">
                {{ timeoutPreview }}
              </div>
            </Form.Item>

            <Divider class="config-divider">空处理人</Divider>

            <Form.Item :label="`${nodeActorText}为空时`">
              <Radio.Group
                v-model:value="userConfigForm.assignEmptyHandlerType"
              >
                <div class="vertical-radio-list">
                  <Radio
                    v-for="item in ASSIGN_EMPTY_HANDLER_TYPES"
                    :key="item.value"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </Radio>
                </div>
              </Radio.Group>
            </Form.Item>

            <Form.Item
              v-if="
                userConfigForm.assignEmptyHandlerType ===
                AssignEmptyHandlerType.ASSIGN_USER
              "
              label="指定成员"
            >
              <Select
                v-model:value="userConfigForm.assignEmptyHandlerUserIds"
                mode="multiple"
                option-filter-prop="label"
                placeholder="请选择成员"
              >
                <Select.Option
                  v-for="item in userOptions"
                  :key="item.id"
                  :value="item.id"
                  :label="item.nickname"
                >
                  {{ item.nickname }}
                </Select.Option>
              </Select>
            </Form.Item>

            <Form.Item label="空处理人规则预览">
              <div class="config-readonly text-sm text-gray-700">
                {{ assignEmptyPreview }}
              </div>
            </Form.Item>

            <template v-if="isPureApproveNode">
              <Divider class="config-divider">同发起人处理</Divider>

              <Form.Item label="审批人与发起人相同时">
                <Radio.Group
                  v-model:value="userConfigForm.assignStartUserHandlerType"
                >
                  <div class="vertical-radio-list">
                    <Radio
                      v-for="item in ASSIGN_START_USER_HANDLER_TYPES"
                      :key="item.value"
                      :value="item.value"
                    >
                      {{ item.label }}
                    </Radio>
                  </div>
                </Radio.Group>
              </Form.Item>
            </template>

            <Divider class="config-divider">扩展规则</Divider>

            <template v-if="isPureApproveNode">
              <Form.Item label="签名">
                <Switch v-model:checked="userConfigForm.signEnable" />
              </Form.Item>

              <Form.Item label="审批意见必填">
                <Switch v-model:checked="userConfigForm.reasonRequire" />
              </Form.Item>
            </template>

            <Form.Item label="跳过表达式">
              <Input.TextArea
                v-model:value="userConfigForm.skipExpression"
                :auto-size="{ minRows: 2, maxRows: 4 }"
                placeholder="满足表达式时自动跳过当前节点"
              />
            </Form.Item>

            <Divider class="config-divider">操作按钮</Divider>

            <div class="button-setting-table">
              <div class="button-setting-head">
                <span>按钮</span>
                <span>显示名称</span>
                <span>启用</span>
              </div>
              <div
                v-for="item in buttonSettings"
                :key="item.id"
                class="button-setting-row"
              >
                <div class="truncate text-gray-700">
                  {{ OPERATION_BUTTON_NAME.get(item.id) }}
                </div>
                <Input
                  v-model:value="item.displayName"
                  size="small"
                  @blur="normalizeButtonDisplayName(item)"
                />
                <div class="flex justify-end">
                  <Switch v-model:checked="item.enable" size="small" />
                </div>
              </div>
            </div>

            <Form.Item label="按钮概览" class="mt-3">
              <div class="config-readonly text-sm text-gray-700">
                {{ buttonSummaryText }}
              </div>
            </Form.Item>
          </template>

          <Form.Item v-else label="自动处理说明">
            <div class="config-readonly text-sm text-gray-700">
              {{ getApproveTypeText(approveType) }}
            </div>
          </Form.Item>
        </template>

        <template v-else-if="isCopyNode">
          <Form.Item label="抄送人设置">
            <Select
              v-model:value="copyConfigForm.candidateStrategy"
              placeholder="请选择抄送人设置"
            >
              <Select.Option
                v-for="item in copyCandidateStrategyOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            v-if="copyConfigForm.candidateStrategy === CandidateStrategy.USER"
            label="指定成员"
          >
            <Select
              v-model:value="copyConfigForm.userIds"
              mode="multiple"
              option-filter-prop="label"
              placeholder="请选择成员"
            >
              <Select.Option
                v-for="item in userOptions"
                :key="item.id"
                :value="item.id"
                :label="item.nickname"
              >
                {{ item.nickname }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            v-if="copyConfigForm.candidateStrategy === CandidateStrategy.ROLE"
            label="指定角色"
          >
            <Select v-model:value="copyConfigForm.roleIds" mode="multiple">
              <Select.Option
                v-for="item in roleOptions"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            v-if="copyConfigForm.candidateStrategy === CandidateStrategy.POST"
            label="指定岗位"
          >
            <Select v-model:value="copyConfigForm.postIds" mode="multiple">
              <Select.Option
                v-for="item in postOptions"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            v-if="
              copyConfigForm.candidateStrategy === CandidateStrategy.USER_GROUP
            "
            label="用户组"
          >
            <Select v-model:value="copyConfigForm.userGroups" mode="multiple">
              <Select.Option
                v-for="item in userGroupOptions"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            v-if="
              [
                CandidateStrategy.DEPT_MEMBER,
                CandidateStrategy.DEPT_LEADER,
                CandidateStrategy.MULTI_LEVEL_DEPT_LEADER,
              ].includes(copyConfigForm.candidateStrategy as CandidateStrategy)
            "
            label="指定部门"
          >
            <TreeSelect
              v-model:value="copyConfigForm.deptIds"
              :tree-data="deptTreeOptions"
              :field-names="{
                label: 'name',
                value: 'id',
                children: 'children',
              }"
              multiple
              tree-checkable
              show-search
              tree-default-expand-all
              placeholder="请选择部门"
            />
          </Form.Item>

          <Form.Item
            v-if="
              [
                CandidateStrategy.DEPT_LEADER,
                CandidateStrategy.MULTI_LEVEL_DEPT_LEADER,
                CandidateStrategy.START_USER_DEPT_LEADER,
                CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER,
              ].includes(copyConfigForm.candidateStrategy as CandidateStrategy)
            "
            label="负责人层级"
          >
            <Select v-model:value="copyConfigForm.deptLevel">
              <Select.Option
                v-for="item in MULTI_LEVEL_DEPT"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            v-if="
              copyConfigForm.candidateStrategy === CandidateStrategy.EXPRESSION
            "
            label="流程表达式"
          >
            <Input.TextArea
              v-model:value="copyConfigForm.expression"
              :auto-size="{ minRows: 2, maxRows: 4 }"
              placeholder="请输入流程表达式"
            />
          </Form.Item>

          <Form.Item label="规则预览">
            <div class="config-readonly text-sm text-gray-700">
              {{ copyCandidatePreview }}
            </div>
          </Form.Item>
        </template>

        <template v-else-if="isDelayNode">
          <Form.Item label="延迟时间">
            <Radio.Group v-model:value="delayConfig.delayType">
              <Radio
                v-for="item in DELAY_TYPE"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </Radio>
            </Radio.Group>
          </Form.Item>

          <Form.Item
            v-if="delayConfig.delayType === DelayTypeEnum.FIXED_TIME_DURATION"
            label="固定时长"
          >
            <div class="flex gap-2">
              <InputNumber
                v-model:value="delayConfig.timeDuration"
                :min="1"
                class="flex-1"
              />
              <Select v-model:value="delayConfig.timeUnit" class="w-28">
                <Select.Option
                  v-for="item in TIME_UNIT_TYPES"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </Select.Option>
              </Select>
            </div>
          </Form.Item>

          <Form.Item
            v-if="delayConfig.delayType === DelayTypeEnum.FIXED_DATE_TIME"
            label="固定日期时间"
          >
            <DatePicker
              v-model:value="delayConfig.dateTime"
              class="w-full"
              show-time
              placeholder="请选择日期和时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </Form.Item>

          <Form.Item label="规则预览">
            <div class="config-readonly text-sm text-gray-700">
              {{ delayPreview }}
            </div>
          </Form.Item>
        </template>

        <template v-else-if="isTriggerNode">
          <Form.Item label="触发器类型">
            <div class="config-readonly text-sm text-gray-700">
              {{ triggerTypeText }}
            </div>
          </Form.Item>
          <Form.Item label="当前配置">
            <div class="config-readonly text-sm text-gray-700">
              {{ currentNode.showText || '当前触发器尚未补充详细配置' }}
            </div>
          </Form.Item>
        </template>

        <template v-else-if="isRouterNode">
          <Form.Item label="路由概览">
            <div class="config-readonly text-sm text-gray-700">
              {{ routerPreview }}
            </div>
          </Form.Item>
          <Form.Item label="当前说明">
            <div class="config-readonly text-sm text-gray-700">
              {{ currentNode.showText || '请补充分支规则说明' }}
            </div>
          </Form.Item>
        </template>

        <template v-else-if="isChildProcessNode">
          <Form.Item label="子流程">
            <div class="config-readonly text-sm text-gray-700">
              {{ childProcessPreview }}
            </div>
          </Form.Item>
          <Form.Item label="执行方式">
            <div class="config-readonly text-sm text-gray-700">
              {{
                currentNode.childProcessSetting?.async ? '异步执行' : '同步执行'
              }}
            </div>
          </Form.Item>
        </template>

        <template v-else-if="isEndNode">
          <Form.Item label="节点说明">
            <div class="config-readonly text-sm text-gray-700">
              结束节点无需额外配置，流程会在这里完成。
            </div>
          </Form.Item>
        </template>

        <template v-else>
          <Form.Item>
            <div class="config-empty-tip">
              当前节点先支持基础信息编辑；复杂配置我会继续补到右侧固定面板。
            </div>
          </Form.Item>
        </template>

        <template v-if="showFieldPermission">
          <div ref="fieldsSectionRef" class="panel-section-anchor"></div>

          <Divider class="config-divider">表单字段权限</Divider>

          <div class="field-permission-table">
            <div class="field-permission-head">
              <span>字段</span>
              <span>权限</span>
            </div>
            <div
              v-for="item in fieldsPermissionConfig"
              :key="item.field"
              class="field-permission-row"
            >
              <div class="truncate text-gray-700">{{ item.title }}</div>
              <Radio.Group v-model:value="item.permission" size="small">
                <Radio.Button :value="FieldPermissionType.READ">
                  只读
                </Radio.Button>
                <Radio.Button :value="FieldPermissionType.WRITE">
                  可编辑
                </Radio.Button>
                <Radio.Button :value="FieldPermissionType.NONE">
                  隐藏
                </Radio.Button>
              </Radio.Group>
            </div>
          </div>
        </template>

        <template v-if="isUserNode">
          <div ref="listenerSectionRef" class="panel-section-anchor"></div>

          <Divider class="config-divider">任务监听器</Divider>

          <div class="listener-panel">
            <UserTaskListener
              v-model="userConfigForm"
              :form-field-options="[]"
            />
          </div>
        </template>
      </Form>
    </template>

    <Empty v-else description="请选择左侧流程节点" />
  </div>
</template>

<style scoped>
.node-config-panel :deep(.ant-form-item) {
  margin-bottom: 6px;
}

.node-config-panel {
  min-height: 100%;
  padding-bottom: 56px;
}

.node-config-panel :deep(.ant-form-item-label) {
  padding-bottom: 2px;
}

.node-config-panel :deep(.ant-form-item-label > label) {
  font-size: 11px;
}

.node-config-panel :deep(.ant-input),
.node-config-panel :deep(.ant-input-number),
.node-config-panel :deep(.ant-picker),
.node-config-panel :deep(.ant-select-selector),
.node-config-panel :deep(.ant-tree-select-selector) {
  min-height: 26px;
  font-size: 11px;
}

.node-config-panel :deep(.ant-input),
.node-config-panel :deep(.ant-input-affix-wrapper),
.node-config-panel :deep(.ant-select-selection-item),
.node-config-panel :deep(.ant-select-selection-placeholder),
.node-config-panel :deep(.ant-picker-input > input),
.node-config-panel :deep(.ant-input-number-input),
.node-config-panel :deep(.ant-radio-button-wrapper),
.node-config-panel :deep(.ant-radio-wrapper) {
  font-size: 11px;
}

.node-config-panel :deep(.ant-radio-button-wrapper) {
  height: 26px;
  line-height: 24px;
  padding-inline: 8px;
}

.node-config-panel :deep(.ant-select-multiple .ant-select-selection-item) {
  height: 16px;
  margin-block: 1px;
  padding-inline: 5px;
  font-size: 10px;
  line-height: 14px;
}

.node-config-panel :deep(.ant-switch) {
  min-width: 24px;
  height: 16px;
}

.node-config-panel :deep(.ant-switch .ant-switch-handle) {
  width: 12px;
  height: 12px;
}

.node-config-panel :deep(.ant-tag) {
  margin-inline-end: 0;
  padding-inline: 6px;
  font-size: 10px;
  line-height: 16px;
}

.config-section {
  margin-bottom: 10px;
}

.compact-section :deep(.ant-form-item-row) {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  align-items: center;
  column-gap: 6px;
}

.compact-section :deep(.ant-form-item .ant-form-item-label) {
  min-width: 0;
  padding: 0;
  text-align: left;
}

.compact-section :deep(.ant-form-item .ant-form-item-label > label) {
  display: flex;
  align-items: center;
  min-height: 26px;
  font-size: 11px;
}

.compact-section :deep(.ant-form-item .ant-form-item-control) {
  min-width: 0;
}

.compact-section :deep(.ant-form-item .ant-form-item-control-input) {
  min-height: 26px;
}

.compact-section :deep(.ant-form-item .ant-form-item-control-input-content) {
  min-width: 0;
}

.config-section-title,
.config-section-title-row {
  margin-bottom: 6px;
}

.config-section-title {
  color: #0f172a;
  font-size: 12px;
  font-weight: 700;
}

.config-section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.config-section-caption {
  color: #94a3b8;
  font-size: 10px;
  line-height: 1;
}

.config-static-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  color: #475569;
  font-size: 11px;
}

.config-static-row strong {
  color: #0f172a;
  font-size: 12px;
}

.config-static-meta {
  margin-top: 1px;
  color: #94a3b8;
  font-size: 10px;
}

.config-empty-line {
  margin-bottom: 6px;
  border: 1px dashed rgb(148 163 184 / 38%);
  border-radius: 8px;
  padding: 6px 8px;
  color: #94a3b8;
  font-size: 11px;
}

.timeout-rule-list {
  display: grid;
  gap: 6px;
  margin-bottom: 6px;
}

.timeout-rule-card {
  border: 1px solid rgb(15 23 42 / 8%);
  border-radius: 8px;
  background: #f8fafc;
  padding: 6px;
}

.timeout-rule-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 66px 88px 38px;
  gap: 4px;
}

.timeout-rule-extra {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  color: #64748b;
  font-size: 11px;
}

.timeout-rule-extra .ant-input-number {
  width: 76px;
}

.panel-section-nav {
  position: sticky;
  top: 0;
  z-index: 4;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  background: linear-gradient(
    180deg,
    #fff 0%,
    #fff 82%,
    rgb(255 255 255 / 0%) 100%
  );
}

.panel-section-button {
  border: 1px solid rgb(15 23 42 / 10%);
  border-radius: 999px;
  background: #f8fafc;
  padding: 2px 8px;
  color: #475569;
  font-size: 11px;
  line-height: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.panel-section-button:hover {
  border-color: rgb(59 130 246 / 28%);
  background: #eff6ff;
  color: #2563eb;
}

.panel-section-anchor {
  scroll-margin-top: 40px;
}

.node-config-panel :deep(.ant-form) {
  min-height: max-content;
}

.node-config-panel :deep(.ant-divider) {
  margin: 10px 0 8px;
  color: #111827;
  font-size: 11px;
}

.config-readonly {
  border: 1px solid rgb(15 23 42 / 6%);
  border-radius: 8px;
  background: #f8fafc;
  padding: 5px 7px;
  line-height: 1.5;
}

.config-empty-tip {
  border: 1px dashed rgb(148 163 184 / 45%);
  border-radius: 8px;
  background: #fafafa;
  padding: 7px 8px;
  color: #6b7280;
  font-size: 11px;
}

.vertical-radio-list {
  display: grid;
  gap: 6px;
}

.vertical-radio-list.two-columns {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.action-matrix-item :deep(.ant-form-item-control-input) {
  min-height: auto;
}

.action-matrix {
  overflow: hidden;
  border: 1px solid rgb(15 23 42 / 8%);
  border-radius: 8px;
}

.action-matrix-head,
.action-matrix-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 34px 34px;
  align-items: center;
  gap: 6px;
  padding: 5px 7px;
}

.action-matrix-head {
  background: #f8fafc;
  color: #6b7280;
  font-size: 10px;
}

.action-matrix-row + .action-matrix-row {
  border-top: 1px solid rgb(15 23 42 / 6%);
}

.action-matrix-cell {
  display: flex;
  justify-content: flex-end;
}

.button-setting-table {
  overflow: hidden;
  border: 1px solid rgb(15 23 42 / 8%);
  border-radius: 8px;
}

.button-setting-head,
.button-setting-row {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) 40px;
  align-items: center;
  gap: 6px;
  padding: 5px 7px;
}

.button-setting-head {
  background: #f8fafc;
  color: #6b7280;
  font-size: 11px;
}

.button-setting-row + .button-setting-row {
  border-top: 1px solid rgb(15 23 42 / 6%);
}

.field-permission-table,
.listener-panel {
  overflow: hidden;
  border: 1px solid rgb(15 23 42 / 8%);
  border-radius: 10px;
  background: #fff;
}

.field-permission-head,
.field-permission-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 6px;
  padding: 5px 7px;
}

.field-permission-head {
  background: #f8fafc;
  color: #6b7280;
  font-size: 11px;
}

.field-permission-row + .field-permission-row {
  border-top: 1px solid rgb(15 23 42 / 6%);
}

.listener-panel {
  padding: 0 7px 7px;
}

.listener-panel :deep(.ant-divider) {
  margin: 10px 0 8px;
}

.listener-panel :deep(.ant-form-item) {
  margin-bottom: 8px;
}

.listener-panel :deep(.ant-alert) {
  padding: 5px 7px;
}
</style>
