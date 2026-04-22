import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalPlanVerifyApi {
  export interface PlanVerify {
    id?: number;
    bizNo?: string;
    planDesignId?: number;
    patientId?: number;
    patientName?: string;
    verifyUserId?: number;
    verifyUserName?: string;
    verifyDeviceName?: string;
    verifyResult?: string;
    reportUrl?: string;
    verifyTime?: Date;
    workflowStatus?: string;
    processInstanceId?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }

  export interface PlanVerifyAudit {
    id: number;
    verifyUserId: number;
    verifyDeviceName?: string;
    reportUrl?: string;
    remark?: string;
  }
}

export function getPlanVerifyPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalPlanVerifyApi.PlanVerify>>('/hospital/plan-verify/page', { params });
}

export function getPlanVerify(id: number) {
  return requestClient.get<HospitalPlanVerifyApi.PlanVerify>(`/hospital/plan-verify/get?id=${id}`);
}

export function verifyPlan(data: HospitalPlanVerifyApi.PlanVerifyAudit) {
  return requestClient.put('/hospital/plan-verify/verify', data);
}

export function rejectPlanVerify(data: HospitalPlanVerifyApi.PlanVerifyAudit) {
  return requestClient.put('/hospital/plan-verify/reject', data);
}
