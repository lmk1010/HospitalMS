import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalContourReviewApi {
  export interface ContourReview {
    id?: number;
    bizNo?: string;
    contourTaskId: number;
    patientId?: number;
    patientName?: string;
    reviewDoctorId?: number;
    reviewDoctorName?: string;
    reviewResult?: string;
    reviewComment?: string;
    reviewTime?: Date;
    workflowStatus?: string;
    processInstanceId?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }

  export interface ContourReviewAudit {
    id: number;
    reviewDoctorId: number;
    reviewComment?: string;
    remark?: string;
  }
}

export function getContourReviewPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalContourReviewApi.ContourReview>>('/hospital/contour-review/page', { params });
}

export function getContourReview(id: number) {
  return requestClient.get<HospitalContourReviewApi.ContourReview>(`/hospital/contour-review/get?id=${id}`);
}

export function approveContourReview(data: HospitalContourReviewApi.ContourReviewAudit) {
  return requestClient.put('/hospital/contour-review/approve', data);
}

export function rejectContourReview(data: HospitalContourReviewApi.ContourReviewAudit) {
  return requestClient.put('/hospital/contour-review/reject', data);
}
