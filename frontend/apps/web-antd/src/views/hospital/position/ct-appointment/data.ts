import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalCtAppointmentApi } from '#/api/hospital/ct-appointment';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getSimplePatientList } from '#/api/hospital/patient';

const workflowStatusType = 'hospital_ct_appointment_status';
const appointmentSlotOptions = [
  { label: '上午', value: '上午' },
  { label: '下午', value: '下午' },
  { label: '晚间', value: '晚间' },
];
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
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'bizNo',
      label: '预约单号',
      component: 'Input',
      componentProps: { placeholder: '留空自动生成' },
    },
    {
      fieldName: 'patientId',
      label: '患者',
      component: 'ApiSelect',
      componentProps: {
        api: getSimplePatientList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择患者',
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'appointmentDate',
      label: '预约日期',
      component: 'DatePicker',
      componentProps: {
        valueFormat: 'YYYY-MM-DD',
        placeholder: '请选择预约日期',
      },
      rules: 'required',
    },
    {
      fieldName: 'appointmentSlot',
      label: '预约时段',
      component: 'Select',
      componentProps: {
        options: appointmentSlotOptions,
        placeholder: '请选择预约时段',
      },
      rules: z.string().default('上午'),
    },
    {
      fieldName: 'ctRoomName',
      label: 'CT室',
      component: 'Input',
      componentProps: { placeholder: '请输入CT室名称' },
    },
    {
      fieldName: 'ctDeviceName',
      label: 'CT设备',
      component: 'Input',
      componentProps: { placeholder: '请输入CT设备名称' },
    },
    {
      fieldName: 'applyDoctorId',
      label: '申请医生',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择申请医生',
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'priority',
      label: '优先级',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: priorityOptions,
      },
      rules: z.string().default('NORMAL'),
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'positionNote',
      label: '定位说明',
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '请输入定位说明' },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '请输入备注' },
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'bizNo',
      label: '预约单号',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入预约单号' },
    },
    {
      fieldName: 'patientName',
      label: '患者姓名',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入患者姓名' },
    },
    {
      fieldName: 'applyDoctorId',
      label: '申请医生',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择申请医生',
      },
    },
    {
      fieldName: 'appointmentDate',
      label: '预约日期',
      component: 'DatePicker',
      componentProps: {
        valueFormat: 'YYYY-MM-DD',
        allowClear: true,
        placeholder: '请选择预约日期',
      },
    },
    {
      fieldName: 'workflowStatus',
      label: '流程状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(workflowStatusType, 'string'),
        allowClear: true,
        placeholder: '请选择流程状态',
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

export function useGridColumns(): VxeTableGridOptions<HospitalCtAppointmentApi.CtAppointment>['columns'] {
  return [
    { field: 'bizNo', title: '预约单号', minWidth: 160 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'appointmentDate', title: '预约日期', minWidth: 120 },
    { field: 'appointmentSlot', title: '预约时段', minWidth: 100 },
    { field: 'ctRoomName', title: 'CT室', minWidth: 120 },
    { field: 'ctDeviceName', title: 'CT设备', minWidth: 140 },
    { field: 'applyDoctorName', title: '申请医生', minWidth: 120 },
    {
      field: 'priority',
      title: '优先级',
      minWidth: 100,
      formatter: ({ cellValue }) => priorityLabelMap[cellValue] || cellValue || '-',
    },
    {
      field: 'workflowStatus',
      title: '流程状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: workflowStatusType },
      },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    { field: 'createTime', title: '创建时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 220, fixed: 'right', slots: { default: 'actions' } },
  ];
}
