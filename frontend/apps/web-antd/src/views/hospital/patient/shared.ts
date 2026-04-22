import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalPatientApi } from '#/api/hospital/patient';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDoctorList } from '#/api/hospital/doctor';

const idTypeOptions = [
  { label: '身份证', value: '身份证' },
  { label: '护照', value: '护照' },
  { label: '军官证', value: '军官证' },
  { label: '港澳台证件', value: '港澳台证件' },
];
const maritalStatusOptions = [
  { label: '未婚', value: '未婚' },
  { label: '已婚', value: '已婚' },
  { label: '离异', value: '离异' },
  { label: '丧偶', value: '丧偶' },
];
const patientTypeOptions = [
  { label: '门诊', value: '门诊' },
  { label: '住院', value: '住院' },
  { label: '会诊', value: '会诊' },
];
const paymentTypeOptions = [
  { label: '医保', value: 'INSURANCE' },
  { label: '自费', value: 'SELF_PAY' },
  { label: '商保', value: 'COMMERCIAL' },
  { label: '公费', value: 'PUBLIC_PAY' },
];
const relationOptions = [
  { label: '本人', value: '本人' },
  { label: '配偶', value: '配偶' },
  { label: '子女', value: '子女' },
  { label: '父母', value: '父母' },
  { label: '其他', value: '其他' },
];
const campusOptions = [
  { label: '本部院区', value: '本部院区' },
  { label: '南区院区', value: '南区院区' },
  { label: '北区院区', value: '北区院区' },
];

export function usePatientFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'patientNo',
      label: '档案号',
      component: 'Input',
      componentProps: { placeholder: '留空自动生成' },
    },
    {
      fieldName: 'name',
      label: '患者姓名',
      component: 'Input',
      componentProps: { placeholder: '请输入患者姓名' },
      rules: 'required',
    },
    {
      fieldName: 'gender',
      label: '性别',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(1),
    },
    {
      fieldName: 'birthday',
      label: '出生日期',
      component: 'DatePicker',
      componentProps: {
        valueFormat: 'YYYY-MM-DD',
        placeholder: '请选择出生日期',
      },
    },
    {
      fieldName: 'idType',
      label: '证件类型',
      component: 'Select',
      componentProps: { options: idTypeOptions, placeholder: '请选择证件类型' },
      rules: z.string().default('身份证'),
    },
    {
      fieldName: 'idNo',
      label: '证件号',
      component: 'Input',
      componentProps: { placeholder: '请输入证件号' },
    },
    {
      fieldName: 'outpatientNo',
      label: '门诊号',
      component: 'Input',
      componentProps: { placeholder: '请输入门诊号' },
    },
    {
      fieldName: 'hospitalizationNo',
      label: '住院号',
      component: 'Input',
      componentProps: { placeholder: '请输入住院号' },
    },
    {
      fieldName: 'radiotherapyNo',
      label: '放疗号',
      component: 'Input',
      componentProps: { placeholder: '请输入放疗号' },
    },
    {
      fieldName: 'medicalRecordNo',
      label: '病案号',
      component: 'Input',
      componentProps: { placeholder: '请输入病案号' },
    },
    {
      fieldName: 'patientPhone',
      label: '患者电话',
      component: 'Input',
      componentProps: { placeholder: '请输入患者电话' },
    },
    {
      fieldName: 'managerDoctorId',
      label: '主管医生',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择主管医生',
      },
    },
    {
      fieldName: 'attendingDoctorId',
      label: '主诊医生',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择主诊医生',
      },
    },
    {
      fieldName: 'patientType',
      label: '患者类型',
      component: 'Select',
      componentProps: {
        options: patientTypeOptions,
        allowClear: true,
        placeholder: '请选择患者类型',
      },
    },
    {
      fieldName: 'paymentType',
      label: '付费方式',
      component: 'Select',
      componentProps: {
        options: paymentTypeOptions,
        allowClear: true,
        placeholder: '请选择付费方式',
      },
    },
    {
      fieldName: 'wardName',
      label: '病区',
      component: 'Input',
      componentProps: { placeholder: '请输入病区' },
    },
    {
      fieldName: 'campus',
      label: '院区',
      component: 'Select',
      componentProps: {
        options: campusOptions,
        allowClear: true,
        placeholder: '请选择院区',
      },
    },
    {
      fieldName: 'maritalStatus',
      label: '婚姻状态',
      component: 'Select',
      componentProps: {
        options: maritalStatusOptions,
        allowClear: true,
        placeholder: '请选择婚姻状态',
      },
    },
    {
      fieldName: 'nativePlace',
      label: '籍贯',
      component: 'Input',
      componentProps: { placeholder: '请输入籍贯' },
    },
    {
      fieldName: 'currentAddress',
      label: '现住址',
      component: 'Textarea',
      componentProps: { rows: 2, placeholder: '请输入现住址' },
    },
    {
      fieldName: 'contactName',
      label: '联系人',
      component: 'Input',
      componentProps: { placeholder: '请输入联系人姓名' },
    },
    {
      fieldName: 'contactRelation',
      label: '联系人关系',
      component: 'Select',
      componentProps: {
        options: relationOptions,
        allowClear: true,
        placeholder: '请选择联系人关系',
      },
    },
    {
      fieldName: 'contactPhone',
      label: '联系人电话',
      component: 'Input',
      componentProps: { placeholder: '请输入联系人电话' },
    },
    {
      fieldName: 'visitNo',
      label: '就诊号',
      component: 'Input',
      componentProps: { placeholder: '请输入就诊号' },
    },
    {
      fieldName: 'firstDoctorName',
      label: '首诊医生',
      component: 'Input',
      componentProps: { placeholder: '请输入首诊医生' },
    },
    {
      fieldName: 'socialSecurityNo',
      label: '社保卡号',
      component: 'Input',
      componentProps: { placeholder: '请输入社保卡号' },
    },
    {
      fieldName: 'tags',
      label: '标签',
      component: 'Input',
      componentProps: { placeholder: '请输入标签，多个逗号分隔' },
    },
    {
      fieldName: 'allergyHistory',
      label: '过敏史',
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '请输入过敏史' },
    },
    {
      fieldName: 'pastHistory',
      label: '既往史',
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '请输入既往史' },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '请输入备注' },
    },
  ];
}

export function usePatientGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'patientNo',
      label: '档案号',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入档案号' },
    },
    {
      fieldName: 'name',
      label: '患者姓名',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入患者姓名' },
    },
    {
      fieldName: 'idNo',
      label: '证件号',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入证件号' },
    },
    {
      fieldName: 'patientPhone',
      label: '联系电话',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '请输入联系电话' },
    },
    {
      fieldName: 'managerDoctorId',
      label: '主管医生',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDoctorList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择主管医生',
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

export function usePatientGridColumns(): VxeTableGridOptions<HospitalPatientApi.Patient>['columns'] {
  return [
    { field: 'patientNo', title: '档案号', minWidth: 160 },
    { field: 'name', title: '患者姓名', minWidth: 120 },
    {
      field: 'gender',
      title: '性别',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_USER_SEX },
      },
    },
    { field: 'age', title: '年龄', minWidth: 80 },
    { field: 'patientPhone', title: '联系电话', minWidth: 140 },
    { field: 'managerDoctorName', title: '主管医生', minWidth: 120 },
    { field: 'attendingDoctorName', title: '主诊医生', minWidth: 120 },
    { field: 'wardName', title: '病区', minWidth: 120 },
    { field: 'paymentType', title: '付费方式', minWidth: 120 },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'createTime',
      title: '建档时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
