import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalFeeRecordApi {
  export interface FeeRecord {
    id?: number;
    bizNo?: string;
    patientId?: number;
    patientName?: string;
    sourceType?: string;
    sourceBizId?: number;
    itemName?: string;
    unitPrice?: number;
    quantity?: number;
    amount?: number;
    chargeTime?: string;
    chargeUserId?: number;
    chargeUserName?: string;
    settlementStatus?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getFeeRecordPage(
  params: PageParam & {
    bizNo?: string;
    chargeUserId?: number;
    itemName?: string;
    patientId?: number;
    patientName?: string;
    settlementStatus?: string;
    sourceType?: string;
    status?: number;
  },
) {
  return requestClient.get<PageResult<HospitalFeeRecordApi.FeeRecord>>('/hospital/fee-record/page', { params });
}

export function getFeeRecord(id: number) {
  return requestClient.get<HospitalFeeRecordApi.FeeRecord>(`/hospital/fee-record/get?id=${id}`);
}

export function createFeeRecord(data: HospitalFeeRecordApi.FeeRecord) {
  return requestClient.post('/hospital/fee-record/create', data);
}

export function updateFeeRecord(data: HospitalFeeRecordApi.FeeRecord) {
  return requestClient.put('/hospital/fee-record/update', data);
}

export function deleteFeeRecord(id: number) {
  return requestClient.delete(`/hospital/fee-record/delete?id=${id}`);
}
