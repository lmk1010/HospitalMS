<!-- eslint-disable vue/html-closing-bracket-newline -->
<script lang="ts" setup>
import type { TableColumnsType } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';

import type { HospitalTreatmentAppointmentApi } from '#/api/hospital/treatment-appointment';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';

import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';

import { ACTION_ICON, TableAction } from '#/adapter/vxe-table';
import {
  deleteTreatmentAppointment,
  getTreatmentAppointment,
  getTreatmentAppointmentScheduleList,
  submitTreatmentAppointment,
} from '#/api/hospital/treatment-appointment';

import Form from './modules/form.vue';

type ViewMode = 'day' | 'list' | 'week';

type NormalizedAppointment =
  HospitalTreatmentAppointmentApi.TreatmentAppointment & {
    __dateKey: string;
    __dateValue: Dayjs;
  };

type ScheduleRow = {
  appointment: NormalizedAppointment;
  timeLabel: string;
};

const weekdayLabels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
const boardStartTime = dayjs('2026-01-01 07:00');
const halfHourRows = Array.from({ length: 32 }, (_, index) =>
  boardStartTime.add(index * 30, 'minute'),
);
const boardHeight = 32 * 24;
const listColumns: TableColumnsType<HospitalTreatmentAppointmentApi.TreatmentAppointment> =
  [
    {
      title: '治疗日期',
      dataIndex: 'appointmentDate',
      key: 'appointmentDate',
      width: 110,
    },
    { title: '患者', dataIndex: 'patientName', key: 'patientName', width: 120 },
    { title: '分次', dataIndex: 'fractionNo', key: 'fractionNo', width: 84 },
    {
      title: '治疗室',
      dataIndex: 'treatmentRoomName',
      key: 'treatmentRoomName',
      width: 120,
    },
    { title: '设备', dataIndex: 'deviceName', key: 'deviceName', width: 140 },
    { title: '预约医生', dataIndex: 'doctorName', key: 'doctorName', width: 100 },
    { title: '状态', key: 'workflowStatus', width: 96 },
    { title: '备注', dataIndex: 'remark', key: 'remark', width: 240 },
    { title: '操作', key: 'actions', fixed: 'right', width: 170 },
  ];

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const loading = ref(false);
const keyword = ref('');
const rangeValue = ref<[Dayjs, Dayjs]>(getWeekRange(dayjs()));
const scheduleData = ref<HospitalTreatmentAppointmentApi.TreatmentAppointment[]>([]);
const selectedRoom = ref<string>();
const viewMode = ref<ViewMode>('week');
const route = useRoute();
const router = useRouter();

function getWeekRange(base: Dayjs): [Dayjs, Dayjs] {
  const weekday = base.day();
  const offset = weekday === 0 ? -6 : 1 - weekday;
  const start = base.add(offset, 'day').startOf('day');
  return [start, start.add(6, 'day').startOf('day')];
}

function normalizeDateValue(value: unknown) {
  if (dayjs.isDayjs(value)) {
    return value;
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value.map((item) => Number(item) || 0);
    return dayjs(
      `${year}-${`${month}`.padStart(2, '0')}-${`${day}`.padStart(2, '0')}`,
    );
  }
  if (value instanceof Date) {
    return dayjs(value);
  }
  if (typeof value === 'string' && value.trim()) {
    return dayjs(value);
  }
  return dayjs();
}

function formatCellText(value?: null | number | string) {
  return value && `${value}`.trim() ? `${value}` : '--';
}

function formatDateLabel(value: Dayjs) {
  return `${weekdayLabels[value.day()]} (${value.format('M月D日')})`;
}

function isToday(value: Dayjs) {
  return value.isSame(dayjs(), 'day');
}

function isWeekend(value: Dayjs) {
  return value.day() === 0 || value.day() === 6;
}

function getStatusLabel(
  row: HospitalTreatmentAppointmentApi.TreatmentAppointment,
) {
  if (row.status === 1) {
    return '已停用';
  }
  if (row.workflowStatus === 'TREATING') {
    return '已排程';
  }
  return '待预约';
}

function getStatusTagColor(
  row: HospitalTreatmentAppointmentApi.TreatmentAppointment,
) {
  if (row.status === 1) {
    return 'default';
  }
  return row.workflowStatus === 'TREATING' ? 'green' : 'processing';
}

function getStatusClass(
  row: HospitalTreatmentAppointmentApi.TreatmentAppointment,
) {
  if (row.status === 1) {
    return 'disabled';
  }
  return row.workflowStatus === 'TREATING' ? 'scheduled' : 'pending';
}

function formatFraction(value?: number) {
  return `第${value && value > 0 ? value : 1}次`;
}

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

async function applyFocusAppointment() {
  const focusId = getFocusId();
  if (!focusId) return undefined;
  const appointment = await getTreatmentAppointment(focusId).catch(
    () => undefined,
  );
  if (!appointment) return undefined;
  if (appointment.appointmentDate) {
    rangeValue.value = getWeekRange(normalizeDateValue(appointment.appointmentDate));
  }
  selectedRoom.value = appointment.treatmentRoomName || undefined;
  return appointment;
}

const normalizedAppointments = computed<NormalizedAppointment[]>(() => {
  return scheduleData.value
    .map((item) => {
      const dateValue = normalizeDateValue(item.appointmentDate);
      return {
        ...item,
        __dateKey: dateValue.format('YYYY-MM-DD'),
        __dateValue: dateValue,
      };
    })
    .toSorted((left, right) => {
      const dateCompare = left.__dateValue.valueOf() - right.__dateValue.valueOf();
      if (dateCompare !== 0) {
        return dateCompare;
      }
      const roomCompare = `${left.treatmentRoomName || ''}`.localeCompare(
        `${right.treatmentRoomName || ''}`,
        'zh-CN',
      );
      if (roomCompare !== 0) {
        return roomCompare;
      }
      const fractionCompare = (left.fractionNo || 0) - (right.fractionNo || 0);
      if (fractionCompare !== 0) {
        return fractionCompare;
      }
      return (left.id || 0) - (right.id || 0);
    });
});

const roomOptions = computed(() => {
  const rooms = new Set<string>();
  for (const item of normalizedAppointments.value) {
    if (item.treatmentRoomName) {
      rooms.add(item.treatmentRoomName);
    }
  }
  return [...rooms]
    .toSorted((left, right) => left.localeCompare(right, 'zh-CN'))
    .map((item) => ({ label: item, value: item }));
});

const visibleDays = computed(() => {
  const [start, end] = rangeValue.value;
  const days: Dayjs[] = [];
  let cursor = start.startOf('day');
  const safeEnd = end.startOf('day');
  while (cursor.isBefore(safeEnd) || cursor.isSame(safeEnd, 'day')) {
    days.push(cursor);
    cursor = cursor.add(1, 'day');
  }
  return viewMode.value === 'day' ? [start.startOf('day')] : days;
});

const dayGridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${Math.max(visibleDays.value.length, 1)}, minmax(${viewMode.value === 'day' ? 420 : 240}px, 1fr))`,
}));

const groupedAppointments = computed(() => {
  const map = new Map<string, NormalizedAppointment[]>();
  for (const item of normalizedAppointments.value) {
    const current = map.get(item.__dateKey) || [];
    current.push(item);
    map.set(item.__dateKey, current);
  }
  return map;
});

const scheduleRowsMap = computed(() => {
  const map = new Map<string, ScheduleRow[]>();
  for (const day of visibleDays.value) {
    const dayKey = day.format('YYYY-MM-DD');
    const rows = (groupedAppointments.value.get(dayKey) || []).map(
      (appointment, index) => ({
        appointment,
        timeLabel: boardStartTime.add((index + 2) * 20, 'minute').format('HH:mm'),
      }),
    );
    map.set(dayKey, rows);
  }
  return map;
});

const scheduledCount = computed(() => {
  return normalizedAppointments.value.filter(
    (item) => item.workflowStatus === 'TREATING' && item.status !== 1,
  ).length;
});

const pendingCount = computed(() => {
  return normalizedAppointments.value.filter(
    (item) => item.workflowStatus !== 'TREATING' && item.status !== 1,
  ).length;
});

function getDayRows(day: Dayjs) {
  return scheduleRowsMap.value.get(day.format('YYYY-MM-DD')) || [];
}

function getColumnText(
  record: HospitalTreatmentAppointmentApi.TreatmentAppointment,
  key: string,
) {
  const value = (record as Record<string, unknown>)[key];
  if (key === 'appointmentDate') {
    return normalizeDateValue(value).format('YYYY-MM-DD');
  }
  if (typeof value === 'string') {
    return formatCellText(value);
  }
  return formatCellText(
    value === null || value === undefined ? undefined : `${value}`,
  );
}

function getAxisLabel(value: Dayjs, index: number) {
  return index % 2 === 0 ? value.format('HH:mm') : '';
}

async function loadSchedule() {
  loading.value = true;
  try {
    scheduleData.value = await getTreatmentAppointmentScheduleList({
      endDate: rangeValue.value[1].format('YYYY-MM-DD'),
      keyword: keyword.value.trim() || undefined,
      startDate: rangeValue.value[0].format('YYYY-MM-DD'),
      treatmentRoomName: selectedRoom.value || undefined,
    });
  } finally {
    loading.value = false;
  }
}

function handleCreate(
  payload?: Partial<HospitalTreatmentAppointmentApi.TreatmentAppointment>,
) {
  formModalApi.setData(payload || null).open();
}

function handleEdit(row: HospitalTreatmentAppointmentApi.TreatmentAppointment) {
  formModalApi.setData(row).open();
}

async function handleSubmit(
  row: HospitalTreatmentAppointmentApi.TreatmentAppointment,
) {
  const hideLoading = message.loading({
    content: `正在提交治疗预约【${row.bizNo}】`,
    duration: 0,
  });
  try {
    await submitTreatmentAppointment(row.id!);
    message.success('提交成功');
    await loadSchedule();
  } finally {
    hideLoading();
  }
}

function handleDelete(
  row: HospitalTreatmentAppointmentApi.TreatmentAppointment,
) {
  Modal.confirm({
    centered: true,
    title: `确认删除治疗预约【${row.patientName || row.bizNo}】吗？`,
    async onOk() {
      await deleteTreatmentAppointment(row.id!);
      message.success('删除成功');
      await loadSchedule();
    },
  });
}

function handleModeChange(mode: ViewMode) {
  viewMode.value = mode;
  const baseDate = rangeValue.value[0];
  if (mode === 'day') {
    rangeValue.value = [baseDate.startOf('day'), baseDate.startOf('day')];
  }
  if (
    mode === 'week' &&
    rangeValue.value[0].isSame(rangeValue.value[1], 'day')
  ) {
    rangeValue.value = getWeekRange(baseDate);
  }
  void loadSchedule();
}

function handleRangeChange(values: [Dayjs, Dayjs] | null) {
  if (!values || !values[0] || !values[1]) {
    return;
  }
  rangeValue.value =
    viewMode.value === 'day'
      ? [values[0].startOf('day'), values[0].startOf('day')]
      : [values[0].startOf('day'), values[1].startOf('day')];
  void loadSchedule();
}

function shiftRange(step: number) {
  const [start, end] = rangeValue.value;
  const span = Math.max(end.diff(start, 'day') + 1, 1);
  rangeValue.value = [
    start.add(step * span, 'day').startOf('day'),
    end.add(step * span, 'day').startOf('day'),
  ];
  void loadSchedule();
}

function handleToday() {
  rangeValue.value =
    viewMode.value === 'day'
      ? [dayjs().startOf('day'), dayjs().startOf('day')]
      : getWeekRange(dayjs());
  void loadSchedule();
}

function handleSearch() {
  void loadSchedule();
}

function handleReset() {
  keyword.value = '';
  selectedRoom.value = undefined;
  viewMode.value = 'week';
  rangeValue.value = getWeekRange(dayjs());
  void loadSchedule();
}

function handleExport() {
  if (normalizedAppointments.value.length === 0) {
    message.warning('当前没有可导出的预约数据');
    return;
  }
  const headers = [
    '治疗日期',
    '患者',
    '分次',
    '治疗室',
    '设备',
    '预约医生',
    '状态',
    '备注',
  ];
  const rows = normalizedAppointments.value.map((item) => [
    item.__dateValue.format('YYYY-MM-DD'),
    item.patientName || '',
    item.fractionNo || '',
    item.treatmentRoomName || '',
    item.deviceName || '',
    item.doctorName || '',
    getStatusLabel(item),
    item.remark || '',
  ]);
  const csvContent = [headers, ...rows]
    .map((row) =>
      row.map((cell) => `"${`${cell ?? ''}`.replaceAll('"', '""')}"`).join(','),
    )
    .join('\n');
  const blob = new Blob([`\uFEFF${csvContent}`], {
    type: 'text/csv;charset=utf-8;',
  });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `治疗预约排班_${rangeValue.value[0].format('YYYYMMDD')}_${rangeValue.value[1].format('YYYYMMDD')}.csv`;
  link.click();
  window.URL.revokeObjectURL(url);
}

onMounted(async () => {
  const focusedAppointment = await applyFocusAppointment();
  await loadSchedule();
  if (focusedAppointment?.id) {
    const currentAppointment =
      scheduleData.value.find((item) => item.id === focusedAppointment.id) ||
      focusedAppointment;
    formModalApi.setData(currentAppointment).open();
    await clearFocusId();
  }
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="loadSchedule" />
    <div class="treatment-appointment-page">
      <div class="card-box rounded-xl p-3">
        <div class="toolbar-panel">
          <div class="toolbar-main">
            <div class="toolbar-title-group">
              <div class="toolbar-title">治疗预约</div>
              <div class="toolbar-subtitle">周视图排班 · 真实业务预约联动</div>
            </div>
            <div class="toolbar-stats">
              <div class="stat-chip">
                <span>待预约</span>
                <strong>{{ pendingCount }}</strong>
              </div>
              <div class="stat-chip stat-chip--green">
                <span>已排程</span>
                <strong>{{ scheduledCount }}</strong>
              </div>
              <div class="stat-chip stat-chip--slate">
                <span>总数</span>
                <strong>{{ normalizedAppointments.length }}</strong>
              </div>
            </div>
          </div>

          <div class="toolbar-room-row">
            <div class="toolbar-room-label">治疗室</div>
            <a-select
              v-model:value="selectedRoom"
              allow-clear
              class="toolbar-room-select"
              placeholder="全部治疗室"
              size="middle"
              @change="loadSchedule"
            >
              <a-select-option
                v-for="item in roomOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </a-select-option>
            </a-select>
            <div class="toolbar-room-tip">
              点击排班条目即可编辑，提交后自动进入治疗排队
            </div>
          </div>

          <div class="toolbar-grid flex flex-wrap items-center gap-2">
            <div class="mode-switch flex items-center rounded-lg bg-muted p-1">
              <a-button
                :type="viewMode === 'week' ? 'primary' : 'text'"
                size="small"
                @click="handleModeChange('week')"
              >
                周
              </a-button>
              <a-button
                :type="viewMode === 'day' ? 'primary' : 'text'"
                size="small"
                @click="handleModeChange('day')"
              >
                日
              </a-button>
              <a-button
                :type="viewMode === 'list' ? 'primary' : 'text'"
                size="small"
                @click="handleModeChange('list')"
              >
                列表
              </a-button>
            </div>

            <a-button size="middle" @click="shiftRange(-1)">‹</a-button>
            <a-range-picker :value="rangeValue" size="middle" @change="handleRangeChange" />
            <a-button size="middle" @click="shiftRange(1)">›</a-button>
            <a-button size="middle" @click="handleToday">今天</a-button>

            <a-input-search
              v-model:value="keyword"
              allow-clear
              class="min-w-[240px] flex-1"
              placeholder="姓名/预约单号/医生/治疗室/设备"
              size="middle"
              @search="handleSearch"
            />
            <a-button size="middle" @click="handleSearch">刷新资源</a-button>
            <a-button size="middle" @click="handleReset">重置筛选</a-button>
            <a-button size="middle" type="primary" @click="handleCreate()">
              新增预约
            </a-button>
            <a-button size="middle" @click="handleExport">导出Excel</a-button>
          </div>
        </div>
      </div>

      <div v-if="viewMode === 'list'" class="card-box rounded-xl p-2">
        <a-table
          :columns="listColumns"
          :data-source="normalizedAppointments"
          :loading="loading"
          :pagination="false"
          :scroll="{ x: 1360 }"
          :locale="{ emptyText: '当前时间范围暂无治疗预约' }"
          row-key="id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'workflowStatus'">
              <a-tag :color="getStatusTagColor(record)">
                {{ getStatusLabel(record) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'actions'">
              <TableAction
                :actions="[
                  {
                    label: '编辑',
                    type: 'link',
                    icon: ACTION_ICON.EDIT,
                    auth: ['hospital:treatment-appointment:update'],
                    ifShow: record.workflowStatus !== 'TREATING',
                    onClick: handleEdit.bind(null, record),
                  },
                  {
                    label: '提交',
                    type: 'link',
                    auth: ['hospital:treatment-appointment:submit'],
                    ifShow:
                      record.workflowStatus !== 'TREATING' &&
                      record.status !== 1,
                    onClick: handleSubmit.bind(null, record),
                  },
                  {
                    label: '删除',
                    type: 'link',
                    danger: true,
                    icon: ACTION_ICON.DELETE,
                    auth: ['hospital:treatment-appointment:delete'],
                    ifShow: record.workflowStatus !== 'TREATING',
                    onClick: handleDelete.bind(null, record),
                  },
                ]"
              />
            </template>
            <template v-else>
              {{ getColumnText(record, column.key as string) }}
            </template>
          </template>
        </a-table>
      </div>

      <div v-else class="card-box rounded-xl p-0">
        <div class="schedule-shell overflow-x-auto">
          <div class="schedule-grid min-w-[1320px]">
            <div class="schedule-corner"></div>
            <div class="schedule-day-heads" :style="dayGridStyle">
              <div
                v-for="day in visibleDays"
                :key="day.format('YYYY-MM-DD')"
                class="day-head"
                :class="[
                  { 'day-head--today': isToday(day) },
                  { 'day-head--weekend': isWeekend(day) },
                ]"
              >
                <div class="day-head__title">{{ formatDateLabel(day) }}</div>
                <div class="day-head__count">{{ getDayRows(day).length }} 项预约</div>
              </div>
            </div>
            <div class="schedule-corner schedule-corner--right"></div>

            <div class="time-axis">
              <div
                v-for="(time, index) in halfHourRows"
                :key="`left-${time.valueOf()}`"
                class="time-axis-cell"
              >
                {{ getAxisLabel(time, index) }}
              </div>
            </div>

            <div class="schedule-day-columns" :style="dayGridStyle">
              <div
                v-for="day in visibleDays"
                :key="`${day.format('YYYY-MM-DD')}-column`"
                class="day-column"
                :class="[
                  { 'day-column--today': isToday(day) },
                  { 'day-column--weekend': isWeekend(day) },
                ]"
              >
                <div class="day-track" :style="{ height: `${boardHeight}px` }">
                  <div v-if="getDayRows(day).length === 0" class="day-empty">
                    当前日期暂无治疗预约
                  </div>
                  <div v-else class="day-list">
                    <div
                      v-for="row in getDayRows(day)"
                      :key="row.appointment.id"
                      class="schedule-row"
                      :class="`schedule-row--${getStatusClass(row.appointment)}`"
                      @click="handleEdit(row.appointment)"
                    >
                      <div class="schedule-row__time">{{ row.timeLabel }}</div>
                      <div class="schedule-row__main">
                        <div class="schedule-row__line">
                          <span class="schedule-row__patient">
                            {{ formatCellText(row.appointment.patientName) }}
                          </span>
                          <span class="schedule-row__room">
                            {{ formatCellText(row.appointment.treatmentRoomName) }}
                          </span>
                        </div>
                        <div class="schedule-row__meta">
                          {{ formatCellText(row.appointment.doctorName) }}
                          <span>·</span>
                          {{ formatCellText(row.appointment.deviceName) }}
                          <span>·</span>
                          {{ formatFraction(row.appointment.fractionNo) }}
                          <span>·</span>
                          {{ formatCellText(row.appointment.bizNo) }}
                        </div>
                      </div>
                      <div class="schedule-row__side">
                        <a-button
                          v-if="
                            row.appointment.workflowStatus !== 'TREATING' &&
                            row.appointment.status !== 1
                          "
                          size="small"
                          type="link"
                          @click.stop="handleSubmit(row.appointment)"
                        >
                          提交
                        </a-button>
                        <span v-else class="schedule-row__state">
                          {{ getStatusLabel(row.appointment) }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="time-axis time-axis--right">
              <div
                v-for="(time, index) in halfHourRows"
                :key="`right-${time.valueOf()}`"
                class="time-axis-cell"
              >
                {{ getAxisLabel(time, index) }}
              </div>
            </div>
          </div>
        </div>

        <div class="legend-bar flex items-center justify-end gap-5 px-4 py-3 text-xs text-foreground/55">
          <div class="flex items-center gap-2">
            <span class="legend-dot legend-dot--pending"></span>
            <span>待预约</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="legend-dot legend-dot--scheduled"></span>
            <span>已排程</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="legend-dot legend-dot--disabled"></span>
            <span>已停用</span>
          </div>
        </div>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.treatment-appointment-page :deep(.ant-input),
.treatment-appointment-page :deep(.ant-input-affix-wrapper),
.treatment-appointment-page :deep(.ant-picker),
.treatment-appointment-page :deep(.ant-select-selector),
.treatment-appointment-page :deep(.ant-btn) {
  border-radius: 8px;
}

.treatment-appointment-page :deep(.ant-table-thead > tr > th) {
  padding: 8px 10px;
  white-space: nowrap;
}

.treatment-appointment-page :deep(.ant-table-tbody > tr > td) {
  padding: 8px 10px;
}

.treatment-appointment-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-main {
  align-items: flex-start;
  display: flex;
  gap: 12px;
  justify-content: space-between;
}

.toolbar-title-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toolbar-title {
  color: rgb(31 41 55);
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.toolbar-subtitle,
.toolbar-room-tip {
  color: rgb(148 163 184);
  font-size: 12px;
}

.toolbar-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.stat-chip {
  align-items: center;
  background: rgb(248 250 252);
  border: 1px solid rgb(226 232 240);
  border-radius: 999px;
  color: rgb(71 85 105);
  display: inline-flex;
  gap: 8px;
  padding: 4px 12px;
}

.stat-chip strong {
  color: rgb(15 23 42);
  font-size: 16px;
  font-weight: 700;
}

.stat-chip--green {
  background: rgb(240 253 244);
  border-color: rgb(187 247 208);
}

.stat-chip--slate {
  background: rgb(248 250 252);
}

.toolbar-room-row {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-room-label {
  color: rgb(71 85 105);
  flex: none;
  font-size: 13px;
  font-weight: 600;
}

.toolbar-room-select {
  width: 220px;
}

.schedule-grid {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) 58px;
}

.schedule-corner {
  background: rgb(250 250 250);
  border-bottom: 1px solid rgb(235 238 245);
  border-right: 1px solid rgb(235 238 245);
  min-height: 52px;
}

.schedule-corner--right {
  border-left: 1px solid rgb(235 238 245);
  border-right: none;
}

.schedule-day-heads,
.schedule-day-columns {
  display: grid;
}

.day-head {
  background: rgb(250 250 250);
  border-bottom: 1px solid rgb(235 238 245);
  border-right: 1px solid rgb(235 238 245);
  min-height: 52px;
  padding: 8px 12px;
}

.day-head__title {
  color: rgb(30 41 59);
  font-size: 13px;
  font-weight: 700;
}

.day-head__count {
  color: rgb(148 163 184);
  font-size: 12px;
  margin-top: 4px;
}

.day-head--today {
  background: rgb(239 246 255);
}

.day-head--weekend {
  background-image: repeating-linear-gradient(
    -45deg,
    rgba(241, 245, 249, 0.7),
    rgba(241, 245, 249, 0.7) 10px,
    rgba(255, 255, 255, 0.9) 10px,
    rgba(255, 255, 255, 0.9) 20px
  );
}

.time-axis {
  border-right: 1px solid rgb(235 238 245);
}

.time-axis--right {
  border-left: 1px solid rgb(235 238 245);
  border-right: none;
}

.time-axis-cell {
  border-bottom: 1px solid rgb(244 246 248);
  color: rgb(148 163 184);
  font-size: 11px;
  height: 24px;
  line-height: 24px;
  padding-right: 8px;
  text-align: right;
}

.time-axis--right .time-axis-cell {
  padding-left: 8px;
  padding-right: 0;
  text-align: left;
}

.day-column {
  border-right: 1px solid rgb(235 238 245);
}

.day-column--today {
  background: rgb(248 252 255);
}

.day-column--weekend .day-track {
  background-image:
    repeating-linear-gradient(
      -45deg,
      rgba(248, 250, 252, 0.45),
      rgba(248, 250, 252, 0.45) 12px,
      rgba(255, 255, 255, 0.6) 12px,
      rgba(255, 255, 255, 0.6) 24px
    ),
    linear-gradient(to bottom, rgba(226, 232, 240, 0.75) 1px, transparent 1px);
  background-size: auto, 100% 24px;
}

.day-track {
  background-image: linear-gradient(
    to bottom,
    rgba(226, 232, 240, 0.8) 1px,
    transparent 1px
  );
  background-size: 100% 24px;
  overflow: hidden;
  position: relative;
}

.day-empty {
  color: rgb(148 163 184);
  font-size: 12px;
  left: 50%;
  position: absolute;
  top: 32px;
  transform: translateX(-50%);
}

.day-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 6px 12px;
}

.schedule-row {
  align-items: center;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgb(226 232 240);
  border-left-width: 3px;
  border-radius: 6px;
  cursor: pointer;
  display: grid;
  gap: 8px;
  grid-template-columns: 42px minmax(0, 1fr) 44px;
  min-height: 42px;
  padding: 4px 6px;
  transition: border-color 0.18s ease;
}

.schedule-row:hover {
  border-color: rgb(147 197 253);
}

.schedule-row--pending {
  border-left-color: #3b82f6;
}

.schedule-row--scheduled {
  border-left-color: #22c55e;
}

.schedule-row--disabled {
  border-left-color: #94a3b8;
}

.schedule-row__time {
  color: rgb(100 116 139);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}

.schedule-row__main {
  min-width: 0;
}

.schedule-row__line {
  align-items: center;
  display: flex;
  gap: 6px;
  min-width: 0;
}

.schedule-row__patient {
  color: rgb(15 23 42);
  font-size: 12px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-row__room {
  color: rgb(59 130 246);
  font-size: 11px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-row__meta {
  align-items: center;
  color: rgb(148 163 184);
  display: flex;
  font-size: 11px;
  gap: 4px;
  line-height: 1.2;
  margin-top: 3px;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-row__side {
  align-items: center;
  display: flex;
  justify-content: flex-end;
}

.schedule-row__state {
  color: rgb(34 197 94);
  font-size: 11px;
  font-weight: 600;
}

.legend-bar {
  border-top: 1px solid rgb(235 238 245);
}

.legend-dot {
  border-radius: 2px;
  display: inline-block;
  height: 8px;
  width: 8px;
}

.legend-dot--pending {
  background: #3b82f6;
}

.legend-dot--scheduled {
  background: #22c55e;
}

.legend-dot--disabled {
  background: #94a3b8;
}

@media (max-width: 1280px) {
  .toolbar-main {
    flex-direction: column;
  }

  .toolbar-stats {
    justify-content: flex-start;
  }
}
</style>
