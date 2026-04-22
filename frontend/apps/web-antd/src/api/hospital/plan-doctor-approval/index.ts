import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalPlanDoctorApprovalApi {
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

export function getPlanDoctorApprovalPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalPlanDoctorApprovalApi.PlanReview>>('/hospital/plan-doctor-approval/page', { params });
}

export function getPlanDoctorApproval(id: number) {
  return requestClient.get<HospitalPlanDoctorApprovalApi.PlanReview>(`/hospital/plan-doctor-approval/get?id=${id}`);
}

export function approvePlanDoctorApproval(data: HospitalPlanDoctorApprovalApi.PlanReviewAudit) {
  return requestClient.put('/hospital/plan-doctor-approval/approve', data);
}

export function rejectPlanDoctorApproval(data: HospitalPlanDoctorApprovalApi.PlanReviewAudit) {
  return requestClient.put('/hospital/plan-doctor-approval/reject', data);
}
