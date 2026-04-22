<script lang="ts" setup>
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';
import type { HospitalContourTaskApi } from '#/api/hospital/contour-task';
import type { HospitalDoctorApi } from '#/api/hospital/doctor';
import type { HospitalPatientApi } from '#/api/hospital/patient';
import type { HospitalPlanApplyApi } from '#/api/hospital/plan-apply';
import type { HospitalPlanDesignApi } from '#/api/hospital/plan-design';
import type { HospitalWorkflowKey } from '#/views/hospital/_shared/workflow';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';

import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

import { getApprovalDetail } from '#/api/bpm/processInstance';
import {
  getContourTask,
  getSimpleContourTaskList,
} from '#/api/hospital/contour-task';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getPatient } from '#/api/hospital/patient';
import {
  getPlanApply,
  getSimplePlanApplyList,
} from '#/api/hospital/plan-apply';
import {
  buildWorkflowStepsFromApprovalNodes,
  getWorkflowModelKey,
  getWorkflowRoutePath,
  getWorkflowStepsFromModelPath,
} from '#/views/hospital/_shared/workflow';

import NodeCustomForm from '#/views/hospital/_shared/node-custom-form.vue';

type EntryKind = 'apply' | 'design';
type EntryStatusKey = 'ALL' | 'DRAFT' | 'REJECTED';
type EntryRecord =
  | HospitalPlanApplyApi.PlanApply
  | HospitalPlanDesignApi.PlanDesign;

type StepTone = 'current' | 'done' | 'pending';

interface ApplyFormState {
  applyDoctorId?: number;
  bizNo: string;
  clinicalDiagnosis: string;
  contourTaskId?: number;
  id?: number;
  prescriptionDose?: number;
  priority: string;
  remark: string;
  status?: number;
  totalFractions?: number;
  treatmentSite: string;
}

interface DesignFormState {
  bizNo: string;
  designUserId?: number;
  fractions?: number;
  id?: number;
  planApplyId?: number;
  planName: string;
  remark: string;
  singleDose?: number;
  status?: number;
  totalDose?: number;
  versionNo: string;
}

interface WorkflowStep {
  actorText?: string;
  desc: string;
  key: string;
  reasonText?: string;
  statusText?: string;
  timeText?: string;
  title: string;
  tone: StepTone;
}

interface EntryWorkbenchApi {
  getDetail: (id: number) => Promise<EntryRecord>;
  getPage: (params: Record<string, any>) => Promise<{
    list?: EntryRecord[];
    total?: number;
  }>;
  remove: (id: number) => Promise<unknown>;
  save: (data: any) => Promise<unknown>;
  submit: (id: number) => Promise<unknown>;
}

interface EntryWorkbenchConfig {
  actorLabel: string;
  bizLabel: string;
  createAuth: string;
  createText: string;
  deleteAuth: string;
  emptyText: string;
  kind: EntryKind;
  listTitle: string;
  pageCode: string;
  submitAuth: string;
  subtitle: string;
  title: string;
  updateAuth: string;
  workflowKey: HospitalWorkflowKey;
}

const props = defineProps<{
  api: EntryWorkbenchApi;
  config: EntryWorkbenchConfig;
  onCreate?: () => void;
}>();

const statusLabelMap: Record<string, string> = {
  APPROVED: '已完成',
  DRAFT: '待维护',
  REJECTED: '已退回',
  REVIEWING: '处理中',
};

const priorityOptions = [
  { label: '常规', value: 'NORMAL' },
  { label: '加急', value: 'URGENT' },
  { label: '紧急', value: 'EMERGENCY' },
];

const priorityLabelMap: Record<string, string> = {
  EMERGENCY: '紧急',
  NORMAL: '常规',
  URGENT: '加急',
};

const { hasAccessByCodes } = useAccess();
const route = useRoute();
const router = useRouter();

function getFocusId() {
  const rawValue = Array.isArray(route.query.focusId)
    ? route.query.focusId[0]
    : route.query.focusId;
  const focusId = Number(rawValue);
  return Number.isFinite(focusId) && focusId > 0 ? focusId : undefined;
}

async function clearFocusId() {
  if (route.query.focusId == null) return;
  const query = { ...route.query };
  delete query.focusId;
  await router.replace({ query });
}


const isApply = computed(() => props.config.kind === 'apply');
const listLoading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const selectedRecordId = ref<number>();
const recordList = ref<EntryRecord[]>([]);
const selectedRecord = ref<EntryRecord>();
const selectedPlanApply = ref<HospitalPlanApplyApi.PlanApply>();
const selectedContourTask = ref<HospitalContourTaskApi.ContourTask>();
const patientDetail = ref<HospitalPatientApi.Patient>();
const approvalNodes = ref<BpmProcessInstanceApi.ApprovalNodeInfo[]>([]);
const modelWorkflowSteps = ref<WorkflowStep[]>([]);
const approvalProcessName = ref('');
const approvalProcessId = ref('');
const approvalProcessKey = ref('');
const doctorOptions = ref<HospitalDoctorApi.Doctor[]>([]);
const contourTaskOptions = ref<HospitalContourTaskApi.ContourTask[]>([]);
const planApplyOptions = ref<HospitalPlanApplyApi.PlanApply[]>([]);
const activeStatus = ref<EntryStatusKey>('ALL');
const currentNodeStatuses = ['DRAFT', 'REJECTED'] as const;

const pager = reactive({
  pageNo: 1,
  pageSize: 12,
  total: 0,
});

const filters = reactive({
  actorId: undefined as number | undefined,
  bizNo: '',
  patientName: '',
});

const applyForm = reactive<ApplyFormState>({
  applyDoctorId: undefined,
  bizNo: '',
  clinicalDiagnosis: '',
  contourTaskId: undefined,
  id: undefined,
  prescriptionDose: 0,
  priority: 'NORMAL',
  remark: '',
  status: 0,
  totalFractions: 0,
  treatmentSite: '',
});

const designForm = reactive<DesignFormState>({
  bizNo: '',
  designUserId: undefined,
  fractions: 0,
  id: undefined,
  planApplyId: undefined,
  planName: '',
  remark: '',
  singleDose: 0,
  status: 0,
  totalDose: 0,
  versionNo: 'V1',
});

const canCreate = computed(() => hasAccessByCodes([props.config.createAuth]));
const canUpdate = computed(() => hasAccessByCodes([props.config.updateAuth]));
const canSubmit = computed(() => hasAccessByCodes([props.config.submitAuth]));
const canDelete = computed(() => hasAccessByCodes([props.config.deleteAuth]));

const canEditCurrent = computed(() => {
  return ['DRAFT', 'REJECTED'].includes(getRecordStatus(selectedRecord.value));
});

const readonlyMessage = computed(() => {
  const status = getRecordStatus(selectedRecord.value);
  if (status === 'REVIEWING') return '当前单据流转中，右侧信息只读。';
  if (status === 'APPROVED') return '当前单据已完成，右侧信息只读。';
  return '';
});

const statusTabs = computed(() => {
  const buildItem = (key: EntryStatusKey, label: string) => ({ key, label });
  return [
    buildItem('ALL', '当前节点全部'),
    buildItem('DRAFT', '待维护'),
    buildItem('REJECTED', '退回待处理'),
  ];
});

const recordWorkflowStatusList = computed(() => {
  return activeStatus.value === 'ALL'
    ? [...currentNodeStatuses]
    : [activeStatus.value];
});

const currentProcessKey = computed(
  () => approvalProcessKey.value || getWorkflowModelKey(props.config.workflowKey),
);
const currentNodeKey = computed(
  () => approvalNodes.value.find((item) => item.status === 0 || item.status === 1)?.id,
);

const nodeFormValues = computed<Record<string, any>>(() => {
  const patientName =
    selectedRecord.value?.patientName ||
    selectedPlanApply.value?.patientName ||
    patientDetail.value?.name;
  const patientNo =
    patientDetail.value?.medicalRecordNo || patientDetail.value?.patientNo;

  if (isApply.value) {
    return {
      applyDoctorName:
        selectedPlanApply.value?.applyDoctorName || getRecordActorName(selectedRecord.value),
      clinicalDiagnosis:
        applyForm.clinicalDiagnosis || selectedPlanApply.value?.clinicalDiagnosis,
      patientName,
      patientNo,
      planRemark: applyForm.remark || selectedPlanApply.value?.remark,
      prescriptionDose:
        applyForm.prescriptionDose ?? selectedPlanApply.value?.prescriptionDose,
      priority: applyForm.priority || selectedPlanApply.value?.priority,
      remark: applyForm.remark || selectedPlanApply.value?.remark,
      totalDose:
        applyForm.prescriptionDose ?? selectedPlanApply.value?.prescriptionDose,
      totalFractions:
        applyForm.totalFractions ?? selectedPlanApply.value?.totalFractions,
      treatmentSite: applyForm.treatmentSite || selectedPlanApply.value?.treatmentSite,
    };
  }

  const design = selectedRecord.value as HospitalPlanDesignApi.PlanDesign | undefined;
  return {
    designUserName: design?.designUserName,
    fractions: designForm.fractions ?? design?.fractions,
    patientName,
    patientNo,
    planName: designForm.planName || design?.planName,
    planRemark: designForm.remark || design?.remark,
    remark: designForm.remark || design?.remark,
    singleDose: designForm.singleDose ?? design?.singleDose,
    totalDose: designForm.totalDose ?? design?.totalDose,
    versionNo: designForm.versionNo || design?.versionNo,
  };
});

const workflowSteps = computed<WorkflowStep[]>(() => {
  if (approvalNodes.value.length > 0) {
    return buildWorkflowStepsFromApprovalNodes(
      props.config.workflowKey,
      approvalNodes.value,
      modelWorkflowSteps.value,
    ) as WorkflowStep[];
  }

  const baseSteps =
    modelWorkflowSteps.value.length > 0
      ? modelWorkflowSteps.value
      : (buildWorkflowStepsFromApprovalNodes(
          props.config.workflowKey,
          undefined,
          modelWorkflowSteps.value,
        ) as WorkflowStep[]);
  const currentIndex = getWorkflowCurrentIndex(
    getRecordStatus(selectedRecord.value),
  );
  return baseSteps.map((item, index) => ({
    ...item,
    desc: getWorkflowDesc(index, item.desc),
    tone:
      index < currentIndex
        ? 'done'
        : index === currentIndex
          ? 'current'
          : 'pending',
  }));
});

const timelineSteps = computed<WorkflowStep[]>(() => {
  return workflowSteps.value.map((item) => ({
    ...item,
    reasonText: undefined,
  }));
});

async function loadModelWorkflowSteps() {
  const workflowRoutePath = getWorkflowRoutePath(props.config.workflowKey);
  if (!workflowRoutePath) {
    modelWorkflowSteps.value = [];
    return;
  }
  try {
    modelWorkflowSteps.value = (await getWorkflowStepsFromModelPath(
      props.config.workflowKey,
      workflowRoutePath,
    )) as WorkflowStep[];
  } catch {
    modelWorkflowSteps.value = [];
  }
}

function displayText(value?: null | number | string) {
  return value == null || `${value}`.trim() === '' ? '--' : `${value}`;
}

function displayDose(value?: null | number | string) {
  return value == null || `${value}`.trim() === '' ? '--' : `${value} Gy`;
}

function displayFractions(value?: null | number | string) {
  return value == null || `${value}`.trim() === '' ? '--' : `${value} 次`;
}

function formatDate(value?: Date | number | string) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '--';
}

function formatDateTime(value?: Date | number | string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--';
}

function formatTime(value?: Date | number | string) {
  return value ? dayjs(value).format('HH:mm') : '--';
}

function getGenderLabel(value?: number) {
  if (value === 1) return '男';
  if (value === 2) return '女';
  return '--';
}

function getPriorityLabel(value?: string) {
  return priorityLabelMap[value || ''] || displayText(value);
}

function getStatusColor(status?: string) {
  if (status === 'APPROVED') return 'success';
  if (status === 'REJECTED') return 'error';
  if (status === 'REVIEWING') return 'processing';
  if (status === 'DRAFT') return 'warning';
  return 'default';
}

function getRecordStatus(record?: EntryRecord) {
  return (record as any)?.workflowStatus || '';
}

function getStatusLabel(status?: string) {
  return statusLabelMap[status || ''] || displayText(status);
}

function getRecordActorName(record?: EntryRecord) {
  if (!record) return '--';
  return isApply.value
    ? displayText((record as HospitalPlanApplyApi.PlanApply).applyDoctorName)
    : displayText((record as HospitalPlanDesignApi.PlanDesign).designUserName);
}

function getRecordBizNo(record?: EntryRecord) {
  return displayText((record as any)?.bizNo);
}

function getRecordSubmitAt(record?: EntryRecord) {
  return (record as any)?.submitTime || (record as any)?.createTime;
}

function getRecordStageValue(record?: EntryRecord) {
  if (!record) return '--';
  return isApply.value
    ? displayText((record as HospitalPlanApplyApi.PlanApply).treatmentSite)
    : displayText((record as HospitalPlanDesignApi.PlanDesign).planName);
}

function getPatientAvatarText() {
  const name =
    selectedRecord.value?.patientName ||
    selectedPlanApply.value?.patientName ||
    patientDetail.value?.name ||
    '患';
  return `${name}`.slice(0, 1);
}

function getWorkflowCurrentIndex(status?: string) {
  if (props.config.kind === 'apply') {
    return ['APPROVED', 'REVIEWING'].includes(status || '') ? 7 : 6;
  }
  return ['APPROVED', 'REVIEWING'].includes(status || '') ? 8 : 7;
}

function getWorkflowDesc(index: number, fallbackDesc: string) {
  if (props.config.kind === 'apply') {
    if (index === 6) {
      return `${getRecordActorName(selectedRecord.value)} · ${getStatusLabel(getRecordStatus(selectedRecord.value))}`;
    }
    if (index === 7) {
      return getRecordStatus(selectedRecord.value) === 'APPROVED'
        ? '已进入计划设计'
        : '待计划设计';
    }
  }
  if (props.config.kind === 'design') {
    if (index === 6) {
      return `${displayText(selectedPlanApply.value?.bizNo)} · ${displayText(selectedPlanApply.value?.applyDoctorName)}`;
    }
    if (index === 7) {
      return `${getRecordActorName(selectedRecord.value)} · ${getStatusLabel(getRecordStatus(selectedRecord.value))}`;
    }
    if (index === 8) {
      return getRecordStatus(selectedRecord.value) === 'APPROVED'
        ? '审核完成'
        : '待组长审核';
    }
  }
  return fallbackDesc;
}

function clearDetail() {
  selectedRecordId.value = undefined;
  selectedRecord.value = undefined;
  selectedPlanApply.value = undefined;
  selectedContourTask.value = undefined;
  patientDetail.value = undefined;
  approvalNodes.value = [];
  approvalProcessName.value = '';
  approvalProcessId.value = '';
  approvalProcessKey.value = '';
  Object.assign(applyForm, {
    applyDoctorId: undefined,
    bizNo: '',
    clinicalDiagnosis: '',
    contourTaskId: undefined,
    id: undefined,
    prescriptionDose: 0,
    priority: 'NORMAL',
    remark: '',
    status: 0,
    totalFractions: 0,
    treatmentSite: '',
  });
  Object.assign(designForm, {
    bizNo: '',
    designUserId: undefined,
    fractions: 0,
    id: undefined,
    planApplyId: undefined,
    planName: '',
    remark: '',
    singleDose: 0,
    status: 0,
    totalDose: 0,
    versionNo: 'V1',
  });
}

function fillApplyForm(record?: HospitalPlanApplyApi.PlanApply) {
  Object.assign(applyForm, {
    applyDoctorId: record?.applyDoctorId,
    bizNo: record?.bizNo || '',
    clinicalDiagnosis: record?.clinicalDiagnosis || '',
    contourTaskId: record?.contourTaskId,
    id: record?.id,
    prescriptionDose: Number(record?.prescriptionDose || 0),
    priority: record?.priority || 'NORMAL',
    remark: record?.remark || '',
    status: record?.status ?? 0,
    totalFractions: Number(record?.totalFractions || 0),
    treatmentSite: record?.treatmentSite || '',
  });
}

function fillDesignForm(record?: HospitalPlanDesignApi.PlanDesign) {
  Object.assign(designForm, {
    bizNo: record?.bizNo || '',
    designUserId: record?.designUserId,
    fractions: Number(record?.fractions || 0),
    id: record?.id,
    planApplyId: record?.planApplyId,
    planName: record?.planName || '',
    remark: record?.remark || '',
    singleDose: Number(record?.singleDose || 0),
    status: record?.status ?? 0,
    totalDose: Number(record?.totalDose || 0),
    versionNo: record?.versionNo || 'V1',
  });
}

async function loadOptions() {
  const [doctorResult, contourTaskResult, planApplyResult] =
    await Promise.allSettled([
      getSimpleDoctorList(),
      getSimpleContourTaskList(),
      getSimplePlanApplyList(),
    ]);

  doctorOptions.value =
    doctorResult.status === 'fulfilled' ? doctorResult.value || [] : [];
  contourTaskOptions.value =
    contourTaskResult.status === 'fulfilled'
      ? contourTaskResult.value || []
      : [];
  planApplyOptions.value =
    planApplyResult.status === 'fulfilled' ? planApplyResult.value || [] : [];
}

async function loadRecordList(preserveSelection = true) {
  listLoading.value = true;
  try {
    const focusId = getFocusId();
    const result = await props.api.getPage({
      id: focusId,
      bizNo: filters.bizNo || undefined,
      pageNo: pager.pageNo,
      pageSize: pager.pageSize,
      patientName: filters.patientName || undefined,
      workflowStatus: undefined,
      workflowStatusList: [...recordWorkflowStatusList.value],
      ...(isApply.value
        ? { applyDoctorId: filters.actorId }
        : { designUserId: filters.actorId }),
    });
    recordList.value = result.list || [];
    pager.total = result.total || 0;

    if (recordList.value.length === 0 && pager.total > 0 && pager.pageNo > 1) {
      pager.pageNo -= 1;
      await loadRecordList(false);
      return;
    }

    const nextId = focusId
      ? focusId
      : preserveSelection &&
            selectedRecordId.value &&
            recordList.value.some((item) => item.id === selectedRecordId.value)
          ? selectedRecordId.value
          : recordList.value[0]?.id;

    if (nextId) {
      await loadRecordDetail(nextId);
      if (focusId) {
        await clearFocusId();
      }
    } else {
      clearDetail();
    }
  } finally {
    listLoading.value = false;
  }
}

async function loadRecordDetail(id: number) {
  detailLoading.value = true;
  try {
    const detail = await props.api.getDetail(id);
    selectedRecordId.value = id;
    selectedRecord.value = detail;
    approvalNodes.value = [];
    approvalProcessName.value = '';
    approvalProcessId.value = (detail as any)?.processInstanceId || '';
    approvalProcessKey.value = '';

    if (isApply.value) {
      const apply = detail as HospitalPlanApplyApi.PlanApply;
      selectedPlanApply.value = apply;
      fillApplyForm(apply);

      const [patientResult, contourTaskResult, approvalResult] =
        await Promise.allSettled([
          apply.patientId
            ? getPatient(apply.patientId)
            : Promise.resolve(undefined),
          apply.contourTaskId
            ? getContourTask(apply.contourTaskId)
            : Promise.resolve(undefined),
          (detail as any)?.processInstanceId
            ? getApprovalDetail({
                processInstanceId: (detail as any).processInstanceId,
              })
            : Promise.resolve(undefined),
        ]);

      patientDetail.value =
        patientResult.status === 'fulfilled' ? patientResult.value : undefined;
      selectedContourTask.value =
        contourTaskResult.status === 'fulfilled'
          ? contourTaskResult.value
          : undefined;
      if (approvalResult.status === 'fulfilled' && approvalResult.value) {
        approvalNodes.value = approvalResult.value.activityNodes || [];
        approvalProcessName.value =
          approvalResult.value.processInstance?.name || '';
        approvalProcessId.value =
          approvalResult.value.processInstance?.id ||
          (detail as any)?.processInstanceId ||
          '';
        approvalProcessKey.value =
          approvalResult.value.processDefinition?.key ||
          approvalResult.value.processInstance?.processDefinition?.key ||
          '';
      }
      return;
    }

    const design = detail as HospitalPlanDesignApi.PlanDesign;
    fillDesignForm(design);

    const [planApplyResult, approvalResult] = await Promise.allSettled([
      design.planApplyId
        ? getPlanApply(design.planApplyId)
        : Promise.resolve(undefined),
      (detail as any)?.processInstanceId
        ? getApprovalDetail({
            processInstanceId: (detail as any).processInstanceId,
          })
        : Promise.resolve(undefined),
    ]);
    selectedPlanApply.value =
      planApplyResult.status === 'fulfilled'
        ? planApplyResult.value
        : undefined;
    if (approvalResult.status === 'fulfilled' && approvalResult.value) {
      approvalNodes.value = approvalResult.value.activityNodes || [];
      approvalProcessName.value =
        approvalResult.value.processInstance?.name || '';
      approvalProcessId.value =
        approvalResult.value.processInstance?.id ||
        (detail as any)?.processInstanceId ||
        '';
      approvalProcessKey.value =
        approvalResult.value.processDefinition?.key ||
        approvalResult.value.processInstance?.processDefinition?.key ||
        '';
    }

    const patientId = design.patientId || selectedPlanApply.value?.patientId;
    const contourTaskId = selectedPlanApply.value?.contourTaskId;
    const [patientResult, contourTaskResult] = await Promise.allSettled([
      patientId ? getPatient(patientId) : Promise.resolve(undefined),
      contourTaskId
        ? getContourTask(contourTaskId)
        : Promise.resolve(undefined),
    ]);

    patientDetail.value =
      patientResult.status === 'fulfilled' ? patientResult.value : undefined;
    selectedContourTask.value =
      contourTaskResult.status === 'fulfilled'
        ? contourTaskResult.value
        : undefined;
  } finally {
    detailLoading.value = false;
  }
}

function buildSavePayload() {
  if (isApply.value) {
    if (
      !selectedRecord.value?.id ||
      !applyForm.contourTaskId ||
      !applyForm.applyDoctorId
    ) {
      return undefined;
    }
    return {
      applyDoctorId: applyForm.applyDoctorId,
      bizNo: applyForm.bizNo.trim(),
      clinicalDiagnosis: applyForm.clinicalDiagnosis.trim(),
      contourTaskId: applyForm.contourTaskId,
      id: applyForm.id,
      prescriptionDose: applyForm.prescriptionDose ?? 0,
      priority: applyForm.priority || 'NORMAL',
      remark: applyForm.remark.trim(),
      status: applyForm.status ?? 0,
      totalFractions: applyForm.totalFractions ?? 0,
      treatmentSite: applyForm.treatmentSite.trim(),
    };
  }

  if (
    !selectedRecord.value?.id ||
    !designForm.planApplyId ||
    !designForm.designUserId
  ) {
    return undefined;
  }
  return {
    bizNo: designForm.bizNo.trim(),
    designUserId: designForm.designUserId,
    fractions: designForm.fractions ?? 0,
    id: designForm.id,
    planApplyId: designForm.planApplyId,
    planName: designForm.planName.trim(),
    remark: designForm.remark.trim(),
    singleDose: designForm.singleDose ?? 0,
    status: designForm.status ?? 0,
    totalDose: designForm.totalDose ?? 0,
    versionNo: designForm.versionNo.trim() || 'V1',
  };
}

async function handleSave() {
  const payload = buildSavePayload();
  if (!payload) {
    message.warning(
      isApply.value
        ? '请先完善勾画任务和申请医生后再保存'
        : '请先完善计划申请和设计人员后再保存',
    );
    return;
  }
  const hideLoading = message.loading({
    content: `正在保存${props.config.title}【${getRecordBizNo(selectedRecord.value)}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await props.api.save(payload);
    message.success('保存成功');
    await loadRecordList(true);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

async function handleSubmit() {
  if (!selectedRecord.value?.id) return;
  const hideLoading = message.loading({
    content: `正在提交${props.config.title}【${getRecordBizNo(selectedRecord.value)}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await props.api.submit(selectedRecord.value.id);
    message.success('提交成功');
    await loadRecordList(true);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

async function handleDelete() {
  if (!selectedRecord.value?.id) return;
  const hideLoading = message.loading({
    content: `正在删除${props.config.title}【${getRecordBizNo(selectedRecord.value)}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await props.api.remove(selectedRecord.value.id);
    message.success('删除成功');
    await loadRecordList(false);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

function handleCreate() {
  props.onCreate?.();
}

function handleSearch() {
  pager.pageNo = 1;
  void loadRecordList(false);
}

function handleReset() {
  activeStatus.value = 'ALL';
  filters.actorId = undefined;
  filters.bizNo = '';
  filters.patientName = '';
  pager.pageNo = 1;
  void loadRecordList(false);
}

function handlePageChange(page: number) {
  pager.pageNo = page;
  void loadRecordList(false);
}

function handleStatusChange(status: EntryStatusKey) {
  activeStatus.value = status;
  pager.pageNo = 1;
  void loadRecordList(false);
}

function reload(preserveSelection = false) {
  if (!preserveSelection) {
    pager.pageNo = 1;
  }
  void loadRecordList(preserveSelection);
}

defineExpose({ reload });

onMounted(async () => {
  await Promise.all([loadOptions(), loadModelWorkflowSteps()]);
  await loadRecordList(false);
});
</script>

<template>
  <Page auto-content-height>
    <div class="contour-board">
      <div class="contour-toolbar">
        <div class="contour-toolbar__heading">
          <div class="contour-toolbar__title">{{ props.config.title }}</div>
          <div class="contour-toolbar__subtitle">
            {{ props.config.subtitle }}
          </div>
        </div>

        <div class="contour-toolbar__filters">
          <a-input
            v-model:value="filters.bizNo"
            allow-clear
            class="w-[160px]"
            :placeholder="props.config.bizLabel"
            size="small"
            @press-enter="handleSearch"
          />
          <a-input
            v-model:value="filters.patientName"
            allow-clear
            class="w-[140px]"
            placeholder="患者姓名"
            size="small"
            @press-enter="handleSearch"
          />
          <a-select
            v-model:value="filters.actorId"
            allow-clear
            class="w-[140px]"
            :placeholder="props.config.actorLabel"
            size="small"
          >
            <a-select-option
              v-for="item in doctorOptions"
              :key="item.id"
              :value="item.id"
            >
              {{ item.name }}
            </a-select-option>
          </a-select>
          <a-button size="small" type="primary" @click="handleSearch">
            查询
          </a-button>
          <a-button size="small" @click="handleReset">重置</a-button>
          <a-button v-if="canCreate" size="small" @click="handleCreate">
            {{ props.config.createText }}
          </a-button>
          <a-button size="small" @click="reload(true)">刷新</a-button>
        </div>
      </div>

      <div class="contour-status">
        <button
          v-for="item in statusTabs"
          :key="item.key"
          class="contour-status__item"
          :class="[
            { 'contour-status__item--active': activeStatus === item.key },
          ]"
          type="button"
          @click="handleStatusChange(item.key)"
        >
          <span>{{ item.label }}</span>
        </button>
      </div>

      <div class="contour-main">
        <section class="contour-panel contour-list-panel">
          <div class="contour-panel__header">
            <div class="contour-panel__title">{{ props.config.listTitle }}</div>
            <div class="contour-panel__extra">共 {{ pager.total }} 条</div>
          </div>

          <a-spin :spinning="listLoading">
            <div class="contour-table-wrap">
              <table v-if="recordList.length > 0" class="contour-table">
                <colgroup>
                  <col style="width: 31%" />
                  <col style="width: 18%" />
                  <col style="width: 16%" />
                  <col style="width: 14%" />
                  <col style="width: 21%" />
                </colgroup>
                <thead>
                  <tr>
                    <th>患者 / {{ props.config.bizLabel }}</th>
                    <th>{{ isApply ? '治疗部位' : '计划名称' }}</th>
                    <th>{{ props.config.actorLabel }}</th>
                    <th>当前阶段</th>
                    <th>提交时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in recordList"
                    :key="item.id"
                    :class="{ 'is-active': selectedRecordId === item.id }"
                    @click="loadRecordDetail(item.id!)"
                  >
                    <td class="is-patient">
                      <div class="contour-table__patient">
                        {{ displayText(item.patientName) }}
                      </div>
                      <div class="contour-table__sub">
                        {{ getRecordBizNo(item) }}
                      </div>
                    </td>
                    <td>{{ getRecordStageValue(item) }}</td>
                    <td>{{ getRecordActorName(item) }}</td>
                    <td>
                      <a-tag :color="getStatusColor(getRecordStatus(item))">
                        {{ getStatusLabel(getRecordStatus(item)) }}
                      </a-tag>
                    </td>
                    <td>
                      <div class="contour-table__date">
                        {{ formatDate(getRecordSubmitAt(item)) }}
                      </div>
                      <div class="contour-table__time">
                        {{ formatTime(getRecordSubmitAt(item)) }}
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <a-empty v-else :description="props.config.emptyText" />
            </div>
          </a-spin>

          <div class="contour-list-panel__footer">
            <a-pagination
              :current="pager.pageNo"
              :page-size="pager.pageSize"
              :show-size-changer="false"
              :total="pager.total"
              size="small"
              @change="handlePageChange"
            />
          </div>
        </section>

        <section class="contour-panel contour-detail-panel">
          <template v-if="selectedRecord">
            <div class="contour-patient-bar">
              <div class="contour-patient-bar__avatar">
                {{ getPatientAvatarText() }}
              </div>
              <div class="contour-patient-bar__main">
                <div class="contour-patient-bar__name">
                  {{
                    displayText(
                      selectedRecord?.patientName ||
                        selectedPlanApply?.patientName ||
                        patientDetail?.name,
                    )
                  }}
                  <span>
                    {{ getGenderLabel(patientDetail?.gender) }} /
                    {{ patientDetail?.age ? `${patientDetail.age}` : '--' }}
                  </span>
                </div>
                <div class="contour-patient-bar__meta">
                  <span>
                    病案号：{{
                      displayText(
                        patientDetail?.medicalRecordNo ||
                          patientDetail?.patientNo,
                      )
                    }}
                  </span>
                  <span
                    >放疗号：{{
                      displayText(patientDetail?.radiotherapyNo)
                    }}</span
                  >
                  <span
                    >{{ props.config.bizLabel }}：{{
                      getRecordBizNo(selectedRecord)
                    }}</span
                  >
                  <span v-if="isApply">
                    勾画任务：{{ displayText(selectedContourTask?.bizNo) }}
                  </span>
                  <span v-else>
                    申请单号：{{ displayText(selectedPlanApply?.bizNo) }}
                  </span>
                  <span>
                    {{ props.config.actorLabel }}：{{
                      getRecordActorName(selectedRecord)
                    }}
                  </span>
                </div>
              </div>
              <a-tag :color="getStatusColor(getRecordStatus(selectedRecord))">
                {{ getStatusLabel(getRecordStatus(selectedRecord)) }}
              </a-tag>
            </div>
          </template>

          <a-spin :spinning="detailLoading || actionLoading">
            <template v-if="selectedRecord">
              <div class="contour-workspace">
                <aside class="contour-timeline">
                  <div class="contour-timeline__title">流程进度</div>
                  <div
                    v-if="approvalProcessName || approvalProcessId"
                    class="contour-timeline__summary"
                  >
                    <span v-if="approvalProcessName">
                      流程：{{ approvalProcessName }}
                    </span>
                    <span v-if="approvalProcessId">
                      实例：{{ approvalProcessId }}
                    </span>
                  </div>
                  <div
                    v-for="(item, index) in timelineSteps"
                    :key="item.key"
                    class="contour-timeline__item"
                    :class="[`is-${item.tone}`]"
                  >
                    <div
                      v-if="index !== timelineSteps.length - 1"
                      class="contour-timeline__line"
                    ></div>
                    <div class="contour-timeline__dot"></div>
                    <div class="contour-timeline__text">
                      <div class="contour-timeline__head">
                        <div class="contour-timeline__name">
                          {{ item.title }}
                        </div>
                        <span
                          v-if="item.statusText"
                          class="contour-timeline__badge"
                        >
                          {{ item.statusText }}
                        </span>
                      </div>
                      <div class="contour-timeline__desc">{{ item.desc }}</div>
                      <div v-if="item.actorText" class="contour-timeline__meta">
                        {{ item.actorText }}
                      </div>
                      <div v-if="item.timeText" class="contour-timeline__meta">
                        {{ item.timeText }}
                      </div>
                    </div>
                  </div>
                </aside>

                <div class="contour-sheet">
                  <div class="contour-sheet__paper">
                    <div class="contour-sheet__header">
                      <div class="contour-sheet__brand">
                        <div class="contour-sheet__brand-logo">
                          <span>沪</span>
                        </div>
                        <div class="contour-sheet__brand-text">
                          <div class="contour-sheet__brand-name">
                            上海XX医院
                          </div>
                          <div class="contour-sheet__brand-subtitle">
                            RADIOTHERAPY CENTER
                          </div>
                        </div>
                      </div>
                      <div class="contour-sheet__title-box">
                        <div class="contour-sheet__title">
                          {{ props.config.title }}
                        </div>
                        <div class="contour-sheet__title-caption">
                          放疗业务单据
                        </div>
                        <div class="contour-sheet__title-meta">
                          <span>
                            {{ props.config.bizLabel }}：{{
                              getRecordBizNo(selectedRecord)
                            }}
                          </span>
                          <span>
                            提交：{{
                              formatDateTime(getRecordSubmitAt(selectedRecord))
                            }}
                          </span>
                        </div>
                      </div>
                      <div class="contour-sheet__header-spacer"></div>
                    </div>

                    <div class="contour-sheet__divider"></div>

                    <div class="contour-section">
                      <div class="contour-section__title">基本信息</div>
                      <div
                        class="contour-sheet__grid contour-sheet__grid--3 contour-sheet__grid--slab"
                      >
                        <div class="contour-field">
                          <label>姓名</label>
                          <span>
                            {{
                              displayText(
                                selectedRecord?.patientName ||
                                  selectedPlanApply?.patientName ||
                                  patientDetail?.name,
                              )
                            }}
                          </span>
                        </div>
                        <div class="contour-field">
                          <label>病案号</label>
                          <span>
                            {{
                              displayText(
                                patientDetail?.medicalRecordNo ||
                                  patientDetail?.patientNo,
                              )
                            }}
                          </span>
                        </div>
                        <div class="contour-field">
                          <label>放疗号</label>
                          <span>{{
                            displayText(patientDetail?.radiotherapyNo)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>住院号</label>
                          <span>{{
                            displayText(patientDetail?.hospitalizationNo)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>{{ isApply ? '申请医生' : '设计人员' }}</label>
                          <span>{{ getRecordActorName(selectedRecord) }}</span>
                        </div>
                        <div class="contour-field">
                          <label>{{ isApply ? '治疗部位' : '计划名称' }}</label>
                          <span>{{ getRecordStageValue(selectedRecord) }}</span>
                        </div>
                      </div>
                    </div>

                    <div v-if="!isApply" class="contour-section">
                      <div class="contour-section__title">计划申请信息</div>
                      <div
                        class="contour-sheet__grid contour-sheet__grid--3 contour-sheet__grid--slab"
                      >
                        <div class="contour-field">
                          <label>申请单号</label>
                          <span>{{
                            displayText(selectedPlanApply?.bizNo)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>申请医生</label>
                          <span>{{
                            displayText(selectedPlanApply?.applyDoctorName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>治疗部位</label>
                          <span>{{
                            displayText(selectedPlanApply?.treatmentSite)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>临床诊断</label>
                          <span>{{
                            displayText(selectedPlanApply?.clinicalDiagnosis)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>处方剂量</label>
                          <span>{{
                            displayDose(selectedPlanApply?.prescriptionDose)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>处方分次</label>
                          <span>{{
                            displayFractions(selectedPlanApply?.totalFractions)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>紧急程度</label>
                          <span>{{
                            getPriorityLabel(selectedPlanApply?.priority)
                          }}</span>
                        </div>
                        <div class="contour-field contour-field--full">
                          <label>申请备注</label>
                          <span>{{
                            displayText(selectedPlanApply?.remark)
                          }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="contour-section">
                      <div class="contour-section__title">靶区来源</div>
                      <div
                        class="contour-remote"
                        :class="[
                          { 'is-empty': !selectedContourTask?.attachmentUrl },
                        ]"
                      >
                        <template v-if="selectedContourTask?.attachmentUrl">
                          <div class="contour-remote__text">
                            {{ selectedContourTask.attachmentUrl }}
                          </div>
                          <a
                            :href="selectedContourTask.attachmentUrl"
                            target="_blank"
                          >
                            打开附件
                          </a>
                        </template>
                        <span v-else>暂无勾画附件</span>
                      </div>
                      <div
                        class="contour-sheet__grid contour-sheet__grid--2 contour-sheet__grid--slab mt-2"
                      >
                        <div class="contour-field">
                          <label>勾画任务</label>
                          <span>{{
                            displayText(selectedContourTask?.bizNo)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>勾画医生</label>
                          <span>{{
                            displayText(selectedContourTask?.contourDoctorName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>治疗部位</label>
                          <span>{{
                            displayText(selectedContourTask?.treatmentSite)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>版本号</label>
                          <span>{{
                            displayText(selectedContourTask?.versionNo)
                          }}</span>
                        </div>
                        <div class="contour-field contour-field--full">
                          <label>任务备注</label>
                          <span>{{
                            displayText(selectedContourTask?.remark)
                          }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="contour-section">
                                            <NodeCustomForm
                        :form-values="nodeFormValues"
                        :page-code="props.config.pageCode"
                        :process-key="currentProcessKey"
                      :node-key="currentNodeKey"
                        :title="`${props.config.title}表单`"
                      />
                    </div>

                    <div class="contour-section">
                      <div class="contour-section__title">
                        {{ isApply ? '申请信息' : '设计信息' }}
                      </div>
                      <a-alert
                        v-if="readonlyMessage"
                        :message="readonlyMessage"
                        show-icon
                        type="info"
                      />
                      <div
                        class="contour-sheet__grid contour-sheet__grid--2 contour-sheet__grid--form contour-editor-grid"
                      >
                        <template v-if="isApply">
                          <div class="contour-editor-item">
                            <label>勾画任务</label>
                            <a-select
                              v-model:value="applyForm.contourTaskId"
                              :disabled="!canUpdate || !canEditCurrent"
                              allow-clear
                              placeholder="请选择勾画任务"
                              size="small"
                            >
                              <a-select-option
                                v-for="item in contourTaskOptions"
                                :key="item.id"
                                :value="item.id"
                              >
                                {{ item.displayName || item.bizNo }}
                              </a-select-option>
                            </a-select>
                          </div>
                          <div class="contour-editor-item">
                            <label>申请医生</label>
                            <a-select
                              v-model:value="applyForm.applyDoctorId"
                              :disabled="!canUpdate || !canEditCurrent"
                              allow-clear
                              placeholder="请选择申请医生"
                              size="small"
                            >
                              <a-select-option
                                v-for="item in doctorOptions"
                                :key="item.id"
                                :value="item.id"
                              >
                                {{ item.name }}
                              </a-select-option>
                            </a-select>
                          </div>
                          <div class="contour-editor-item">
                            <label>申请单号</label>
                            <a-input
                              v-model:value="applyForm.bizNo"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="留空自动生成"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>紧急程度</label>
                            <a-select
                              v-model:value="applyForm.priority"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="请选择紧急程度"
                              size="small"
                            >
                              <a-select-option
                                v-for="item in priorityOptions"
                                :key="item.value"
                                :value="item.value"
                              >
                                {{ item.label }}
                              </a-select-option>
                            </a-select>
                          </div>
                          <div class="contour-editor-item">
                            <label>治疗部位</label>
                            <a-input
                              v-model:value="applyForm.treatmentSite"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="请输入治疗部位"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>处方总剂量</label>
                            <a-input-number
                              v-model:value="applyForm.prescriptionDose"
                              :controls="false"
                              :disabled="!canUpdate || !canEditCurrent"
                              :min="0"
                              :precision="2"
                              placeholder="请输入处方总剂量"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>总分次</label>
                            <a-input-number
                              v-model:value="applyForm.totalFractions"
                              :controls="false"
                              :disabled="!canUpdate || !canEditCurrent"
                              :min="0"
                              :precision="0"
                              placeholder="请输入总分次"
                              size="small"
                            />
                          </div>
                          <div
                            class="contour-editor-item contour-editor-item--full"
                          >
                            <label>临床诊断</label>
                            <a-textarea
                              v-model:value="applyForm.clinicalDiagnosis"
                              :auto-size="{ minRows: 2, maxRows: 4 }"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="请输入临床诊断"
                              size="small"
                            />
                          </div>
                          <div
                            class="contour-editor-item contour-editor-item--full"
                          >
                            <label>备注</label>
                            <a-textarea
                              v-model:value="applyForm.remark"
                              :auto-size="{ minRows: 2, maxRows: 4 }"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="请输入备注"
                              size="small"
                            />
                          </div>
                        </template>

                        <template v-else>
                          <div class="contour-editor-item">
                            <label>计划申请</label>
                            <a-select
                              v-model:value="designForm.planApplyId"
                              :disabled="!canUpdate || !canEditCurrent"
                              allow-clear
                              placeholder="请选择计划申请"
                              size="small"
                            >
                              <a-select-option
                                v-for="item in planApplyOptions"
                                :key="item.id"
                                :value="item.id"
                              >
                                {{ item.displayName || item.bizNo }}
                              </a-select-option>
                            </a-select>
                          </div>
                          <div class="contour-editor-item">
                            <label>设计人员</label>
                            <a-select
                              v-model:value="designForm.designUserId"
                              :disabled="!canUpdate || !canEditCurrent"
                              allow-clear
                              placeholder="请选择设计人员"
                              size="small"
                            >
                              <a-select-option
                                v-for="item in doctorOptions"
                                :key="item.id"
                                :value="item.id"
                              >
                                {{ item.name }}
                              </a-select-option>
                            </a-select>
                          </div>
                          <div class="contour-editor-item">
                            <label>设计单号</label>
                            <a-input
                              v-model:value="designForm.bizNo"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="留空自动生成"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>版本号</label>
                            <a-input
                              v-model:value="designForm.versionNo"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="默认 V1"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>计划名称</label>
                            <a-input
                              v-model:value="designForm.planName"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="请输入计划名称"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>总剂量</label>
                            <a-input-number
                              v-model:value="designForm.totalDose"
                              :controls="false"
                              :disabled="!canUpdate || !canEditCurrent"
                              :min="0"
                              :precision="2"
                              placeholder="请输入总剂量"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>单次剂量</label>
                            <a-input-number
                              v-model:value="designForm.singleDose"
                              :controls="false"
                              :disabled="!canUpdate || !canEditCurrent"
                              :min="0"
                              :precision="2"
                              placeholder="请输入单次剂量"
                              size="small"
                            />
                          </div>
                          <div class="contour-editor-item">
                            <label>分次数</label>
                            <a-input-number
                              v-model:value="designForm.fractions"
                              :controls="false"
                              :disabled="!canUpdate || !canEditCurrent"
                              :min="0"
                              :precision="0"
                              placeholder="请输入分次数"
                              size="small"
                            />
                          </div>
                          <div
                            class="contour-editor-item contour-editor-item--full"
                          >
                            <label>备注</label>
                            <a-textarea
                              v-model:value="designForm.remark"
                              :auto-size="{ minRows: 2, maxRows: 4 }"
                              :disabled="!canUpdate || !canEditCurrent"
                              placeholder="请输入备注"
                              size="small"
                            />
                          </div>
                        </template>
                      </div>
                    </div>

                    <div class="contour-actions">
                      <a-button
                        v-if="canUpdate"
                        :disabled="!canEditCurrent"
                        size="small"
                        type="primary"
                        @click="handleSave"
                      >
                        保存
                      </a-button>
                      <a-button
                        v-if="canSubmit"
                        :disabled="!canEditCurrent"
                        size="small"
                        @click="handleSubmit"
                      >
                        提交
                      </a-button>
                      <a-popconfirm
                        v-if="canDelete"
                        :title="`确认删除${props.config.title}【${getRecordBizNo(selectedRecord)}】吗？`"
                        @confirm="handleDelete"
                      >
                        <a-button
                          :disabled="!canEditCurrent"
                          danger
                          size="small"
                        >
                          删除
                        </a-button>
                      </a-popconfirm>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <a-empty v-else description="请选择左侧单据" />
          </a-spin>
        </section>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.contour-board {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contour-toolbar,
.contour-status,
.contour-panel {
  background: #fff;
  border: 1px solid #e6ebf2;
  border-radius: 4px;
}

.contour-toolbar {
  align-items: center;
  display: flex;
  gap: 10px;
  justify-content: space-between;
  padding: 10px 12px;
}

.contour-toolbar__title {
  color: #1f2937;
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
}

.contour-toolbar__subtitle,
.contour-panel__extra,
.contour-patient-bar__meta,
.contour-table__sub,
.contour-timeline__desc,
.contour-field label,
.contour-editor-item label {
  color: #8a94a6;
  font-size: 12px;
}

.contour-table__sub,
.contour-field span,
.contour-patient-bar__meta span,
.contour-remote__text {
  overflow-wrap: anywhere;
  word-break: break-word;
}

.contour-toolbar__filters {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.contour-status {
  display: grid;
  gap: 6px;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  padding: 6px;
}

.contour-status__item {
  background: #f7f9fc;
  border: 1px solid #eef2f7;
  border-radius: 4px;
  color: #5b6475;
  cursor: pointer;
  font-size: 12px;
  padding: 7px 8px;
}

.contour-status__item--active {
  background: #eaf3ff;
  border-color: #8ec2ff;
  color: #1677ff;
}

.contour-main {
  display: grid;
  gap: 8px;
  grid-template-columns: minmax(360px, 44%) minmax(0, 1fr);
}

.contour-panel {
  display: flex;
  flex-direction: column;
  min-height: 720px;
  overflow: hidden;
}

.contour-panel__header {
  align-items: center;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
}

.contour-panel__title,
.contour-patient-bar__name,
.contour-sheet__brand-name,
.contour-timeline__name {
  color: #1f2937;
  font-weight: 600;
}

.contour-table-wrap {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.contour-table {
  border-collapse: separate;
  border-spacing: 0;
  table-layout: fixed;
  width: 100%;
}

.contour-table th,
.contour-table td {
  border-bottom: 1px solid #eef2f7;
  font-size: 12px;
  line-height: 1.5;
  padding: 9px 10px;
  text-align: left;
  vertical-align: middle;
}

.contour-table th {
  background: #fafcff;
  color: #6b7280;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 1;
}

.contour-table tbody tr {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.contour-table tbody tr:hover,
.contour-table tbody tr.is-active {
  background: #f4f9ff;
}

.contour-table tbody tr.is-active td:first-child {
  box-shadow: inset 2px 0 0 #1677ff;
}

.contour-table__patient {
  color: #111827;
  font-size: 13px;
  font-weight: 600;
}

.contour-table__date,
.contour-table__time {
  white-space: nowrap;
}

.contour-list-panel__footer {
  border-top: 1px solid #eef2f7;
  display: flex;
  justify-content: flex-end;
  padding: 8px 12px;
}

.contour-detail-panel {
  background: #f6f8fb;
  gap: 0;
}

.contour-patient-bar {
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #eef2f7;
  display: grid;
  gap: 10px;
  grid-template-columns: auto 1fr auto;
  padding: 12px 14px;
}

.contour-patient-bar__avatar {
  align-items: center;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border-radius: 8px;
  color: #fff;
  display: inline-flex;
  font-size: 20px;
  font-weight: 700;
  height: 42px;
  justify-content: center;
  width: 42px;
}

.contour-patient-bar__name {
  align-items: baseline;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 16px;
}

.contour-patient-bar__name span {
  color: #8a94a6;
  font-size: 12px;
  font-weight: 400;
}

.contour-patient-bar__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  margin-top: 4px;
}

.contour-workspace {
  align-items: start;
  background: #f6f8fb;
  display: grid;
  flex: 1;
  gap: 12px;
  grid-template-columns: 188px minmax(0, 1fr);
  min-height: 0;
  padding: 10px 12px 12px;
}

.contour-timeline,
.contour-sheet {
  min-height: 0;
  min-width: 0;
}

.contour-timeline {
  align-self: start;
  background: linear-gradient(180deg, #fbfdff 0%, #f4f7fb 100%);
  border: 1px solid #e6edf5;
  border-radius: 4px;
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 85%);
  height: fit-content;
  max-height: calc(100vh - 220px);
  overflow: auto;
  padding: 10px 10px 8px 12px;
  position: sticky;
  top: 10px;
}

.contour-timeline__title {
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 6px;
}

.contour-timeline__summary {
  background: rgb(255 255 255 / 78%);
  border: 1px solid #edf1f5;
  border-radius: 4px;
  color: #64748b;
  display: flex;
  flex-direction: column;
  font-size: 10px;
  gap: 2px;
  line-height: 1.35;
  margin-bottom: 8px;
  overflow-wrap: anywhere;
  padding: 6px 8px;
  word-break: break-word;
}

.contour-timeline__item {
  padding: 0 0 10px 18px;
  position: relative;
}

.contour-timeline__item:last-child {
  padding-bottom: 0;
}

.contour-timeline__line {
  background: #d9e0ea;
  height: calc(100% - 9px);
  left: 5px;
  position: absolute;
  top: 11px;
  width: 1px;
}

.contour-timeline__dot {
  background: #d7dde8;
  border: 1px solid #fff;
  border-radius: 999px;
  box-shadow: 0 0 0 2px #fff;
  height: 10px;
  left: 1px;
  position: absolute;
  top: 3px;
  width: 10px;
}

.contour-timeline__item.is-done .contour-timeline__dot {
  background: #5ad8a6;
}

.contour-timeline__item.is-done .contour-timeline__line {
  background: #b9e7d1;
}

.contour-timeline__item.is-done .contour-timeline__line {
  background: #b9e7d1;
}

.contour-timeline__item.is-done .contour-timeline__badge {
  background: #ecfdf3;
  color: #15924f;
}

.contour-timeline__item.is-current .contour-timeline__dot {
  background: #1677ff;
  box-shadow: none;
}

.contour-timeline__item.is-current .contour-timeline__badge {
  background: #e8f3ff;
  color: #1677ff;
}

.contour-timeline__head {
  align-items: flex-start;
  display: flex;
  gap: 4px;
  justify-content: space-between;
}

.contour-timeline__text {
  min-width: 0;
}

.contour-timeline__name {
  color: #1f2937;
  flex: 1;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.3;
  min-width: 0;
  word-break: break-word;
}

.contour-timeline__badge {
  background: #f3f4f6;
  border-radius: 999px;
  color: #6b7280;
  flex: none;
  font-size: 9px;
  line-height: 15px;
  padding: 0 5px;
}

.contour-timeline__desc {
  color: #64748b;
  font-size: 10px;
  line-height: 1.35;
  margin-top: 1px;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.contour-timeline__meta {
  color: #8a94a6;
  font-size: 9px;
  line-height: 1.35;
  margin-top: 1px;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.contour-sheet {
  padding: 12px;
}

.contour-sheet__paper {
  background: #fff;
  border: 1px solid #edf1f5;
  border-radius: 4px;
  min-height: 100%;
  padding: 16px 18px 14px;
}

.contour-sheet__header {
  align-items: center;
  display: grid;
  gap: 8px;
  grid-template-columns: auto 1fr auto;
}

.contour-sheet__brand {
  align-items: center;
  display: flex;
  gap: 10px;
}

.contour-sheet__brand-logo {
  align-items: center;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-radius: 6px;
  color: #fff;
  display: inline-flex;
  font-size: 18px;
  font-weight: 700;
  height: 42px;
  justify-content: center;
  width: 42px;
}

.contour-sheet__brand-subtitle {
  color: #8a94a6;
  font-size: 10px;
  letter-spacing: 1px;
  margin-top: 2px;
}

.contour-sheet__title-box {
  text-align: center;
}

.contour-sheet__title {
  font-size: 21px;
  letter-spacing: 2px;
  text-align: center;
}

.contour-sheet__title-caption {
  color: #8a94a6;
  font-size: 10px;
  letter-spacing: 1px;
  margin-top: 4px;
}

.contour-sheet__title-meta {
  color: #8a94a6;
  display: flex;
  flex-wrap: wrap;
  font-size: 9px;
  gap: 2px 10px;
  justify-content: center;
  line-height: 1.4;
  margin-top: 6px;
}

.contour-sheet__title-meta span,
.contour-patient-bar__meta span {
  max-width: 100%;
  min-width: 0;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.contour-sheet__divider {
  border-top: 1px solid #edf1f5;
  margin-bottom: 14px;
}

.contour-section {
  margin-bottom: 14px;
}

.contour-section__title {
  border-left: 3px solid #4ea4ff;
  font-size: 12px;
  margin-bottom: 8px;
  padding-left: 6px;
}

.contour-sheet__grid {
  display: grid;
  gap: 8px 12px;
}

.contour-sheet__grid--2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.contour-sheet__grid--3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.contour-sheet__grid--slab,
.contour-sheet__grid--form {
  background: #fafcfe;
  border: 1px solid #edf1f5;
  border-radius: 2px;
  padding: 12px 14px;
}

.contour-field,
.contour-remote {
  background: transparent;
  border: 0;
  border-radius: 0;
  min-width: 0;
  padding: 0;
}

.contour-field {
  align-items: flex-start;
  display: flex;
  gap: 4px;
}

.contour-editor-item {
  align-items: start;
  background: transparent;
  border: 0;
  border-radius: 0;
  display: grid;
  gap: 8px;
  grid-template-columns: 70px minmax(0, 1fr);
  min-width: 0;
  padding: 0;
}

.contour-field span,
.contour-remote,
.contour-editor-grid {
  color: #1f2937;
  font-size: 12px;
}

.contour-field--full,
.contour-editor-item--full {
  grid-column: 1 / -1;
}

.contour-field label,
.contour-editor-item label {
  flex-shrink: 0;
  line-height: 22px;
  min-width: 60px;
}

.contour-field label::after,
.contour-editor-item label::after {
  content: '：';
}

.contour-field span {
  flex: 1;
  min-width: 0;
}

.contour-remote {
  align-items: center;
  background: #fafcfe;
  border: 1px solid #edf1f5;
  color: #8a94a6;
  display: flex;
  justify-content: space-between;
  min-height: 96px;
  padding: 12px;
}

.contour-remote.is-empty {
  justify-content: center;
}

.contour-remote__text {
  color: #1f2937;
  margin-right: 12px;
}

.contour-actions {
  display: flex;
  gap: 6px;
  justify-content: center;
  margin-top: 8px;
}

.contour-board :deep(.ant-tag) {
  font-size: 10px;
  line-height: 15px;
  margin-inline-end: 0;
  padding: 0 4px;
}

.contour-editor-item :deep(.ant-input),
.contour-editor-item :deep(.ant-input-affix-wrapper),
.contour-editor-item :deep(.ant-input-number),
.contour-editor-item :deep(.ant-input-textarea),
.contour-editor-item :deep(.ant-select) {
  width: 100%;
}

.contour-board :deep(.ant-alert) {
  margin-bottom: 4px;
  padding: 2px 6px;
}

.contour-board :deep(.ant-btn-sm) {
  font-size: 11px;
  padding-inline: 8px;
}

.contour-board :deep(.ant-input-sm),
.contour-board :deep(.ant-input-number-sm),
.contour-board :deep(.ant-select-single.ant-select-sm),
.contour-board :deep(.ant-select-single.ant-select-sm .ant-select-selector),
.contour-board :deep(.ant-input-affix-wrapper-sm),
.contour-board :deep(.ant-input-textarea textarea) {
  font-size: 11px;
}

.contour-board :deep(.ant-select-single.ant-select-sm .ant-select-selector),
.contour-board :deep(.ant-input-number-sm) {
  min-height: 26px;
}

@media (width <= 1200px) {
  .contour-main {
    grid-template-columns: 1fr;
  }

  .contour-panel {
    min-height: auto;
  }
}

@media (width <= 960px) {
  .contour-workspace {
    grid-template-columns: 1fr;
  }

  .contour-timeline {
    max-height: none;
    position: static;
  }

  .contour-sheet {
    border-left: 0;
    padding: 0;
  }

  .contour-sheet__paper {
    padding: 14px 14px 12px;
  }

  .contour-sheet__header {
    grid-template-columns: 1fr;
  }

  .contour-status,
  .contour-sheet__grid--2,
  .contour-sheet__grid--3,
  .contour-sheet__grid--form {
    grid-template-columns: 1fr;
  }

  .contour-editor-item {
    gap: 4px;
    grid-template-columns: 1fr;
  }
}
</style>
