import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalContourTaskApi } from '#/api/hospital/contour-task';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getSimplePatientList } from '#/api/hospital/patient';

const workflowStatusType = 'hospital_contour_status';

export function useFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    {
      fieldName: 'bizNo',
      label: '任务单号',
      component: 'Input',
      componentProps: { placeholder: '留空自动生成' },
    },
    {
      fieldName: 'patientId',
      label: '患者',
      component: 'ApiSelect',
      componentProps: { api: getSimplePatientList, labelField: 'name', valueField: 'id', placeholder: '请选择患者' },
      rules: 'selectRequired',
    },
    {
      fieldName: 'ctAppointmentId',
      label: 'CT预约ID',
      component: 'InputNumber',
      componentProps: { min: 0, precision: 0, placeholder: '可选，关联CT预约' },
    },
    {
      fieldName: 'contourDoctorId',
      label: '勾画医生',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', placeholder: '请选择勾画医生' },
      rules: 'selectRequired',
    },
    { fieldName: 'treatmentSite', label: '治疗部位', component: 'Input', componentProps: { placeholder: '请输入治疗部位' } },
    {
      fieldName: 'versionNo',
      label: '版本号',
      component: 'Input',
      componentProps: { placeholder: '默认 V1' },
      rules: z.string().default('V1'),
    },
    { fieldName: 'attachmentUrl', label: '附件地址', component: 'Input', componentProps: { placeholder: '请输入附件地址' } },
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
    { fieldName: 'bizNo', label: '任务单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入任务单号' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    {
      fieldName: 'contourDoctorId',
      label: '勾画医生',
      component: 'ApiSelect',
      componentProps: { api: getSimpleDoctorList, labelField: 'name', valueField: 'id', allowClear: true, placeholder: '请选择勾画医生' },
    },
    { fieldName: 'treatmentSite', label: '治疗部位', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入治疗部位' } },
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

export function useGridColumns(): VxeTableGridOptions<HospitalContourTaskApi.ContourTask>['columns'] {
  return [
    { field: 'bizNo', title: '任务单号', minWidth: 170 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'ctAppointmentId', title: 'CT预约ID', minWidth: 110 },
    { field: 'contourDoctorName', title: '勾画医生', minWidth: 120 },
    { field: 'treatmentSite', title: '治疗部位', minWidth: 140 },
    { field: 'versionNo', title: '版本号', minWidth: 100 },
    { field: 'submitTime', title: '提交时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'workflowStatus', title: '流程状态', minWidth: 120, cellRender: { name: 'CellDict', props: { type: workflowStatusType } } },
    { field: 'status', title: '状态', minWidth: 100, cellRender: { name: 'CellDict', props: { type: DICT_TYPE.COMMON_STATUS } } },
    { field: 'createTime', title: '创建时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 240, fixed: 'right', slots: { default: 'actions' } },
  ];
}
