<script lang="ts" setup>
import type { TableColumnsType } from 'ant-design-vue';

import type { HospitalFeeRecordApi } from '#/api/hospital/fee-record';
import type { HospitalFeeSettlementApi } from '#/api/hospital/fee-settlement';
import type { HospitalPatientApi } from '#/api/hospital/patient';

import { computed, onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';

import { ACTION_ICON, TableAction } from '#/adapter/vxe-table';
import {
  deleteFeeRecord,
  getFeeRecordPage,
} from '#/api/hospital/fee-record';
import {
  getFeeSettlementPage,
  reverseFeeSettlement,
} from '#/api/hospital/fee-settlement';
import { getPatientDashboardPage } from '#/api/hospital/patient';

import SettleForm from '../settlement/modules/settle-form.vue';
import HospitalFormSheet from '#/views/hospital/_shared/hospital-form-sheet.vue';
import Form from './modules/form.vue';

type PatientFilterMode = 'all' | 'pending';

type PatientFeeStats = {
  count: number;
  settledAmount: number;
  totalAmount: number;
  waitAmount: number;
  waitCount: number;
};

const sourceTypeLabelMap: Record<string, string> = {
  CT_APPOINTMENT: '定位费',
  OTHER: '其他',
  PLAN_APPLY: '计划费',
  TREATMENT_APPOINTMENT: '治疗预约',
  TREATMENT_EXECUTE: '治疗执行',
};
const settlementLabelMap: Record<string, string> = {
  PART_SETTLEMENT: '部分收费',
  SETTLED: '已收费',
  VOID: '已冲正',
  WAIT_SETTLEMENT: '待收费',
};
const paymentTypeLabelMap: Record<string, string> = {
  CASH: '现金',
  INSURANCE: '医保',
  ONLINE: '线上支付',
  SELF_PAY: '自费',
  WECHAT: '微信',
  ALIPAY: '支付宝',
};
const patientColumns: TableColumnsType<HospitalPatientApi.PatientDashboardRow> = [
  { title: '姓名/性别/年龄', key: 'patientInfo', width: 190 },
  { title: '放疗号', dataIndex: 'radiotherapyNo', key: 'radiotherapyNo', width: 110 },
  { title: '诊断', dataIndex: 'diagnosis', key: 'diagnosis', width: 140 },
  { title: '主诊医生', dataIndex: 'managerDoctorName', key: 'managerDoctorName', width: 96 },
  { title: '当前阶段', dataIndex: 'currentNodeName', key: 'currentNodeName', width: 110 },
  { title: '待收费', key: 'pendingFee', width: 100 },
];
const feeColumns: TableColumnsType<HospitalFeeRecordApi.FeeRecord> = [
  { title: '序号', key: 'index', width: 58 },
  { title: '收费项目', dataIndex: 'itemName', key: 'itemName', width: 200 },
  { title: '费用类型', dataIndex: 'sourceType', key: 'sourceType', width: 110 },
  { title: '收费编码', dataIndex: 'bizNo', key: 'bizNo', width: 140 },
  { title: '单价/单位', dataIndex: 'unitPrice', key: 'unitPrice', width: 100 },
  { title: '应收数量', dataIndex: 'quantity', key: 'quantity', width: 88 },
  { title: '应收金额', dataIndex: 'amount', key: 'amount', width: 96 },
  { title: '状态', dataIndex: 'settlementStatus', key: 'settlementStatus', width: 98 },
  { title: '操作', key: 'actions', fixed: 'right', width: 130 },
];

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [SettleModal, settleModalApi] = useVbenModal({
  connectedComponent: SettleForm,
  destroyOnClose: true,
});

const loading = ref(false);
const patientFilterMode = ref<PatientFilterMode>('pending');
const patientKeyword = ref('');
const sourceType = ref<string>();
const activePatientId = ref<number>();
const patientRows = ref<HospitalPatientApi.PatientDashboardRow[]>([]);
const feeOverviewRows = ref<HospitalFeeRecordApi.FeeRecord[]>([]);
const patientFeeRows = ref<HospitalFeeRecordApi.FeeRecord[]>([]);
const patientSettlementRows = ref<HospitalFeeSettlementApi.FeeSettlement[]>([]);
const selectedFeeKeys = ref<number[]>([]);

function toAmount(value?: number | string) {
  const amount = Number(value || 0);
  return Number.isFinite(amount) ? amount : 0;
}

function formatMoney(value?: number | string) {
  return toAmount(value).toFixed(2);
}

function formatDateTime(value?: unknown) {
  if (!value) return '--';
  if (Array.isArray(value) && value.length >= 6) {
    const [year, month, day, hour, minute, second] = value.map((item) =>
      Number(item || 0),
    );
    return dayjs(
      `${year}-${`${month}`.padStart(2, '0')}-${`${day}`.padStart(2, '0')} ${`${hour}`.padStart(2, '0')}:${`${minute}`.padStart(2, '0')}:${`${second}`.padStart(2, '0')}`,
    ).format('YYYY-MM-DD HH:mm:ss');
  }
  return dayjs(value as string).isValid()
    ? dayjs(value as string).format('YYYY-MM-DD HH:mm:ss')
    : '--';
}

function formatGender(value?: number) {
  if (value === 1) return '男';
  if (value === 2) return '女';
  return '--';
}

function formatSourceType(value?: string) {
  return sourceTypeLabelMap[value || ''] || value || '--';
}

function formatSettlementStatus(value?: string) {
  return settlementLabelMap[value || ''] || value || '--';
}

function formatPaymentType(value?: string) {
  return paymentTypeLabelMap[value || ''] || value || '--';
}

function getSettlementColor(value?: string) {
  switch (value) {
    case 'SETTLED': {
      return 'success';
    }
    case 'PART_SETTLEMENT': {
      return 'processing';
    }
    case 'VOID': {
      return 'default';
    }
    default: {
      return 'warning';
    }
  }
}

const feeStatsMap = computed(() => {
  const map = new Map<number, PatientFeeStats>();
  for (const row of feeOverviewRows.value) {
    const patientId = row.patientId;
    if (!patientId) continue;
    const current = map.get(patientId) || {
      count: 0,
      settledAmount: 0,
      totalAmount: 0,
      waitAmount: 0,
      waitCount: 0,
    };
    current.count += 1;
    current.totalAmount += toAmount(row.amount);
    if (row.settlementStatus === 'WAIT_SETTLEMENT') {
      current.waitAmount += toAmount(row.amount);
      current.waitCount += 1;
    } else if (row.settlementStatus !== 'VOID') {
      current.settledAmount += toAmount(row.amount);
    }
    map.set(patientId, current);
  }
  return map;
});

const patientRowsForFee = computed(() => {
  const feePatientIds = new Set(
    feeOverviewRows.value
      .map((item) => item.patientId)
      .filter((item): item is number => Number.isFinite(item)),
  );
  let list = patientRows.value.filter(
    (item) => feePatientIds.size === 0 || feePatientIds.has(item.id),
  );
  const keyword = patientKeyword.value.trim().toLowerCase();
  if (keyword) {
    list = list.filter((item) => {
      return [
        item.name,
        item.patientNo,
        item.radiotherapyNo,
        item.diagnosis,
        item.managerDoctorName,
      ].some((field) => `${field || ''}`.toLowerCase().includes(keyword));
    });
  }
  if (patientFilterMode.value === 'pending') {
    list = list.filter((item) => (feeStatsMap.value.get(item.id)?.waitAmount || 0) > 0);
  }
  return list.toSorted((left, right) => {
    const waitDiff =
      (feeStatsMap.value.get(right.id)?.waitAmount || 0) -
      (feeStatsMap.value.get(left.id)?.waitAmount || 0);
    if (waitDiff !== 0) return waitDiff;
    return left.id - right.id;
  });
});

const activePatient = computed(() => {
  return patientRowsForFee.value.find((item) => item.id === activePatientId.value);
});

const sourceTypeOptions = computed(() => {
  const set = new Set<string>();
  for (const row of patientFeeRows.value) {
    if (row.sourceType) set.add(row.sourceType);
  }
  return [...set].map((item) => ({ label: formatSourceType(item), value: item }));
});

const displayedFeeRows = computed(() => {
  if (!sourceType.value) return patientFeeRows.value;
  return patientFeeRows.value.filter((item) => item.sourceType === sourceType.value);
});

const selectedFeeRows = computed(() => {
  const idSet = new Set(selectedFeeKeys.value);
  if (idSet.size === 0) return [];
  return patientFeeRows.value.filter((item) => item.id && idSet.has(item.id));
});

const confirmFeeRows = computed(() => {
  if (selectedFeeRows.value.length > 0) {
    return selectedFeeRows.value;
  }
  return patientFeeRows.value.filter(
    (item) => item.settlementStatus === 'WAIT_SETTLEMENT',
  );
});

const selectedPatientStats = computed(() => {
  if (!activePatientId.value) return undefined;
  return feeStatsMap.value.get(activePatientId.value);
});

const latestSettlement = computed(() => {
  return (
    patientSettlementRows.value.find((item) => item.settlementStatus !== 'VOID') ||
    patientSettlementRows.value[0]
  );
});

const totalReceivable = computed(() => {
  return displayedFeeRows.value.reduce((sum, item) => sum + toAmount(item.amount), 0);
});

const totalReceived = computed(() => {
  return displayedFeeRows.value.reduce((sum, item) => {
    return item.settlementStatus && item.settlementStatus !== 'WAIT_SETTLEMENT' && item.settlementStatus !== 'VOID'
      ? sum + toAmount(item.amount)
      : sum;
  }, 0);
});

const totalPending = computed(() => {
  return displayedFeeRows.value.reduce((sum, item) => {
    return item.settlementStatus === 'WAIT_SETTLEMENT'
      ? sum + toAmount(item.amount)
      : sum;
  }, 0);
});

const confirmTotalAmount = computed(() => {
  return confirmFeeRows.value.reduce((sum, item) => sum + toAmount(item.amount), 0);
});

const confirmFeeCount = computed(() => confirmFeeRows.value.length);

const pendingPatientCount = computed(() => {
  return patientRowsForFee.value.filter(
    (item) => (feeStatsMap.value.get(item.id)?.waitAmount || 0) > 0,
  ).length;
});

const rowSelection = computed(() => ({
  selectedRowKeys: selectedFeeKeys.value,
  onChange: (keys: (number | string)[]) => {
    selectedFeeKeys.value = keys.map((item) => Number(item)).filter((item) => Number.isFinite(item));
  },
  getCheckboxProps: (record: HospitalFeeRecordApi.FeeRecord) => ({
    disabled: record.settlementStatus !== 'WAIT_SETTLEMENT',
  }),
}));

async function loadPatientList() {
  const page = await getPatientDashboardPage({
    keyword: patientKeyword.value.trim() || undefined,
    pageNo: 1,
    pageSize: 200,
  });
  patientRows.value = page.list;
}

async function loadFeeOverview() {
  const page = await getFeeRecordPage({
    pageNo: 1,
    pageSize: 500,
  });
  feeOverviewRows.value = page.list;
}

function ensureActivePatient() {
  if (
    activePatientId.value &&
    patientRowsForFee.value.some((item) => item.id === activePatientId.value)
  ) {
    return;
  }
  activePatientId.value = patientRowsForFee.value[0]?.id;
}

async function loadActivePatientDetail() {
  selectedFeeKeys.value = [];
  if (!activePatientId.value) {
    patientFeeRows.value = [];
    patientSettlementRows.value = [];
    return;
  }
  const [feePage, settlementPage] = await Promise.all([
    getFeeRecordPage({ pageNo: 1, pageSize: 200, patientId: activePatientId.value }),
    getFeeSettlementPage({ pageNo: 1, pageSize: 20, patientId: activePatientId.value }),
  ]);
  patientFeeRows.value = feePage.list;
  patientSettlementRows.value = settlementPage.list;
}

async function loadPageData() {
  loading.value = true;
  try {
    await Promise.all([loadPatientList(), loadFeeOverview()]);
    ensureActivePatient();
    await loadActivePatientDetail();
  } finally {
    loading.value = false;
  }
}

async function refreshCurrentPatient() {
  loading.value = true;
  try {
    await loadFeeOverview();
    ensureActivePatient();
    await loadActivePatientDetail();
  } finally {
    loading.value = false;
  }
}

function handlePatientSelect(row: HospitalPatientApi.PatientDashboardRow) {
  if (activePatientId.value === row.id) return;
  activePatientId.value = row.id;
  void loadActivePatientDetail();
}

function getPatientCustomRow(record: HospitalPatientApi.PatientDashboardRow) {
  return { onClick: () => handlePatientSelect(record) };
}

function getPatientRowClassName(record: HospitalPatientApi.PatientDashboardRow) {
  return record.id === activePatientId.value ? 'patient-row--active' : '';
}

function handlePatientSearch() {
  void loadPageData();
}

function handleCreateFee() {
  if (!activePatient.value) {
    message.warning('请先选择患者');
    return;
  }
  formModalApi
    .setData({
      chargeUserId: activePatient.value.managerDoctorId,
      patientId: activePatient.value.id,
      sourceType: 'OTHER',
      status: 0,
    })
    .open();
}

function handleEdit(row: HospitalFeeRecordApi.FeeRecord) {
  formModalApi.setData(row).open();
}

async function handleDeleteSingle(row: HospitalFeeRecordApi.FeeRecord) {
  const hide = message.loading({
    content: `正在删除费用【${row.itemName}】`,
    duration: 0,
  });
  try {
    await deleteFeeRecord(row.id!);
    message.success('删除成功');
    await refreshCurrentPatient();
  } finally {
    hide();
  }
}

function handleDeleteSelected() {
  if (!selectedFeeKeys.value.length) {
    message.warning('请先勾选待收费项目');
    return;
  }
  Modal.confirm({
    centered: true,
    title: `确认删除选中的 ${selectedFeeKeys.value.length} 条费用记录吗？`,
    async onOk() {
      for (const id of selectedFeeKeys.value) {
        await deleteFeeRecord(id);
      }
      message.success('删除成功');
      await refreshCurrentPatient();
    },
  });
}

function handleSettle() {
  if (!activePatient.value) {
    message.warning('请先选择患者');
    return;
  }
  if (confirmFeeRows.value.length <= 0 || confirmTotalAmount.value <= 0) {
    message.warning('当前患者没有可收费项目');
    return;
  }
  settleModalApi
    .setData({
      cashierId: activePatient.value.managerDoctorId,
      patientId: activePatient.value.id,
      patientName: activePatient.value.name,
      payableAmount: confirmTotalAmount.value,
      recordIds: confirmFeeRows.value
        .map((item) => item.id)
        .filter((item): item is number => Number.isFinite(item)),
      status: 0,
      totalAmount: confirmTotalAmount.value,
    })
    .open();
}

function handleReverseSettlement() {
  if (!activePatient.value) {
    message.warning('请先选择患者');
    return;
  }
  if (!latestSettlement.value || latestSettlement.value.settlementStatus === 'VOID') {
    message.warning('当前患者没有可退费的结算单');
    return;
  }
  Modal.confirm({
    centered: true,
    title: `确认冲正结算单【${latestSettlement.value.bizNo}】吗？`,
    async onOk() {
      await reverseFeeSettlement(latestSettlement.value!.id!);
      message.success('退费确认成功');
      await refreshCurrentPatient();
    },
  });
}

function handlePrint() {
  if (!activePatient.value) {
    message.warning('请先选择患者');
    return;
  }
  const printWindow = window.open('', '_blank', 'width=1024,height=768');
  if (!printWindow) {
    message.warning('打印窗口打开失败，请检查浏览器拦截设置');
    return;
  }
  const rows = displayedFeeRows.value
    .map(
      (item, index) => `
        <tr>
          <td>${index + 1}</td>
          <td>${item.itemName || ''}</td>
          <td>${formatSourceType(item.sourceType)}</td>
          <td>${item.bizNo || ''}</td>
          <td style="text-align:right;">${formatMoney(item.unitPrice)}</td>
          <td style="text-align:right;">${toAmount(item.quantity)}</td>
          <td style="text-align:right;">${formatMoney(item.amount)}</td>
          <td>${formatSettlementStatus(item.settlementStatus)}</td>
        </tr>
      `,
    )
    .join('');
  printWindow.document.write(`
    <html>
      <head>
        <title>费用确认单</title>
        <style>
          body { font-family: Arial, 'PingFang SC', sans-serif; padding: 24px; color: #0f172a; }
          h1 { font-size: 24px; margin: 0 0 8px; }
          .meta { color: #475569; font-size: 13px; margin-bottom: 16px; }
          table { border-collapse: collapse; width: 100%; }
          th, td { border: 1px solid #dbe2ea; padding: 8px 10px; font-size: 12px; }
          th { background: #f8fafc; }
          .total { display: flex; justify-content: flex-end; gap: 24px; margin-top: 16px; font-size: 13px; }
        </style>
      </head>
      <body>
        <h1>费用确认单</h1>
        <div class="meta">
          患者：${activePatient.value.name} / ${formatGender(activePatient.value.gender)} / ${activePatient.value.age || '--'}岁
          &nbsp;&nbsp;放疗号：${activePatient.value.radiotherapyNo || '--'}
          &nbsp;&nbsp;诊断：${activePatient.value.diagnosis || '--'}
        </div>
        <table>
          <thead>
            <tr>
              <th>序号</th>
              <th>收费项目</th>
              <th>费用类型</th>
              <th>收费编码</th>
              <th>单价</th>
              <th>数量</th>
              <th>金额</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
        <div class="total">
          <span>应收总额：${formatMoney(totalReceivable.value)}</span>
          <span>已收金额：${formatMoney(totalReceived.value)}</span>
          <span>待收费：${formatMoney(totalPending.value)}</span>
        </div>
      </body>
    </html>
  `);
  printWindow.document.close();
  printWindow.focus();
  printWindow.print();
}

function getPatientPendingAmount(patientId: number) {
  return formatMoney(feeStatsMap.value.get(patientId)?.waitAmount || 0);
}

function getPatientAvatarText(row: HospitalPatientApi.PatientDashboardRow) {
  return row.name?.slice(0, 1) || '患';
}

onMounted(() => {
  void loadPageData();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshCurrentPatient" />
    <SettleModal @success="refreshCurrentPatient" />
    <div class="fee-confirm-page">
      <div class="left-panel card-box">
        <div class="panel-header">
          <div>
            <div class="panel-title">费用确认</div>
            <div class="panel-subtitle">患者收费工作台</div>
          </div>
          <div class="panel-stats">
            <div
              class="stat-tab"
              :class="{ 'stat-tab--active': patientFilterMode === 'pending' }"
              @click="patientFilterMode = 'pending'"
            >
              待处理 {{ pendingPatientCount }}
            </div>
            <div
              class="stat-tab"
              :class="{ 'stat-tab--active': patientFilterMode === 'all' }"
              @click="patientFilterMode = 'all'"
            >
              全部 {{ patientRowsForFee.length }}
            </div>
          </div>
        </div>

        <div class="panel-toolbar">
          <a-input-search
            v-model:value="patientKeyword"
            allow-clear
            placeholder="姓名/放疗号/诊断/医生"
            @search="handlePatientSearch"
          />
          <a-button @click="handlePatientSearch">刷新</a-button>
        </div>

        <a-table
          :columns="patientColumns"
          :custom-row="getPatientCustomRow"
          :data-source="patientRowsForFee"
          :loading="loading"
          :pagination="false"
          :row-class-name="getPatientRowClassName"
          row-key="id"
          size="small"
          :scroll="{ y: 620, x: 760 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'patientInfo'">
              <div class="patient-cell">
                <div class="patient-avatar">
                  <img
                    v-if="record.photoUrl"
                    :src="record.photoUrl"
                    alt="avatar"
                  />
                  <span v-else>{{ getPatientAvatarText(record) }}</span>
                </div>
                <div class="patient-cell__info">
                  <div class="patient-cell__name">
                    {{ record.name }}/{{ formatGender(record.gender) }}/{{ record.age || '--' }}
                  </div>
                  <div class="patient-cell__sub">
                    {{ record.patientNo || '--' }}
                  </div>
                </div>
              </div>
            </template>
            <template v-else-if="column.key === 'pendingFee'">
              <span class="money-text">{{ getPatientPendingAmount(record.id) }}</span>
            </template>
            <template v-else>
              <span>{{ record[column.dataIndex as keyof HospitalPatientApi.PatientDashboardRow] || '--' }}</span>
            </template>
          </template>
        </a-table>
      </div>

      <div class="right-panel card-box">
        <div v-if="activePatient" class="patient-summary">
          <div class="summary-avatar">
            <img v-if="activePatient.photoUrl" :src="activePatient.photoUrl" alt="avatar" />
            <span v-else>{{ getPatientAvatarText(activePatient) }}</span>
          </div>
          <div class="summary-main">
            <div class="summary-title-row">
              <div class="summary-title">
                {{ activePatient.name }}/{{ formatGender(activePatient.gender) }}/{{ activePatient.age || '--' }}
              </div>
              <a-button type="link" size="small" @click="handlePrint">打印确认单</a-button>
            </div>
            <div class="summary-meta">
              <span>病案号：{{ activePatient.patientNo || '--' }}</span>
              <span>放疗号：{{ activePatient.radiotherapyNo || '--' }}</span>
              <span>主诊医生：{{ activePatient.managerDoctorName || '--' }}</span>
            </div>
            <div class="summary-meta">
              <span>诊断：{{ activePatient.diagnosis || '--' }}</span>
              <span>当前节点：{{ activePatient.currentNodeName || '--' }}</span>
              <span>登记时间：{{ formatDateTime(activePatient.registrationTime) }}</span>
            </div>
            <div class="summary-tags">
              <div class="summary-tag">
                <label>待收费</label>
                <strong>{{ formatMoney(selectedPatientStats?.waitAmount) }}</strong>
              </div>
              <div class="summary-tag">
                <label>已收费</label>
                <strong>{{ formatMoney(selectedPatientStats?.settledAmount) }}</strong>
              </div>
              <div class="summary-tag">
                <label>确认项目</label>
                <strong>{{ confirmFeeCount }}</strong>
              </div>
              <div class="summary-tag summary-tag--light">
                <label>最近结算</label>
                <strong>{{ latestSettlement?.bizNo || '--' }}</strong>
              </div>
            </div>
          </div>
        </div>

        <div class="fee-content">
          <div class="fee-workbench">
            <div class="fee-toolbar">
              <div>
                <div class="fee-toolbar__title">费用明细</div>
                <div class="fee-toolbar__hint">
                  已选 {{ selectedFeeRows.length }} 项，不选时默认按全部待收费项目结算
                </div>
              </div>
              <div class="fee-toolbar__controls">
                <a-select
                  v-model:value="sourceType"
                  allow-clear
                  class="w-[180px]"
                  placeholder="按费用类型"
                >
                  <a-select-option
                    v-for="item in sourceTypeOptions"
                    :key="item.value"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </a-select-option>
                </a-select>
                <a-button @click="handlePrint">打印</a-button>
                <a-button danger @click="handleDeleteSelected">删除费用</a-button>
                <a-button @click="handleReverseSettlement">退费确认</a-button>
                <a-button type="primary" ghost @click="handleSettle">收费确认</a-button>
                <a-button type="primary" @click="handleCreateFee">新增费用</a-button>
              </div>
            </div>

            <div class="fee-table-wrap">
              <a-table
                :columns="feeColumns"
                :data-source="displayedFeeRows"
                :loading="loading"
                :pagination="false"
                :row-selection="rowSelection"
                :scroll="{ y: 510, x: 980 }"
                row-key="id"
                size="small"
              >
                <template #bodyCell="{ column, record, index }">
                  <template v-if="column.key === 'index'">
                    {{ index + 1 }}
                  </template>
                  <template v-else-if="column.key === 'sourceType'">
                    {{ formatSourceType(record.sourceType) }}
                  </template>
                  <template v-else-if="column.key === 'unitPrice'">
                    {{ formatMoney(record.unitPrice) }}/次
                  </template>
                  <template v-else-if="column.key === 'quantity'">
                    {{ toAmount(record.quantity) }}
                  </template>
                  <template v-else-if="column.key === 'amount'">
                    <span class="money-text">{{ formatMoney(record.amount) }}</span>
                  </template>
                  <template v-else-if="column.key === 'settlementStatus'">
                    <a-tag :color="getSettlementColor(record.settlementStatus)">
                      {{ formatSettlementStatus(record.settlementStatus) }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'actions'">
                    <TableAction
                      :actions="[
                        {
                          label: '编辑',
                          type: 'link',
                          icon: ACTION_ICON.EDIT,
                          auth: ['hospital:fee-record:update'],
                          ifShow: record.settlementStatus === 'WAIT_SETTLEMENT',
                          onClick: handleEdit.bind(null, record),
                        },
                        {
                          label: '删除',
                          type: 'link',
                          danger: true,
                          icon: ACTION_ICON.DELETE,
                          auth: ['hospital:fee-record:delete'],
                          ifShow: record.settlementStatus === 'WAIT_SETTLEMENT',
                          onClick: handleDeleteSingle.bind(null, record),
                        },
                      ]"
                    />
                  </template>
                  <template v-else>
                    {{ record[column.dataIndex as keyof HospitalFeeRecordApi.FeeRecord] || '--' }}
                  </template>
                </template>
              </a-table>
            </div>

            <div class="fee-footer">
              <div>应收总额：<span class="money-text">{{ formatMoney(totalReceivable) }}</span></div>
              <div>已收金额：<span class="money-text money-text--green">{{ formatMoney(totalReceived) }}</span></div>
              <div>待收费：<span class="money-text money-text--orange">{{ formatMoney(totalPending) }}</span></div>
            </div>
          </div>

          <div class="fee-document-panel">
            <HospitalFormSheet
              :code="latestSettlement?.bizNo || ''"
              :meta-text="activePatient ? `患者：${activePatient.name}｜放疗号：${activePatient.radiotherapyNo || '--'}` : ''"
              title="费用确认单"
            >
              <div class="confirm-sheet">
                <div class="confirm-sheet__patient">
                  <div><label>患者姓名</label><span>{{ activePatient?.name || '--' }}</span></div>
                  <div><label>性别/年龄</label><span>{{ activePatient ? `${formatGender(activePatient.gender)}/${activePatient.age || '--'}岁` : '--' }}</span></div>
                  <div><label>病案号</label><span>{{ activePatient?.patientNo || '--' }}</span></div>
                  <div><label>放疗号</label><span>{{ activePatient?.radiotherapyNo || '--' }}</span></div>
                  <div><label>主诊医生</label><span>{{ activePatient?.managerDoctorName || '--' }}</span></div>
                  <div><label>当前节点</label><span>{{ activePatient?.currentNodeName || '--' }}</span></div>
                  <div class="confirm-sheet__patient--full"><label>诊断</label><span>{{ activePatient?.diagnosis || '--' }}</span></div>
                </div>

                <table class="confirm-sheet__table">
                  <thead>
                    <tr>
                      <th>序号</th>
                      <th>收费项目</th>
                      <th>类型</th>
                      <th>编码</th>
                      <th>单价</th>
                      <th>数量</th>
                      <th>金额</th>
                    </tr>
                  </thead>
                  <tbody v-if="confirmFeeRows.length > 0">
                    <tr v-for="(item, index) in confirmFeeRows" :key="item.id || index">
                      <td>{{ index + 1 }}</td>
                      <td>{{ item.itemName || '--' }}</td>
                      <td>{{ formatSourceType(item.sourceType) }}</td>
                      <td>{{ item.bizNo || '--' }}</td>
                      <td class="is-number">{{ formatMoney(item.unitPrice) }}</td>
                      <td class="is-number">{{ toAmount(item.quantity) }}</td>
                      <td class="is-number amount-cell">{{ formatMoney(item.amount) }}</td>
                    </tr>
                  </tbody>
                  <tbody v-else>
                    <tr>
                      <td colspan="7" class="confirm-sheet__empty">暂无待确认费用项目</td>
                    </tr>
                  </tbody>
                </table>

                <div class="confirm-sheet__summary">
                  <div>
                    <label>确认项目</label>
                    <strong>{{ confirmFeeCount }} 项</strong>
                  </div>
                  <div>
                    <label>待收金额</label>
                    <strong class="money-text money-text--orange">{{ formatMoney(confirmTotalAmount) }}</strong>
                  </div>
                  <div>
                    <label>最近支付方式</label>
                    <strong>{{ formatPaymentType(latestSettlement?.paymentType) }}</strong>
                  </div>
                  <div>
                    <label>最近结算状态</label>
                    <strong>{{ formatSettlementStatus(latestSettlement?.settlementStatus || (confirmFeeCount > 0 ? 'WAIT_SETTLEMENT' : undefined)) }}</strong>
                  </div>
                </div>

                <div class="confirm-sheet__block">
                  <div class="confirm-sheet__block-title">结算追踪</div>
                  <div class="confirm-sheet__track">
                    <span>最近结算单号：{{ latestSettlement?.bizNo || '--' }}</span>
                    <span>收费员：{{ latestSettlement?.cashierName || activePatient?.managerDoctorName || '--' }}</span>
                    <span>支付时间：{{ formatDateTime(latestSettlement?.payTime) }}</span>
                    <span>说明：勾选费用后点击“收费确认”，可仅结算选中项目。</span>
                  </div>
                </div>

                <div class="confirm-sheet__sign">
                  <span>患者/家属确认：________________</span>
                  <span>收费员：{{ latestSettlement?.cashierName || activePatient?.managerDoctorName || '--' }}</span>
                </div>
              </div>
            </HospitalFormSheet>
          </div>
        </div>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.fee-confirm-page {
  display: grid;
  gap: 12px;
  grid-template-columns: minmax(380px, 420px) minmax(0, 1fr);
}

.left-panel,
.right-panel {
  display: flex;
  flex-direction: column;
  min-height: 860px;
  padding: 12px;
}

.panel-header,
.fee-toolbar,
.summary-title-row,
.summary-meta,
.summary-tags,
.panel-toolbar {
  align-items: center;
  display: flex;
}

.panel-header,
.fee-toolbar,
.summary-title-row {
  justify-content: space-between;
}

.panel-header {
  margin-bottom: 10px;
}

.panel-title,
.fee-toolbar__title {
  color: #111827;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

.panel-subtitle,
.fee-toolbar__hint {
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.45;
  margin-top: 4px;
}

.panel-stats {
  display: flex;
  gap: 8px;
}

.stat-tab {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  color: #64748b;
  cursor: pointer;
  font-size: 12px;
  padding: 6px 12px;
}

.stat-tab--active {
  background: #e0f2fe;
  border-color: #93c5fd;
  color: #2563eb;
}

.panel-toolbar {
  gap: 8px;
  margin-bottom: 10px;
}

.patient-cell {
  align-items: center;
  display: flex;
  gap: 10px;
}

.patient-avatar,
.summary-avatar {
  align-items: center;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
  border-radius: 999px;
  color: #2563eb;
  display: inline-flex;
  font-weight: 700;
  justify-content: center;
  overflow: hidden;
}

.patient-avatar {
  flex: none;
  height: 32px;
  width: 32px;
}

.summary-avatar {
  height: 58px;
  width: 58px;
}

.patient-avatar img,
.summary-avatar img {
  height: 100%;
  object-fit: cover;
  width: 100%;
}

.patient-cell__name {
  color: #0f172a;
  font-size: 12px;
  font-weight: 600;
}

.patient-cell__sub {
  color: #94a3b8;
  font-size: 11px;
  margin-top: 2px;
}

.money-text {
  color: #2563eb;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}

.money-text--green {
  color: #16a34a;
}

.money-text--orange {
  color: #ea580c;
}

.right-panel {
  gap: 12px;
}

.patient-summary {
  align-items: flex-start;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  display: flex;
  gap: 14px;
  padding: 12px;
}

.summary-main {
  flex: 1;
  min-width: 0;
}

.summary-title {
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.3;
}

.summary-meta {
  color: #64748b;
  flex-wrap: wrap;
  font-size: 12px;
  gap: 14px;
  margin-top: 6px;
}

.summary-tags {
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.summary-tag {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-width: 104px;
  padding: 8px 10px;
}

.summary-tag label {
  color: #94a3b8;
  display: block;
  font-size: 11px;
}

.summary-tag strong {
  color: #0f172a;
  display: block;
  font-size: 15px;
  line-height: 1.35;
  margin-top: 4px;
}

.summary-tag--light {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.fee-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 520px);
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.fee-workbench {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.fee-toolbar {
  align-items: flex-start;
  gap: 12px;
}

.fee-toolbar__controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.fee-table-wrap {
  flex: 1;
  min-height: 0;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.fee-footer {
  border-top: 1px solid #e5e7eb;
  color: #475569;
  display: flex;
  flex-wrap: wrap;
  font-size: 13px;
  gap: 18px;
  justify-content: flex-end;
  padding-top: 12px;
}

.fee-document-panel {
  min-width: 0;
  padding: 8px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: auto;
}

.confirm-sheet {
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: #1f2937;
}

.confirm-sheet__patient {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 14px;
}

.confirm-sheet__patient > div,
.confirm-sheet__summary > div {
  min-width: 0;
}

.confirm-sheet__patient label,
.confirm-sheet__summary label {
  display: block;
  color: #94a3b8;
  font-size: 11px;
  line-height: 1.3;
}

.confirm-sheet__patient span,
.confirm-sheet__summary strong {
  display: block;
  margin-top: 4px;
  color: #0f172a;
  font-size: 13px;
  line-height: 1.45;
  word-break: break-all;
}

.confirm-sheet__patient--full {
  grid-column: 1 / -1;
}

.confirm-sheet__table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.confirm-sheet__table th,
.confirm-sheet__table td {
  border: 1px solid #dbe2ea;
  padding: 7px 8px;
  font-size: 12px;
  line-height: 1.45;
  text-align: left;
  vertical-align: top;
  word-break: break-word;
}

.confirm-sheet__table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
}

.confirm-sheet__table .is-number {
  text-align: right;
}

.amount-cell {
  font-weight: 700;
}

.confirm-sheet__empty {
  color: #94a3b8;
  text-align: center !important;
  padding: 18px 8px !important;
}

.confirm-sheet__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.confirm-sheet__summary > div {
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
}

.confirm-sheet__block {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px 12px;
}

.confirm-sheet__block-title {
  color: #111827;
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 8px;
}

.confirm-sheet__track {
  display: grid;
  gap: 6px;
  color: #475569;
  font-size: 12px;
  line-height: 1.5;
}

.confirm-sheet__sign {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-top: 4px;
  color: #334155;
  font-size: 12px;
}

:deep(.fee-document-panel .hospital-form-sheet) {
  padding: 0;
}

:deep(.fee-document-panel .hospital-form-sheet__paper) {
  width: 100%;
  min-height: auto;
  padding: 22px 24px 20px;
  border-color: #dfe7f1;
  box-shadow: none;
}

:deep(.fee-document-panel .hospital-form-sheet__header) {
  grid-template-columns: 170px 1fr 92px;
}

:deep(.patient-row--active > td) {
  background: #eff6ff !important;
}

:deep(.ant-table-thead > tr > th) {
  padding: 8px 10px;
  white-space: nowrap;
}

:deep(.ant-table-tbody > tr > td) {
  padding: 8px 10px;
}

@media (max-width: 1560px) {
  .fee-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1360px) {
  .fee-confirm-page {
    grid-template-columns: 1fr;
  }

  .left-panel,
  .right-panel {
    min-height: auto;
  }
}

@media (max-width: 900px) {
  .confirm-sheet__summary,
  .confirm-sheet__patient {
    grid-template-columns: 1fr;
  }

  .confirm-sheet__sign,
  .summary-title-row,
  .fee-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .fee-toolbar__controls {
    justify-content: flex-start;
  }
}
</style>

