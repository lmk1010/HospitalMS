import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalFeeSettlementApi {
  export interface FeeSettlement {
    id?: number;
    bizNo?: string;
    patientId?: number;
    patientName?: string;
    totalAmount?: number;
    discountAmount?: number;
    payableAmount?: number;
    paidAmount?: number;
    paymentType?: string;
    payTime?: string;
    cashierId?: number;
    cashierName?: string;
    settlementStatus?: string;
    status?: number;
    remark?: string;
    recordIds?: number[];
    createTime?: Date;
  }
}

export function getFeeSettlementPage(
  params: PageParam & {
    bizNo?: string;
    cashierId?: number;
    patientId?: number;
    patientName?: string;
    paymentType?: string;
    settlementStatus?: string;
    status?: number;
  },
) {
  return requestClient.get<PageResult<HospitalFeeSettlementApi.FeeSettlement>>('/hospital/fee-settlement/page', { params });
}

export function settleFee(data: HospitalFeeSettlementApi.FeeSettlement) {
  return requestClient.post('/hospital/fee-settlement/settle', data);
}

export function reverseFeeSettlement(id: number) {
  return requestClient.put(`/hospital/fee-settlement/reverse?id=${id}`);
}
