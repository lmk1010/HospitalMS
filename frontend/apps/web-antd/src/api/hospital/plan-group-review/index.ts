import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalPlanGroupReviewApi {
  export interface PlanReview {
    id?: number;
    bizNo?: string;
    planDesignId?: number;
    patientId?: number;
    patientName?: string;
    reviewStage?: string;
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

  export interface PlanReviewAudit {
    id: number;
    reviewDoctorId: number;
    reviewComment?: string;
    remark?: string;
  }
}

export function getPlanGroupReviewPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalPlanGroupReviewApi.PlanReview>>('/hospital/plan-group-review/page', { params });
}

export function getPlanGroupReview(id: number) {
  return requestClient.get<HospitalPlanGroupReviewApi.PlanReview>(`/hospital/plan-group-review/get?id=${id}`);
}

export function approvePlanGroupReview(data: HospitalPlanGroupReviewApi.PlanReviewAudit) {
  return requestClient.put('/hospital/plan-group-review/approve', data);
}

export function rejectPlanGroupReview(data: HospitalPlanGroupReviewApi.PlanReviewAudit) {
  return requestClient.put('/hospital/plan-group-review/reject', data);
}
