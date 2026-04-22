import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalPlanDesignApi } from '#/api/hospital/plan-design';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getSimplePlanApplyList } from '#/api/hospital/plan-apply';

const workflowStatusType = 'hospital_plan_status';

export function useFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    { fieldName: 'bizNo', label: '设计单号', component: 'Input', componentProps: { placeholder: '留空自动生成' } },
    {
      fieldName: 'planApplyId',
      label: '计划申请',
      component: 'ApiSelect',
      componentProps: { api: getSimplePlanApplyList, labelField: 'displayName', valueField: 'id', placeholder: '请选择计划申请' },
      rules: 'selectRequired',
    },
    {
      fieldName: 'designUserId',
      label: '设计人员',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择设计人员' },
      rules: 'selectRequired',
    },
    { fieldName: 'planName', label: '计划名称', component: 'Input', componentProps: { placeholder: '留空默认自动生成' } },
    { fieldName: 'versionNo', label: '版本号', component: 'Input', componentProps: { placeholder: '默认 V1' }, rules: z.string().default('V1') },
    { fieldName: 'totalDose', label: '总剂量', component: 'InputNumber', componentProps: { min: 0, precision: 2, step: 0.1, placeholder: '请输入总剂量' } },
    { fieldName: 'singleDose', label: '单次剂量', component: 'InputNumber', componentProps: { min: 0, precision: 2, step: 0.1, placeholder: '留空自动计算' } },
    { fieldName: 'fractions', label: '分次数', component: 'InputNumber', componentProps: { min: 0, precision: 0, placeholder: '请输入分次数' } },
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
    { fieldName: 'bizNo', label: '设计单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入设计单号' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    {
      fieldName: 'planApplyId',
      label: '计划申请',
      component: 'ApiSelect',
      componentProps: { api: getSimplePlanApplyList, labelField: 'displayName', valueField: 'id', allowClear: true, placeholder: '请选择计划申请' },
    },
    {
      fieldName: 'designUserId',
      label: '设计人员',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择设计人员' },
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

export function useGridColumns(): VxeTableGridOptions<HospitalPlanDesignApi.PlanDesign>['columns'] {
  return [
    { field: 'bizNo', title: '设计单号', minWidth: 170 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'planApplyId', title: '计划申请ID', minWidth: 120 },
    { field: 'designUserName', title: '设计人员', minWidth: 120 },
    { field: 'planName', title: '计划名称', minWidth: 140 },
    { field: 'versionNo', title: '版本号', minWidth: 100 },
    { field: 'totalDose', title: '总剂量', minWidth: 100 },
    { field: 'singleDose', title: '单次剂量', minWidth: 100 },
    { field: 'fractions', title: '分次数', minWidth: 100 },
    { field: 'workflowStatus', title: '流程状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: workflowStatusType } } },
    { field: 'submitTime', title: '提交时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 240, fixed: 'right', slots: { default: 'actions' } },
  ];
}
