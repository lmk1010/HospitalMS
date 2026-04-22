import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalFeeRecordApi } from '#/api/hospital/fee-record';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getSimplePatientList } from '#/api/hospital/patient';

const feeStatusType = 'hospital_fee_status';
const sourceTypeOptions = [
  { label: 'CT预约', value: 'CT_APPOINTMENT' },
  { label: '计划申请', value: 'PLAN_APPLY' },
  { label: '治疗预约', value: 'TREATMENT_APPOINTMENT' },
  { label: '治疗执行', value: 'TREATMENT_EXECUTE' },
  { label: '其他', value: 'OTHER' },
];

export function useFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    { fieldName: 'bizNo', label: '登记单号', component: 'Input', componentProps: { placeholder: '留空自动生成' } },
    {
      fieldName: 'patientId', label: '患者', component: 'ApiSelect',
      componentProps: { api: getSimplePatientList, labelField: 'name', valueField: 'id', placeholder: '请选择患者' }, rules: 'selectRequired',
    },
    {
      fieldName: 'sourceType', label: '来源类型', component: 'Select',
      componentProps: { options: sourceTypeOptions, allowClear: true, placeholder: '请选择来源类型' },
    },
    { fieldName: 'sourceBizId', label: '来源业务ID', component: 'InputNumber', componentProps: { min: 0, precision: 0, placeholder: '请输入来源业务ID' } },
    { fieldName: 'itemName', label: '费用项目', component: 'Input', componentProps: { placeholder: '请输入费用项目' }, rules: 'required' },
    { fieldName: 'unitPrice', label: '单价', component: 'InputNumber', componentProps: { min: 0, precision: 2, step: 0.01, placeholder: '请输入单价' } },
    { fieldName: 'quantity', label: '数量', component: 'InputNumber', componentProps: { min: 0, precision: 2, step: 1, placeholder: '请输入数量' }, rules: z.number().default(1) },
    {
      fieldName: 'chargeUserId', label: '收费人', component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择收费人' }, rules: 'selectRequired',
    },
    { fieldName: 'status', label: '状态', component: 'RadioGroup', componentProps: { options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'), buttonStyle: 'solid', optionType: 'button' }, rules: z.number().default(CommonStatusEnum.ENABLE) },
    { fieldName: 'remark', label: '备注', component: 'Textarea', componentProps: { rows: 3, placeholder: '请输入备注' } },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'bizNo', label: '登记单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入登记单号' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    { fieldName: 'sourceType', label: '来源类型', component: 'Select', componentProps: { options: sourceTypeOptions, allowClear: true, placeholder: '请选择来源类型' } },
    { fieldName: 'itemName', label: '费用项目', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入费用项目' } },
    {
      fieldName: 'chargeUserId', label: '收费人', component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择收费人' },
    },
    { fieldName: 'settlementStatus', label: '结算状态', component: 'Select', componentProps: { options: getDictOptions(feeStatusType, 'string'), allowClear: true, placeholder: '请选择结算状态' } },
    { fieldName: 'status', label: '状态', component: 'Select', componentProps: { options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'), allowClear: true, placeholder: '请选择状态' } },
  ];
}

export function useGridColumns(): VxeTableGridOptions<HospitalFeeRecordApi.FeeRecord>['columns'] {
  return [
    { field: 'bizNo', title: '登记单号', minWidth: 160 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'sourceType', title: '来源类型', minWidth: 120 },
    { field: 'sourceBizId', title: '来源业务ID', minWidth: 120 },
    { field: 'itemName', title: '费用项目', minWidth: 160 },
    { field: 'unitPrice', title: '单价', minWidth: 100 },
    { field: 'quantity', title: '数量', minWidth: 100 },
    { field: 'amount', title: '金额', minWidth: 100 },
    { field: 'chargeUserName', title: '收费人', minWidth: 120 },
    { field: 'chargeTime', title: '收费时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'settlementStatus', title: '结算状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: feeStatusType } } },
    { field: 'status', title: '状态', minWidth: 100, cellRender: { name: 'CellDict', props: { type: DICT_TYPE.COMMON_STATUS } } },
    { title: '操作', width: 180, fixed: 'right', slots: { default: 'actions' } },
  ];
}
