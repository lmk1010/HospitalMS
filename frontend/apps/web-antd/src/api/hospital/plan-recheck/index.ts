import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalPlanRecheckApi {
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

export function getPlanRecheckPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalPlanRecheckApi.PlanReview>>('/hospital/plan-recheck/page', { params });
}

export function getPlanRecheck(id: number) {
  return requestClient.get<HospitalPlanRecheckApi.PlanReview>(`/hospital/plan-recheck/get?id=${id}`);
}

export function approvePlanRecheck(data: HospitalPlanRecheckApi.PlanReviewAudit) {
  return requestClient.put('/hospital/plan-recheck/approve', data);
}

export function rejectPlanRecheck(data: HospitalPlanRecheckApi.PlanReviewAudit) {
  return requestClient.put('/hospital/plan-recheck/reject', data);
}
