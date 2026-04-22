import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalPlanVerifyApi } from '#/api/hospital/plan-verify';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getSimpleDoctorList } from '#/api/hospital/doctor';

const workflowStatusType = 'hospital_plan_status';
const verifyResultOptions = [
  { label: '通过', value: 'APPROVED' },
  { label: '不通过', value: 'REJECTED' },
];
const verifyResultLabelMap: Record<string, string> = {
  APPROVED: '通过',
  REJECTED: '不通过',
};

export function useAuditFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    {
      fieldName: 'verifyUserId',
      label: '验证人',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择验证人' },
      rules: 'selectRequired',
    },
    { fieldName: 'verifyDeviceName', label: '验证设备', component: 'Input', componentProps: { placeholder: '请输入验证设备名称' } },
    { fieldName: 'reportUrl', label: '报告地址', component: 'Input', componentProps: { placeholder: '请输入验证报告地址' } },
    { fieldName: 'remark', label: '备注', component: 'Textarea', componentProps: { rows: 3, placeholder: '请输入备注' } },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'bizNo', label: '验证单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入验证单号' } },
    { fieldName: 'planDesignId', label: '设计单ID', component: 'InputNumber', componentProps: { min: 0, precision: 0, placeholder: '请输入设计单ID' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    {
      fieldName: 'verifyUserId',
      label: '验证人',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择验证人' },
    },
    {
      fieldName: 'verifyResult',
      label: '验证结果',
      component: 'Select',
      componentProps: { options: verifyResultOptions, allowClear: true, placeholder: '请选择验证结果' },
    },
    {
      fieldName: 'workflowStatus',
      label: '流程状态',
      component: 'Select',
      componentProps: { options: getDictOptions(workflowStatusType, 'string'), allowClear: true, placeholder: '请选择流程状态' },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<HospitalPlanVerifyApi.PlanVerify>['columns'] {
  return [
    { field: 'bizNo', title: '验证单号', minWidth: 170 },
    { field: 'planDesignId', title: '设计单ID', minWidth: 110 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'verifyUserName', title: '验证人', minWidth: 120 },
    { field: 'verifyDeviceName', title: '验证设备', minWidth: 140 },
    {
      field: 'verifyResult',
      title: '验证结果',
      minWidth: 100,
      formatter: ({ cellValue }) => verifyResultLabelMap[cellValue] || cellValue || '-',
    },
    { field: 'reportUrl', title: '报告地址', minWidth: 220, showOverflow: 'tooltip' },
    { field: 'verifyTime', title: '验证时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'workflowStatus', title: '流程状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: workflowStatusType } } },
    { field: 'status', title: '状态', minWidth: 100, cellRender: { name: 'CellDict', props: { type: DICT_TYPE.COMMON_STATUS } } },
    { field: 'createTime', title: '创建时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 220, fixed: 'right', slots: { default: 'actions' } },
  ];
}
