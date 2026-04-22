import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalFeeRecordApi } from '#/api/hospital/fee-record';

import { DICT_TYPE } from '@vben/constants';

import { useGridFormSchema as useFeeRecordGridFormSchema } from '../record/data';

const feeStatusType = 'hospital_fee_status';

export function useGridFormSchema() {
  return useFeeRecordGridFormSchema();
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
    { field: 'remark', title: '备注', minWidth: 220 },
  ];
}
