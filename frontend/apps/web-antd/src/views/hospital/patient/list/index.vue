<script lang="ts" setup>
import type { TableColumnsType, TablePaginationConfig } from 'ant-design-vue';

import type { HospitalDoctorApi } from '#/api/hospital/doctor';
import type { HospitalPatientApi } from '#/api/hospital/patient';

import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction } from '#/adapter/vxe-table';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import {
  deletePatient,
  getPatientDashboardPage,
  getPatientDashboardSummary,
} from '#/api/hospital/patient';

import Form from './modules/form.vue';

const route = useRoute();
const router = useRouter();
const defaultPageSize = 30;

const patientTypeOptions = [
  { label: '门诊', value: '门诊' },
  { label: '住院', value: '住院' },
  { label: '会诊', value: '会诊' },
];

const stageMeta = [
  {
    code: 'REGISTER',
    color: '#69b1ff',
    label: '登记',
    summaryKey: 'registerCount',
  },
  {
    code: 'POSITIONING',
    color: '#36cfc9',
    label: '定位',
    summaryKey: 'positioningCount',
  },
  {
    code: 'CONTOUR',
    color: '#9254de',
    label: '勾画',
    summaryKey: 'contourCount',
  },
  {
    code: 'PLANNING',
    color: '#1677ff',
    label: '计划',
    summaryKey: 'planningCount',
  },
  {
    code: 'REVIEW',
    color: '#fa8c16',
    label: '复核',
    summaryKey: 'reviewCount',
  },
  {
    code: 'TREATMENT',
    color: '#52c41a',
    label: '治疗',
    summaryKey: 'treatmentCount',
  },
] as const;

const columns: TableColumnsType<HospitalPatientApi.PatientDashboardRow> = [
  { title: '患者信息', key: 'patient', fixed: 'left', width: 188 },
  {
    title: '病案号',
    dataIndex: 'medicalRecordNo',
    key: 'medicalRecordNo',
    width: 96,
    ellipsis: true,
  },
  {
    title: '放疗号',
    dataIndex: 'radiotherapyNo',
    key: 'radiotherapyNo',
    width: 96,
    ellipsis: true,
  },
  {
    title: '联系电话',
    dataIndex: 'patientPhone',
    key: 'patientPhone',
    width: 108,
    ellipsis: true,
  },
  {
    title: '诊断',
    dataIndex: 'diagnosis',
    key: 'diagnosis',
    width: 150,
    ellipsis: true,
  },
  {
    title: '主管医生',
    dataIndex: 'managerDoctorName',
    key: 'managerDoctorName',
    width: 78,
    ellipsis: true,
  },
  {
    title: '计划物理师',
    dataIndex: 'planPhysicistName',
    key: 'planPhysicistName',
    width: 84,
    ellipsis: true,
  },
  { title: '阶段 / 节点', key: 'currentStage', width: 138 },
  { title: '疗程进度', key: 'progress', width: 106 },
  { title: '登记时间', key: 'registrationTime', width: 116 },
  {
    title: '登记人',
    key: 'registrationUserName',
    width: 76,
    ellipsis: true,
  },
  { title: '操作', key: 'actions', fixed: 'right', width: 96 },
];

const loading = ref(false);
const tableData = ref<HospitalPatientApi.PatientDashboardRow[]>([]);
const doctorOptions = ref<HospitalDoctorApi.Doctor[]>([]);
const summary = ref<HospitalPatientApi.PatientDashboardSummary>({
  contourCount: 0,
  monthCount: 0,
  planningCount: 0,
  positioningCount: 0,
  registerCount: 0,
  reviewCount: 0,
  todayCount: 0,
  totalCount: 0,
  treatmentCount: 0,
});
const queryForm = reactive<HospitalPatientApi.PatientDashboardQuery>({
  currentStage: undefined,
  keyword: '',
  managerDoctorId: undefined,
  pageNo: 1,
  pageSize: defaultPageSize,
  patientType: undefined,
  planPhysicistName: '',
});
const pagination = reactive({
  current: 1,
  pageSize: defaultPageSize,
  total: 0,
});
const brokenAvatarMap = reactive<Record<number, boolean>>({});

const openingPatientId = ref<number>();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const stageCards = computed(() => {
  return stageMeta.map((item) => ({
    active: queryForm.currentStage === item.code,
    code: item.code,
    color: item.color,
    count: summary.value[item.summaryKey] ?? 0,
    label: item.label,
  }));
});

const tablePagination = computed<TablePaginationConfig>(() => ({
  current: pagination.current,
  pageSize: pagination.pageSize,
  pageSizeOptions: ['10', '20', '30', '50', '100'],
  showQuickJumper: true,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
  total: pagination.total,
}));

function getGenderLabel(gender?: number) {
  if (gender === 1) return '男';
  if (gender === 2) return '女';
  return '未知';
}

function getPatientAvatarText(record: HospitalPatientApi.PatientDashboardRow) {
  return record.name?.trim()?.slice(0, 1) || '患';
}

function handleAvatarError(patientId: number) {
  brokenAvatarMap[patientId] = true;
}

function shouldUsePhoto(record: HospitalPatientApi.PatientDashboardRow) {
  return Boolean(record.photoUrl) && !brokenAvatarMap[record.id];
}

function getStageTagColor(stageCode?: string) {
  switch (stageCode) {
    case 'CONTOUR': {
      return '#9254de';
    }
    case 'PLANNING': {
      return '#1677ff';
    }
    case 'POSITIONING': {
      return '#13c2c2';
    }
    case 'REGISTER': {
      return '#8c8c8c';
    }
    case 'REVIEW': {
      return '#fa8c16';
    }
    case 'TREATMENT': {
      return '#52c41a';
    }
    default: {
      return '#8c8c8c';
    }
  }
}

function getStageBadgeStyle(stageCode?: string) {
  const color = getStageTagColor(stageCode);
  return {
    backgroundColor: `${color}12`,
    borderColor: `${color}33`,
    color,
  };
}

function formatCellText(value?: null | string) {
  return value && `${value}`.trim() ? value : '--';
}

function sanitizeDisplayText(value?: null | string) {
  const text = formatCellText(value);
  if (text === '--') {
    return text;
  }
  return /芋道|yudao/i.test(text) ? '系统导入' : text;
}

function formatDateTime(value?: Date | number | string) {
  if (!value) {
    return '--';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return formatCellText(String(value));
  }
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  const hour = `${date.getHours()}`.padStart(2, '0');
  const minute = `${date.getMinutes()}`.padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}`;
}

function formatProgress(row: HospitalPatientApi.PatientDashboardRow) {
  const completed = Math.max(row.completedFractions ?? 0, 0);
  const total = Math.max(row.totalFractions ?? 0, 0);
  if (total <= 0) {
    return completed > 0 ? `${completed}` : '--';
  }
  return `${Math.min(completed, total)}/${total}`;
}

function getProgressPercent(row: HospitalPatientApi.PatientDashboardRow) {
  const total = Math.max(row.totalFractions ?? 0, 0);
  if (total <= 0) {
    return 0;
  }
  const completed = Math.max(row.completedFractions ?? 0, 0);
  return Math.min(100, Math.round((completed / total) * 100));
}

function getColumnKey(column: { dataIndex?: unknown; key?: unknown }) {
  if (typeof column.key === 'string') {
    return column.key;
  }
  if (typeof column.dataIndex === 'string') {
    return column.dataIndex;
  }
  return undefined;
}

function getColumnText(
  record: HospitalPatientApi.PatientDashboardRow,
  columnKey?: string,
) {
  if (!columnKey) {
    return '--';
  }
  const value = (
    record as unknown as Record<string, null | number | string | undefined>
  )[columnKey];
  return value === null || value === undefined
    ? '--'
    : formatCellText(String(value));
}

function getColumnTextByColumn(
  record: HospitalPatientApi.PatientDashboardRow,
  column: { dataIndex?: unknown; key?: unknown },
) {
  return getColumnText(record, getColumnKey(column));
}

function buildSummaryParams() {
  return {
    keyword: queryForm.keyword?.trim() || undefined,
    managerDoctorId: queryForm.managerDoctorId,
    patientType: queryForm.patientType,
    planPhysicistName: queryForm.planPhysicistName?.trim() || undefined,
  };
}

function buildPageParams(): HospitalPatientApi.PatientDashboardQuery {
  return {
    currentStage: queryForm.currentStage,
    keyword: queryForm.keyword?.trim() || undefined,
    managerDoctorId: queryForm.managerDoctorId,
    pageNo: pagination.current,
    pageSize: pagination.pageSize,
    patientType: queryForm.patientType,
    planPhysicistName: queryForm.planPhysicistName?.trim() || undefined,
  };
}

async function loadDoctorOptions() {
  doctorOptions.value = await getSimpleDoctorList();
}

async function loadSummary() {
  summary.value = await getPatientDashboardSummary(buildSummaryParams());
}

async function loadTable() {
  loading.value = true;
  try {
    const pageResult = await getPatientDashboardPage(buildPageParams());
    tableData.value = pageResult.list || [];
    pagination.total = pageResult.total || 0;
  } finally {
    loading.value = false;
  }
}

async function reloadPage() {
  await Promise.all([loadSummary(), loadTable()]);
}

function handleSearch() {
  pagination.current = 1;
  void reloadPage();
}

function handleReset() {
  queryForm.currentStage = undefined;
  queryForm.keyword = '';
  queryForm.managerDoctorId = undefined;
  queryForm.patientType = undefined;
  queryForm.planPhysicistName = '';
  pagination.current = 1;
  pagination.pageSize = defaultPageSize;
  void reloadPage();
}

function isPatientRegistrationRoute(routePath?: string) {
  return routePath?.endsWith('/hospital-patient/registration') ?? false;
}

function handleCreate() {
  void router.push('/hospital-flow/hospital-patient/registration');
}

function handleEdit(row: HospitalPatientApi.PatientDashboardRow) {
  formModalApi.setData({ id: row.id }).open();
}

function handleProcess(row: HospitalPatientApi.PatientDashboardRow) {
  if (
    isPatientRegistrationRoute(row.currentRoutePath) ||
    !row.currentRoutePath
  ) {
    handleEdit(row);
    return;
  }
  void router.push({
    path: row.currentRoutePath,
    query: {
      focusId: row.currentBizId ? String(row.currentBizId) : undefined,
      patientId: String(row.id),
    },
  });
}

async function handleDelete(row: HospitalPatientApi.PatientDashboardRow) {
  const hideLoading = message.loading({
    content: `正在删除患者【${row.name}】`,
    duration: 0,
  });
  try {
    await deletePatient(row.id);
    message.success('删除成功');
    await reloadPage();
  } finally {
    hideLoading();
  }
}

function handleStageCardClick(stageCode: string) {
  queryForm.currentStage =
    queryForm.currentStage === stageCode ? undefined : stageCode;
  pagination.current = 1;
  void loadTable();
}

function handleTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1;
  pagination.pageSize = page.pageSize || defaultPageSize;
  void loadTable();
}

async function openPatientByRouteQuery() {
  const openIdRaw = Array.isArray(route.query.openId)
    ? route.query.openId[0]
    : route.query.openId;
  const openId = Number(openIdRaw);
  if (!openIdRaw || !Number.isFinite(openId) || openId <= 0) {
    return;
  }
  if (openingPatientId.value === openId) {
    return;
  }
  openingPatientId.value = openId;
  formModalApi.setData({ id: openId }).open();
  const nextQuery = { ...route.query };
  delete nextQuery.openId;
  await router.replace({ path: route.path, query: nextQuery });
}

watch(
  () => route.query.openId,
  () => {
    void openPatientByRouteQuery();
  },
);

onMounted(async () => {
  await loadDoctorOptions();
  await reloadPage();
  await openPatientByRouteQuery();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="reloadPage" />
    <div class="patient-list-page space-y-2.5">
      <div class="grid gap-1.5 xl:grid-cols-[220px_minmax(0,1fr)]">
        <div
          class="summary-hero rounded-lg bg-[linear-gradient(135deg,#40a9ff_0%,#1677ff_100%)] px-3 py-2.5 text-white shadow-sm dark:bg-[linear-gradient(135deg,#1d39c4_0%,#1677ff_100%)]"
        >
          <div class="summary-hero__head">
            <div class="summary-hero__title">患者列表</div>
            <div class="summary-hero__tag">真实数据</div>
          </div>
          <div class="summary-hero__body">
            <div class="summary-hero__main">
              <div class="summary-hero__total">{{ summary.totalCount }}</div>
              <div class="summary-hero__desc">当前筛选患者</div>
            </div>
            <div class="summary-hero__stats">
              <div class="summary-hero__metric">
                <span>今日登记</span>
                <strong>{{ summary.todayCount }}</strong>
              </div>
              <div class="summary-hero__metric">
                <span>本月登记</span>
                <strong>{{ summary.monthCount }}</strong>
              </div>
            </div>
          </div>
        </div>

        <div
          class="stage-strip grid overflow-hidden rounded-lg border md:grid-cols-3 xl:grid-cols-6"
        >
          <button
            v-for="item in stageCards"
            :key="item.code"
            class="stage-card px-3 py-2 text-left transition-all"
            :class="item.active ? 'stage-card--active' : ''"
            type="button"
            :title="item.active ? '点击取消筛选' : '点击查看该阶段'"
            @click="handleStageCardClick(item.code)"
          >
            <span
              class="stage-card__dot"
              :style="{ backgroundColor: item.color }"
            ></span>
            <div class="stage-card__count">{{ item.count }}</div>
            <div class="stage-card__label">{{ item.label }}</div>
          </button>
        </div>
      </div>

      <div class="card-box rounded-lg p-2.5">
        <div
          class="filter-grid grid gap-1.5 md:grid-cols-2 xl:grid-cols-[156px_120px_136px_minmax(0,1fr)_68px_68px_86px]"
        >
          <a-select
            v-model:value="queryForm.managerDoctorId"
            allow-clear
            placeholder="主管医生"
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

          <a-select
            v-model:value="queryForm.patientType"
            allow-clear
            placeholder="患者类型"
            size="small"
          >
            <a-select-option
              v-for="item in patientTypeOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>

          <a-input
            v-model:value="queryForm.planPhysicistName"
            allow-clear
            placeholder="计划物理师"
            size="small"
            @press-enter="handleSearch"
          />

          <a-input
            v-model:value="queryForm.keyword"
            allow-clear
            placeholder="姓名 / 档案号 / 病案号 / 放疗号 / 住院号 / 身份证 / 电话 / ID"
            size="small"
            @press-enter="handleSearch"
          />

          <a-button size="small" type="primary" @click="handleSearch">
            查询
          </a-button>
          <a-button size="small" @click="handleReset">重置</a-button>
          <a-button size="small" type="dashed" @click="handleCreate">
            患者登记
          </a-button>
        </div>
      </div>

      <div class="card-box rounded-lg p-1.5">
        <a-table
          :columns="columns"
          :data-source="tableData"
          :loading="loading"
          :pagination="tablePagination"
          :scroll="{ x: 1332 }"
          :locale="{ emptyText: '暂无患者数据' }"
          row-key="id"
          size="small"
          table-layout="fixed"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'patient'">
              <div class="patient-cell">
                <div class="patient-cell__avatar">
                  <img
                    v-if="shouldUsePhoto(record)"
                    :src="record.photoUrl || undefined"
                    alt="avatar"
                    @error="handleAvatarError(record.id)"
                  />
                  <span v-else class="patient-cell__avatar-text">
                    {{ getPatientAvatarText(record) }}
                  </span>
                </div>
                <div class="patient-cell__body">
                  <div class="patient-cell__title">
                    <span class="patient-cell__name">{{ record.name }}</span>
                    <span
                      v-text="
                        `${getGenderLabel(record.gender)}/${record.age ?? '--'}岁`
                      "
                    ></span>
                    <span
                      v-if="record.patientType"
                      v-text="record.patientType"
                    ></span>
                  </div>
                  <div class="patient-cell__meta">
                    <span>{{ formatCellText(record.patientNo) }}</span>
                    <span
                      v-if="record.hospitalizationNo"
                      v-text="record.hospitalizationNo"
                    ></span>
                  </div>
                  <div class="patient-cell__meta patient-cell__meta--muted">
                    <span>{{ formatCellText(record.wardName) }}</span>
                  </div>
                </div>
              </div>
            </template>

            <template v-else-if="column.key === 'diagnosis'">
              <a-tooltip :title="formatCellText(record.diagnosis)">
                <div class="diagnosis-cell">
                  {{ formatCellText(record.diagnosis) }}
                </div>
              </a-tooltip>
            </template>

            <template v-else-if="column.key === 'currentStage'">
              <div class="stage-cell">
                <div class="stage-cell__head">
                  <span
                    class="stage-cell__dot"
                    :style="{
                      backgroundColor: getStageTagColor(
                        record.currentStageCode,
                      ),
                    }"
                  ></span>
                  <span
                    class="stage-cell__badge"
                    :style="getStageBadgeStyle(record.currentStageCode)"
                  >
                    {{ record.currentStage || '--' }}
                  </span>
                </div>
                <div
                  class="stage-cell__node"
                  :title="formatCellText(record.currentNodeName)"
                >
                  {{ formatCellText(record.currentNodeName) }}
                </div>
              </div>
            </template>

            <template v-else-if="column.key === 'progress'">
              <div class="progress-cell">
                <div class="progress-cell__head">
                  <strong>{{ formatProgress(record) }}</strong>
                  <span>{{ getProgressPercent(record) }}%</span>
                </div>
                <div class="progress-cell__track">
                  <div
                    class="progress-cell__fill"
                    :style="{ width: `${getProgressPercent(record)}%` }"
                  ></div>
                </div>
              </div>
            </template>

            <template v-else-if="column.key === 'registrationTime'">
              <div class="time-cell">
                {{
                  formatDateTime(record.registrationTime).split(' ').join('\n')
                }}
              </div>
            </template>

            <template v-else-if="column.key === 'registrationUserName'">
              <span class="text-[12px] leading-4 text-foreground/75">
                {{ sanitizeDisplayText(record.registrationUserName) }}
              </span>
            </template>

            <template v-else-if="column.key === 'actions'">
              <TableAction
                :actions="[
                  {
                    label: '进入',
                    type: 'link',
                    auth: ['hospital:patient:update'],
                    onClick: handleProcess.bind(null, record),
                  },
                ]"
                :drop-down-actions="[
                  {
                    label: '编辑',
                    type: 'link',
                    icon: ACTION_ICON.EDIT,
                    auth: ['hospital:patient:update'],
                    onClick: handleEdit.bind(null, record),
                  },
                  {
                    label: '删除',
                    type: 'link',
                    danger: true,
                    icon: ACTION_ICON.DELETE,
                    auth: ['hospital:patient:delete'],
                    popConfirm: {
                      title: `确认删除患者【${record.name}】吗？`,
                      confirm: handleDelete.bind(null, record),
                    },
                  },
                ]"
              >
                <template #more>
                  <a-button
                    class="patient-action-more"
                    size="small"
                    type="link"
                  >
                    <IconifyIcon icon="lucide:ellipsis-vertical" />
                  </a-button>
                </template>
              </TableAction>
            </template>

            <template v-else>
              {{ getColumnTextByColumn(record, column) }}
            </template>
          </template>
        </a-table>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.patient-list-page :deep(.ant-table-thead > tr > th) {
  padding: 5px 6px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.patient-list-page :deep(.ant-table-tbody > tr > td) {
  padding: 4px 6px;
  font-size: 12px;
  line-height: 1.35;
  vertical-align: top;
}

.patient-list-page :deep(.ant-pagination) {
  margin: 8px 6px 2px;
}

.patient-list-page :deep(.ant-progress) {
  line-height: 1;
}

.patient-list-page :deep(.ant-progress-inner) {
  background-color: rgb(15 23 42 / 8%);
}

.patient-list-page :deep(.ant-tag) {
  margin-inline-end: 0;
  padding-inline: 6px;
  font-size: 11px;
  line-height: 18px;
}

.patient-list-page :deep(.ant-input),
.patient-list-page :deep(.ant-input-affix-wrapper),
.patient-list-page :deep(.ant-select-selector),
.patient-list-page :deep(.ant-btn) {
  border-radius: 8px;
}

.patient-list-page :deep(.ant-select-single.ant-select-sm .ant-select-selector),
.patient-list-page :deep(.ant-input-sm),
.patient-list-page :deep(.ant-input-affix-wrapper-sm) {
  min-height: 30px;
}

.patient-list-page :deep(.ant-btn-sm) {
  height: 30px;
  padding-inline: 10px;
}

.patient-list-page :deep(.ant-table-pagination-right) {
  justify-content: flex-end;
}

.summary-hero {
  min-height: 82px;
}

.summary-hero__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.summary-hero__title {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.2;
}

.summary-hero__tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgb(255 255 255 / 15%);
  font-size: 10px;
  line-height: 1.2;
}

.summary-hero__body {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  margin-top: 10px;
}

.summary-hero__main {
  min-width: 0;
}

.summary-hero__total {
  font-size: 34px;
  font-weight: 700;
  line-height: 0.95;
}

.summary-hero__desc {
  margin-top: 4px;
  font-size: 11px;
  color: rgb(255 255 255 / 82%);
  line-height: 1.3;
}

.summary-hero__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  min-width: 110px;
}

.summary-hero__metric {
  min-width: 0;
}

.summary-hero__metric span {
  display: block;
  font-size: 10px;
  color: rgb(255 255 255 / 70%);
  line-height: 1.2;
}

.summary-hero__metric strong {
  display: block;
  margin-top: 3px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
}

.stage-strip {
  gap: 1px;
  border-color: rgb(226 232 240 / 90%);
  background: rgb(226 232 240 / 80%);
}

.stage-card {
  position: relative;
  display: flex;
  min-height: 76px;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  background: #fff;
}

.stage-card--active {
  background: linear-gradient(135deg, #40a9ff 0%, #1677ff 100%);
}

.stage-card__dot {
  position: absolute;
  top: 11px;
  right: 12px;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  opacity: 0.9;
}

.stage-card__count {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
}

.stage-card__label {
  color: rgb(15 23 42 / 60%);
  font-size: 11px;
  line-height: 1.2;
}

.stage-card--active .stage-card__count,
.stage-card--active .stage-card__label {
  color: #fff;
}

.stage-card--active .stage-card__dot {
  background-color: rgb(255 255 255 / 88%) !important;
}

.patient-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.patient-cell__avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex: none;
  margin-top: 1px;
  overflow: hidden;
  border: 1px solid rgb(148 163 184 / 18%);
  border-radius: 999px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef5ff 100%);
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 55%);
}

.patient-cell__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.patient-cell__avatar-text {
  color: #3b82f6;
  font-size: 13px;
  font-weight: 700;
  line-height: 1;
}

.patient-cell__body {
  min-width: 0;
  flex: 1;
}

.patient-cell__title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px 6px;
  font-size: 11px;
  line-height: 1.35;
  color: rgb(15 23 42 / 68%);
}

.patient-cell__name {
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.25;
}

.patient-cell__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1px 8px;
  margin-top: 2px;
  font-size: 11px;
  line-height: 1.35;
  color: rgb(15 23 42 / 60%);
}

.patient-cell__meta--muted {
  color: rgb(15 23 42 / 42%);
}

.patient-cell__meta span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.diagnosis-cell {
  display: -webkit-box;
  overflow: hidden;
  color: rgb(15 23 42 / 78%);
  font-size: 12px;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.stage-cell {
  min-width: 0;
}

.stage-cell__head {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
}

.stage-cell__dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  flex: none;
}

.stage-cell__badge {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  height: 18px;
  padding: 0 6px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
}

.stage-cell__node {
  overflow: hidden;
  margin-top: 3px;
  color: rgb(15 23 42 / 55%);
  font-size: 11px;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-cell {
  min-width: 0;
}

.progress-cell__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  color: rgb(15 23 42 / 50%);
  font-size: 11px;
  line-height: 1.2;
}

.progress-cell__head strong {
  color: #0f172a;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
}

.progress-cell__track {
  position: relative;
  height: 5px;
  margin-top: 5px;
  overflow: hidden;
  border-radius: 999px;
  background: rgb(15 23 42 / 10%);
}

.progress-cell__fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #95de64 0%, #52c41a 100%);
}

.time-cell {
  color: rgb(15 23 42 / 72%);
  font-size: 11px;
  line-height: 1.35;
  white-space: pre-line;
}

.patient-list-page :deep(.table-action),
.patient-list-page :deep(.table-action__action) {
  gap: 2px;
}

.patient-list-page :deep(.ant-btn-link) {
  padding-inline: 4px;
  font-size: 12px;
}

.patient-list-page :deep(.patient-action-more.ant-btn-link) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  padding-inline: 2px;
  white-space: nowrap;
}

.patient-list-page :deep(.patient-action-more .iconify) {
  font-size: 14px;
}

@media (max-width: 1280px) {
  .summary-hero__body {
    gap: 10px;
  }

  .summary-hero__stats {
    min-width: 96px;
  }
}

@media (max-width: 768px) {
  .summary-hero__body {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-hero__stats {
    width: 100%;
  }
}
</style>
