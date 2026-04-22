import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';
import type { HospitalFeeRecordApi } from '#/api/hospital/fee-record';

export function getFeeQueryPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalFeeRecordApi.FeeRecord>>('/hospital/fee-query/page', { params });
}
