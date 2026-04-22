import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalTreatmentSummaryApi {
  export interface TreatmentSummary {
    id?: number;
    bizNo?: string;
    patientId?: number;
    patientName?: string;
    courseStartDate?: string;
    courseEndDate?: string;
    completedFractions?: number;
    summaryDoctorId?: number;
    summaryDoctorName?: string;
    evaluationResult?: string;
    abnormalDesc?: string;
    summaryContent?: string;
    summaryTime?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getTreatmentSummaryPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalTreatmentSummaryApi.TreatmentSummary>>('/hospital/treatment-summary/page', { params });
}

export function getTreatmentSummary(id: number) {
  return requestClient.get<HospitalTreatmentSummaryApi.TreatmentSummary>(`/hospital/treatment-summary/get?id=${id}`);
}

export function createTreatmentSummary(data: HospitalTreatmentSummaryApi.TreatmentSummary) {
  return requestClient.post('/hospital/treatment-summary/create', data);
}

export function updateTreatmentSummary(data: HospitalTreatmentSummaryApi.TreatmentSummary) {
  return requestClient.put('/hospital/treatment-summary/update', data);
}
