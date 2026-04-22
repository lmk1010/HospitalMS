import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalDoctorApi } from '#/api/hospital/doctor';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDepartmentList } from '#/api/hospital/department';
import { getSimpleUserList } from '#/api/system/user';

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'deptId',
      label: '所属科室',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDepartmentList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择所属科室',
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'doctorCode',
      label: '医生工号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入医生工号',
      },
      rules: 'required',
    },
    {
      fieldName: 'name',
      label: '医生姓名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入医生姓名',
      },
      rules: 'required',
    },
    {
      fieldName: 'userId',
      label: '系统账号',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择系统账号',
      },
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
      fieldName: 'phone',
      label: '手机号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机号',
      },
    },
    {
      fieldName: 'title',
      label: '职称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入职称',
      },
    },
    {
      fieldName: 'practicingLicenseNo',
      label: '执业证号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入执业证号',
      },
    },
    {
      fieldName: 'specialty',
      label: '擅长方向',
      component: 'Textarea',
      componentProps: {
        rows: 3,
        placeholder: '请输入擅长方向',
      },
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入排序',
      },
      rules: z.number().default(0),
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
      componentProps: {
        rows: 3,
        placeholder: '请输入备注',
      },
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'deptId',
      label: '所属科室',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleDepartmentList,
        labelField: 'name',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择所属科室',
      },
    },
    {
      fieldName: 'doctorCode',
      label: '医生工号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入医生工号',
      },
    },
    {
      fieldName: 'name',
      label: '医生姓名',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入医生姓名',
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

export function useGridColumns(): VxeTableGridOptions<HospitalDoctorApi.Doctor>['columns'] {
  return [
    {
      field: 'doctorCode',
      title: '医生工号',
      minWidth: 140,
    },
    {
      field: 'name',
      title: '医生姓名',
      minWidth: 120,
    },
    {
      field: 'deptName',
      title: '所属科室',
      minWidth: 140,
    },
    {
      field: 'gender',
      title: '性别',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_USER_SEX },
      },
    },
    {
      field: 'title',
      title: '职称',
      minWidth: 120,
    },
    {
      field: 'phone',
      title: '手机号',
      minWidth: 140,
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
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
