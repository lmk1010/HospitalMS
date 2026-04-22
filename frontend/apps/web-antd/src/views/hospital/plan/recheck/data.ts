import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalPlanRecheckApi } from '#/api/hospital/plan-recheck';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getSimpleDoctorList } from '#/api/hospital/doctor';

const workflowStatusType = 'hospital_plan_status';
const reviewStageType = 'hospital_plan_review_stage';
const reviewResultOptions = [
  { label: '通过', value: 'APPROVED' },
  { label: '退回', value: 'REJECTED' },
];
const reviewResultLabelMap: Record<string, string> = {
  APPROVED: '通过',
  REJECTED: '退回',
};

export function useAuditFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    {
      fieldName: 'reviewDoctorId',
      label: '复核人',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择复核人' },
      rules: 'selectRequired',
    },
    { fieldName: 'reviewComment', label: '复核意见', component: 'Textarea', componentProps: { rows: 4, placeholder: '请输入复核意见' } },
    { fieldName: 'remark', label: '备注', component: 'Textarea', componentProps: { rows: 3, placeholder: '请输入备注' } },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'bizNo', label: '复核单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入复核单号' } },
    { fieldName: 'planDesignId', label: '设计单ID', component: 'InputNumber', componentProps: { min: 0, precision: 0, placeholder: '请输入设计单ID' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    {
      fieldName: 'reviewDoctorId',
      label: '复核人',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择复核人' },
    },
    {
      fieldName: 'reviewResult',
      label: '复核结果',
      component: 'Select',
      componentProps: { options: reviewResultOptions, allowClear: true, placeholder: '请选择复核结果' },
    },
    {
      fieldName: 'workflowStatus',
      label: '流程状态',
      component: 'Select',
      componentProps: { options: getDictOptions(workflowStatusType, 'string'), allowClear: true, placeholder: '请选择流程状态' },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<HospitalPlanRecheckApi.PlanReview>['columns'] {
  return [
    { field: 'bizNo', title: '复核单号', minWidth: 170 },
    { field: 'planDesignId', title: '设计单ID', minWidth: 110 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'reviewStage', title: '审核阶段', minWidth: 120, cellRender: { name: 'CellDict', props: { type: reviewStageType } } },
    { field: 'reviewDoctorName', title: '复核人', minWidth: 120 },
    {
      field: 'reviewResult',
      title: '复核结果',
      minWidth: 100,
      formatter: ({ cellValue }) => reviewResultLabelMap[cellValue] || cellValue || '-',
    },
    { field: 'reviewComment', title: '复核意见', minWidth: 220, showOverflow: 'tooltip' },
    { field: 'reviewTime', title: '复核时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'workflowStatus', title: '流程状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: workflowStatusType } } },
    { field: 'status', title: '状态', minWidth: 100, cellRender: { name: 'CellDict', props: { type: DICT_TYPE.COMMON_STATUS } } },
    { field: 'createTime', title: '创建时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 220, fixed: 'right', slots: { default: 'actions' } },
  ];
}
