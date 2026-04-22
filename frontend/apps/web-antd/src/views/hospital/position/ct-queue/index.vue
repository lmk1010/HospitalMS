<script lang="ts" setup>
import type { TableColumnsType } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';

import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';
import type { HospitalCtAppointmentApi } from '#/api/hospital/ct-appointment';
import type { HospitalCtQueueApi } from '#/api/hospital/ct-queue';
import type { HospitalPatientApi } from '#/api/hospital/patient';

import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

import { getApprovalDetail } from '#/api/bpm/processInstance';
import { getCtAppointment } from '#/api/hospital/ct-appointment';
import {
  callCtQueue,
  finishCtQueue,
  getCtQueuePage,
  skipCtQueue,
} from '#/api/hospital/ct-queue';
import { getPatient } from '#/api/hospital/patient';
import {
  buildWorkflowStepsFromApprovalNodes,
  getWorkflowRoutePath,
  getWorkflowStepsFromModelPath,
} from '#/views/hospital/_shared/workflow';

type LeftTabKey = 'DONE' | 'SIGNED' | 'SKIPPED';
type RightTabKey = 'PAUSED' | 'WAIT_SIGN';
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

const queueStatusLabelMap: Record<string, string> = {
  CALLED: '已叫号',
  DONE: '已完成',
  PAUSED: '已暂停',
  QUEUING: '已签到',
  SKIPPED: '已过号',
  WAIT_SIGN: '未报到',
};

const appointmentWorkflowStatusLabelMap: Record<string, string> = {
  BOOKED: '已预约',
  PENDING: '待提交',
};

const leftColumns: TableColumnsType<HospitalCtQueueApi.CtQueue> = [
  { title: '姓名 / 队列号', key: 'patientInfo', width: 210 },
  { title: '机房', dataIndex: 'ctRoomName', key: 'ctRoomName', width: 120 },
  { title: '叫号屏', dataIndex: 'windowName', key: 'windowName', width: 120 },
  { title: '预约时间', key: 'queueDate', width: 150 },
  { title: '报到时间', key: 'signInTime', width: 170 },
  { title: '状态', key: 'queueStatus', width: 100 },
  { title: '操作', key: 'actions', width: 170, fixed: 'right' },
];

const rightColumns: TableColumnsType<HospitalCtQueueApi.CtQueue> = [
  { title: '姓名 / 队列号', key: 'patientInfo', width: 220 },
  { title: '机房', dataIndex: 'ctRoomName', key: 'ctRoomName', width: 120 },
  { title: '预约时间', key: 'queueDate', width: 150 },
  { title: '状态', key: 'queueStatus', width: 100 },
  { title: '操作', key: 'actions', width: 140, fixed: 'right' },
];

const loading = ref(false);
const detailLoading = ref(false);
const detailVisible = ref(false);
const keyword = ref('');
const queueDate = ref<Dayjs>(dayjs());
const selectedRoom = ref<string>();
const activeLeftTab = ref<LeftTabKey>('SIGNED');
const activeRightTab = ref<RightTabKey>('WAIT_SIGN');
const machineFault = ref(false);
const rawQueueData = ref<HospitalCtQueueApi.CtQueue[]>([]);
const focusedQueueId = ref<number>();
const patientDetail = ref<HospitalPatientApi.Patient>();
const appointmentDetail = ref<HospitalCtAppointmentApi.CtAppointment>();
const approvalNodes = ref<BpmProcessInstanceApi.ApprovalNodeInfo[]>([]);
const modelWorkflowSteps = ref<WorkflowStep[]>([]);
const approvalProcessName = ref('');
const approvalProcessId = ref('');
const detailRequestId = ref(0);
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
  if (route.query.focusId === undefined || route.query.focusId === null) return;
  const query = { ...route.query };
  delete query.focusId;
  await router.replace({ query });
}

async function resolveFocusQueue() {
  const focusId = getFocusId();
  if (!focusId) return false;
  const result = await getCtQueuePage({
    id: focusId,
    pageNo: 1,
    pageSize: 1,
  } as any).catch(() => undefined);
  const target = result?.list?.[0];
  if (!target) return false;
  focusedQueueId.value = target.id;
  if (target.queueDate) {
    queueDate.value = dayjs(target.queueDate);
  }
  selectedRoom.value = target.ctRoomName || undefined;
  return true;
}

const filteredRows = computed(() => {
  const searchValue = keyword.value.trim().toLowerCase();
  if (!searchValue) {
    return rawQueueData.value;
  }
  return rawQueueData.value.filter((item) => {
    return [item.patientName, item.queueNo, item.ctRoomName, item.windowName]
      .filter(Boolean)
      .some((value) => `${value}`.toLowerCase().includes(searchValue));
  });
});

const roomOptions = computed(() => {
  const rooms = new Set<string>();
  for (const item of rawQueueData.value) {
    if (item.ctRoomName) {
      rooms.add(item.ctRoomName);
    }
  }
  return [...rooms]
    .toSorted((left, right) => left.localeCompare(right, 'zh-CN'))
    .map((item) => ({ label: item, value: item }));
});

const currentPatient = computed(() => {
  return (
    filteredRows.value.find((item) => item.queueStatus === 'CALLED') || null
  );
});

const signedRows = computed(() => {
  return filteredRows.value.filter((item) => {
    return ['CALLED', 'QUEUING'].includes(item.queueStatus || '');
  });
});

const skippedRows = computed(() => {
  return filteredRows.value.filter((item) => item.queueStatus === 'SKIPPED');
});

const doneRows = computed(() => {
  return filteredRows.value.filter((item) => item.queueStatus === 'DONE');
});

const waitSignRows = computed(() => {
  return filteredRows.value.filter((item) => item.queueStatus === 'WAIT_SIGN');
});

const pausedRows = computed(() => {
  return filteredRows.value.filter((item) => item.queueStatus === 'PAUSED');
});

const leftTabs = computed(() => [
  { key: 'SIGNED' as const, label: '已签到', count: signedRows.value.length },
  { key: 'SKIPPED' as const, label: '已过号', count: skippedRows.value.length },
  { key: 'DONE' as const, label: '已完成', count: doneRows.value.length },
]);

const rightTabs = computed(() => [
  {
    key: 'WAIT_SIGN' as const,
    label: '未报到',
    count: waitSignRows.value.length,
  },
  { key: 'PAUSED' as const, label: '已暂停', count: pausedRows.value.length },
]);

const leftTableData = computed(() => {
  switch (activeLeftTab.value) {
    case 'DONE': {
      return doneRows.value;
    }
    case 'SKIPPED': {
      return skippedRows.value;
    }
    default: {
      return signedRows.value;
    }
  }
});

const rightTableData = computed(() => {
  switch (activeRightTab.value) {
    case 'PAUSED': {
      return pausedRows.value;
    }
    default: {
      return waitSignRows.value;
    }
  }
});

const autoFocusedQueue = computed(() => {
  return (
    currentPatient.value ||
    signedRows.value[0] ||
    waitSignRows.value[0] ||
    pausedRows.value[0] ||
    skippedRows.value[0] ||
    doneRows.value[0] ||
    filteredRows.value[0] ||
    null
  );
});

const focusedQueue = computed(() => {
  const selected = rawQueueData.value.find(
    (item) => item.id === focusedQueueId.value,
  );
  return selected || autoFocusedQueue.value;
});

const bannerPatient = computed(
  () => currentPatient.value || focusedQueue.value || null,
);

const bannerTitle = computed(() => {
  if (currentPatient.value) {
    return `当前患者：${formatCellText(currentPatient.value.patientName)}`;
  }
  if (focusedQueue.value) {
    return `当前聚焦：${formatCellText(focusedQueue.value.patientName)}`;
  }
  return '当前没有患者正在定位';
});

const bannerDesc = computed(() => {
  if (!bannerPatient.value) {
    return '队列监控面板会在签到、叫号或选中列表行后显示当前业务患者。';
  }
  return `${formatCellText(bannerPatient.value.queueNo)} · ${formatCellText(
    bannerPatient.value.ctRoomName,
  )} · ${formatCellText(bannerPatient.value.windowName)}`;
});

const queueMetaDateText = computed(
  () => `排队日期：${formatQueueDate(focusedQueue.value?.queueDate)}`,
);
const queueMetaBizNoText = computed(
  () => `任务单号：${displayText(appointmentDetail.value?.bizNo)}`,
);
const queueMetaCreateTimeText = computed(
  () => `生成时间：${formatDateTime(appointmentDetail.value?.createTime)}`,
);
const approvalProcessNameText = computed(
  () => `流程：${approvalProcessName.value}`,
);
const approvalProcessIdText = computed(
  () => `实例：${approvalProcessId.value}`,
);

const workflowSteps = computed<WorkflowStep[]>(() => {
  if (approvalNodes.value.length > 0) {
    return buildWorkflowStepsFromApprovalNodes(
      'radiotherapy-gz',
      approvalNodes.value,
      modelWorkflowSteps.value,
    ) as WorkflowStep[];
  }
  const baseSteps =
    modelWorkflowSteps.value.length > 0
      ? modelWorkflowSteps.value
      : (buildWorkflowStepsFromApprovalNodes(
          'radiotherapy-gz',
          undefined,
          modelWorkflowSteps.value,
        ) as WorkflowStep[]);
  const currentIndex = findWorkflowStepIndex(
    baseSteps,
    resolveCurrentWorkflowTitle(),
  );
  return baseSteps.map((step, index) => {
    let tone = step.tone;
    if (currentIndex >= 0) {
      if (index < currentIndex) {
        tone = 'done';
      } else if (index === currentIndex) {
        tone = 'current';
      } else {
        tone = 'pending';
      }
    }
    return {
      ...step,
      tone,
    };
  });
});

function displayText(value?: null | number | string) {
  return value === null || value === undefined || `${value}`.trim() === ''
    ? '--'
    : `${value}`;
}

function formatCellText(value?: null | string) {
  return value && `${value}`.trim() ? value : '--';
}

function formatDate(value?: Date | number | string) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '--';
}

function formatQueueDate(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '--';
}

function formatDateTime(value?: Date | number | string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--';
}

function getGenderLabel(value?: number) {
  if (value === 1) return '男';
  if (value === 2) return '女';
  return '--';
}

function getColumnText(record: HospitalCtQueueApi.CtQueue, key: string) {
  const value = (record as Record<string, unknown>)[key];
  if (typeof value === 'string') {
    return formatCellText(value);
  }
  return formatCellText(
    value === null || value === undefined ? undefined : `${value}`,
  );
}

function getStatusLabel(status?: string) {
  return queueStatusLabelMap[status || ''] || '--';
}

function getStatusColor(status?: string) {
  if (status === 'DONE') return 'success';
  if (status === 'SKIPPED') return 'error';
  if (status === 'CALLED') return 'warning';
  if (status === 'QUEUING') return 'processing';
  return 'default';
}

function getAppointmentWorkflowStatusLabel(status?: string) {
  return appointmentWorkflowStatusLabelMap[status || ''] || displayText(status);
}

function canCall(row: HospitalCtQueueApi.CtQueue) {
  return ['QUEUING', 'SKIPPED'].includes(row.queueStatus || '');
}

function canSkip(row: HospitalCtQueueApi.CtQueue) {
  return ['CALLED', 'QUEUING'].includes(row.queueStatus || '');
}

function canFinish(row: HospitalCtQueueApi.CtQueue) {
  return row.queueStatus === 'CALLED';
}

function findWorkflowStepIndex(steps: WorkflowStep[], title: string) {
  if (!title) return -1;
  return steps.findIndex((step) => {
    return step.title.includes(title) || title.includes(step.title);
  });
}

function resolveCurrentWorkflowTitle() {
  const status = focusedQueue.value?.queueStatus;
  if (status === 'DONE') return '靶区勾画';
  if (status === 'CALLED') return '定位执行';
  return 'CT定位';
}

function clearFocusDetail() {
  patientDetail.value = undefined;
  appointmentDetail.value = undefined;
  approvalNodes.value = [];
  approvalProcessName.value = '';
  approvalProcessId.value = '';
}

async function loadModelWorkflowSteps() {
  const workflowRoutePath = getWorkflowRoutePath('radiotherapy-gz');
  if (!workflowRoutePath) {
    modelWorkflowSteps.value = [];
    return;
  }
  try {
    modelWorkflowSteps.value = (await getWorkflowStepsFromModelPath(
      'radiotherapy-gz',
      workflowRoutePath,
    )) as WorkflowStep[];
  } catch {
    modelWorkflowSteps.value = [];
  }
}

async function loadFocusedQueueDetail(row?: HospitalCtQueueApi.CtQueue | null) {
  const requestId = ++detailRequestId.value;
  if (!row) {
    clearFocusDetail();
    return;
  }
  detailLoading.value = true;
  approvalNodes.value = [];
  approvalProcessName.value = '';
  approvalProcessId.value = '';
  try {
    const [appointmentResult, patientResult] = await Promise.allSettled([
      row.appointmentId
        ? getCtAppointment(row.appointmentId)
        : Promise.resolve(undefined),
      row.patientId ? getPatient(row.patientId) : Promise.resolve(undefined),
    ]);

    if (requestId !== detailRequestId.value) {
      return;
    }

    appointmentDetail.value =
      appointmentResult.status === 'fulfilled'
        ? appointmentResult.value
        : undefined;
    patientDetail.value =
      patientResult.status === 'fulfilled' ? patientResult.value : undefined;

    if (!patientDetail.value && appointmentDetail.value?.patientId) {
      patientDetail.value = await getPatient(
        appointmentDetail.value.patientId,
      ).catch(() => undefined);
      if (requestId !== detailRequestId.value) {
        return;
      }
    }

    const processInstanceId =
      appointmentDetail.value?.processInstanceId?.trim();
    if (processInstanceId) {
      const approvalDetail = await getApprovalDetail({
        processInstanceId,
      }).catch(() => undefined);
      if (requestId !== detailRequestId.value) {
        return;
      }
      approvalNodes.value = approvalDetail?.activityNodes || [];
      approvalProcessName.value = approvalDetail?.processInstance?.name || '';
      approvalProcessId.value = `${approvalDetail?.processInstance?.id || processInstanceId}`;
    }
  } finally {
    if (requestId === detailRequestId.value) {
      detailLoading.value = false;
    }
  }
}

async function loadQueueBoard() {
  loading.value = true;
  try {
    const result = await getCtQueuePage({
      pageNo: 1,
      pageSize: 200,
      queueDate: queueDate.value.format('YYYY-MM-DD'),
      ctRoomName: selectedRoom.value || undefined,
    });
    rawQueueData.value = result.list || [];
    if (
      focusedQueueId.value &&
      !rawQueueData.value.some((item) => item.id === focusedQueueId.value)
    ) {
      focusedQueueId.value = undefined;
    }
  } finally {
    loading.value = false;
  }
}

async function handleCall(row: HospitalCtQueueApi.CtQueue) {
  const hideLoading = message.loading({
    content: `正在叫号【${row.queueNo}】`,
    duration: 0,
  });
  try {
    focusedQueueId.value = row.id;
    await callCtQueue(row.id!);
    message.success('叫号成功');
    await loadQueueBoard();
  } finally {
    hideLoading();
  }
}

async function handleSkip(row: HospitalCtQueueApi.CtQueue) {
  const hideLoading = message.loading({
    content: `正在过号【${row.queueNo}】`,
    duration: 0,
  });
  try {
    focusedQueueId.value = row.id;
    await skipCtQueue(row.id!);
    message.success('过号成功');
    await loadQueueBoard();
  } finally {
    hideLoading();
  }
}

async function handleFinish(row: HospitalCtQueueApi.CtQueue) {
  const hideLoading = message.loading({
    content: `正在完成【${row.queueNo}】`,
    duration: 0,
  });
  try {
    focusedQueueId.value = row.id;
    await finishCtQueue(row.id!);
    message.success('完成成功');
    await loadQueueBoard();
  } finally {
    hideLoading();
  }
}

function handleFeatureClick(label: string) {
  message.info(`${label}待接入`);
}

function handleFocusRow(row: HospitalCtQueueApi.CtQueue) {
  focusedQueueId.value = row.id;
}

function openDetailDrawer(row?: HospitalCtQueueApi.CtQueue | null) {
  const target = row || focusedQueue.value;
  if (!target?.id) {
    return;
  }
  focusedQueueId.value = target.id;
  detailVisible.value = true;
}

function buildTableRowProps(record: HospitalCtQueueApi.CtQueue) {
  return {
    class: focusedQueue.value?.id === record.id ? 'queue-row--active' : '',
    onClick: () => handleFocusRow(record),
    onDblclick: () => openDetailDrawer(record),
  };
}

function handleDateChange(value: Dayjs | null) {
  queueDate.value = value || dayjs();
  void loadQueueBoard();
}

function handleRoomChange() {
  void loadQueueBoard();
}

function handleReset() {
  keyword.value = '';
  selectedRoom.value = undefined;
  queueDate.value = dayjs();
  activeLeftTab.value = 'SIGNED';
  activeRightTab.value = 'WAIT_SIGN';
  machineFault.value = false;
  focusedQueueId.value = undefined;
  void loadQueueBoard();
}

watch(
  () =>
    [
      focusedQueue.value?.id,
      focusedQueue.value?.appointmentId,
      focusedQueue.value?.patientId,
    ].join(':'),
  () => {
    void loadFocusedQueueDetail(focusedQueue.value);
  },
  { immediate: true },
);

onMounted(async () => {
  await loadModelWorkflowSteps();
  const focused = await resolveFocusQueue();
  await loadQueueBoard();
  if (focused) {
    await clearFocusId();
  }
});
</script>

<template>
  <Page auto-content-height>
    <div class="ct-queue-board">
      <div class="queue-toolbar card-box">
        <div class="queue-toolbar__left">
          <div class="queue-toolbar__title">CT排队叫号</div>
          <a-select
            v-model:value="selectedRoom"
            allow-clear
            class="w-[160px]"
            placeholder="定位机房"
            size="small"
            @change="handleRoomChange"
          >
            <a-select-option
              v-for="item in roomOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
          <a-button size="small" @click="handleFeatureClick('签名授权')">
            签名授权
          </a-button>
        </div>

        <div class="queue-toolbar__right">
          <a-date-picker
            :value="queueDate"
            size="small"
            @change="handleDateChange"
          />
          <a-input-search
            v-model:value="keyword"
            allow-clear
            class="w-[200px]"
            placeholder="姓名/队列号/机房"
            size="small"
          />
          <a-button size="small" @click="handleFeatureClick('放疗排队信息')">
            放疗排队信息
          </a-button>
          <a-button size="small" @click="handleFeatureClick('叫号屏通知')">
            叫号屏通知
          </a-button>
          <div class="queue-toolbar__switch">
            <span>机房故障</span>
            <a-switch v-model:checked="machineFault" disabled size="small" />
          </div>
          <a-button size="small" @click="loadQueueBoard">刷新</a-button>
          <a-button size="small" @click="handleReset">重置</a-button>
        </div>
      </div>

      <div
        class="queue-current card-box"
        :class="{ 'queue-current--empty': !bannerPatient }"
      >
        <div class="queue-current__avatar">CT</div>
        <div class="min-w-0 flex-1">
          <div class="queue-current__title">{{ bannerTitle }}</div>
          <div class="queue-current__desc">{{ bannerDesc }}</div>
        </div>
        <div v-if="focusedQueue" class="queue-current__actions">
          <a-button size="small" type="link" @click="openDetailDrawer()">
            查看任务单
          </a-button>
        </div>
      </div>

      <div class="queue-content">
        <div class="queue-panel card-box">
          <div class="queue-panel__tabs">
            <button
              v-for="item in leftTabs"
              :key="item.key"
              class="queue-tab"
              :class="[{ 'queue-tab--active': activeLeftTab === item.key }]"
              type="button"
              @click="activeLeftTab = item.key"
            >
              {{ item.label }}
              <span class="queue-tab__count">{{ item.count }}</span>
            </button>
          </div>

          <a-table
            :columns="leftColumns"
            :custom-row="buildTableRowProps"
            :data-source="leftTableData"
            :loading="loading"
            :locale="{ emptyText: '暂无数据' }"
            :pagination="false"
            :scroll="{ x: 980, y: 520 }"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'patientInfo'">
                <div class="queue-patient-cell">
                  <div class="queue-patient-cell__name">
                    {{ formatCellText(record.patientName) }}
                  </div>
                  <div class="queue-patient-cell__sub">
                    {{ formatCellText(record.queueNo) }}
                  </div>
                </div>
              </template>

              <template v-else-if="column.key === 'queueDate'">
                {{ formatQueueDate(record.queueDate) }}
              </template>

              <template v-else-if="column.key === 'signInTime'">
                {{ formatDateTime(record.signInTime) }}
              </template>

              <template v-else-if="column.key === 'queueStatus'">
                <a-tag :color="getStatusColor(record.queueStatus)">
                  {{ getStatusLabel(record.queueStatus) }}
                </a-tag>
              </template>

              <template v-else-if="column.key === 'actions'">
                <div class="queue-actions">
                  <a-button
                    v-if="canCall(record)"
                    size="small"
                    type="link"
                    @click.stop="handleCall(record)"
                  >
                    叫号
                  </a-button>
                  <a-button
                    v-if="canSkip(record)"
                    size="small"
                    type="link"
                    @click.stop="handleSkip(record)"
                  >
                    过号
                  </a-button>
                  <a-button
                    v-if="canFinish(record)"
                    size="small"
                    type="link"
                    @click.stop="handleFinish(record)"
                  >
                    完成
                  </a-button>
                  <span
                    v-if="
                      !canCall(record) && !canSkip(record) && !canFinish(record)
                    "
                  >
                    --
                  </span>
                </div>
              </template>

              <template v-else>
                {{ getColumnText(record, column.key as string) }}
              </template>
            </template>
          </a-table>
        </div>

        <div class="queue-panel card-box">
          <div class="queue-panel__tabs queue-panel__tabs--right">
            <button
              v-for="item in rightTabs"
              :key="item.key"
              class="queue-tab"
              :class="[{ 'queue-tab--active': activeRightTab === item.key }]"
              type="button"
              @click="activeRightTab = item.key"
            >
              {{ item.label }}
              <span class="queue-tab__count">{{ item.count }}</span>
            </button>
          </div>

          <a-table
            :columns="rightColumns"
            :custom-row="buildTableRowProps"
            :data-source="rightTableData"
            :loading="loading"
            :locale="{ emptyText: '暂无数据' }"
            :pagination="false"
            :scroll="{ x: 760, y: 520 }"
            row-key="id"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'patientInfo'">
                <div class="queue-patient-cell">
                  <div class="queue-patient-cell__name">
                    {{ formatCellText(record.patientName) }}
                  </div>
                  <div class="queue-patient-cell__sub">
                    {{ formatCellText(record.queueNo) }}
                  </div>
                </div>
              </template>

              <template v-else-if="column.key === 'queueDate'">
                {{ formatQueueDate(record.queueDate) }}
              </template>

              <template v-else-if="column.key === 'queueStatus'">
                <a-tag :color="getStatusColor(record.queueStatus)">
                  {{ getStatusLabel(record.queueStatus) }}
                </a-tag>
              </template>

              <template v-else-if="column.key === 'actions'">
                <div class="queue-actions">
                  <a-button
                    v-if="canCall(record)"
                    size="small"
                    type="link"
                    @click.stop="handleCall(record)"
                  >
                    叫号
                  </a-button>
                  <span v-else>--</span>
                </div>
              </template>

              <template v-else>
                {{ getColumnText(record, column.key as string) }}
              </template>
            </template>
          </a-table>
        </div>
      </div>

      <a-drawer
        v-model:open="detailVisible"
        :body-style="{ padding: '12px 14px' }"
        class="queue-detail-drawer"
        placement="right"
        title="CT定位排队任务单"
        :width="1180"
      >
        <div
          v-if="focusedQueue"
          class="queue-detail__focus queue-detail__focus--drawer"
        >
          当前聚焦：{{ formatCellText(focusedQueue.patientName) }} /
          {{ formatCellText(focusedQueue.queueNo) }}
        </div>
        <a-spin :spinning="detailLoading">
          <template v-if="focusedQueue">
            <div class="queue-detail__content">
              <div class="queue-sheet">
                <div class="queue-sheet__paper">
                  <div class="queue-sheet__header">
                    <div class="queue-sheet__brand">
                      <div class="queue-sheet__logo">沪</div>
                      <div>
                        <div class="queue-sheet__brand-name">上海XX医院</div>
                        <div class="queue-sheet__brand-subtitle">
                          RADIOTHERAPY CENTER · CT POSITIONING UNIT
                        </div>
                      </div>
                    </div>
                    <a-tag :color="getStatusColor(focusedQueue.queueStatus)">
                      {{ getStatusLabel(focusedQueue.queueStatus) }}
                    </a-tag>
                  </div>

                  <div class="queue-sheet__title">CT定位排队任务单</div>
                  <div class="queue-sheet__meta">
                    <span>{{ queueMetaDateText }}</span>
                    <span>{{ queueMetaBizNoText }}</span>
                    <span>{{ queueMetaCreateTimeText }}</span>
                  </div>

                  <div class="queue-sheet__section">
                    <div class="queue-sheet__section-title">患者信息</div>
                    <div class="queue-sheet__grid queue-sheet__grid--2">
                      <div class="queue-field">
                        <label>患者姓名</label>
                        <span>{{
                          displayText(
                            patientDetail?.name || focusedQueue.patientName,
                          )
                        }}</span>
                      </div>
                      <div class="queue-field">
                        <label>性别 / 年龄</label>
                        <span>
                          {{ getGenderLabel(patientDetail?.gender) }} /
                          {{
                            patientDetail?.age ? `${patientDetail.age}` : '--'
                          }}
                        </span>
                      </div>
                      <div class="queue-field">
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
                      <div class="queue-field">
                        <label>放疗号</label>
                        <span>{{
                          displayText(patientDetail?.radiotherapyNo)
                        }}</span>
                      </div>
                      <div class="queue-field">
                        <label>主管医生</label>
                        <span>
                          {{
                            displayText(
                              patientDetail?.managerDoctorName ||
                                appointmentDetail?.applyDoctorName,
                            )
                          }}
                        </span>
                      </div>
                      <div class="queue-field">
                        <label>联系电话</label>
                        <span>{{
                          displayText(patientDetail?.patientPhone)
                        }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="queue-sheet__section">
                    <div class="queue-sheet__section-title">预约与排队</div>
                    <div class="queue-sheet__grid queue-sheet__grid--2">
                      <div class="queue-field">
                        <label>队列号</label>
                        <span>{{ displayText(focusedQueue.queueNo) }}</span>
                      </div>
                      <div class="queue-field">
                        <label>排队序号</label>
                        <span>{{ displayText(focusedQueue.queueSeq) }}</span>
                      </div>
                      <div class="queue-field">
                        <label>预约日期</label>
                        <span>{{
                          formatDate(appointmentDetail?.appointmentDate)
                        }}</span>
                      </div>
                      <div class="queue-field">
                        <label>预约时段</label>
                        <span>{{
                          displayText(appointmentDetail?.appointmentSlot)
                        }}</span>
                      </div>
                      <div class="queue-field">
                        <label>定位机房</label>
                        <span>
                          {{
                            displayText(
                              appointmentDetail?.ctRoomName ||
                                focusedQueue.ctRoomName,
                            )
                          }}
                        </span>
                      </div>
                      <div class="queue-field">
                        <label>定位设备</label>
                        <span>{{
                          displayText(appointmentDetail?.ctDeviceName)
                        }}</span>
                      </div>
                      <div class="queue-field">
                        <label>叫号屏</label>
                        <span>{{ displayText(focusedQueue.windowName) }}</span>
                      </div>
                      <div class="queue-field">
                        <label>流程状态</label>
                        <span>
                          {{
                            getAppointmentWorkflowStatusLabel(
                              appointmentDetail?.workflowStatus,
                            )
                          }}
                        </span>
                      </div>
                      <div class="queue-field">
                        <label>报到时间</label>
                        <span>{{
                          formatDateTime(focusedQueue.signInTime)
                        }}</span>
                      </div>
                      <div class="queue-field">
                        <label>叫号时间</label>
                        <span>{{ formatDateTime(focusedQueue.callTime) }}</span>
                      </div>
                      <div class="queue-field">
                        <label>完成时间</label>
                        <span>{{
                          formatDateTime(focusedQueue.finishTime)
                        }}</span>
                      </div>
                      <div class="queue-field">
                        <label>实例编号</label>
                        <span>{{
                          displayText(
                            approvalProcessId ||
                              appointmentDetail?.processInstanceId,
                          )
                        }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="queue-sheet__section">
                    <div class="queue-sheet__section-title">定位备注</div>
                    <div class="queue-sheet__note">
                      {{
                        displayText(
                          appointmentDetail?.positionNote ||
                            focusedQueue.remark,
                        )
                      }}
                    </div>
                  </div>
                </div>
              </div>

              <aside class="queue-timeline">
                <div class="queue-timeline__title">流程进度</div>
                <div
                  v-if="approvalProcessName || approvalProcessId"
                  class="queue-timeline__summary"
                >
                  <span v-if="approvalProcessName">{{
                    approvalProcessNameText
                  }}</span>
                  <span v-if="approvalProcessId">{{
                    approvalProcessIdText
                  }}</span>
                </div>
                <div
                  v-for="(item, index) in workflowSteps"
                  :key="item.key"
                  class="queue-timeline__item"
                  :class="[`is-${item.tone}`]"
                >
                  <div
                    v-if="index !== workflowSteps.length - 1"
                    class="queue-timeline__line"
                  ></div>
                  <div class="queue-timeline__dot"></div>
                  <div class="queue-timeline__text">
                    <div class="queue-timeline__head">
                      <div class="queue-timeline__name">{{ item.title }}</div>
                      <span
                        v-if="item.statusText"
                        class="queue-timeline__badge"
                      >
                        {{ item.statusText }}
                      </span>
                    </div>
                    <div class="queue-timeline__desc">{{ item.desc }}</div>
                    <div v-if="item.actorText" class="queue-timeline__meta">
                      {{ item.actorText }}
                    </div>
                    <div v-if="item.timeText" class="queue-timeline__meta">
                      {{ item.timeText }}
                    </div>
                    <div
                      v-if="item.reasonText"
                      class="queue-timeline__meta queue-timeline__meta--reason"
                    >
                      {{ item.reasonText }}
                    </div>
                  </div>
                </div>
              </aside>
            </div>
          </template>
          <a-empty v-else description="暂无队列数据" />
        </a-spin>
      </a-drawer>
    </div>
  </Page>
</template>

<style scoped>
.ct-queue-board {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.queue-toolbar,
.queue-current,
.queue-panel,
.queue-detail {
  background: #fff;
  border: 1px solid #e7edf5;
  border-radius: 4px;
}

.queue-toolbar {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: space-between;
  padding: 8px 10px;
}

.queue-toolbar__left,
.queue-toolbar__right {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.queue-toolbar__title {
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
  line-height: 1;
  margin-right: 4px;
}

.queue-toolbar__switch {
  align-items: center;
  color: #606266;
  display: flex;
  font-size: 12px;
  gap: 8px;
}

.queue-current {
  align-items: center;
  background: linear-gradient(180deg, #f4fbff 0%, #eef8ff 100%);
  border-color: #b6d8f4;
  display: flex;
  gap: 12px;
  min-height: 74px;
  padding: 10px 12px;
}

.queue-current--empty {
  color: #9aa4b2;
}

.queue-current__avatar {
  align-items: center;
  background: #d9edf9;
  border-radius: 10px;
  color: #5793bf;
  display: flex;
  font-size: 20px;
  font-weight: 700;
  height: 46px;
  justify-content: center;
  width: 60px;
}

.queue-current__title {
  color: #4b5563;
  font-size: 13px;
  font-weight: 600;
}

.queue-current__desc {
  color: #8f99aa;
  font-size: 12px;
  line-height: 1.5;
  margin-top: 2px;
}

.queue-current__actions {
  display: flex;
  flex: 0 0 auto;
  justify-content: flex-end;
}

.queue-detail__focus--drawer {
  margin-bottom: 10px;
}

.queue-detail-drawer :deep(.ant-drawer-header) {
  padding: 10px 14px;
}

.queue-detail-drawer :deep(.ant-drawer-body) {
  background: #f7f9fc;
}

.queue-content {
  display: grid;
  align-items: stretch;
  gap: 8px;
  grid-template-columns: minmax(0, 1.12fr) minmax(0, 0.88fr);
}

.queue-panel {
  display: flex;
  flex-direction: column;
  min-height: 620px;
  overflow: hidden;
}

.queue-panel__tabs {
  align-items: center;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  gap: 2px;
  padding: 0 8px;
}

.queue-panel__tabs--right {
  justify-content: flex-start;
}

.queue-tab {
  background: transparent;
  border: 0;
  border-bottom: 2px solid transparent;
  color: #7c8aa5;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  padding: 8px 8px 6px;
}

.queue-tab--active {
  border-bottom-color: #61a9ec;
  color: #61a9ec;
}

.queue-tab__count {
  margin-left: 4px;
}

.queue-patient-cell__name {
  color: #303133;
  font-weight: 600;
}

.queue-patient-cell__sub {
  color: #a8b1bf;
  font-size: 12px;
  margin-top: 4px;
}

.queue-actions {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.queue-detail {
  padding: 12px;
}

.queue-detail__header {
  align-items: center;
  display: flex;
  gap: 12px;
  justify-content: space-between;
  margin-bottom: 12px;
}

.queue-detail__title {
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
}

.queue-detail__subtitle,
.queue-detail__focus,
.queue-field label,
.queue-timeline__desc,
.queue-sheet__brand-subtitle,
.queue-sheet__meta,
.queue-timeline__meta {
  color: #8a94a6;
  font-size: 12px;
}

.queue-detail__content {
  display: grid;
  gap: 12px;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 0.8fr);
}

.queue-sheet__paper,
.queue-timeline {
  background: #fff;
  border: 1px solid #edf1f6;
  border-radius: 4px;
}

.queue-sheet__paper {
  min-height: 100%;
  padding: 18px 20px;
}

.queue-sheet__header {
  align-items: center;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  justify-content: space-between;
  padding-bottom: 12px;
}

.queue-sheet__brand {
  align-items: center;
  display: flex;
  gap: 12px;
}

.queue-sheet__logo {
  align-items: center;
  background: linear-gradient(135deg, #66aee8 0%, #3f86d6 100%);
  border-radius: 10px;
  color: #fff;
  display: flex;
  font-size: 16px;
  font-weight: 700;
  height: 42px;
  justify-content: center;
  width: 42px;
}

.queue-sheet__brand-name {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
}

.queue-sheet__title {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-top: 18px;
  text-align: center;
}

.queue-sheet__meta {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
  margin-top: 8px;
}

.queue-sheet__section {
  margin-top: 18px;
}

.queue-sheet__section-title {
  border-left: 3px solid #5ba3e5;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 10px;
  padding-left: 8px;
}

.queue-sheet__grid {
  display: grid;
  gap: 10px 16px;
}

.queue-sheet__grid--2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.queue-field {
  align-items: flex-start;
  display: flex;
  gap: 12px;
  min-width: 0;
}

.queue-field label {
  flex: 0 0 84px;
  margin-top: 1px;
}

.queue-field span,
.queue-sheet__note {
  color: #1f2937;
  flex: 1;
  line-height: 1.7;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.queue-sheet__note {
  background: #f8fafc;
  border: 1px solid #edf2f7;
  border-radius: 4px;
  min-height: 68px;
  padding: 10px 12px;
}

.queue-timeline {
  padding: 12px 12px 8px;
}

.queue-timeline__title {
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
}

.queue-timeline__summary {
  display: flex;
  flex-direction: column;
  flex-wrap: wrap;
  gap: 0;
  margin-top: 8px;
}

.queue-timeline__item {
  display: flex;
  gap: 8px;
  padding: 12px 0 0;
  position: relative;
}

.queue-timeline__line {
  background: #dbe5f0;
  left: 7px;
  position: absolute;
  top: 24px;
  width: 1px;
  bottom: -2px;
}

.queue-timeline__dot {
  background: #c3cedb;
  border-radius: 999px;
  flex: 0 0 12px;
  height: 12px;
  margin-top: 2px;
  position: relative;
  z-index: 1;
}

.queue-timeline__text {
  min-width: 0;
}

.queue-timeline__head {
  align-items: center;
  display: flex;
  gap: 8px;
  justify-content: space-between;
}

.queue-timeline__name {
  color: #1f2937;
  font-size: 13px;
  font-weight: 700;
}

.queue-timeline__badge {
  background: #eef5ff;
  border-radius: 999px;
  color: #3978d6;
  font-size: 10px;
  line-height: 1;
  padding: 4px 8px;
}

.queue-timeline__desc {
  line-height: 1.45;
  margin-top: 3px;
}

.queue-timeline__meta {
  line-height: 1.4;
  margin-top: 1px;
}

.queue-timeline__meta--reason {
  color: #d97706;
}

.queue-timeline__item.is-current .queue-timeline__dot {
  background: #5ba3e5;
  box-shadow: 0 0 0 4px rgb(91 163 229 / 15%);
}

.queue-timeline__item.is-done .queue-timeline__dot {
  background: #3fb27f;
}

.queue-timeline__item.is-current .queue-timeline__name,
.queue-timeline__item.is-done .queue-timeline__name {
  color: #0f172a;
}

.queue-panel :deep(.ant-spin-nested-loading),
.queue-panel :deep(.ant-spin-container),
.queue-panel :deep(.ant-table-wrapper) {
  display: flex;
  flex: 1;
  flex-direction: column;
}

.queue-panel :deep(.ant-btn-link) {
  min-height: auto;
  padding-inline: 4px;
}

.ct-queue-board :deep(.ant-input),
.ct-queue-board :deep(.ant-input-affix-wrapper),
.ct-queue-board :deep(.ant-picker),
.ct-queue-board :deep(.ant-select-selector),
.ct-queue-board :deep(.ant-btn) {
  border-radius: 6px;
}

.ct-queue-board :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  color: #606266;
  font-size: 12px;
  font-weight: 600;
  padding: 7px 8px;
}

.ct-queue-board :deep(.ant-table-tbody > tr > td) {
  font-size: 12px;
  padding: 7px 8px;
}

.ct-queue-board :deep(.ant-table-tbody > tr) {
  cursor: pointer;
}

.ct-queue-board :deep(.queue-row--active > td) {
  background: #eef6ff !important;
}

@media (max-width: 1480px) {
  .queue-detail__content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1280px) {
  .queue-content,
  .queue-sheet__grid--2 {
    grid-template-columns: 1fr;
  }

  .queue-field {
    flex-direction: column;
    gap: 4px;
  }

  .queue-field label {
    flex: none;
  }
}
</style>
