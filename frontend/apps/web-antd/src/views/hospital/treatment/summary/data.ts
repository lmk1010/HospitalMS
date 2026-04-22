import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalTreatmentSummaryApi } from '#/api/hospital/treatment-summary';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';
import { getSimplePatientList } from '#/api/hospital/patient';

export function useFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'id', component: 'Input', dependencies: { triggerFields: [''], show: () => false } },
    { fieldName: 'bizNo', label: '小结单号', component: 'Input', componentProps: { placeholder: '留空自动生成' } },
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
      fieldName: 'summaryDoctorId',
      label: '总结医生',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择总结医生',
      },
      rules: 'selectRequired',
    },
    { fieldName: 'evaluationResult', label: '评估结果', component: 'Input', componentProps: { placeholder: '请输入评估结果' } },
    { fieldName: 'abnormalDesc', label: '异常说明', component: 'Textarea', componentProps: { rows: 3, placeholder: '请输入异常说明' } },
    { fieldName: 'summaryContent', label: '小结内容', component: 'Textarea', componentProps: { rows: 5, placeholder: '请输入小结内容' } },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: { buttonStyle: 'solid', optionType: 'button', options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number') },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    { fieldName: 'remark', label: '备注', component: 'Textarea', componentProps: { rows: 2, placeholder: '请输入备注' } },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'bizNo', label: '小结单号', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入小结单号' } },
    { fieldName: 'patientName', label: '患者姓名', component: 'Input', componentProps: { allowClear: true, placeholder: '请输入患者姓名' } },
    {
      fieldName: 'summaryDoctorId',
      label: '总结医生',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择总结医生',
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

export function useGridColumns(): VxeTableGridOptions<HospitalTreatmentSummaryApi.TreatmentSummary>['columns'] {
  return [
    { field: 'bizNo', title: '小结单号', minWidth: 160 },
    { field: 'patientName', title: '患者姓名', minWidth: 120 },
    { field: 'courseStartDate', title: '疗程开始', minWidth: 120 },
    { field: 'courseEndDate', title: '疗程结束', minWidth: 120 },
    { field: 'completedFractions', title: '完成分次', minWidth: 100 },
    { field: 'summaryDoctorName', title: '总结医生', minWidth: 120 },
    { field: 'evaluationResult', title: '评估结果', minWidth: 160 },
    { field: 'summaryTime', title: '小结时间', minWidth: 180, formatter: 'formatDateTime' },
    { field: 'status', title: '状态', minWidth: 100, cellRender: { name: 'CellDict', props: { type: DICT_TYPE.COMMON_STATUS } } },
    { title: '操作', width: 180, fixed: 'right', slots: { default: 'actions' } },
  ];
}
