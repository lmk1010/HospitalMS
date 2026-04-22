<script lang="ts" setup>
import type { TableColumnsType } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';

import type { HospitalCtAppointmentApi } from '#/api/hospital/ct-appointment';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import dayjs from 'dayjs';

import { Page, useVbenModal } from '@vben/common-ui';

import { message, Modal } from 'ant-design-vue';

import { ACTION_ICON, TableAction } from '#/adapter/vxe-table';
import {
  deleteCtAppointment,
  getCtAppointment,
  getCtAppointmentScheduleList,
  submitCtAppointment,
} from '#/api/hospital/ct-appointment';

import Form from './modules/form.vue';

type ViewMode = 'day' | 'list' | 'week';

type SlotConfig = {
  background: string;
  color: string;
  key: string;
  label: string;
  rowSpan: number;
  startRow: number;
};

const rowHeight = 28;
const weekdayLabels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
const slotConfigs: SlotConfig[] = [
  {
    background: 'rgba(73, 143, 255, 0.14)',
    color: '#498fff',
    key: '上午',
    label: '上午',
    rowSpan: 12,
    startRow: 48,
  },
  {
    background: 'rgba(245, 154, 35, 0.14)',
    color: '#f59a23',
    key: '下午',
    label: '下午',
    rowSpan: 12,
    startRow: 84,
  },
  {
    background: 'rgba(103, 194, 58, 0.14)',
    color: '#67c23a',
    key: '晚间',
    label: '晚间',
    rowSpan: 12,
    startRow: 114,
  },
];
const slotOrder = Object.fromEntries(
  slotConfigs.map((item, index) => [item.key, index]),
);
const timeRows = Array.from({ length: 24 * 6 }, (_, index) =>
  dayjs('2026-01-01 00:00')
    .add(index * 10, 'minute')
    .format('HH:mm'),
);
const listColumns: TableColumnsType<HospitalCtAppointmentApi.CtAppointment> = [
  {
    title: '预约日期',
    dataIndex: 'appointmentDate',
    key: 'appointmentDate',
    width: 110,
  },
  {
    title: '时段',
    dataIndex: 'appointmentSlot',
    key: 'appointmentSlot',
    width: 84,
  },
  { title: '患者', dataIndex: 'patientName', key: 'patientName', width: 110 },
  { title: 'CT室', dataIndex: 'ctRoomName', key: 'ctRoomName', width: 120 },
  {
    title: 'CT设备',
    dataIndex: 'ctDeviceName',
    key: 'ctDeviceName',
    width: 120,
  },
  {
    title: '申请医生',
    dataIndex: 'applyDoctorName',
    key: 'applyDoctorName',
    width: 100,
  },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 84 },
  { title: '状态', key: 'workflowStatus', width: 96 },
  { title: '备注', dataIndex: 'positionNote', key: 'positionNote', width: 220 },
  { title: '操作', key: 'actions', fixed: 'right', width: 170 },
];

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const loading = ref(false);
const keyword = ref('');
const rangeValue = ref<[Dayjs, Dayjs]>(getWeekRange(dayjs()));
const scheduleData = ref<HospitalCtAppointmentApi.CtAppointment[]>([]);
const selectedRoom = ref<string>();
const viewMode = ref<ViewMode>('week');
const route = useRoute();
const router = useRouter();
const focusedAppointmentId = ref<number>();

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
  const appointment = await getCtAppointment(focusId).catch(() => undefined);
  if (!appointment) return undefined;
  focusedAppointmentId.value = appointment.id;
  if (appointment.appointmentDate) {
    rangeValue.value = getWeekRange(dayjs(appointment.appointmentDate));
  }
  selectedRoom.value = appointment.ctRoomName || undefined;
  return appointment;
}


const boardHeight = computed(() => timeRows.length * rowHeight);
const filteredRooms = computed(() => {
  const rooms = new Set<string>();
  for (const item of scheduleData.value) {
    const roomName = formatCellText(item.ctRoomName);
    if (roomName !== '--') {
      rooms.add(roomName);
    }
  }
  return [...rooms].sort((left, right) => left.localeCompare(right, 'zh-CN'));
});
const roomOptions = computed(() => {
  return filteredRooms.value.map((item) => ({ label: item, value: item }));
});
const sortedAppointments = computed(() => {
  return [...scheduleData.value].sort((left, right) => {
    const dateCompare = `${left.appointmentDate}`.localeCompare(
      `${right.appointmentDate}`,
    );
    if (dateCompare !== 0) {
      return dateCompare;
    }
    const slotCompare =
      (slotOrder[left.appointmentSlot || ''] ?? 99) -
      (slotOrder[right.appointmentSlot || ''] ?? 99);
    if (slotCompare !== 0) {
      return slotCompare;
    }
    return (left.id || 0) - (right.id || 0);
  });
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
const dayColumnWidth = computed(() => {
  if (viewMode.value === 'day') {
    return 'minmax(320px, 1fr)';
  }
  return 'minmax(200px, 1fr)';
});
const dayGridStyle = computed(() => {
  return {
    gridTemplateColumns: `repeat(${Math.max(visibleDays.value.length, 1)}, ${dayColumnWidth.value})`,
  };
});
const groupedAppointments = computed(() => {
  const map = new Map<string, HospitalCtAppointmentApi.CtAppointment[]>();
  for (const item of sortedAppointments.value) {
    const key = `${item.appointmentDate}_${item.appointmentSlot || ''}`;
    const current = map.get(key) || [];
    current.push(item);
    map.set(key, current);
  }
  return map;
});
const bookedCount = computed(() => {
  return scheduleData.value.filter(
    (item) => item.workflowStatus === 'BOOKED' && item.status !== 1,
  ).length;
});
const pendingCount = computed(() => {
  return scheduleData.value.filter(
    (item) => item.workflowStatus !== 'BOOKED' && item.status !== 1,
  ).length;
});

function getWeekRange(base: Dayjs): [Dayjs, Dayjs] {
  const weekday = base.day();
  const offset = weekday === 0 ? -6 : 1 - weekday;
  const start = base.add(offset, 'day').startOf('day');
  return [start, start.add(6, 'day').startOf('day')];
}

function formatCellText(value?: null | string) {
  return value && `${value}`.trim() ? value : '--';
}

function formatDateLabel(value: Dayjs) {
  return `${weekdayLabels[value.day()]} (${value.format('M月D日')})`;
}

function isToday(value: Dayjs) {
  return value.isSame(dayjs(), 'day');
}

function getPriorityLabel(priority?: string) {
  switch (priority) {
    case 'EMERGENCY': {
      return '紧急';
    }
    case 'URGENT': {
      return '加急';
    }
    default: {
      return '常规';
    }
  }
}

function getStatusLabel(row: HospitalCtAppointmentApi.CtAppointment) {
  if (row.status === 1) {
    return '已停用';
  }
  return row.workflowStatus === 'BOOKED' ? '已预约' : '待提交';
}

function getStatusTagColor(row: HospitalCtAppointmentApi.CtAppointment) {
  if (row.status === 1) {
    return 'default';
  }
  return row.workflowStatus === 'BOOKED' ? 'green' : 'processing';
}

function getDaySlotAppointments(day: Dayjs, slot: string) {
  return (
    groupedAppointments.value.get(`${day.format('YYYY-MM-DD')}_${slot}`) || []
  );
}

function getCardStyle(slot: SlotConfig, index: number) {
  const top = slot.startRow * rowHeight + 6 + index * 70;
  return {
    background: slot.background,
    borderColor: slot.color,
    height: `${Math.max(slot.rowSpan * rowHeight - 10, 64)}px`,
    top: `${top}px`,
  };
}

function getColumnText(
  record: HospitalCtAppointmentApi.CtAppointment,
  key: string,
) {
  const value = (record as Record<string, unknown>)[key];
  if (typeof value === 'string') {
    return formatCellText(value);
  }
  return formatCellText(value == null ? undefined : `${value}`);
}

async function loadSchedule() {
  loading.value = true;
  try {
    scheduleData.value = await getCtAppointmentScheduleList({
      ctRoomName: selectedRoom.value || undefined,
      endDate: rangeValue.value[1].format('YYYY-MM-DD'),
      keyword: keyword.value.trim() || undefined,
      startDate: rangeValue.value[0].format('YYYY-MM-DD'),
    });
  } finally {
    loading.value = false;
  }
}

function handleCreate(
  payload?: Partial<HospitalCtAppointmentApi.CtAppointment>,
) {
  formModalApi.setData(payload || null).open();
}

function handleEdit(row: HospitalCtAppointmentApi.CtAppointment) {
  formModalApi.setData(row).open();
}

async function handleSubmit(row: HospitalCtAppointmentApi.CtAppointment) {
  const hideLoading = message.loading({
    content: `正在提交CT预约【${row.bizNo}】`,
    duration: 0,
  });
  try {
    await submitCtAppointment(row.id!);
    message.success('提交成功');
    await loadSchedule();
  } finally {
    hideLoading();
  }
}

function handleDelete(row: HospitalCtAppointmentApi.CtAppointment) {
  Modal.confirm({
    centered: true,
    title: `确认删除CT预约【${row.patientName || row.bizNo}】吗？`,
    async onOk() {
      await deleteCtAppointment(row.id!);
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

function handleRangeChange(values: null | [Dayjs, Dayjs]) {
  if (!values || !values[0] || !values[1]) {
    return;
  }
  if (viewMode.value === 'day') {
    rangeValue.value = [values[0].startOf('day'), values[0].startOf('day')];
  } else {
    rangeValue.value = [values[0].startOf('day'), values[1].startOf('day')];
  }
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
  if (!sortedAppointments.value.length) {
    message.warning('当前没有可导出的预约数据');
    return;
  }
  const headers = [
    '预约日期',
    '时段',
    '患者',
    'CT室',
    'CT设备',
    '申请医生',
    '优先级',
    '状态',
    '定位说明',
  ];
  const rows = sortedAppointments.value.map((item) => [
    item.appointmentDate,
    item.appointmentSlot || '',
    item.patientName || '',
    item.ctRoomName || '',
    item.ctDeviceName || '',
    item.applyDoctorName || '',
    getPriorityLabel(item.priority),
    getStatusLabel(item),
    item.positionNote || '',
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
  link.download = `ct预约排班_${rangeValue.value[0].format('YYYYMMDD')}_${rangeValue.value[1].format('YYYYMMDD')}.csv`;
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
    <div class="ct-schedule-page">
      <div class="card-box rounded-xl p-3">
        <div class="toolbar-panel">
          <div class="toolbar-title-row">
            <div class="toolbar-title">CT预约</div>
            <a-select
              v-model:value="selectedRoom"
              allow-clear
              class="w-[190px]"
              placeholder="请选择治疗室"
              size="middle"
            >
              <a-select-option
                v-for="item in roomOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </a-select-option>
            </a-select>
            <div class="toolbar-meta">
              待提交 {{ pendingCount }} / 已预约 {{ bookedCount }} / 总数
              {{ scheduleData.length }}
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
            <a-range-picker
              :value="rangeValue"
              size="middle"
              value-format="YYYY-MM-DD"
              @change="handleRangeChange"
            />
            <a-button size="middle" @click="shiftRange(1)">›</a-button>
            <a-button size="middle" @click="handleToday">今天</a-button>

            <a-input-search
              v-model:value="keyword"
              allow-clear
              class="min-w-[240px] flex-1"
              placeholder="姓名/预约单号/医生/CT室/设备"
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
          :data-source="sortedAppointments"
          :loading="loading"
          :pagination="false"
          :scroll="{ x: 1400 }"
          :locale="{ emptyText: '当前时间范围暂无CT预约' }"
          row-key="id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'priority'">
              <a-tag
                :color="
                  record.priority === 'EMERGENCY'
                    ? 'red'
                    : record.priority === 'URGENT'
                      ? 'orange'
                      : 'blue'
                "
              >
                {{ getPriorityLabel(record.priority) }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'workflowStatus'">
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
                    auth: ['hospital:ct-appointment:update'],
                    onClick: handleEdit.bind(null, record),
                  },
                  {
                    label: '提交',
                    type: 'link',
                    auth: ['hospital:ct-appointment:submit'],
                    ifShow:
                      record.workflowStatus !== 'BOOKED' && record.status !== 1,
                    onClick: handleSubmit.bind(null, record),
                  },
                  {
                    label: '删除',
                    type: 'link',
                    danger: true,
                    icon: ACTION_ICON.DELETE,
                    auth: ['hospital:ct-appointment:delete'],
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
        <div class="schedule-wrapper overflow-x-auto">
          <div class="schedule-grid min-w-[1180px]">
            <div class="schedule-corner"></div>
            <div class="schedule-day-heads" :style="dayGridStyle">
              <div
                v-for="day in visibleDays"
                :key="day.format('YYYY-MM-DD')"
                :class="['day-head', { 'day-head--today': isToday(day) }]"
              >
                <div class="day-head__title">{{ formatDateLabel(day) }}</div>
              </div>
            </div>

            <div class="time-axis">
              <div
                v-for="item in timeRows"
                :key="item"
                class="time-axis-cell"
                :style="{ height: `${rowHeight}px` }"
              >
                {{ item }}
              </div>
            </div>

            <div class="schedule-day-columns" :style="dayGridStyle">
              <div
                v-for="day in visibleDays"
                :key="`${day.format('YYYY-MM-DD')}-column`"
                :class="['day-column', { 'day-column--today': isToday(day) }]"
              >
                <div class="day-track" :style="{ height: `${boardHeight}px` }">
                  <div
                    v-for="slot in slotConfigs"
                    :key="`${day.format('YYYY-MM-DD')}-${slot.key}`"
                  >
                    <div
                      v-for="(item, index) in getDaySlotAppointments(
                        day,
                        slot.key,
                      )"
                      :key="item.id"
                      class="appointment-card"
                      :style="getCardStyle(slot, index)"
                      @click="handleEdit(item)"
                    >
                      <div class="appointment-card__slot">
                        {{ slot.label }}
                      </div>
                      <div class="flex items-start justify-between gap-2">
                        <div class="min-w-0 flex-1">
                          <div
                            class="truncate text-sm font-semibold text-foreground"
                          >
                            {{ item.patientName }}
                          </div>
                          <div
                            class="mt-1 truncate text-[12px] text-foreground/55"
                          >
                            {{ formatCellText(item.ctRoomName) }} ·
                            {{ formatCellText(item.ctDeviceName) }}
                          </div>
                        </div>
                        <a-tag :color="getStatusTagColor(item)">
                          {{ getStatusLabel(item) }}
                        </a-tag>
                      </div>
                      <div
                        class="mt-2 flex items-center justify-between gap-2 text-[12px] text-foreground/55"
                      >
                        <span>{{ formatCellText(item.applyDoctorName) }}</span>
                        <span>{{ getPriorityLabel(item.priority) }}</span>
                      </div>
                      <div class="mt-2 flex items-center justify-between gap-2">
                        <span class="truncate text-[11px] text-foreground/45">
                          {{ formatCellText(item.bizNo) }}
                        </span>
                        <a-button
                          v-if="
                            item.workflowStatus !== 'BOOKED' &&
                            item.status !== 1
                          "
                          size="small"
                          type="link"
                          @click.stop="handleSubmit(item)"
                        >
                          提交
                        </a-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div
          class="legend-bar flex items-center justify-end gap-5 px-4 py-3 text-xs text-foreground/55"
        >
          <div class="flex items-center gap-2">
            <span class="legend-dot bg-blue-500"></span>
            <span>待提交</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="legend-dot bg-green-500"></span>
            <span>已预约</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="legend-dot bg-slate-300"></span>
            <span>已停用</span>
          </div>
        </div>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.ct-schedule-page :deep(.ant-input),
.ct-schedule-page :deep(.ant-input-affix-wrapper),
.ct-schedule-page :deep(.ant-picker),
.ct-schedule-page :deep(.ant-select-selector),
.ct-schedule-page :deep(.ant-btn) {
  border-radius: 10px;
}

.ct-schedule-page :deep(.ant-table-thead > tr > th) {
  padding: 8px 10px;
  white-space: nowrap;
}

.ct-schedule-page :deep(.ant-table-tbody > tr > td) {
  padding: 8px 10px;
}

.ct-schedule-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-title-row {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-title {
  color: rgb(31 41 55);
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
}

.toolbar-meta {
  color: rgb(148 163 184);
  font-size: 12px;
}

.schedule-grid {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
}

.schedule-corner {
  background: rgb(250 250 250);
  border-bottom: 1px solid rgb(235 238 245);
  border-right: 1px solid rgb(235 238 245);
  min-height: 44px;
}

.schedule-day-heads,
.schedule-day-columns {
  display: grid;
}

.day-head {
  align-items: center;
  background: rgb(250 250 250);
  border-bottom: 1px solid rgb(235 238 245);
  border-right: 1px solid rgb(235 238 245);
  display: flex;
  justify-content: center;
  min-height: 44px;
  padding: 10px 12px;
}

.day-head__title {
  color: rgb(96 98 102);
  font-size: 13px;
  font-weight: 600;
}

.day-head--today {
  background: rgb(232 248 238);
}

.day-head--today .day-head__title {
  color: rgb(72 143 88);
}

.time-axis {
  border-right: 1px solid rgb(235 238 245);
}

.time-axis-cell {
  border-bottom: 1px solid rgb(240 242 245);
  color: rgb(144 147 153);
  font-size: 11px;
  padding: 6px 8px 0 0;
  text-align: right;
}

.day-column {
  border-right: 1px solid rgb(235 238 245);
}

.day-column--today {
  background: rgb(103 194 58 / 4%);
}

.day-track {
  background-image: linear-gradient(
    to bottom,
    rgb(240 242 245) 1px,
    transparent 1px
  );
  background-size: 100% 28px;
  position: relative;
}

.appointment-card {
  border: 1px solid transparent;
  border-left-width: 3px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgb(15 23 42 / 8%);
  cursor: pointer;
  left: 6px;
  min-height: 64px;
  padding: 10px 12px 8px;
  position: absolute;
  right: 6px;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.appointment-card__slot {
  color: rgb(96 98 102);
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 8px;
}

.appointment-card:hover {
  box-shadow: 0 8px 18px rgb(15 23 42 / 12%);
  transform: translateY(-1px);
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
</style>
