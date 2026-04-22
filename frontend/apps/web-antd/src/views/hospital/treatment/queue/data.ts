import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalTreatmentQueueApi } from '#/api/hospital/treatment-queue';

import { getDictOptions } from '@vben/hooks';

const queueStatusType = 'hospital_queue_status';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'queueNo',
      label: '队列号',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入队列号' },
    },
    {
      fieldName: 'patientName',
      label: '患者姓名',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入患者姓名' },
    },
    {
      fieldName: 'queueDate',
      label: '排队日期',
      component: 'DatePicker',
      componentProps: {
        valueFormat: 'YYYY-MM-DD',
        allowClear: true,
        placeholder: '请选择排队日期',
      },
    },
    {
      fieldName: 'queueStatus',
      label: '排队状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(queueStatusType, 'string'),
        allowClear: true,
        placeholder: '请选择排队状态',
      },
    },
    {
      fieldName: 'treatmentRoomName',
      label: '治疗室',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入治疗室' },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<HospitalTreatmentQueueApi.TreatmentQueue>['columns'] {
  return [
    { field: 'queueNo', title: '队列号', minWidth: 150 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'queueDate', title: '排队日期', minWidth: 120 },
    { field: 'queueSeq', title: '排队序号', minWidth: 100 },
    { field: 'treatmentRoomName', title: '治疗室', minWidth: 120 },
    { field: 'deviceName', title: '设备名称', minWidth: 140 },
    {
      field: 'queueStatus',
      title: '排队状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: queueStatusType },
      },
    },
    { field: 'signInTime', title: '签到时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'callTime', title: '叫号时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'startTime', title: '开始时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'finishTime', title: '完成时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 220, fixed: 'right', slots: { default: 'actions' } },
  ];
}
