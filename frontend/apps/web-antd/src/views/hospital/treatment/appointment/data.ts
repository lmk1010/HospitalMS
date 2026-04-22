import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalTreatmentAppointmentApi } from '#/api/hospital/treatment-appointment';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';

const workflowStatusType = 'hospital_treatment_status';

export function useFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    { fieldName: 'bizNo', label: '预约单号', component: 'Input', componentProps: { placeholder: '留空自动生成' } },
    {
      fieldName: 'planVerifyId',
      label: '计划验证ID',
      component: 'InputNumber',
      componentProps: { min: 0, precision: 0, placeholder: '请输入已验证计划编号' },
      rules: 'selectRequired',
    },
    {
      fieldName: 'appointmentDate',
      label: '治疗日期',
      component: 'DatePicker',
      componentProps: { valueFormat: 'YYYY-MM-DD', placeholder: '请选择治疗日期' },
      rules: 'required',
    },
    {
      fieldName: 'fractionNo',
      label: '治疗分次',
      component: 'InputNumber',
      componentProps: { min: 1, precision: 0, placeholder: '请输入治疗分次' },
      rules: z.number().default(1),
    },
    { fieldName: 'treatmentRoomName', label: '治疗室', component: 'Input', componentProps: { placeholder: '请输入治疗室' } },
    { fieldName: 'deviceName', label: '设备名称', component: 'Input', componentProps: { placeholder: '请输入设备名称' } },
    {
      fieldName: 'doctorId',
      label: '预约医生',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择预约医生' },
      rules: 'selectRequired',
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
    { fieldName: 'bizNo', label: '预约单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入预约单号' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    { fieldName: 'planVerifyId', label: '计划验证ID', component: 'InputNumber', componentProps: { min: 0, precision: 0, placeholder: '请输入计划验证ID' } },
    {
      fieldName: 'appointmentDate',
      label: '治疗日期',
      component: 'DatePicker',
      componentProps: { valueFormat: 'YYYY-MM-DD', allowClear: true, placeholder: '请选择治疗日期' },
    },
    {
      fieldName: 'doctorId',
      label: '预约医生',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择预约医生' },
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

export function useGridColumns(): VxeTableGridOptions<HospitalTreatmentAppointmentApi.TreatmentAppointment>['columns'] {
  return [
    { field: 'bizNo', title: '预约单号', minWidth: 160 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'planVerifyId', title: '计划验证ID', minWidth: 120 },
    { field: 'appointmentDate', title: '治疗日期', minWidth: 120 },
    { field: 'fractionNo', title: '治疗分次', minWidth: 100 },
    { field: 'treatmentRoomName', title: '治疗室', minWidth: 120 },
    { field: 'deviceName', title: '设备名称', minWidth: 140 },
    { field: 'doctorName', title: '预约医生', minWidth: 120 },
    { field: 'workflowStatus', title: '流程状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: workflowStatusType } } },
    { field: 'status', title: '状态', minWidth: 100, cellRender: { name: 'CellDict', props: { type: DICT_TYPE.COMMON_STATUS } } },
    { field: 'createTime', title: '创建时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 240, fixed: 'right', slots: { default: 'actions' } },
  ];
}
