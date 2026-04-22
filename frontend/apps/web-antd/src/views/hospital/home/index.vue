<script lang="ts" setup>
import type { TableColumnsType } from 'ant-design-vue';

import type { PageResult } from '@vben/request';

import type { HospitalContourTaskApi } from '#/api/hospital/contour-task';
import type { HospitalCtQueueApi } from '#/api/hospital/ct-queue';
import type { HospitalFeeSettlementApi } from '#/api/hospital/fee-settlement';
import type { HospitalPatientApi } from '#/api/hospital/patient';
import type { HospitalPlanApplyApi } from '#/api/hospital/plan-apply';
import type { HospitalTreatmentAppointmentApi } from '#/api/hospital/treatment-appointment';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Empty, Input, message, Table } from 'ant-design-vue';

import { getContourTaskPage } from '#/api/hospital/contour-task';
import { getCtQueuePage } from '#/api/hospital/ct-queue';
import { getFeeSettlementPage } from '#/api/hospital/fee-settlement';
import { getPatientPage } from '#/api/hospital/patient';
import { getPlanApplyPage } from '#/api/hospital/plan-apply';
import { getTreatmentAppointmentPage } from '#/api/hospital/treatment-appointment';

type MonitorFilterKey = 'feeException' | 'highRisk' | 'workflowWarning';
type StatusTone = 'blue' | 'gold' | 'gray' | 'green' | 'red';

interface PatientCard {
  ageLabel: string;
  campusName: string;
  careLabel: string;
  doctorName: string;
  genderLabel: string;
  id: number;
  name: string;
  patientNo: string;
  patientTypeLabel: string;
  paymentTypeLabel: string;
  tags: string[];
  visitNo: string;
  wardName: string;
}

interface TodoItem {
  description: string;
  id: string;
  owner: string;
  patientName: string;
  route: string;
  sortAt: number;
  sortScore: number;
  statusClass: string;
  statusLabel: string;
  statusTone: StatusTone;
  time: string;
  title: string;
}

interface MonitorItem {
  age: string;
  doctorName: string;
  gender: string;
  id: number | string;
  nodeName: string;
  patientName: string;
  processName: string;
  route?: string;
  warningInfo: string;
}

const router = useRouter();

const loading = ref(false);
const monitorKeyword = ref('');
const activeFilter = ref<MonitorFilterKey>('workflowWarning');
const patientCards = ref<PatientCard[]>([]);
const todoItems = ref<TodoItem[]>([]);
const monitorDataMap = ref<Record<MonitorFilterKey, MonitorItem[]>>({
  feeException: [],
  highRisk: [],
  workflowWarning: [],
});
const summaryStats = ref({
  appointmentCount: 0,
  contourCount: 0,
  feeCount: 0,
  monitorCount: 0,
  patientCount: 0,
  planCount: 0,
  queueCount: 0,
  todoCount: 0,
});
const lastRefreshTime = ref('--');
const workbenchLoadWarning = ref('');

const planStatusLabelMap: Record<string, string> = {
  APPROVED: '已通过',
  DRAFT: '待提交',
  REJECTED: '已退回',
  REVIEWING: '审核中',
  VERIFIED: '已验证',
};
const contourStatusLabelMap: Record<string, string> = {
  APPROVED: '已通过',
  REJECTED: '已退回',
  REVIEWING: '审核中',
  WAIT_CONTOUR: '待勾画',
};
const treatmentStatusLabelMap: Record<string, string> = {
  DONE: '已完成',
  STOPPED: '已终止',
  TREATING: '治疗中',
  WAIT_APPOINTMENT: '待预约',
};
const queueStatusLabelMap: Record<string, string> = {
  CALLED: '已叫号',
  DONE: '已完成',
  QUEUING: '排队中',
  SKIPPED: '已过号',
  WAIT_SIGN: '待签到',
};
const feeStatusLabelMap: Record<string, string> = {
  PART_SETTLEMENT: '部分结算',
  SETTLED: '已结算',
  VOID: '已作废',
  WAIT_SETTLEMENT: '待结算',
};
const priorityLabelMap: Record<string, string> = {
  EMERGENCY: '紧急',
  NORMAL: '常规',
  URGENT: '加急',
};
const paymentTypeLabelMap: Record<string, string> = {
  COMMERCIAL: '商保',
  INSURANCE: '医保',
  SELF_PAY: '自费',
};
const toneScoreMap: Record<StatusTone, number> = {
  blue: 2,
  gold: 3,
  gray: 0,
  green: 1,
  red: 4,
};

const todoLegend = [
  { color: '#69b1ff', label: '待处理' },
  { color: '#73d13d', label: '已推进' },
  { color: '#faad14', label: '需关注' },
  { color: '#ff7875', label: '异常/退回' },
];

const summaryCards = computed(() => [
  {
    icon: 'mdi:account-group-outline',
    label: '患者',
    tone: 'green',
    value: summaryStats.value.patientCount,
  },
  {
    icon: 'mdi:file-document-edit-outline',
    label: '计划',
    tone: 'blue',
    value: summaryStats.value.planCount,
  },
  {
    icon: 'mdi:vector-square',
    label: '勾画',
    tone: 'purple',
    value: summaryStats.value.contourCount,
  },
  {
    icon: 'mdi:calendar-clock-outline',
    label: '预约',
    tone: 'cyan',
    value: summaryStats.value.appointmentCount,
  },
  {
    icon: 'mdi:account-voice',
    label: 'CT队列',
    tone: 'red',
    value: summaryStats.value.queueCount,
  },
  {
    icon: 'mdi:cash-register',
    label: '结算',
    tone: 'gold',
    value: summaryStats.value.feeCount,
  },
]);

const monitorFilters = computed(() => [
  {
    count: monitorDataMap.value.workflowWarning.length,
    key: 'workflowWarning' as MonitorFilterKey,
    label: '流程预警',
  },
  {
    count: monitorDataMap.value.highRisk.length,
    key: 'highRisk' as MonitorFilterKey,
    label: '高风险',
  },
  {
    count: monitorDataMap.value.feeException.length,
    key: 'feeException' as MonitorFilterKey,
    label: '费用异常',
  },
]);

const monitorColumns: TableColumnsType<MonitorItem> = [
  {
    dataIndex: 'patientName',
    key: 'patientName',
    title: '患者',
    customRender: ({ record }) =>
      `${record.patientName}/${record.gender}/${record.age}`,
  },
  {
    dataIndex: 'nodeName',
    key: 'nodeName',
    title: '任务节点',
  },
  {
    dataIndex: 'warningInfo',
    key: 'warningInfo',
    title: '异常信息',
  },
  {
    dataIndex: 'processName',
    key: 'processName',
    title: '流程名称',
  },
  {
    dataIndex: 'doctorName',
    key: 'doctorName',
    title: '主管医生',
  },
];

const filteredMonitorData = computed(() => {
  const keyword = monitorKeyword.value.trim().toLowerCase();
  const list = monitorDataMap.value[activeFilter.value];
  if (!keyword) {
    return list;
  }
  return list.filter((item) => {
    return [
      item.patientName,
      item.gender,
      item.age,
      item.nodeName,
      item.warningInfo,
      item.processName,
      item.doctorName,
    ]
      .join(' ')
      .toLowerCase()
      .includes(keyword);
  });
});

onMounted(() => {
  refreshWorkbench();
});

function createEmptyPageResult<T>(): PageResult<T> {
  return { list: [], total: 0 };
}

function unwrapPageResult<T>(result: PromiseSettledResult<PageResult<T>>) {
  return result.status === 'fulfilled'
    ? result.value
    : createEmptyPageResult<T>();
}

function formatGender(value?: number) {
  if (value === 1) return '男';
  if (value === 2) return '女';
  return '未知';
}

function formatAge(value?: number) {
  return value || value === 0 ? `${value}岁` : '--';
}

function formatPaymentType(value?: string) {
  if (!value) return '未标注';
  return paymentTypeLabelMap[value] || value;
}

function formatDateTime(value?: Date | number | string) {
  if (!value) return '--';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '--';
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  const hours = `${date.getHours()}`.padStart(2, '0');
  const minutes = `${date.getMinutes()}`.padStart(2, '0');
  return `${month}/${day} ${hours}:${minutes}`;
}

function splitTags(tags?: string) {
  return (tags || '')
    .split(/[，,、]/)
    .map((item) => item.trim())
    .filter(Boolean)
    .slice(0, 3);
}

function getStatusTone(status?: string): StatusTone {
  if (!status) return 'gray';
  if (['REJECTED', 'SKIPPED', 'STOPPED', 'VOID'].includes(status)) return 'red';
  if (['CALLED', 'PART_SETTLEMENT', 'REVIEWING', 'TREATING'].includes(status))
    return 'gold';
  if (['APPROVED', 'DONE', 'SETTLED', 'VERIFIED'].includes(status))
    return 'green';
  return 'blue';
}

function getPlanWarningInfo(status: string) {
  if (status === 'REJECTED') return '计划申请被退回，请尽快修订后重新提交';
  if (status === 'REVIEWING') return '计划申请正在审核中，请关注审批时效';
  return '计划申请仍未提交，影响后续计划设计';
}

function getContourWarningInfo(status: string) {
  if (status === 'REJECTED') return '勾画任务被退回，需重新修订靶区附件';
  if (status === 'REVIEWING') return '勾画任务审核中，请及时跟进审核意见';
  return '勾画任务尚未提交，可能影响计划设计排程';
}

function getQueueWarningInfo(status: string) {
  if (status === 'SKIPPED') return '患者已过号，需人工复核后重新排队';
  if (status === 'CALLED') return '已叫号仍未完成，请关注患者到位情况';
  return '患者尚未签到，排队链路存在延迟风险';
}

function getFeeWarningInfo(
  status: string,
  paidAmount?: number,
  payableAmount?: number,
) {
  if (status === 'VOID') return '结算单已作废，请确认原始收费记录与原因';
  if (status === 'PART_SETTLEMENT') {
    return `仍有¥${Number((payableAmount || 0) - (paidAmount || 0)).toFixed(2)}待收`;
  }
  return '费用仍未结算，请跟进收费或医保处理进度';
}

function getPatientProfile(
  patientId: number | undefined,
  patientName: string | undefined,
  patientMap: Map<number, HospitalPatientApi.Patient>,
) {
  const patient = patientId ? patientMap.get(patientId) : undefined;
  return {
    age: patient?.age ? `${patient.age}岁` : '--',
    doctorName:
      patient?.firstDoctorName ||
      patient?.managerDoctorName ||
      patient?.attendingDoctorName ||
      '未分配',
    gender: formatGender(patient?.gender),
    patientName: patient?.name || patientName || '--',
  };
}

function buildPatientCard(
  item: HospitalPatientApi.Patient,
): null | PatientCard {
  if (!item.id) return null;
  return {
    ageLabel: formatAge(item.age),
    campusName: item.campus || '本部院区',
    careLabel: `${item.patientType || '未分类'}/${formatPaymentType(item.paymentType)}`,
    doctorName:
      item.firstDoctorName ||
      item.managerDoctorName ||
      item.attendingDoctorName ||
      '未分配主管医生',
    genderLabel: formatGender(item.gender),
    id: item.id,
    name: item.name,
    patientNo: item.patientNo || '--',
    patientTypeLabel: item.patientType || '未分类',
    paymentTypeLabel: formatPaymentType(item.paymentType),
    tags: splitTags(item.tags),
    visitNo: item.visitNo || '--',
    wardName: item.wardName || item.patientType || '门诊患者',
  };
}

function pushTodoItem(todoPool: TodoItem[], item: TodoItem) {
  todoPool.push(item);
}

function pushMonitorItem(list: MonitorItem[], payload: MonitorItem) {
  list.push(payload);
}

function goRoute(path: string) {
  router.push(path).catch(() => {});
}

function goPatientRegistration() {
  goRoute('/hospital-flow/hospital-patient/registration');
}

function goPatientList() {
  goRoute('/hospital-flow/hospital-patient/list');
}

function selectFilter(key: MonitorFilterKey) {
  activeFilter.value = key;
}

function getMonitorRowProps(record: MonitorItem) {
  if (!record.route) return {};
  return {
    class: 'monitor-table__row monitor-table__row--clickable',
    onClick: () => goRoute(record.route!),
  };
}

async function refreshWorkbench() {
  loading.value = true;
  workbenchLoadWarning.value = '';
  try {
    const results = await Promise.allSettled([
      getPatientPage({ pageNo: 1, pageSize: 50 }),
      getPlanApplyPage({ pageNo: 1, pageSize: 20 }),
      getContourTaskPage({ pageNo: 1, pageSize: 20 }),
      getTreatmentAppointmentPage({ pageNo: 1, pageSize: 20 }),
      getCtQueuePage({ pageNo: 1, pageSize: 20 }),
      getFeeSettlementPage({ pageNo: 1, pageSize: 20 }),
    ]);

    const failedRequests = results
      .map((item, index) => ({
        item,
        name: [
          '患者数据',
          '计划申请',
          '勾画任务',
          '治疗预约',
          'CT叫号',
          '费用结算',
        ][index],
      }))
      .filter(({ item }) => item.status === 'rejected');

    if (failedRequests.length > 0) {
      const failedNames = failedRequests.map(({ name }) => name).join('、');
      workbenchLoadWarning.value = `部分数据加载失败：${failedNames}`;
      message.warning(
        `${workbenchLoadWarning.value}，请检查当前租户或接口状态`,
      );
      console.error('hospital workbench load failed', failedRequests);
    }

    const patientPage = unwrapPageResult<HospitalPatientApi.Patient>(
      results[0],
    );
    const planPage = unwrapPageResult<HospitalPlanApplyApi.PlanApply>(
      results[1],
    );
    const contourPage = unwrapPageResult<HospitalContourTaskApi.ContourTask>(
      results[2],
    );
    const appointmentPage =
      unwrapPageResult<HospitalTreatmentAppointmentApi.TreatmentAppointment>(
        results[3],
      );
    const queuePage = unwrapPageResult<HospitalCtQueueApi.CtQueue>(results[4]);
    const feePage = unwrapPageResult<HospitalFeeSettlementApi.FeeSettlement>(
      results[5],
    );

    const patientMap = new Map<number, HospitalPatientApi.Patient>();
    (patientPage.list || []).forEach((item) => {
      if (item.id) patientMap.set(item.id, item);
    });

    patientCards.value = (patientPage.list || [])
      .map((item) => buildPatientCard(item))
      .filter(Boolean)
      .slice(0, 12) as PatientCard[];

    const todoPool: TodoItem[] = [];
    const workflowWarning: MonitorItem[] = [];
    const highRisk: MonitorItem[] = [];
    const feeException: MonitorItem[] = [];

    (planPage.list || []).forEach((item) => {
      const status = item.workflowStatus || '';
      if (!item.id || !['DRAFT', 'REJECTED', 'REVIEWING'].includes(status))
        return;
      const tone = getStatusTone(status);
      pushTodoItem(todoPool, {
        description: `${item.treatmentSite || '放疗计划'} · ${priorityLabelMap[item.priority || 'NORMAL'] || '常规'}`,
        id: `plan-${item.id}`,
        owner:
          item.applyDoctorName ||
          getPatientProfile(item.patientId, item.patientName, patientMap)
            .doctorName,
        patientName: item.patientName || '--',
        route: '/hospital-flow/hospital-plan/apply',
        sortAt: new Date(item.submitTime || item.createTime || 0).getTime(),
        sortScore: toneScoreMap[tone],
        statusClass: `is-${tone}`,
        statusLabel: planStatusLabelMap[status] || status,
        statusTone: tone,
        time: formatDateTime(item.submitTime || item.createTime),
        title: '计划申请待处理',
      });
      pushMonitorItem(workflowWarning, {
        ...getPatientProfile(item.patientId, item.patientName, patientMap),
        id: item.id,
        nodeName: planStatusLabelMap[status] || '计划申请',
        processName: '计划申请流程',
        route: '/hospital-flow/hospital-plan/apply',
        warningInfo: getPlanWarningInfo(status),
      });
    });

    (contourPage.list || []).forEach((item) => {
      const status = item.workflowStatus || '';
      if (
        !item.id ||
        !['REJECTED', 'REVIEWING', 'WAIT_CONTOUR'].includes(status)
      )
        return;
      const tone = getStatusTone(status);
      pushTodoItem(todoPool, {
        description: `${item.treatmentSite || '靶区'} · ${item.versionNo || 'V1'}`,
        id: `contour-${item.id}`,
        owner:
          item.contourDoctorName ||
          getPatientProfile(item.patientId, item.patientName, patientMap)
            .doctorName,
        patientName: item.patientName || '--',
        route: '/hospital-flow/hospital-contour/task',
        sortAt: new Date(item.submitTime || item.createTime || 0).getTime(),
        sortScore: toneScoreMap[tone],
        statusClass: `is-${tone}`,
        statusLabel: contourStatusLabelMap[status] || status,
        statusTone: tone,
        time: formatDateTime(item.submitTime || item.createTime),
        title: '靶区勾画待处理',
      });
      pushMonitorItem(workflowWarning, {
        ...getPatientProfile(item.patientId, item.patientName, patientMap),
        id: item.id,
        nodeName: contourStatusLabelMap[status] || '靶区勾画',
        processName: '勾画审核流程',
        route: '/hospital-flow/hospital-contour/task',
        warningInfo: getContourWarningInfo(status),
      });
    });

    (appointmentPage.list || []).forEach((item) => {
      const status = item.workflowStatus || '';
      if (
        !item.id ||
        !['STOPPED', 'TREATING', 'WAIT_APPOINTMENT'].includes(status)
      )
        return;
      const tone = getStatusTone(status);
      pushTodoItem(todoPool, {
        description: `${item.treatmentRoomName || '治疗室'} · ${item.deviceName || '设备待定'} · 第${item.fractionNo || 1}次`,
        id: `appointment-${item.id}`,
        owner:
          item.doctorName ||
          getPatientProfile(item.patientId, item.patientName, patientMap)
            .doctorName,
        patientName: item.patientName || '--',
        route: '/hospital-flow/hospital-treatment/appointment',
        sortAt: new Date(
          item.appointmentDate || item.createTime || 0,
        ).getTime(),
        sortScore: toneScoreMap[tone],
        statusClass: `is-${tone}`,
        statusLabel: treatmentStatusLabelMap[status] || status,
        statusTone: tone,
        time: formatDateTime(item.appointmentDate || item.createTime),
        title: '治疗预约待处理',
      });
      if (status !== 'TREATING') {
        pushMonitorItem(highRisk, {
          ...getPatientProfile(item.patientId, item.patientName, patientMap),
          id: item.id,
          nodeName: treatmentStatusLabelMap[status] || '治疗排程',
          processName: '治疗预约流程',
          route: '/hospital-flow/hospital-treatment/appointment',
          warningInfo:
            status === 'STOPPED'
              ? '治疗流程已终止，请确认原因并补充医嘱'
              : '治疗预约待排期，请避免影响治疗周期',
        });
      }
    });

    (queuePage.list || []).forEach((item) => {
      const status = item.queueStatus || '';
      if (
        !item.id ||
        !['CALLED', 'QUEUING', 'SKIPPED', 'WAIT_SIGN'].includes(status)
      )
        return;
      const tone = getStatusTone(status);
      pushTodoItem(todoPool, {
        description: `${item.ctRoomName || 'CT室'} · ${item.windowName || '签到窗'} · 序号${item.queueSeq || '--'}`,
        id: `queue-${item.id}`,
        owner: item.windowName || item.ctRoomName || 'CT岗位',
        patientName: item.patientName || '--',
        route: '/hospital-flow/hospital-position/ct-queue',
        sortAt: new Date(
          item.callTime || item.signInTime || item.createTime || 0,
        ).getTime(),
        sortScore: toneScoreMap[tone],
        statusClass: `is-${tone}`,
        statusLabel: queueStatusLabelMap[status] || status,
        statusTone: tone,
        time: formatDateTime(
          item.callTime || item.signInTime || item.createTime,
        ),
        title: 'CT排队叫号跟进',
      });
      if (status !== 'QUEUING') {
        pushMonitorItem(highRisk, {
          ...getPatientProfile(item.patientId, item.patientName, patientMap),
          id: item.id,
          nodeName: queueStatusLabelMap[status] || 'CT排队',
          processName: 'CT排队流程',
          route: '/hospital-flow/hospital-position/ct-queue',
          warningInfo: getQueueWarningInfo(status),
        });
      }
    });

    (feePage.list || []).forEach((item) => {
      const status = item.settlementStatus || '';
      if (
        !item.id ||
        !['PART_SETTLEMENT', 'VOID', 'WAIT_SETTLEMENT'].includes(status)
      )
        return;
      const tone = getStatusTone(status);
      pushTodoItem(todoPool, {
        description: `${feeStatusLabelMap[status] || status} · 应收¥${Number(item.payableAmount || 0).toFixed(2)}`,
        id: `fee-${item.id}`,
        owner: item.cashierName || '收费岗',
        patientName: item.patientName || '--',
        route: '/hospital-flow/hospital-fee/settlement',
        sortAt: new Date(item.payTime || item.createTime || 0).getTime(),
        sortScore: toneScoreMap[tone],
        statusClass: `is-${tone}`,
        statusLabel: feeStatusLabelMap[status] || status,
        statusTone: tone,
        time: formatDateTime(item.payTime || item.createTime),
        title: '费用结算跟进',
      });
      pushMonitorItem(feeException, {
        ...getPatientProfile(item.patientId, item.patientName, patientMap),
        id: item.id,
        nodeName: feeStatusLabelMap[status] || '费用结算',
        processName: '收费结算流程',
        route: '/hospital-flow/hospital-fee/settlement',
        warningInfo: getFeeWarningInfo(
          status,
          item.paidAmount,
          item.payableAmount,
        ),
      });
    });

    todoItems.value = todoPool
      .toSorted((left, right) => {
        if (right.sortScore !== left.sortScore) {
          return right.sortScore - left.sortScore;
        }
        return right.sortAt - left.sortAt;
      })
      .slice(0, 12);

    monitorDataMap.value = {
      feeException: feeException.slice(0, 20),
      highRisk: highRisk.slice(0, 20),
      workflowWarning: workflowWarning.slice(0, 20),
    };

    summaryStats.value = {
      appointmentCount: appointmentPage.total ?? appointmentPage.list.length,
      contourCount: contourPage.total ?? contourPage.list.length,
      feeCount: feePage.total ?? feePage.list.length,
      monitorCount:
        workflowWarning.length + highRisk.length + feeException.length,
      patientCount: patientPage.total ?? patientCards.value.length,
      planCount: planPage.total ?? planPage.list.length,
      queueCount: queuePage.total ?? queuePage.list.length,
      todoCount: todoPool.length,
    };
    lastRefreshTime.value = formatDateTime(new Date());
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Page auto-content-height>
    <section class="hospital-workbench">
      <div class="hospital-workbench__hero">
        <div class="hospital-workbench__hero-header">
          <div>
            <div class="hospital-workbench__hero-title">我的患者</div>
            <div class="hospital-workbench__hero-subtitle">
              本地 MySQL 实时汇总 · 患者 {{ summaryStats.patientCount }} 名 ·
              待办 {{ summaryStats.todoCount }} 条 · 监测
              {{ summaryStats.monitorCount }} 条 · 刷新 {{ lastRefreshTime }}
            </div>
            <div
              v-if="workbenchLoadWarning"
              class="hospital-workbench__hero-warning"
            >
              {{ workbenchLoadWarning }}
            </div>
          </div>
          <div class="hospital-workbench__hero-actions">
            <Button :loading="loading" @click="refreshWorkbench">
              刷新看板
            </Button>
            <Button
              class="hospital-workbench__hero-action"
              type="primary"
              ghost
              @click="goPatientRegistration"
            >
              患者登记
            </Button>
          </div>
        </div>

        <div class="hospital-workbench__overview">
          <div class="hospital-workbench__stats">
            <div
              v-for="card in summaryCards"
              :key="card.label"
              class="hospital-workbench__stat-card"
              :class="[`is-${card.tone}`]"
            >
              <div>
                <div class="hospital-workbench__stat-label">
                  {{ card.label }}
                </div>
                <div class="hospital-workbench__stat-value">
                  {{ card.value }}
                </div>
              </div>
              <div class="hospital-workbench__stat-icon">
                <IconifyIcon :icon="card.icon" />
              </div>
            </div>
          </div>

          <div class="patient-shelf">
            <template v-if="patientCards.length > 0">
              <button
                v-for="patient in patientCards"
                :key="patient.id"
                class="patient-card"
                type="button"
                @click="goPatientList"
              >
                <div class="patient-card__topline">
                  <div class="patient-card__name-wrap">
                    <div class="patient-card__name">{{ patient.name }}</div>
                    <span class="patient-card__badge">{{
                      patient.genderLabel
                    }}</span>
                    <span class="patient-card__badge is-muted">{{
                      patient.ageLabel
                    }}</span>
                  </div>
                  <div class="patient-card__campus">
                    {{ patient.campusName }}
                  </div>
                </div>
                <div class="patient-card__row">
                  <span>{{ patient.patientNo }}</span>
                  <span>{{ patient.visitNo }}</span>
                </div>
                <div class="patient-card__row">
                  <span>{{ patient.wardName }}</span>
                  <span>{{ patient.doctorName }}</span>
                  <span>{{ patient.careLabel }}</span>
                </div>
                <div class="patient-card__tags">
                  <template v-for="tag in patient.tags" :key="tag">
                    <span class="patient-card__tag">{{ tag }}</span>
                  </template>
                  <span
                    class="patient-card__tag is-muted"
                    v-if="patient.tags.length === 0"
                  >
                    待完善标签
                  </span>
                </div>
              </button>
            </template>
            <div
              v-else
              class="patient-shelf__empty"
              @click="goPatientRegistration"
            >
              <IconifyIcon
                class="patient-shelf__empty-icon"
                icon="mdi:stethoscope"
              />
              <div class="patient-shelf__empty-title">暂无患者数据</div>
              <div class="patient-shelf__empty-text">
                先完成患者登记，工作台会自动联动后续流程监测。
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="hospital-workbench__panel-grid">
        <div class="hospital-workbench__panel hospital-workbench__panel--todo">
          <div class="hospital-workbench__panel-header">
            <div class="hospital-workbench__panel-title">待办任务</div>
            <Button
              type="link"
              @click="goRoute('/hospital-flow/hospital-plan/apply')"
            >
              进入流程中心
            </Button>
          </div>
          <div class="todo-board">
            <div class="todo-board__body todo-board__body--list">
              <div v-if="todoItems.length > 0" class="todo-list">
                <button
                  v-for="item in todoItems"
                  :key="item.id"
                  class="todo-list__item"
                  type="button"
                  @click="goRoute(item.route)"
                >
                  <div class="todo-list__main">
                    <span
                      class="todo-list__status-dot"
                      :class="[`is-${item.statusTone}`]"
                    ></span>
                    <div class="todo-list__content">
                      <div class="todo-list__title-row">
                        <span class="todo-list__title">{{ item.title }}</span>
                        <span
                          class="todo-list__badge"
                          :class="item.statusClass"
                        >
                          {{ item.statusLabel }}
                        </span>
                      </div>
                      <div class="todo-list__desc">{{ item.description }}</div>
                      <div class="todo-list__meta">
                        <span>
                          <IconifyIcon icon="mdi:account-heart-outline" />
                          {{ item.patientName }}
                        </span>
                        <span>
                          <IconifyIcon icon="mdi:account-tie-outline" />
                          {{ item.owner }}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div class="todo-list__time">{{ item.time }}</div>
                </button>
              </div>
              <Empty
                v-else
                :image="Empty.PRESENTED_IMAGE_SIMPLE"
                description="暂无待办任务"
              />
            </div>
            <div class="todo-board__legend">
              <div
                v-for="item in todoLegend"
                :key="item.label"
                class="todo-board__legend-item"
              >
                <span
                  :style="{ backgroundColor: item.color }"
                  class="todo-board__legend-dot"
                ></span>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <div
          class="hospital-workbench__panel hospital-workbench__panel--monitor"
        >
          <div
            class="hospital-workbench__panel-header hospital-workbench__panel-header--monitor"
          >
            <div class="hospital-workbench__panel-title">数据监测</div>
            <div class="monitor-toolbar">
              <Input
                v-model:value="monitorKeyword"
                allow-clear
                class="monitor-toolbar__search"
                placeholder="请输入..."
              >
                <template #suffix>
                  <IconifyIcon
                    class="monitor-toolbar__search-icon"
                    icon="lucide:search"
                  />
                </template>
              </Input>
              <div class="monitor-toolbar__filters">
                <button
                  v-for="item in monitorFilters"
                  :key="item.key"
                  class="monitor-toolbar__filter"
                  :class="[item.key === activeFilter ? 'is-active' : '']"
                  type="button"
                  @click="selectFilter(item.key)"
                >
                  <span>{{ item.label }}</span>
                  <span class="monitor-toolbar__filter-count">{{
                    item.count
                  }}</span>
                </button>
              </div>
            </div>
          </div>

          <Table
            :columns="monitorColumns"
            :custom-row="getMonitorRowProps"
            :data-source="filteredMonitorData"
            :loading="loading"
            :pagination="{ pageSize: 15, showSizeChanger: false }"
            row-key="id"
            class="monitor-table"
            size="small"
          />
        </div>
      </div>
    </section>
  </Page>
</template>

<style scoped>
.hospital-workbench {
  --hw-surface: hsl(var(--card));
  --hw-surface-soft: hsl(var(--card) / 0.92);
  --hw-surface-muted: hsl(var(--accent-lighter));
  --hw-border: hsl(var(--border));
  --hw-border-strong: hsl(var(--primary) / 0.24);
  --hw-text: hsl(var(--foreground));
  --hw-text-soft: hsl(var(--muted-foreground));
  --hw-primary: hsl(var(--primary));
  --hw-primary-soft: hsl(var(--primary) / 0.12);
  --hw-primary-hover: hsl(var(--primary) / 0.18);
  --hw-purple: #9254de;
  --hw-purple-soft: rgb(146 84 222 / 0.14);
  --hw-success: hsl(var(--success));
  --hw-success-soft: hsl(var(--success) / 0.14);
  --hw-warning: hsl(var(--warning));
  --hw-warning-soft: hsl(var(--warning) / 0.16);
  --hw-danger: hsl(var(--destructive));
  --hw-danger-soft: hsl(var(--destructive) / 0.14);
  --hw-neutral-soft: hsl(var(--muted));
  --hw-hover-bg: hsl(var(--accent));
  --hw-shadow: 0 10px 24px hsl(220 35% 12% / 0.08);
  --hw-shadow-hover: 0 14px 28px hsl(220 35% 12% / 0.12);
  --hw-hero-bg: linear-gradient(
    135deg,
    hsl(var(--primary) / 0.1) 0%,
    hsl(var(--background)) 38%,
    hsl(var(--accent-lighter)) 100%
  );
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: var(--hw-text);
}

.dark .hospital-workbench {
  --hw-surface-soft: hsl(var(--card) / 0.96);
  --hw-surface-muted: hsl(var(--accent));
  --hw-border-strong: hsl(var(--primary) / 0.34);
  --hw-purple-soft: rgb(167 139 250 / 0.18);
  --hw-hover-bg: hsl(var(--accent-hover));
  --hw-shadow: 0 14px 32px hsl(0 0% 0% / 0.28);
  --hw-shadow-hover: 0 16px 36px hsl(0 0% 0% / 0.34);
  --hw-hero-bg: linear-gradient(
    135deg,
    hsl(var(--primary) / 0.18) 0%,
    hsl(var(--card)) 42%,
    hsl(var(--accent-dark)) 100%
  );
}

.hospital-workbench__hero {
  border: 1px solid var(--hw-border);
  border-radius: 12px;
  background: var(--hw-hero-bg);
  box-shadow: var(--hw-shadow);
  padding: 12px;
}

.hospital-workbench__hero-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.hospital-workbench__hero-title {
  color: var(--hw-text);
  font-size: 18px;
  font-weight: 700;
  line-height: 24px;
}

.hospital-workbench__hero-subtitle {
  margin-top: 2px;
  color: var(--hw-text-soft);
  font-size: 11px;
  line-height: 16px;
}

.hospital-workbench__hero-warning {
  margin-top: 4px;
  color: var(--hw-danger);
  font-size: 11px;
  line-height: 16px;
}

.hospital-workbench__hero-actions {
  display: flex;
  gap: 8px;
}

.hospital-workbench__hero-actions :deep(.ant-btn) {
  height: 30px;
  padding-inline: 12px;
}

.hospital-workbench__overview {
  display: grid;
  grid-template-columns: minmax(260px, 312px) minmax(0, 1fr);
  gap: 8px;
  margin-top: 10px;
  align-items: stretch;
}

.hospital-workbench__hero-action {
  min-width: 92px;
  border-radius: 8px;
  box-shadow: none;
}

.hospital-workbench__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  align-content: start;
}

.hospital-workbench__stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
  min-height: 60px;
  padding: 8px 10px;
  border: 1px solid var(--hw-border);
  border-radius: 10px;
  background: var(--hw-surface-soft);
  box-shadow: var(--hw-shadow);
}

.hospital-workbench__stat-label {
  color: var(--hw-text-soft);
  font-size: 11px;
  line-height: 16px;
}

.hospital-workbench__stat-value {
  margin-top: 2px;
  color: var(--hw-text);
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.hospital-workbench__stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  font-size: 16px;
}

.hospital-workbench__stat-card.is-blue .hospital-workbench__stat-icon {
  color: var(--hw-primary);
  background: var(--hw-primary-soft);
}

.hospital-workbench__stat-card.is-purple .hospital-workbench__stat-icon {
  color: var(--hw-purple);
  background: var(--hw-purple-soft);
}

.hospital-workbench__stat-card.is-green .hospital-workbench__stat-icon {
  color: var(--hw-success);
  background: var(--hw-success-soft);
}

.hospital-workbench__stat-card.is-cyan .hospital-workbench__stat-icon {
  color: #08979c;
  background: rgb(8 151 156 / 0.14);
}

.hospital-workbench__stat-card.is-red .hospital-workbench__stat-icon {
  color: #cf1322;
  background: rgb(207 19 34 / 0.12);
}

.hospital-workbench__stat-card.is-gold .hospital-workbench__stat-icon {
  color: #d48806;
  background: rgb(212 136 6 / 0.14);
}

.patient-shelf {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px;
  min-height: 100%;
}

.patient-card {
  min-height: 80px;
  padding: 8px 9px;
  border: 1px solid var(--hw-border-strong);
  border-radius: 10px;
  background: var(--hw-surface-soft);
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
}

.patient-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--hw-shadow-hover);
}

.patient-card__topline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 6px;
}

.patient-card__name-wrap {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.patient-card__name {
  color: var(--hw-text);
  font-size: 12px;
  font-weight: 600;
  line-height: 16px;
}

.patient-card__badge {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--hw-primary-soft);
  color: var(--hw-primary);
  font-size: 10px;
}

.patient-card__badge.is-muted {
  background: var(--hw-neutral-soft);
  color: var(--hw-text-soft);
}

.patient-card__campus,
.patient-card__row {
  color: var(--hw-text-soft);
  font-size: 10px;
  line-height: 14px;
}

.patient-card__campus {
  flex-shrink: 0;
}

.patient-card__row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.patient-card__row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.patient-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 5px;
}

.patient-card__tag {
  display: inline-flex;
  align-items: center;
  height: 16px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--hw-primary-soft);
  color: var(--hw-primary);
  font-size: 10px;
}

.patient-card__tag.is-muted {
  background: var(--hw-neutral-soft);
  color: var(--hw-text-soft);
}

.patient-shelf__empty {
  display: flex;
  grid-column: 1 / -1;
  min-height: 80px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 14px 12px;
  border: 1px dashed var(--hw-border-strong);
  border-radius: 12px;
  background: hsl(var(--card) / 0.72);
  color: var(--hw-text-soft);
  text-align: center;
  cursor: pointer;
}

.patient-shelf__empty-icon {
  color: var(--hw-primary);
  font-size: 24px;
}

.patient-shelf__empty-title {
  color: var(--hw-text);
  font-size: 14px;
  font-weight: 600;
}

.patient-shelf__empty-text {
  font-size: 11px;
}

.hospital-workbench__panel-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(430px, 1fr);
  gap: 8px;
  align-items: stretch;
}

.hospital-workbench__panel {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  border: 1px solid var(--hw-border);
  border-radius: 12px;
  background: var(--hw-surface);
  box-shadow: var(--hw-shadow);
  overflow: hidden;
}

.hospital-workbench__panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px 6px;
}

.hospital-workbench__panel-title {
  color: var(--hw-primary);
  font-size: 16px;
  font-weight: 700;
  line-height: 22px;
}

.hospital-workbench__panel--todo {
  display: flex;
  flex-direction: column;
}

.todo-board {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 360px;
  border-top: 1px solid var(--hw-border);
}

.todo-board__body {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
}

.todo-board__body--list {
  align-items: stretch;
  justify-content: flex-start;
  padding: 8px 10px 0;
}

.todo-list {
  display: grid;
  flex: 1;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}

.todo-list__item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 7px 8px;
  border: 1px solid var(--hw-border);
  border-radius: 10px;
  background: linear-gradient(
    180deg,
    var(--hw-surface) 0%,
    var(--hw-surface-muted) 100%
  );
  text-align: left;
  transition: all 0.2s ease;
}

.todo-list__item:hover {
  border-color: var(--hw-border-strong);
  transform: translateY(-1px);
  box-shadow: var(--hw-shadow-hover);
}

.todo-list__main {
  display: flex;
  flex: 1;
  gap: 8px;
}

.todo-list__status-dot {
  flex-shrink: 0;
  width: 8px;
  height: 8px;
  margin-top: 5px;
  border-radius: 999px;
}

.todo-list__status-dot.is-blue {
  background: #69b1ff;
}
.todo-list__status-dot.is-gold {
  background: #faad14;
}
.todo-list__status-dot.is-green {
  background: #73d13d;
}
.todo-list__status-dot.is-red {
  background: #ff7875;
}
.todo-list__status-dot.is-gray {
  background: #bfbfbf;
}

.todo-list__content {
  min-width: 0;
}

.todo-list__title-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.todo-list__title {
  color: var(--hw-text);
  font-size: 12px;
  font-weight: 600;
}

.todo-list__badge {
  display: inline-flex;
  align-items: center;
  height: 18px;
  padding: 0 7px;
  border-radius: 999px;
  font-size: 10px;
}

.todo-list__badge.is-blue {
  background: var(--hw-primary-soft);
  color: var(--hw-primary);
}
.todo-list__badge.is-gold {
  background: var(--hw-warning-soft);
  color: var(--hw-warning);
}
.todo-list__badge.is-green {
  background: var(--hw-success-soft);
  color: var(--hw-success);
}
.todo-list__badge.is-red {
  background: var(--hw-danger-soft);
  color: var(--hw-danger);
}
.todo-list__badge.is-gray {
  background: var(--hw-neutral-soft);
  color: var(--hw-text-soft);
}

.todo-list__desc {
  margin-top: 2px;
  color: var(--hw-text-soft);
  font-size: 10px;
  line-height: 14px;
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.todo-list__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
  color: var(--hw-text-soft);
  font-size: 10px;
}

.todo-list__meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.todo-list__time {
  flex-shrink: 0;
  min-width: 60px;
  color: var(--hw-text-soft);
  font-size: 10px;
  line-height: 14px;
  text-align: right;
}

.todo-board__legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 6px 10px;
  color: var(--hw-text-soft);
  font-size: 10px;
}

.todo-board__legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.todo-board__legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
}

.hospital-workbench__panel--monitor {
  display: flex;
  flex-direction: column;
}

.hospital-workbench__panel-header--monitor {
  border-bottom: 1px solid var(--hw-border);
}

.monitor-toolbar {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.monitor-toolbar__search {
  width: 168px;
}

.monitor-toolbar__search-icon {
  color: var(--hw-text-soft);
  font-size: 12px;
}

.monitor-toolbar__filters {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 6px;
}

.monitor-toolbar__filter {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 8px;
  border: 1px solid var(--hw-border);
  border-radius: 999px;
  background: var(--hw-surface-muted);
  color: var(--hw-text-soft);
  font-size: 11px;
  line-height: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.monitor-toolbar__filter:hover {
  border-color: var(--hw-border-strong);
  background: var(--hw-hover-bg);
}

.monitor-toolbar__filter.is-active {
  border-color: var(--hw-primary);
  background: var(--hw-primary);
  color: hsl(var(--primary-foreground));
}

.monitor-toolbar__filter-count {
  font-weight: 600;
}

.monitor-table {
  flex: 1;
  min-height: 360px;
}

:deep(.monitor-toolbar__search .ant-input-affix-wrapper) {
  border-color: var(--hw-border);
  background: var(--hw-surface-muted);
  box-shadow: none;
}

:deep(.monitor-toolbar__search .ant-input) {
  color: var(--hw-text);
  background: transparent;
}

:deep(.monitor-toolbar__search .ant-input::placeholder) {
  color: var(--hw-text-soft);
}

:deep(.monitor-table .ant-table-wrapper),
:deep(.monitor-table .ant-spin-nested-loading),
:deep(.monitor-table .ant-spin-container),
:deep(.monitor-table .ant-table),
:deep(.monitor-table .ant-table-container) {
  height: 100%;
}

:deep(.monitor-table .ant-table),
:deep(.monitor-table .ant-table-container),
:deep(.monitor-table .ant-table-content) {
  background: transparent;
}

:deep(.monitor-table .ant-table-thead > tr > th) {
  padding: 7px 8px !important;
  color: var(--hw-text-soft);
  background: var(--hw-surface-muted) !important;
  border-bottom-color: var(--hw-border) !important;
  font-size: 11px;
}

:deep(.monitor-table .ant-table-tbody > tr > td) {
  padding: 6px 8px !important;
  color: var(--hw-text-soft);
  background: transparent !important;
  border-bottom-color: var(--hw-border) !important;
  font-size: 11px;
}

:deep(.monitor-table .ant-table-tbody > tr > td:first-child) {
  color: var(--hw-text);
}

:deep(.monitor-table .ant-table-placeholder) {
  height: 240px;
  background: transparent !important;
}

:deep(.monitor-table .ant-empty),
:deep(.todo-board__body .ant-empty) {
  margin-block: 32px;
}

:deep(.monitor-table .ant-empty-image),
:deep(.todo-board__body .ant-empty-image) {
  margin-bottom: 10px;
}

:deep(.monitor-table .ant-pagination) {
  margin: 6px 10px 8px;
  color: var(--hw-text-soft);
}

:deep(.monitor-table .monitor-table__row--clickable) {
  cursor: pointer;
}

:deep(.monitor-table .monitor-table__row--clickable:hover > td) {
  background: var(--hw-hover-bg) !important;
}

:deep(.monitor-table .ant-empty-description),
:deep(.todo-board__body .ant-empty-description) {
  color: var(--hw-text-soft);
}

@media (max-width: 1440px) {
  .patient-shelf {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1200px) {
  .todo-list {
    grid-template-columns: 1fr;
  }

  .hospital-workbench__overview,
  .hospital-workbench__panel-grid {
    grid-template-columns: 1fr;
  }

  .hospital-workbench__stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .hospital-workbench__stat-card.is-green {
    grid-column: auto;
  }

  .patient-shelf {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hospital-workbench__hero-header,
  .hospital-workbench__panel-header,
  .todo-list__item {
    flex-direction: column;
    align-items: stretch;
  }

  .hospital-workbench__hero-actions {
    width: 100%;
    flex-direction: column;
  }

  .hospital-workbench__stats,
  .patient-shelf {
    grid-template-columns: 1fr;
  }

  .monitor-toolbar,
  .monitor-toolbar__filters {
    align-items: stretch;
    justify-content: flex-start;
  }

  .monitor-toolbar__search,
  .hospital-workbench__hero-actions .ant-btn {
    width: 100%;
  }

  .todo-list__time {
    text-align: left;
  }
}
</style>
