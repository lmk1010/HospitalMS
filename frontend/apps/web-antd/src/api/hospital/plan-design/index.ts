import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalPlanDesignApi {
  export interface PlanDesign {
    id?: number;
    bizNo?: string;
    planApplyId?: number;
    patientId?: number;
    patientName?: string;
    designUserId?: number;
    designUserName?: string;
    planName?: string;
    versionNo?: string;
    totalDose?: number;
    singleDose?: number;
    fractions?: number;
    designTime?: Date;
    submitTime?: Date;
    workflowStatus?: string;
    processInstanceId?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getPlanDesignPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalPlanDesignApi.PlanDesign>>('/hospital/plan-design/page', { params });
}

export function getPlanDesign(id: number) {
  return requestClient.get<HospitalPlanDesignApi.PlanDesign>(`/hospital/plan-design/get?id=${id}`);
}

export function createPlanDesign(data: HospitalPlanDesignApi.PlanDesign) {
  return requestClient.post('/hospital/plan-design/create', data);
}

export function updatePlanDesign(data: HospitalPlanDesignApi.PlanDesign) {
  return requestClient.put('/hospital/plan-design/update', data);
}

export function submitPlanDesign(id: number) {
  return requestClient.put(`/hospital/plan-design/submit?id=${id}`);
}

export function deletePlanDesign(id: number) {
  return requestClient.delete(`/hospital/plan-design/delete?id=${id}`);
}
