import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalTreatmentExecuteApi } from '#/api/hospital/treatment-execute';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getSimpleDoctorList } from '#/api/hospital/doctor';

const executeResultOptions = [
  { label: '待执行', value: 'WAIT_EXECUTE' },
  { label: '执行中', value: 'EXECUTING' },
  { label: '已完成', value: 'DONE' },
];

const executeResultMap = Object.fromEntries(
  executeResultOptions.map((item) => [item.value, item.label]),
);

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'bizNo',
      label: '执行单号',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入执行单号' },
    },
    {
      fieldName: 'patientName',
      label: '患者姓名',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入患者姓名' },
    },
    {
      fieldName: 'treatmentDate',
      label: '治疗日期',
      component: 'DatePicker',
      componentProps: {
        valueFormat: 'YYYY-MM-DD',
        allowClear: true,
        placeholder: '请选择治疗日期',
      },
    },
    {
      fieldName: 'executorId',
      label: '执行人',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择执行人',
      },
    },
    {
      fieldName: 'executeResult',
      label: '执行结果',
      component: 'Select',
      componentProps: {
        options: executeResultOptions,
        allowClear: true,
        placeholder: '请选择执行结果',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择状态',
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<HospitalTreatmentExecuteApi.TreatmentExecute>['columns'] {
  return [
    { field: 'bizNo', title: '执行单号', minWidth: 160 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'treatmentDate', title: '治疗日期', minWidth: 120 },
    { field: 'fractionNo', title: '治疗分次', minWidth: 100 },
    { field: 'executorName', title: '执行人', minWidth: 120 },
    { field: 'plannedDose', title: '计划剂量', minWidth: 120 },
    { field: 'actualDose', title: '实际剂量', minWidth: 120 },
    {
      field: 'executeResult',
      title: '执行结果',
      minWidth: 120,
      formatter: ({ cellValue }) => executeResultMap[cellValue as string] || cellValue || '-',
    },
    { field: 'startTime', title: '开始时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'finishTime', title: '结束时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 220, fixed: 'right', slots: { default: 'actions' } },
  ];
}
