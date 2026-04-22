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
import { getContourTask } from '#/api/hospital/contour-task';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getPatient } from '#/api/hospital/patient';
import { getPlanApply } from '#/api/hospital/plan-apply';
import { getPlanDesign } from '#/api/hospital/plan-design';
import {
  buildWorkflowStepsFromApprovalNodes,
  getWorkflowModelKey,
  getWorkflowRoutePath,
  getWorkflowStepsFromModelPath,
} from '#/views/hospital/_shared/workflow';

import NodeCustomForm from '#/views/hospital/_shared/node-custom-form.vue';

type ReviewStatusKey = 'REVIEWING';

interface AuditFormState {
  id?: number;
  remark: string;
  reviewComment: string;
  reviewDoctorId?: number;
}

interface ReviewRecord {
  bizNo?: string;
  createTime?: Date | number | string;
  id?: number;
  patientId?: number;
  patientName?: string;
  planDesignId?: number;
  processInstanceId?: string;
  remark?: string;
  reviewComment?: string;
  reviewDoctorId?: number;
  reviewDoctorName?: string;
  reviewResult?: string;
  reviewStage?: string;
  reviewTime?: Date | number | string;
  workflowStatus?: string;
}

interface ReviewAuditPayload {
  id: number;
  remark?: string;
  reviewComment?: string;
  reviewDoctorId: number;
}

type StepTone = 'current' | 'done' | 'pending';

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

interface ReviewWorkbenchApi {
  approve: (data: ReviewAuditPayload) => Promise<unknown>;
  getDetail: (id: number) => Promise<ReviewRecord>;
  getPage: (params: Record<string, any>) => Promise<{
    list?: ReviewRecord[];
    total?: number;
  }>;
  reject: (data: ReviewAuditPayload) => Promise<unknown>;
}

interface ReviewWorkbenchConfig {
  actorLabel: string;
  actorPlaceholder: string;
  approveAuth: string;
  approvedIndex: number;
  bizLabel: string;
  commentLabel: string;
  commentPlaceholder: string;
  pageCode: string;
  emptyText?: string;
  rejectAuth: string;
  resultLabel: string;
  reviewingIndex: number;
  subtitle?: string;
  title: string;
  workflowKey: HospitalWorkflowKey;
}

const props = defineProps<{
  api: ReviewWorkbenchApi;
  config: ReviewWorkbenchConfig;
}>();

const reviewStatusLabelMap: Record<string, string> = {
  APPROVED: '已通过',
  REJECTED: '已退回',
  REVIEWING: '处理中',
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


const listLoading = ref(false);
const detailLoading = ref(false);
const actionLoading = ref(false);
const selectedReviewId = ref<number>();
const reviewList = ref<ReviewRecord[]>([]);
const doctorOptions = ref<HospitalDoctorApi.Doctor[]>([]);
const selectedReview = ref<ReviewRecord>();
const selectedPlanDesign = ref<HospitalPlanDesignApi.PlanDesign>();
const selectedPlanApply = ref<HospitalPlanApplyApi.PlanApply>();
const selectedContourTask = ref<HospitalContourTaskApi.ContourTask>();
const patientDetail = ref<HospitalPatientApi.Patient>();
const approvalNodes = ref<BpmProcessInstanceApi.ApprovalNodeInfo[]>([]);
const modelWorkflowSteps = ref<WorkflowStep[]>([]);
const approvalProcessName = ref('');
const approvalProcessId = ref('');
const approvalProcessKey = ref('');
const activeStatus = ref<ReviewStatusKey>('REVIEWING');

const pager = reactive({
  pageNo: 1,
  pageSize: 12,
  total: 0,
});

const filters = reactive({
  bizNo: '',
  patientName: '',
  reviewDoctorId: undefined as number | undefined,
});

const auditForm = reactive<AuditFormState>({
  id: undefined,
  remark: '',
  reviewComment: '',
  reviewDoctorId: undefined,
});

const canApprove = computed(() => hasAccessByCodes([props.config.approveAuth]));
const canReject = computed(() => hasAccessByCodes([props.config.rejectAuth]));

const canEditCurrent = computed(() => {
  return getReviewStatusCode(selectedReview.value) === 'REVIEWING';
});

const readonlyMessage = computed(() => {
  const status = getReviewStatusCode(selectedReview.value);
  if (status === 'APPROVED') return '当前单据已通过，右侧信息只读。';
  if (status === 'REJECTED') return '当前单据已退回，右侧信息只读。';
  return '';
});

const statusTabs = computed(() => {
  const buildItem = (key: ReviewStatusKey, label: string) => ({ key, label });
  return [buildItem('REVIEWING', '待审核')];
});

const currentProcessKey = computed(
  () => approvalProcessKey.value || getWorkflowModelKey(props.config.workflowKey),
);
const currentNodeKey = computed(
  () => approvalNodes.value.find((item) => item.status === 0 || item.status === 1)?.id,
);

const nodeFormValues = computed<Record<string, any>>(() => ({
  handleOpinion:
    auditForm.reviewComment ||
    selectedReview.value?.reviewComment ||
    selectedReview.value?.remark,
  handleResult:
    selectedReview.value?.reviewResult || selectedReview.value?.workflowStatus,
  handlerName: selectedReview.value?.reviewDoctorName,
  patientName: selectedReview.value?.patientName || patientDetail.value?.name,
  patientNo:
    patientDetail.value?.medicalRecordNo || patientDetail.value?.patientNo,
  planName: selectedPlanDesign.value?.planName,
  remark: auditForm.remark || selectedReview.value?.remark,
  totalDose:
    selectedPlanDesign.value?.totalDose || selectedPlanApply.value?.prescriptionDose,
}));

const workflowSteps = computed<WorkflowStep[]>(() => {
  if (approvalNodes.value.length > 0) {
    return buildWorkflowStepsFromApprovalNodes(
      props.config.workflowKey,
      approvalNodes.value,
      modelWorkflowSteps.value,
    );
  }

  const status = getReviewStatusCode(selectedReview.value);
  const currentIndex = status
    ? (status === 'APPROVED'
      ? props.config.approvedIndex
      : props.config.reviewingIndex)
    : -1;
  const baseSteps =
    modelWorkflowSteps.value.length > 0
      ? modelWorkflowSteps.value
      : buildWorkflowStepsFromApprovalNodes(
          props.config.workflowKey,
          undefined,
          modelWorkflowSteps.value,
        );

  return baseSteps.map((item, index) => ({
    ...item,
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

function getStatusColor(status?: string) {
  if (status === 'APPROVED') return 'success';
  if (status === 'REJECTED') return 'error';
  if (status === 'REVIEWING') return 'processing';
  return 'default';
}

function getReviewStatusCode(review?: ReviewRecord) {
  return review?.reviewResult || review?.workflowStatus || '';
}

function getStatusLabel(status?: string) {
  return reviewStatusLabelMap[status || ''] || displayText(status);
}

function clearDetail() {
  selectedReviewId.value = undefined;
  selectedReview.value = undefined;
  selectedPlanDesign.value = undefined;
  selectedPlanApply.value = undefined;
  selectedContourTask.value = undefined;
  patientDetail.value = undefined;
  approvalNodes.value = [];
  approvalProcessName.value = '';
  approvalProcessId.value = '';
  approvalProcessKey.value = '';
  Object.assign(auditForm, {
    id: undefined,
    remark: '',
    reviewComment: '',
    reviewDoctorId: undefined,
  });
}

function fillAuditForm(review?: ReviewRecord) {
  Object.assign(auditForm, {
    id: review?.id,
    remark: review?.remark || '',
    reviewComment: review?.reviewComment || '',
    reviewDoctorId: review?.reviewDoctorId,
  });
}

async function loadDoctorOptions() {
  try {
    doctorOptions.value = (await getSimpleDoctorList()) || [];
  } catch {
    doctorOptions.value = [];
  }
}

async function loadReviewList(preserveSelection = true) {
  listLoading.value = true;
  try {
    const focusId = getFocusId();
    const result = await props.api.getPage({
      id: focusId,
      bizNo: filters.bizNo || undefined,
      pageNo: pager.pageNo,
      pageSize: pager.pageSize,
      patientName: filters.patientName || undefined,
      reviewDoctorId: filters.reviewDoctorId,
      workflowStatus: 'REVIEWING',
    });
    reviewList.value = result.list || [];
    pager.total = result.total || 0;

    if (reviewList.value.length === 0 && pager.total > 0 && pager.pageNo > 1) {
      pager.pageNo -= 1;
      await loadReviewList(false);
      return;
    }

    const nextId = focusId
      ? focusId
      : preserveSelection &&
            selectedReviewId.value &&
            reviewList.value.some((item) => item.id === selectedReviewId.value)
          ? selectedReviewId.value
          : reviewList.value[0]?.id;

    if (nextId) {
      await loadReviewDetail(nextId);
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

async function loadReviewDetail(id: number) {
  detailLoading.value = true;
  try {
    const review = await props.api.getDetail(id);
    selectedReviewId.value = id;
    selectedReview.value = review;
    fillAuditForm(review);

    if (review.planDesignId) {
      try {
        selectedPlanDesign.value = await getPlanDesign(review.planDesignId);
      } catch {
        selectedPlanDesign.value = undefined;
      }
    } else {
      selectedPlanDesign.value = undefined;
    }

    if (selectedPlanDesign.value?.planApplyId) {
      try {
        selectedPlanApply.value = await getPlanApply(
          selectedPlanDesign.value.planApplyId,
        );
      } catch {
        selectedPlanApply.value = undefined;
      }
    } else {
      selectedPlanApply.value = undefined;
    }

    if (selectedPlanApply.value?.contourTaskId) {
      try {
        selectedContourTask.value = await getContourTask(
          selectedPlanApply.value.contourTaskId,
        );
      } catch {
        selectedContourTask.value = undefined;
      }
    } else {
      selectedContourTask.value = undefined;
    }

    const patientId =
      review.patientId ||
      selectedPlanDesign.value?.patientId ||
      selectedPlanApply.value?.patientId ||
      selectedContourTask.value?.patientId;

    if (patientId) {
      try {
        patientDetail.value = await getPatient(patientId);
      } catch {
        patientDetail.value = undefined;
      }
    } else {
      patientDetail.value = undefined;
    }

    if (review.processInstanceId) {
      try {
        const approvalDetail = await getApprovalDetail({
          processInstanceId: review.processInstanceId,
        });
        approvalNodes.value = approvalDetail?.activityNodes || [];
        approvalProcessName.value = approvalDetail?.processInstance?.name || '';
        approvalProcessId.value = `${approvalDetail?.processInstance?.id || review.processInstanceId || ''}`;
        approvalProcessKey.value =
          approvalDetail?.processDefinition?.key ||
          approvalDetail?.processInstance?.processDefinition?.key ||
          '';
      } catch {
        approvalNodes.value = [];
        approvalProcessName.value = '';
        approvalProcessId.value = review.processInstanceId || '';
        approvalProcessKey.value = '';
      }
    } else {
      approvalNodes.value = [];
      approvalProcessName.value = '';
      approvalProcessId.value = '';
      approvalProcessKey.value = '';
    }
  } finally {
    detailLoading.value = false;
  }
}

function buildAuditPayload(): ReviewAuditPayload | undefined {
  if (!selectedReview.value?.id || !auditForm.reviewDoctorId) {
    return undefined;
  }
  return {
    id: selectedReview.value.id,
    remark: auditForm.remark.trim(),
    reviewComment: auditForm.reviewComment.trim(),
    reviewDoctorId: auditForm.reviewDoctorId,
  };
}

async function handleApprove() {
  const payload = buildAuditPayload();
  if (!payload) {
    message.warning(`请先选择${props.config.actorLabel}`);
    return;
  }
  const hideLoading = message.loading({
    content: `正在提交【${selectedReview.value?.bizNo || ''}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await props.api.approve(payload);
    message.success('提交成功');
    await loadReviewList(true);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

async function handleReject() {
  const payload = buildAuditPayload();
  if (!payload) {
    message.warning(`请先选择${props.config.actorLabel}`);
    return;
  }
  const hideLoading = message.loading({
    content: `正在退回【${selectedReview.value?.bizNo || ''}】`,
    duration: 0,
  });
  actionLoading.value = true;
  try {
    await props.api.reject(payload);
    message.success('退回成功');
    await loadReviewList(true);
  } finally {
    actionLoading.value = false;
    hideLoading();
  }
}

function handleSearch() {
  pager.pageNo = 1;
  void loadReviewList(false);
}

function handleReset() {
  activeStatus.value = 'REVIEWING';
  filters.bizNo = '';
  filters.patientName = '';
  filters.reviewDoctorId = undefined;
  pager.pageNo = 1;
  void loadReviewList(false);
}

function handlePageChange(page: number) {
  pager.pageNo = page;
  void loadReviewList(false);
}

function handleStatusChange(status: ReviewStatusKey) {
  activeStatus.value = status;
  pager.pageNo = 1;
  void loadReviewList(false);
}

onMounted(async () => {
  await Promise.all([loadDoctorOptions(), loadModelWorkflowSteps()]);
  await loadReviewList(false);
});
</script>

<template>
  <Page auto-content-height>
    <div class="contour-board">
      <div class="contour-toolbar">
        <div class="contour-toolbar__heading">
          <div class="contour-toolbar__title">{{ props.config.title }}</div>
          <div class="contour-toolbar__subtitle">
            {{
              props.config.subtitle ||
              '选中左侧单据后，在右侧查看流程、填写处理意见并提交。'
            }}
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
            v-model:value="filters.reviewDoctorId"
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
          <a-button size="small" @click="loadReviewList(true)">刷新</a-button>
        </div>
      </div>

      <div v-if="statusTabs.length > 1" class="contour-status">
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
            <div class="contour-panel__title">{{ props.config.title }}</div>
            <div class="contour-panel__extra">共 {{ pager.total }} 条</div>
          </div>

          <a-spin :spinning="listLoading">
            <div class="contour-table-wrap">
              <table v-if="reviewList.length > 0" class="contour-table">
                <colgroup>
                  <col style="width: 31%" />
                  <col style="width: 16%" />
                  <col style="width: 16%" />
                  <col style="width: 16%" />
                  <col style="width: 21%" />
                </colgroup>
                <thead>
                  <tr>
                    <th>患者 / 单号</th>
                    <th>计划设计</th>
                    <th>{{ props.config.actorLabel }}</th>
                    <th>当前阶段</th>
                    <th>提交时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in reviewList"
                    :key="item.id"
                    :class="{ 'is-active': selectedReviewId === item.id }"
                    @click="loadReviewDetail(item.id!)"
                  >
                    <td class="is-patient">
                      <div class="contour-table__patient">
                        {{ displayText(item.patientName) }}
                      </div>
                      <div class="contour-table__sub">
                        {{ displayText(item.bizNo) }}
                      </div>
                    </td>
                    <td>#{{ displayText(item.planDesignId) }}</td>
                    <td>{{ displayText(item.reviewDoctorName) }}</td>
                    <td>
                      <a-tag :color="getStatusColor(getReviewStatusCode(item))">
                        {{ getStatusLabel(getReviewStatusCode(item)) }}
                      </a-tag>
                    </td>
                    <td>
                      <div class="contour-table__date">
                        {{ formatDate(item.reviewTime || item.createTime) }}
                      </div>
                      <div class="contour-table__time">
                        {{ formatTime(item.reviewTime || item.createTime) }}
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <a-empty
                v-else
                :description="
                  props.config.emptyText || `暂无${props.config.title}`
                "
              />
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
          <div
            class="contour-patient-bar"
            :class="[{ 'contour-patient-bar--empty': !selectedReview }]"
          >
            <div class="contour-patient-bar__avatar">
              {{
                (
                  selectedReview?.patientName ||
                  patientDetail?.name ||
                  '患'
                ).slice(0, 1)
              }}
            </div>
            <div class="contour-patient-bar__main">
              <div class="contour-patient-bar__name">
                {{
                  displayText(
                    selectedReview?.patientName || patientDetail?.name,
                  )
                }}
                <span>
                  {{ getGenderLabel(patientDetail?.gender) }} /
                  {{ patientDetail?.age ? `${patientDetail.age}` : '--' }}
                </span>
              </div>
              <div class="contour-patient-bar__meta">
                <span>
                  病案号：
                  {{
                    displayText(
                      patientDetail?.medicalRecordNo ||
                        patientDetail?.patientNo,
                    )
                  }}
                </span>
                <span>
                  放疗号：{{ displayText(patientDetail?.radiotherapyNo) }}
                </span>
                <span>
                  {{ props.config.bizLabel }}：{{
                    displayText(selectedReview?.bizNo)
                  }}
                </span>
                <span>
                  设计单号：{{ displayText(selectedPlanDesign?.bizNo) }}
                </span>
                <span>
                  {{ props.config.actorLabel }}：{{
                    displayText(selectedReview?.reviewDoctorName)
                  }}
                </span>
              </div>
            </div>
            <a-tag
              :color="
                selectedReview
                  ? getStatusColor(getReviewStatusCode(selectedReview))
                  : 'default'
              "
            >
              {{
                selectedReview
                  ? getStatusLabel(getReviewStatusCode(selectedReview))
                  : '待选择'
              }}
            </a-tag>
          </div>

          <a-spin :spinning="detailLoading || actionLoading">
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
                      <div class="contour-timeline__name">{{ item.title }}</div>
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
                        <div class="contour-sheet__brand-name">上海XX医院</div>
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
                            displayText(selectedReview?.bizNo)
                          }}
                        </span>
                        <span>
                          提交：{{
                            formatDateTime(
                              selectedReview?.reviewTime ||
                                selectedReview?.createTime,
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
                        <label>姓名</label>
                        <span>
                          {{
                            displayText(
                              selectedReview?.patientName ||
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
                    </div>
                  </div>

                  <div class="contour-section">
                    <div class="contour-section__title">计划申请信息</div>
                    <div
                      class="contour-sheet__grid contour-sheet__grid--3 contour-sheet__grid--slab"
                    >
                      <div class="contour-field">
                        <label>申请单号</label>
                        <span>{{ displayText(selectedPlanApply?.bizNo) }}</span>
                      </div>
                      <div class="contour-field">
                        <label>申请医生</label>
                        <span>{{
                          displayText(selectedPlanApply?.applyDoctorName)
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
                          displayText(selectedPlanApply?.priority)
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
                    <div class="contour-section__title">计划设计信息</div>
                    <div
                      class="contour-sheet__grid contour-sheet__grid--3 contour-sheet__grid--slab"
                    >
                      <div class="contour-field">
                        <label>设计单号</label>
                        <span>{{
                          displayText(selectedPlanDesign?.bizNo)
                        }}</span>
                      </div>
                      <div class="contour-field">
                        <label>计划名称</label>
                        <span>{{
                          displayText(selectedPlanDesign?.planName)
                        }}</span>
                      </div>
                      <div class="contour-field">
                        <label>版本号</label>
                        <span>{{
                          displayText(selectedPlanDesign?.versionNo)
                        }}</span>
                      </div>
                      <div class="contour-field">
                        <label>设计师</label>
                        <span>{{
                          displayText(selectedPlanDesign?.designUserName)
                        }}</span>
                      </div>
                      <div class="contour-field">
                        <label>总剂量</label>
                        <span>{{
                          displayDose(selectedPlanDesign?.totalDose)
                        }}</span>
                      </div>
                      <div class="contour-field">
                        <label>单次剂量</label>
                        <span>{{
                          displayDose(selectedPlanDesign?.singleDose)
                        }}</span>
                      </div>
                      <div class="contour-field">
                        <label>设计分次</label>
                        <span>{{
                          displayFractions(selectedPlanDesign?.fractions)
                        }}</span>
                      </div>
                      <div class="contour-field">
                        <label>设计时间</label>
                        <span>
                          {{
                            formatDateTime(
                              selectedPlanDesign?.designTime ||
                                selectedPlanDesign?.submitTime,
                            )
                          }}
                        </span>
                      </div>
                      <div class="contour-field contour-field--full">
                        <label>设计备注</label>
                        <span>{{
                          displayText(selectedPlanDesign?.remark)
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
                    <div class="contour-section__title">处理信息</div>
                    <a-alert
                      v-if="selectedReview && readonlyMessage"
                      :message="readonlyMessage"
                      show-icon
                      type="info"
                    />
                    <div
                      v-else-if="!selectedReview"
                      class="contour-sheet__empty-tip"
                    >
                      当前暂无处理数据，右侧保留工作台布局。
                    </div>
                    <div
                      class="contour-sheet__grid contour-sheet__grid--2 contour-sheet__grid--form contour-editor-grid"
                    >
                      <div class="contour-editor-item">
                        <label>{{ props.config.actorLabel }}</label>
                        <a-select
                          v-model:value="auditForm.reviewDoctorId"
                          :disabled="!selectedReview || !canEditCurrent"
                          allow-clear
                          :placeholder="props.config.actorPlaceholder"
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
                        <label>{{ props.config.resultLabel }}</label>
                        <a-input
                          :value="
                            getStatusLabel(getReviewStatusCode(selectedReview))
                          "
                          disabled
                          size="small"
                        />
                      </div>
                      <div
                        class="contour-editor-item contour-editor-item--full"
                      >
                        <label>{{ props.config.commentLabel }}</label>
                        <a-textarea
                          v-model:value="auditForm.reviewComment"
                          :auto-size="{ minRows: 3, maxRows: 5 }"
                          :disabled="!selectedReview || !canEditCurrent"
                          :placeholder="props.config.commentPlaceholder"
                          size="small"
                        />
                      </div>
                      <div
                        class="contour-editor-item contour-editor-item--full"
                      >
                        <label>备注</label>
                        <a-textarea
                          v-model:value="auditForm.remark"
                          :auto-size="{ minRows: 2, maxRows: 4 }"
                          :disabled="!selectedReview || !canEditCurrent"
                          placeholder="请输入备注"
                          size="small"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div class="contour-actions">
                  <a-button
                    v-if="canReject"
                    :disabled="!selectedReview || !canEditCurrent"
                    danger
                    size="small"
                    @click="handleReject"
                  >
                    退回
                  </a-button>
                  <a-button
                    v-if="canApprove"
                    :disabled="!selectedReview || !canEditCurrent"
                    size="small"
                    type="primary"
                    @click="handleApprove"
                  >
                    通过
                  </a-button>
                </div>
              </div>
            </div>
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
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

.contour-patient-bar--empty {
  background: linear-gradient(180deg, #fbfcfe 0%, #f6f8fb 100%);
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

.contour-timeline__item.is-current .contour-timeline__dot {
  background: #1677ff;
}

.contour-timeline__item.is-current .contour-timeline__name {
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

.contour-sheet__empty-tip {
  background: #f7faff;
  border: 1px dashed #bfd9ff;
  border-radius: 2px;
  color: #6b7280;
  font-size: 11px;
  line-height: 1.5;
  margin-bottom: 8px;
  padding: 6px 10px;
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
  .contour-sheet__grid--form {
    grid-template-columns: 1fr;
  }

  .contour-editor-item {
    gap: 4px;
    grid-template-columns: 1fr;
  }
}
</style>
