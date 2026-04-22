import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalPlanApplyApi {
  export interface PlanApply {
    id?: number;
    bizNo?: string;
    patientId?: number;
    patientName?: string;
    contourTaskId?: number;
    applyDoctorId?: number;
    applyDoctorName?: string;
    treatmentSite?: string;
    clinicalDiagnosis?: string;
    prescriptionDose?: number;
    totalFractions?: number;
    priority?: string;
    submitTime?: Date;
    workflowStatus?: string;
    processInstanceId?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
    displayName?: string;
  }
}

export function getPlanApplyPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalPlanApplyApi.PlanApply>>('/hospital/plan-apply/page', { params });
}

export function getSimplePlanApplyList() {
  return requestClient.get<HospitalPlanApplyApi.PlanApply[]>('/hospital/plan-apply/simple-list');
}

export function getPlanApply(id: number) {
  return requestClient.get<HospitalPlanApplyApi.PlanApply>(`/hospital/plan-apply/get?id=${id}`);
}

export function createPlanApply(data: HospitalPlanApplyApi.PlanApply) {
  return requestClient.post('/hospital/plan-apply/create', data);
}

export function updatePlanApply(data: HospitalPlanApplyApi.PlanApply) {
  return requestClient.put('/hospital/plan-apply/update', data);
}

export function submitPlanApply(id: number) {
  return requestClient.put(`/hospital/plan-apply/submit?id=${id}`);
}

export function deletePlanApply(id: number) {
  return requestClient.delete(`/hospital/plan-apply/delete?id=${id}`);
}
