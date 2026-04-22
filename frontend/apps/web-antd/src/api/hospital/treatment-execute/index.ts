import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalTreatmentExecuteApi {
  export interface TreatmentExecute {
    id?: number;
    bizNo?: string;
    treatmentAppointmentId?: number;
    patientId?: number;
    patientName?: string;
    treatmentDate?: string;
    fractionNo?: number;
    executorId?: number;
    executorName?: string;
    plannedDose?: number;
    actualDose?: number;
    executeResult?: string;
    abnormalDesc?: string;
    startTime?: string;
    finishTime?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getTreatmentExecutePage(params: PageParam) {
  return requestClient.get<PageResult<HospitalTreatmentExecuteApi.TreatmentExecute>>('/hospital/treatment-execute/page', { params });
}

export function executeTreatment(id: number) {
  return requestClient.put(`/hospital/treatment-execute/execute?id=${id}`);
}

export function completeTreatment(id: number) {
  return requestClient.put(`/hospital/treatment-execute/complete?id=${id}`);
}
