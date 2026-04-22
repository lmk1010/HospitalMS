import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalTreatmentQueueApi {
  export interface TreatmentQueue {
    id?: number;
    queueNo?: string;
    treatmentAppointmentId?: number;
    patientId?: number;
    patientName?: string;
    queueDate?: string;
    queueSeq?: number;
    signInTime?: string;
    callTime?: string;
    startTime?: string;
    finishTime?: string;
    queueStatus?: string;
    treatmentRoomName?: string;
    deviceName?: string;
    remark?: string;
    createTime?: Date;
  }
}

export function getTreatmentQueuePage(params: PageParam) {
  return requestClient.get<PageResult<HospitalTreatmentQueueApi.TreatmentQueue>>('/hospital/treatment-queue/page', { params });
}

export function callTreatmentQueue(id: number) {
  return requestClient.put(`/hospital/treatment-queue/call?id=${id}`);
}

export function skipTreatmentQueue(id: number) {
  return requestClient.put(`/hospital/treatment-queue/skip?id=${id}`);
}

export function finishTreatmentQueue(id: number) {
  return requestClient.put(`/hospital/treatment-queue/finish?id=${id}`);
}
