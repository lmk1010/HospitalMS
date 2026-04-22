<script lang="ts" setup>
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';
import type { HospitalContourReviewApi } from '#/api/hospital/contour-review';
import type { HospitalContourTaskApi } from '#/api/hospital/contour-task';
import type { HospitalCtAppointmentApi } from '#/api/hospital/ct-appointment';
import type { HospitalDoctorApi } from '#/api/hospital/doctor';
import type { HospitalPatientApi } from '#/api/hospital/patient';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

import { getApprovalDetail } from '#/api/bpm/processInstance';
import { getContourReviewPage } from '#/api/hospital/contour-review';
import {
  deleteContourTask,
  getContourTask,
  getContourTaskPage,
  submitContourTask,
  updateContourTask,
} from '#/api/hospital/contour-task';
import { getCtAppointment } from '#/api/hospital/ct-appointment';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getPatient } from '#/api/hospital/patient';
import {
  buildWorkflowStepsFromApprovalNodes,
  getWorkflowRoutePath,
  getWorkflowStepsFromModelPath,
} from '#/views/hospital/_shared/workflow';

import Form from './modules/form.vue';

type TaskStatusKey = 'ALL' | 'REJECTED' | 'WAIT_CONTOUR';

type StepTone = 'current' | 'done' | 'pending';

interface EditorFormState {
  attachmentUrl: string;
  bizNo: string;
  contourDoctorId?: number;
  ctAppointmentId?: number;
  id?: number;
  patientId?: number;
  remark: string;
  status?: number;
  treatmentSite: string;
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

const contourStatusLabelMap: Record<string, string> = {
  APPROVED: '已通过',
  REJECTED: '已退回',
  REVIEWING: '审核中',
  WAIT_CONTOUR: '待勾画',
};

const CONTOUR_WORKFLOW_KEY = 'contour-task' as const;
const contourCurrentNodeStatuses = ['WAIT_CONTOUR', 'REJECTED'] as const;
const contourTimelineKeywordList = [
  'CT预约',
  'CT定位',
  '定位执行',
  '靶区勾画',
  '勾画处理',
  '勾画审核',
  '审核确认',
  '计划申请',
] as const;

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

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

const listLoading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const selectedTaskId = ref<number>();
const taskList = ref<HospitalContourTaskApi.ContourTask[]>([]);
const doctorOptions = ref<HospitalDoctorApi.Doctor[]>([]);
const selectedTask = ref<HospitalContourTaskApi.ContourTask>();
const patientDetail = ref<HospitalPatientApi.Patient>();
const appointmentDetail = ref<HospitalCtAppointmentApi.CtAppointment>();
const latestReview = ref<HospitalContourReviewApi.ContourReview>();
const approvalNodes = ref<BpmProcessInstanceApi.ApprovalNodeInfo[]>([]);
const modelWorkflowSteps = ref<WorkflowStep[]>([]);
const approvalProcessName = ref('');
const approvalProcessId = ref('');
const activeStatus = ref<TaskStatusKey>('ALL');

const pager = reactive({
  pageNo: 1,
  pageSize: 12,
  total: 0,
});

const filters = reactive({
  bizNo: '',
  contourDoctorId: undefined as number | undefined,
  patientName: '',
});

const editorForm = reactive<EditorFormState>({
  attachmentUrl: '',
  bizNo: '',
  contourDoctorId: undefined,
  ctAppointmentId: undefined,
  id: undefined,
  patientId: undefined,
  remark: '',
  status: 0,
  treatmentSite: '',
  versionNo: 'V1',
});

const canCreate = computed(() =>
  hasAccessByCodes(['hospital:contour-task:create']),
);
const canDelete = computed(() =>
  hasAccessByCodes(['hospital:contour-task:delete']),
);
const canSubmit = computed(() =>
  hasAccessByCodes(['hospital:contour-task:submit']),
);
const canUpdate = computed(() =>
  hasAccessByCodes(['hospital:contour-task:update']),
);

const canEditCurrent = computed(() => {
  return ['REJECTED', 'WAIT_CONTOUR'].includes(
    selectedTask.value?.workflowStatus || '',
  );
});

const readonlyMessage = computed(() => {
  const status = selectedTask.value?.workflowStatus;
  if (status === 'REVIEWING') return '当前任务审核中，右侧信息只读。';
  if (status === 'APPROVED') return '当前任务已审核通过，可流转到计划设计。';
  return '';
});

const statusTabs = computed(() => {
  const buildItem = (key: TaskStatusKey, label: string) => ({ key, label });
  return [
    buildItem('ALL', '当前节点全部'),
    buildItem('WAIT_CONTOUR', '待勾画'),
    buildItem('REJECTED', '退回待处理'),
  ];
});

const taskWorkflowStatusList = computed(() => {
  return activeStatus.value === 'ALL'
    ? [...contourCurrentNodeStatuses]
    : [activeStatus.value];
});

function displayText(value?: null | number | string) {
  return value === null || value === undefined || `${value}`.trim() === ''
    ? '--'
    : `${value}`;
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

function getStatusColor(status?: string) {
  if (status === 'APPROVED') return 'success';
  if (status === 'REJECTED') return 'error';
  if (status === 'REVIEWING') return 'processing';
  if (status === 'WAIT_CONTOUR') return 'warning';
  return 'default';
}

function getStatusLabel(status?: string) {
  return contourStatusLabelMap[status || ''] || displayText(status);
}

function getReviewResultLabel(result?: string) {
  if (result === 'APPROVED') return '审核通过';
  if (result === 'REJECTED') return '审核退回';
  if (result === 'REVIEWING') return '审核中';
  return '--';
}

function getWorkflowCurrentTitle(status?: string) {
  if (status === 'APPROVED') return '计划申请';
  if (status === 'REVIEWING') return '勾画审核';
  return '靶区勾画';
}

function getWorkflowTone(
  index: number,
  steps: WorkflowStep[],
  baseTone: StepTone = 'pending',
) {
  const status = selectedTask.value?.workflowStatus;
  if (!status) {
    return baseTone;
  }
  const currentTitle = getWorkflowCurrentTitle(status);
  const currentIndex = steps.findIndex((item) => {
    const title = item.title?.trim();
    return !!title &&
      (title.includes(currentTitle) || currentTitle.includes(title));
  });
  if (currentIndex < 0) {
    return baseTone;
  }
  if (index < currentIndex) return 'done';
  if (index === currentIndex) return 'current';
  return 'pending';
}

function isContourTimelineStep(title?: string) {
  const normalizedTitle = title?.trim();
  return (
    !!normalizedTitle &&
    contourTimelineKeywordList.some(
      (keyword) =>
        normalizedTitle.includes(keyword) || keyword.includes(normalizedTitle),
    )
  );
}

const workflowSteps = computed<WorkflowStep[]>(() => {
  if (approvalNodes.value.length > 0) {
    return buildWorkflowStepsFromApprovalNodes(
      CONTOUR_WORKFLOW_KEY,
      approvalNodes.value,
      modelWorkflowSteps.value,
    ) as WorkflowStep[];
  }
  const baseSteps =
    modelWorkflowSteps.value.length > 0
      ? modelWorkflowSteps.value
      : (buildWorkflowStepsFromApprovalNodes(
          CONTOUR_WORKFLOW_KEY,
          undefined,
          modelWorkflowSteps.value,
        ) as WorkflowStep[]);
  return baseSteps.map((step, index, steps) => ({
    ...step,
    tone: getWorkflowTone(index, steps, step.tone),
  }));
});

const timelineSteps = computed<WorkflowStep[]>(() => {
  const source = workflowSteps.value.map((item) => ({
    ...item,
    reasonText: undefined,
  }));
  const filtered = source.filter((item) => isContourTimelineStep(item.title));
  if (filtered.length >= 4) {
    return filtered;
  }
  return source.slice(0, Math.min(source.length, 6));
});

async function loadModelWorkflowSteps() {
  const workflowRoutePath = getWorkflowRoutePath(CONTOUR_WORKFLOW_KEY);
  if (!workflowRoutePath) {
    modelWorkflowSteps.value = [];
    return;
  }
  try {
    modelWorkflowSteps.value = (await getWorkflowStepsFromModelPath(
      CONTOUR_WORKFLOW_KEY,
      workflowRoutePath,
    )) as WorkflowStep[];
  } catch {
    modelWorkflowSteps.value = [];
  }
}

function clearDetail() {
  selectedTaskId.value = undefined;
  selectedTask.value = undefined;
  patientDetail.value = undefined;
  appointmentDetail.value = undefined;
  latestReview.value = undefined;
  approvalNodes.value = [];
  approvalProcessName.value = '';
  approvalProcessId.value = '';
  Object.assign(editorForm, {
    attachmentUrl: '',
    bizNo: '',
    contourDoctorId: undefined,
    ctAppointmentId: undefined,
    id: undefined,
    patientId: undefined,
    remark: '',
    status: 0,
    treatmentSite: '',
    versionNo: 'V1',
  });
}

function fillEditorForm(task?: HospitalContourTaskApi.ContourTask) {
  Object.assign(editorForm, {
    attachmentUrl: task?.attachmentUrl || '',
    bizNo: task?.bizNo || '',
    contourDoctorId: task?.contourDoctorId,
    ctAppointmentId: task?.ctAppointmentId,
    id: task?.id,
    patientId: task?.patientId,
    remark: task?.remark || '',
    status: task?.status ?? 0,
    treatmentSite: task?.treatmentSite || '',
    versionNo: task?.versionNo || 'V1',
  });
}

async function loadDoctorOptions() {
  try {
    doctorOptions.value = (await getSimpleDoctorList()) || [];
  } catch {
    doctorOptions.value = [];
  }
}

async function loadTaskList(preserveSelection = true) {
  listLoading.value = true;
  try {
    const focusId = getFocusId();
    const result = await getContourTaskPage({
      id: focusId,
      bizNo: filters.bizNo || undefined,
      contourDoctorId: filters.contourDoctorId,
      pageNo: pager.pageNo,
      pageSize: pager.pageSize,
      patientName: filters.patientName || undefined,
      workflowStatus: undefined,
      workflowStatusList: [...taskWorkflowStatusList.value],
    });
    taskList.value = result.list || [];
    pager.total = result.total || 0;
    if (taskList.value.length === 0 && pager.total > 0 && pager.pageNo > 1) {
      pager.pageNo -= 1;
      await loadTaskList(false);
      return;
    }
    const nextId = focusId
      ? focusId
      : preserveSelection &&
            selectedTaskId.value &&
            taskList.value.some((item) => item.id === selectedTaskId.value)
          ? selectedTaskId.value
          : taskList.value[0]?.id;
    if (nextId) {
      await loadTaskDetail(nextId);
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

async function loadTaskDetail(id: number) {
  detailLoading.value = true;
  approvalNodes.value = [];
  approvalProcessName.value = '';
  approvalProcessId.value = '';
  try {
    const task = await getContourTask(id);
    selectedTaskId.value = id;
    selectedTask.value = task;
    fillEditorForm(task);

    const [patientResult, appointmentResult, reviewResult] =
      await Promise.allSettled([
        task.patientId
          ? getPatient(task.patientId)
          : Promise.resolve(undefined),
        task.ctAppointmentId && task.ctAppointmentId > 0
          ? getCtAppointment(task.ctAppointmentId)
          : Promise.resolve(undefined),
        getContourReviewPage({ contourTaskId: id, pageNo: 1, pageSize: 1 }),
      ]);

    patientDetail.value =
      patientResult.status === 'fulfilled' ? patientResult.value : undefined;
    appointmentDetail.value =
      appointmentResult.status === 'fulfilled'
        ? appointmentResult.value
        : undefined;
    latestReview.value =
      reviewResult.status === 'fulfilled'
        ? reviewResult.value.list?.[0]
        : undefined;

    const processInstanceId =
      task.processInstanceId || latestReview.value?.processInstanceId;
    if (processInstanceId) {
      const approvalDetail = await getApprovalDetail({
        processInstanceId,
      }).catch(() => undefined);
      approvalNodes.value = approvalDetail?.activityNodes || [];
      approvalProcessName.value = approvalDetail?.processInstance?.name || '';
      approvalProcessId.value = `${approvalDetail?.processInstance?.id || processInstanceId || ''}`;
    }
  } finally {
    detailLoading.value = false;
  }
}

function buildSavePayload(): HospitalContourTaskApi.ContourTask | undefined {
  if (
    !selectedTask.value?.id ||
    !editorForm.contourDoctorId ||
    !editorForm.patientId
  ) {
    return undefined;
  }
  return {
    attachmentUrl: editorForm.attachmentUrl.trim(),
    bizNo: editorForm.bizNo.trim(),
    contourDoctorId: editorForm.contourDoctorId,
    ctAppointmentId: editorForm.ctAppointmentId,
    id: editorForm.id,
    patientId: editorForm.patientId,
    remark: editorForm.remark.trim(),
    status: editorForm.status ?? 0,
    treatmentSite: editorForm.treatmentSite.trim(),
    versionNo: editorForm.versionNo.trim() || 'V1',
  };
}

async function handleSave() {
  const payload = buildSavePayload();
  if (!payload) {
    message.warning('请先完善勾画医生后再保存');
    return;
  }
  const hideLoading = message.loading({
    content: `正在保存勾画任务【${editorForm.bizNo || selectedTask.value?.bizNo || ''}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await updateContourTask(payload);
    message.success('保存成功');
    await loadTaskList(true);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

async function handleSubmit() {
  if (!selectedTask.value?.id) return;
  const hideLoading = message.loading({
    content: `正在提交勾画任务【${selectedTask.value.bizNo}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await submitContourTask(selectedTask.value.id);
    message.success('提交成功');
    await loadTaskList(true);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

async function handleDelete() {
  if (!selectedTask.value?.id) return;
  const hideLoading = message.loading({
    content: `正在删除勾画任务【${selectedTask.value.bizNo}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await deleteContourTask(selectedTask.value.id);
    message.success('删除成功');
    await loadTaskList(false);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

function handleCreate() {
  formModalApi.setData(null).open();
}

function handleSearch() {
  pager.pageNo = 1;
  void loadTaskList(false);
}

function handleReset() {
  activeStatus.value = 'ALL';
  filters.bizNo = '';
  filters.contourDoctorId = undefined;
  filters.patientName = '';
  pager.pageNo = 1;
  void loadTaskList(false);
}

function handlePageChange(page: number) {
  pager.pageNo = page;
  void loadTaskList(false);
}

function handleStatusChange(status: TaskStatusKey) {
  activeStatus.value = status;
  pager.pageNo = 1;
  void loadTaskList(false);
}

function handleModalSuccess() {
  pager.pageNo = 1;
  void loadTaskList(false);
}

onMounted(async () => {
  await Promise.all([loadDoctorOptions(), loadModelWorkflowSteps()]);
  await loadTaskList(false);
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleModalSuccess" />

    <div class="contour-board">
      <div class="contour-toolbar">
        <div class="contour-toolbar__heading">
          <div class="contour-toolbar__title">靶区勾画</div>
          <div class="contour-toolbar__subtitle">
            选中左侧任务后，在右侧完成勾画维护并提交审核。
          </div>
        </div>

        <div class="contour-toolbar__filters">
          <a-input
            v-model:value="filters.bizNo"
            allow-clear
            class="w-[160px]"
            placeholder="任务单号"
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
            v-model:value="filters.contourDoctorId"
            allow-clear
            class="w-[140px]"
            placeholder="勾画医生"
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
            新建任务
          </a-button>
          <a-button size="small" @click="loadTaskList(true)">刷新</a-button>
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
            <div class="contour-panel__title">勾画任务</div>
            <div class="contour-panel__extra">共 {{ pager.total }} 条</div>
          </div>

          <a-spin :spinning="listLoading">
            <div class="contour-table-wrap">
              <table v-if="taskList.length > 0" class="contour-table">
                <colgroup>
                  <col style="width: 31%" />
                  <col style="width: 18%" />
                  <col style="width: 16%" />
                  <col style="width: 14%" />
                  <col style="width: 21%" />
                </colgroup>
                <thead>
                  <tr>
                    <th>患者 / 任务单</th>
                    <th>治疗部位</th>
                    <th>勾画医生</th>
                    <th>当前阶段</th>
                    <th>提交时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in taskList"
                    :key="item.id"
                    :class="{ 'is-active': selectedTaskId === item.id }"
                    @click="loadTaskDetail(item.id!)"
                  >
                    <td class="is-patient">
                      <div class="contour-table__patient">
                        {{ displayText(item.patientName) }}
                      </div>
                      <div class="contour-table__sub">
                        {{ displayText(item.bizNo) }}
                      </div>
                    </td>
                    <td>{{ item.treatmentSite || '--' }}</td>
                    <td>{{ displayText(item.contourDoctorName) }}</td>
                    <td>
                      <a-tag :color="getStatusColor(item.workflowStatus)">
                        {{ getStatusLabel(item.workflowStatus) }}
                      </a-tag>
                    </td>
                    <td>
                      <div class="contour-table__date">
                        {{ formatDate(item.submitTime || item.createTime) }}
                      </div>
                      <div class="contour-table__time">
                        {{ formatTime(item.submitTime || item.createTime) }}
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <a-empty v-else description="暂无勾画任务" />
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
          <template v-if="selectedTask">
            <div class="contour-patient-bar">
              <div class="contour-patient-bar__avatar">
                {{ (selectedTask.patientName || '患').slice(0, 1) }}
              </div>
              <div class="contour-patient-bar__main">
                <div class="contour-patient-bar__name">
                  {{ displayText(selectedTask.patientName) }}
                  <span>
                    {{ getGenderLabel(patientDetail?.gender) }} /
                    {{ patientDetail?.age ? `${patientDetail.age}` : '--' }}
                  </span>
                </div>
                <div class="contour-patient-bar__meta">
                  <span
                    >病案号：{{
                      displayText(
                        patientDetail?.medicalRecordNo ||
                          patientDetail?.patientNo,
                      )
                    }}</span
                  >
                  <span
                    >放疗号：{{
                      displayText(patientDetail?.radiotherapyNo)
                    }}</span
                  >
                  <span>任务单号：{{ displayText(selectedTask.bizNo) }}</span>
                  <span
                    >主管医生：{{
                      displayText(
                        patientDetail?.managerDoctorName ||
                          selectedTask.contourDoctorName,
                      )
                    }}</span
                  >
                </div>
              </div>
              <a-tag :color="getStatusColor(selectedTask.workflowStatus)">
                {{ getStatusLabel(selectedTask.workflowStatus) }}
              </a-tag>
            </div>
          </template>

          <a-spin :spinning="detailLoading || actionLoading">
            <template v-if="selectedTask">
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
                        <div class="contour-sheet__title">靶区勾画</div>
                        <div class="contour-sheet__title-caption">
                          放疗业务单据
                        </div>
                        <div class="contour-sheet__title-meta">
                          <span
                            >任务单：{{ displayText(selectedTask.bizNo) }}</span
                          >
                          <span>
                            提交：{{
                              formatDateTime(
                                selectedTask.submitTime ||
                                  selectedTask.createTime,
                              )
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
                          <label>姓名</label
                          ><span>{{
                            displayText(selectedTask.patientName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>病案号</label
                          ><span>{{
                            displayText(
                              patientDetail?.medicalRecordNo ||
                                patientDetail?.patientNo,
                            )
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>放疗号</label
                          ><span>{{
                            displayText(patientDetail?.radiotherapyNo)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>住院号</label
                          ><span>{{
                            displayText(patientDetail?.hospitalizationNo)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>主管医生</label
                          ><span>{{
                            displayText(patientDetail?.managerDoctorName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>治疗部位</label
                          ><span>{{
                            displayText(selectedTask.treatmentSite)
                          }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="contour-section">
                      <div class="contour-section__title">CT定位信息</div>
                      <div
                        class="contour-sheet__grid contour-sheet__grid--3 contour-sheet__grid--slab"
                      >
                        <div class="contour-field">
                          <label>预约单号</label
                          ><span>{{
                            displayText(appointmentDetail?.bizNo)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>预约日期</label
                          ><span>{{
                            formatDate(appointmentDetail?.appointmentDate)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>定位机房</label
                          ><span>{{
                            displayText(appointmentDetail?.ctRoomName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>定位设备</label
                          ><span>{{
                            displayText(appointmentDetail?.ctDeviceName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>申请医生</label
                          ><span>{{
                            displayText(appointmentDetail?.applyDoctorName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>定位备注</label
                          ><span>{{
                            displayText(appointmentDetail?.positionNote)
                          }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="contour-section">
                      <div class="contour-section__title">远程勾画</div>
                      <div
                        class="contour-remote"
                        :class="[{ 'is-empty': !editorForm.attachmentUrl }]"
                      >
                        <template v-if="editorForm.attachmentUrl">
                          <div class="contour-remote__text">
                            {{ editorForm.attachmentUrl }}
                          </div>
                          <a :href="editorForm.attachmentUrl" target="_blank"
                            >打开附件</a
                          >
                        </template>
                        <span v-else>无远程控制系统</span>
                      </div>
                    </div>

                    <div class="contour-section">
                      <div class="contour-section__title">勾画信息</div>
                      <a-alert
                        v-if="readonlyMessage"
                        :message="readonlyMessage"
                        show-icon
                        type="info"
                      />
                      <div
                        class="contour-sheet__grid contour-sheet__grid--2 contour-sheet__grid--form contour-editor-grid"
                      >
                        <div class="contour-editor-item">
                          <label>勾画医生</label>
                          <a-select
                            v-model:value="editorForm.contourDoctorId"
                            :disabled="!canUpdate || !canEditCurrent"
                            allow-clear
                            placeholder="请选择勾画医生"
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
                          <label>版本号</label>
                          <a-input
                            v-model:value="editorForm.versionNo"
                            :disabled="!canUpdate || !canEditCurrent"
                            placeholder="默认 V1"
                            size="small"
                          />
                        </div>
                        <div class="contour-editor-item">
                          <label>治疗部位</label>
                          <a-input
                            v-model:value="editorForm.treatmentSite"
                            :disabled="!canUpdate || !canEditCurrent"
                            placeholder="请输入治疗部位"
                            size="small"
                          />
                        </div>
                        <div class="contour-editor-item">
                          <label>CT预约ID</label>
                          <a-input
                            :value="displayText(editorForm.ctAppointmentId)"
                            disabled
                            size="small"
                          />
                        </div>
                        <div
                          class="contour-editor-item contour-editor-item--full"
                        >
                          <label>附件地址</label>
                          <a-input
                            v-model:value="editorForm.attachmentUrl"
                            :disabled="!canUpdate || !canEditCurrent"
                            placeholder="请输入勾画附件地址"
                            size="small"
                          />
                        </div>
                        <div
                          class="contour-editor-item contour-editor-item--full"
                        >
                          <label>审核备注</label>
                          <a-textarea
                            v-model:value="editorForm.remark"
                            :auto-size="{ minRows: 2, maxRows: 4 }"
                            :disabled="!canUpdate || !canEditCurrent"
                            placeholder="请输入备注"
                            size="small"
                          />
                        </div>
                      </div>
                    </div>

                    <div class="contour-section">
                      <div class="contour-section__title">最近审核</div>
                      <div
                        class="contour-sheet__grid contour-sheet__grid--2 contour-sheet__grid--slab contour-sheet__grid--review"
                      >
                        <div class="contour-field">
                          <label>审核单号</label
                          ><span>{{ displayText(latestReview?.bizNo) }}</span>
                        </div>
                        <div class="contour-field">
                          <label>审核医生</label
                          ><span>{{
                            displayText(latestReview?.reviewDoctorName)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>审核结果</label
                          ><span>{{
                            getReviewResultLabel(latestReview?.reviewResult)
                          }}</span>
                        </div>
                        <div class="contour-field">
                          <label>审核时间</label
                          ><span>{{
                            formatDateTime(latestReview?.reviewTime)
                          }}</span>
                        </div>
                        <div class="contour-field contour-field--full">
                          <label>审核意见</label
                          ><span>{{
                            displayText(latestReview?.reviewComment)
                          }}</span>
                        </div>
                      </div>
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
                      :title="`确认删除勾画任务【${selectedTask.bizNo}】吗？`"
                      @confirm="handleDelete"
                    >
                      <a-button :disabled="!canEditCurrent" danger size="small">
                        删除
                      </a-button>
                    </a-popconfirm>
                  </div>
                </div>
              </div>
            </template>
            <a-empty v-else description="请选择左侧任务" />
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
  gap: 10px;
  grid-template-columns: minmax(380px, 0.88fr) minmax(620px, 1.12fr);
}

.contour-panel {
  min-height: 660px;
  overflow: hidden;
}

.contour-panel__header {
  align-items: center;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  justify-content: space-between;
  padding: 8px 10px;
}

.contour-panel__title {
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
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

.contour-timeline__meta--reason {
  color: #c05a4f;
}

.contour-section__title {
  color: #1f2937;
  font-weight: 600;
}

.contour-list-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.contour-table-wrap {
  max-height: 640px;
  overflow: auto;
}

.contour-table {
  border-collapse: collapse;
  font-size: 12px;
  table-layout: fixed;
  width: 100%;
}

.contour-table th,
.contour-table td {
  border-bottom: 1px solid #eef2f7;
  padding: 6px 8px;
  text-align: left;
  vertical-align: top;
}

.contour-table th {
  background: #fafbfd;
  color: #6b7280;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 1;
}

.contour-table tbody tr {
  cursor: pointer;
}

.contour-table tbody tr.is-active {
  background: #eef6ff;
}

.contour-table__date {
  color: #1f2937;
  font-weight: 600;
  line-height: 1.2;
}

.contour-table__time {
  color: #6b7280;
  line-height: 1.2;
  margin-top: 2px;
}

.contour-table__patient,
.contour-patient-bar__name,
.contour-sheet__title {
  color: #1f2937;
  font-size: 13px;
  font-weight: 700;
}

.contour-list-panel__footer {
  border-top: 1px solid #eef2f7;
  padding: 8px 10px;
}

.contour-detail-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.contour-patient-bar {
  align-items: center;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  gap: 8px;
  padding: 6px 10px;
}

.contour-patient-bar__avatar {
  align-items: center;
  background: #dcecff;
  border-radius: 6px;
  color: #1677ff;
  display: flex;
  font-size: 14px;
  font-weight: 700;
  height: 34px;
  justify-content: center;
  width: 34px;
}

.contour-patient-bar__main {
  flex: 1;
  min-width: 0;
}

.contour-patient-bar__name {
  align-items: baseline;
  display: flex;
  flex-wrap: wrap;
  gap: 2px 8px;
  min-width: 0;
}

.contour-patient-bar__name span {
  color: #8a94a6;
  font-size: 11px;
  font-weight: 500;
}

.contour-patient-bar__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 2px 10px;
  margin-top: 1px;
}

.contour-workspace {
  align-items: start;
  background: #f6f8fb;
  display: grid;
  gap: 12px;
  grid-template-columns: 188px minmax(0, 1fr);
  min-height: 0;
  padding: 10px 12px 12px;
}

.contour-timeline,
.contour-sheet {
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

.contour-timeline__item.is-done .contour-timeline__badge {
  background: #ecfdf3;
  color: #15924f;
}

.contour-timeline__item.is-current .contour-timeline__dot {
  background: #1677ff;
}

.contour-timeline__item.is-current .contour-timeline__name {
  color: #1677ff;
}

.contour-timeline__item.is-current .contour-timeline__badge {
  background: #e8f3ff;
  color: #1677ff;
}

.contour-sheet {
  background: transparent;
  border-left: 0;
  padding: 0;
}

.contour-sheet__paper {
  background: #fff;
  border: 1px solid #e8edf3;
  box-shadow: 0 10px 24px rgb(15 23 42 / 6%);
  min-height: 0;
  padding: 18px 20px 16px;
}

.contour-sheet__header {
  align-items: center;
  display: grid;
  gap: 12px;
  grid-template-columns: 160px minmax(0, 1fr) 160px;
  margin-bottom: 12px;
}

.contour-sheet__brand {
  align-items: center;
  display: flex;
  gap: 10px;
  min-width: 0;
}

.contour-sheet__brand-logo {
  align-items: center;
  background: linear-gradient(180deg, #f4fffb 0%, #e3f7ef 100%);
  border: 1px solid #ccebdd;
  border-radius: 999px;
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 90%);
  color: #1a9c6d;
  display: flex;
  flex-shrink: 0;
  font-size: 16px;
  font-weight: 700;
  height: 44px;
  justify-content: center;
  width: 44px;
}

.contour-sheet__brand-name {
  color: #23a16b;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.contour-sheet__brand-subtitle {
  color: #8da095;
  font-size: 10px;
  letter-spacing: 1px;
  margin-top: 2px;
}

.contour-sheet__brand-text,
.contour-sheet__title-box {
  min-width: 0;
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

.contour-sheet__header-spacer {
  min-width: 0;
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

.contour-sheet__grid--review {
  grid-template-columns: repeat(2, minmax(0, 1fr));
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
  word-break: break-all;
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
.contour-editor-item :deep(.ant-input-textarea),
.contour-editor-item :deep(.ant-select) {
  width: 100%;
}

.contour-patient-bar :deep(.ant-tag) {
  flex-shrink: 0;
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
.contour-board :deep(.ant-select-single.ant-select-sm),
.contour-board :deep(.ant-select-single.ant-select-sm .ant-select-selector),
.contour-board :deep(.ant-input-affix-wrapper-sm),
.contour-board :deep(.ant-input-textarea textarea) {
  font-size: 11px;
}

.contour-board :deep(.ant-select-single.ant-select-sm .ant-select-selector) {
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
  .contour-sheet__grid--review,
  .contour-sheet__grid--form {
    grid-template-columns: 1fr;
  }

  .contour-editor-item {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
