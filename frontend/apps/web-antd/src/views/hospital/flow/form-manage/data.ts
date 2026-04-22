import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalCustomFormApi } from '#/api/hospital/custom-form';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDepartmentList } from '#/api/hospital/department';

export const HOSPITAL_FORM_BIZ_CATEGORY_OPTIONS = [
  { label: '定位管理', value: '定位管理' },
  { label: '勾画管理', value: '勾画管理' },
  { label: '计划管理', value: '计划管理' },
  { label: '治疗管理', value: '治疗管理' },
  { label: '费用管理', value: '费用管理' },
  { label: '综合业务', value: '综合业务' },
];

export const HOSPITAL_FORM_PAGE_OPTIONS = [
  {
    label: '诊疗建档',
    value: 'medical-record',
    path: '/hospital-workflow/external-beam-px',
  },
  {
    label: 'CT预约',
    value: 'ct-appointment',
    path: '/hospital-flow/hospital-position/ct-appointment',
  },
  {
    label: 'CT排队',
    value: 'ct-queue',
    path: '/hospital-flow/hospital-position/ct-queue',
  },
  {
    label: '影像采集',
    value: 'image-collect',
    path: '/hospital-workflow/external-beam-px',
  },
  {
    label: '定位申请',
    value: 'position-apply',
    path: '/hospital-workflow/external-beam-sj',
  },
  {
    label: '影像融合',
    value: 'image-fusion',
    path: '/hospital-workflow/external-beam-sj',
  },
  {
    label: '术前评估',
    value: 'preoperative-evaluate',
    path: '/hospital-workflow/brachytherapy',
  },
  {
    label: '后装申请',
    value: 'brachy-apply',
    path: '/hospital-workflow/brachytherapy',
  },
  {
    label: '后装定位',
    value: 'brachy-position',
    path: '/hospital-workflow/brachytherapy',
  },
  {
    label: '勾画任务',
    value: 'contour-task',
    path: '/hospital-flow/hospital-contour/task',
  },
  {
    label: '勾画审核',
    value: 'contour-review',
    path: '/hospital-flow/hospital-contour/review',
  },
  {
    label: '计划申请',
    value: 'plan-apply',
    path: '/hospital-flow/hospital-plan/apply',
  },
  {
    label: '计划设计',
    value: 'plan-design',
    path: '/hospital-flow/hospital-plan/design',
  },
  {
    label: '计划组审',
    value: 'plan-group-review',
    path: '/hospital-flow/hospital-plan/group-review',
  },
  {
    label: '医师审批',
    value: 'plan-doctor-approval',
    path: '/hospital-flow/hospital-plan/doctor-approval',
  },
  {
    label: '计划复核',
    value: 'plan-recheck',
    path: '/hospital-flow/hospital-plan/recheck',
  },
  {
    label: '计划验证',
    value: 'plan-verify',
    path: '/hospital-flow/hospital-plan/verify',
  },
  {
    label: '治疗预约',
    value: 'treatment-appointment',
    path: '/hospital-flow/hospital-treatment/appointment',
  },
  {
    label: '治疗排队',
    value: 'treatment-queue',
    path: '/hospital-flow/hospital-treatment/queue',
  },
  {
    label: '治疗执行',
    value: 'treatment-execute',
    path: '/hospital-flow/hospital-treatment/execute',
  },
  {
    label: '治疗小结',
    value: 'treatment-summary',
    path: '/hospital-flow/hospital-treatment/summary',
  },
  {
    label: '疗程评估',
    value: 'course-evaluate',
    path: '/hospital-flow/hospital-treatment/summary',
  },
  {
    label: '费用登记',
    value: 'fee-record',
    path: '/hospital-flow/hospital-fee/record',
  },
  {
    label: '费用结算',
    value: 'fee-settlement',
    path: '/hospital-flow/hospital-fee/settlement',
  },
];

export function resolvePagePathByCode(pageCode?: string) {
  return HOSPITAL_FORM_PAGE_OPTIONS.find((item) => item.value === pageCode)
    ?.path;
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '表单名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入表单名称',
      },
    },
    {
      fieldName: 'code',
      label: '表单编码',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入表单编码',
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
        allowClear: true,
        placeholder: '请选择所属科室',
      },
    },
    {
      fieldName: 'bizCategory',
      label: '业务分类',
      component: 'Select',
      componentProps: {
        options: HOSPITAL_FORM_BIZ_CATEGORY_OPTIONS,
        allowClear: true,
        placeholder: '请选择业务分类',
      },
    },
    {
      fieldName: 'pageCode',
      label: '页面编码',
      component: 'Select',
      componentProps: {
        options: HOSPITAL_FORM_PAGE_OPTIONS.map(({ label, value }) => ({
          label,
          value,
        })),
        allowClear: true,
        placeholder: '请选择节点页面',
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

export function useGridColumns(): VxeTableGridOptions<HospitalCustomFormApi.CustomForm>['columns'] {
  return [
    {
      field: 'name',
      title: '表单名称',
      minWidth: 180,
    },
    {
      field: 'code',
      title: '表单编码',
      minWidth: 180,
    },
    {
      field: 'bizCategory',
      title: '业务分类',
      minWidth: 120,
    },
    {
      field: 'deptName',
      title: '所属科室',
      minWidth: 140,
    },
    {
      field: 'processName',
      title: '流程名称',
      minWidth: 180,
    },
    {
      field: 'nodeName',
      title: '节点名称',
      minWidth: 140,
    },
    {
      field: 'pageCode',
      title: '页面编码',
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
      field: 'updater',
      title: '更新人',
      minWidth: 120,
    },
    {
      field: 'updateTime',
      title: '更新时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 180,
    },
    {
      title: '操作',
      width: 260,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

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
      fieldName: 'name',
      label: '表单名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入表单名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'code',
      label: '表单编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入表单编码',
      },
      rules: 'required',
    },
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
      fieldName: 'bizCategory',
      label: '业务分类',
      component: 'Select',
      componentProps: {
        options: HOSPITAL_FORM_BIZ_CATEGORY_OPTIONS,
        allowClear: true,
        placeholder: '请选择业务分类',
      },
    },
    {
      fieldName: 'processName',
      label: '流程名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入流程名称',
      },
    },
    {
      fieldName: 'processKey',
      label: '流程编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入流程编码',
      },
    },
    {
      fieldName: 'nodeName',
      label: '节点名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入节点名称',
      },
    },
    {
      fieldName: 'nodeKey',
      label: '节点编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入节点编码',
      },
    },
    {
      fieldName: 'pageCode',
      label: '页面编码',
      component: 'Select',
      componentProps: {
        options: HOSPITAL_FORM_PAGE_OPTIONS.map(({ label, value }) => ({
          label,
          value,
        })),
        showSearch: true,
        placeholder: '请选择节点页面',
      },
      rules: 'required',
    },
    {
      fieldName: 'pagePath',
      label: '页面路由',
      component: 'Input',
      componentProps: {
        placeholder: '不填则按页面编码自动回填',
      },
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
