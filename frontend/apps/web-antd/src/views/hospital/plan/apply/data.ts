import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalPlanApplyApi } from '#/api/hospital/plan-apply';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleContourTaskList } from '#/api/hospital/contour-task';
import { getSimpleDoctorList } from '#/api/hospital/doctor';

const workflowStatusType = 'hospital_plan_status';
const priorityOptions = [
  { label: '常规', value: 'NORMAL' },
  { label: '加急', value: 'URGENT' },
  { label: '紧急', value: 'EMERGENCY' },
];
const priorityLabelMap: Record<string, string> = {
  EMERGENCY: '紧急',
  NORMAL: '常规',
  URGENT: '加急',
};

export function useFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    { fieldName: 'bizNo', label: '申请单号', component: 'Input', componentProps: { placeholder: '留空自动生成' } },
    {
      fieldName: 'contourTaskId',
      label: '勾画任务',
      component: 'ApiSelect',
      componentProps: { api: getSimpleContourTaskList, labelField: 'displayName', valueField: 'id', placeholder: '请选择已通过勾画任务' },
      rules: 'selectRequired',
    },
    {
      fieldName: 'applyDoctorId',
      label: '申请医生',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择申请医生' },
      rules: 'selectRequired',
    },
    { fieldName: 'treatmentSite', label: '治疗部位', component: 'Input', componentProps: { placeholder: '留空默认带出勾画治疗部位' } },
    { fieldName: 'clinicalDiagnosis', label: '临床诊断', component: 'Textarea', componentProps: { rows: 3, placeholder: '请输入临床诊断' } },
    {
      fieldName: 'prescriptionDose',
      label: '处方总剂量',
      component: 'InputNumber',
      componentProps: { min: 0, precision: 2, step: 0.1, placeholder: '请输入处方总剂量' },
    },
    {
      fieldName: 'totalFractions',
      label: '总分次',
      component: 'InputNumber',
      componentProps: { min: 0, precision: 0, placeholder: '请输入总分次' },
    },
    {
      fieldName: 'priority',
      label: '紧急程度',
      component: 'RadioGroup',
      componentProps: { buttonStyle: 'solid', optionType: 'button', options: priorityOptions },
      rules: z.string().default('NORMAL'),
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: { buttonStyle: 'solid', optionType: 'button', options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number') },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    { fieldName: 'remark', label: '备注', component: 'Textarea', componentProps: { rows: 3, placeholder: '请输入备注' } },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'bizNo', label: '申请单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入申请单号' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    {
      fieldName: 'contourTaskId',
      label: '勾画任务',
      component: 'ApiSelect',
      componentProps: { api: getSimpleContourTaskList, labelField: 'displayName', valueField: 'id', allowClear: true, placeholder: '请选择勾画任务' },
    },
    {
      fieldName: 'applyDoctorId',
      label: '申请医生',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择申请医生' },
    },
    {
      fieldName: 'workflowStatus',
      label: '流程状态',
      component: 'Select',
      componentProps: { options: getDictOptions(workflowStatusType, 'string'), allowClear: true, placeholder: '请选择流程状态' },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: { options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'), allowClear: true, placeholder: '请选择状态' },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<HospitalPlanApplyApi.PlanApply>['columns'] {
  return [
    { field: 'bizNo', title: '申请单号', minWidth: 170 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'contourTaskId', title: '勾画任务ID', minWidth: 110 },
    { field: 'applyDoctorName', title: '申请医生', minWidth: 120 },
    { field: 'treatmentSite', title: '治疗部位', minWidth: 140 },
    { field: 'prescriptionDose', title: '处方总剂量', minWidth: 120 },
    { field: 'totalFractions', title: '总分次', minWidth: 100 },
    { field: 'priority', title: '紧急程度', minWidth: 100, formatter: ({ cellValue }) => priorityLabelMap[cellValue] || cellValue || '-' },
    { field: 'workflowStatus', title: '流程状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: workflowStatusType } } },
    { field: 'submitTime', title: '提交时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'createTime', title: '创建时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 240, fixed: 'right', slots: { default: 'actions' } },
  ];
}
