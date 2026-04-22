import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalDepartmentApi } from '#/api/hospital/department';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { z } from '#/adapter/form';
import { getDepartmentList } from '#/api/hospital/department';

const departmentTypeOptions = [
  { label: '临床', value: 1 },
  { label: '医技', value: 2 },
  { label: '管理', value: 3 },
];

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
      fieldName: 'parentId',
      label: '上级科室',
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: async () => {
          const data = await getDepartmentList();
          data.unshift({ id: 0, name: '顶级科室', code: 'ROOT' });
          return handleTree(data);
        },
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: '请选择上级科室',
        treeDefaultExpandAll: true,
      },
    },
    {
      fieldName: 'name',
      label: '科室名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入科室名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'code',
      label: '科室编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入科室编码',
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '科室类型',
      component: 'Select',
      componentProps: {
        options: departmentTypeOptions,
        placeholder: '请选择科室类型',
      },
      rules: z.number().default(1),
    },
    {
      fieldName: 'directorName',
      label: '负责人',
      component: 'Input',
      componentProps: {
        placeholder: '请输入负责人姓名',
      },
    },
    {
      fieldName: 'phone',
      label: '联系电话',
      component: 'Input',
      componentProps: {
        placeholder: '请输入联系电话',
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

export function useGridColumns(): VxeTableGridOptions<HospitalDepartmentApi.Department>['columns'] {
  return [
    {
      field: 'name',
      title: '科室名称',
      minWidth: 180,
      fixed: 'left',
      align: 'left',
      treeNode: true,
    },
    {
      field: 'code',
      title: '科室编码',
      minWidth: 140,
    },
    {
      field: 'type',
      title: '科室类型',
      minWidth: 120,
      formatter: ({ cellValue }) =>
        departmentTypeOptions.find((item) => item.value === cellValue)?.label || '-',
    },
    {
      field: 'directorName',
      title: '负责人',
      minWidth: 120,
    },
    {
      field: 'phone',
      title: '联系电话',
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
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
