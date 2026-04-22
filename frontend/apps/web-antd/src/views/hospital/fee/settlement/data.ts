import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalFeeSettlementApi } from '#/api/hospital/fee-settlement';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getSimplePatientList } from '#/api/hospital/patient';

const feeStatusType = 'hospital_fee_status';
const paymentType = 'hospital_payment_type';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'patientId', label: '患者', component: 'ApiSelect',
      componentProps: { api: getSimplePatientList, labelField: 'name', valueField: 'id', placeholder: '请选择患者' }, rules: 'selectRequired',
    },
    { fieldName: 'discountAmount', label: '优惠金额', component: 'InputNumber', componentProps: { min: 0, precision: 2, step: 0.01, placeholder: '请输入优惠金额' }, rules: z.number().default(0) },
    { fieldName: 'paidAmount', label: '实收金额', component: 'InputNumber', componentProps: { min: 0, precision: 2, step: 0.01, placeholder: '留空默认按应收全额结算' } },
    {
      fieldName: 'paymentType', label: '支付方式', component: 'Select',
      componentProps: { options: getDictOptions(paymentType, 'string'), allowClear: true, placeholder: '请选择支付方式' },
    },
    {
      fieldName: 'cashierId', label: '收费员', component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择收费员' }, rules: 'selectRequired',
    },
    { fieldName: 'status', label: '状态', component: 'RadioGroup', componentProps: { options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'), buttonStyle: 'solid', optionType: 'button' }, rules: z.number().default(CommonStatusEnum.ENABLE) },
    { fieldName: 'remark', label: '备注', component: 'Textarea', componentProps: { rows: 3, placeholder: '请输入备注' } },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'bizNo', label: '结算单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入结算单号' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    { fieldName: 'paymentType', label: '支付方式', component: 'Select', componentProps: { options: getDictOptions(paymentType, 'string'), allowClear: true, placeholder: '请选择支付方式' } },
    {
      fieldName: 'cashierId', label: '收费员', component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择收费员' },
    },
    { fieldName: 'settlementStatus', label: '结算状态', component: 'Select', componentProps: { options: getDictOptions(feeStatusType, 'string'), allowClear: true, placeholder: '请选择结算状态' } },
    { fieldName: 'status', label: '状态', component: 'Select', componentProps: { options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'), allowClear: true, placeholder: '请选择状态' } },
  ];
}

export function useGridColumns(): VxeTableGridOptions<HospitalFeeSettlementApi.FeeSettlement>['columns'] {
  return [
    { field: 'bizNo', title: '结算单号', minWidth: 160 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'totalAmount', title: '总金额', minWidth: 100 },
    { field: 'discountAmount', title: '优惠金额', minWidth: 100 },
    { field: 'payableAmount', title: '应收金额', minWidth: 100 },
    { field: 'paidAmount', title: '实收金额', minWidth: 100 },
    { field: 'paymentType', title: '支付方式', minWidth: 120, cellRender: { name: 'CellDict', props: { type: paymentType } } },
    { field: 'cashierName', title: '收费员', minWidth: 120 },
    { field: 'settlementStatus', title: '结算状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: feeStatusType } } },
    { field: 'payTime', title: '支付时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'status', title: '状态', minWidth: 100, cellRender: { name: 'CellDict', props: { type: DICT_TYPE.COMMON_STATUS } } },
    { field: 'remark', title: '备注', minWidth: 220 },
    { title: '操作', width: 140, fixed: 'right', slots: { default: 'actions' } },
  ];
}
