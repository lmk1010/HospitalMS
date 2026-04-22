import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalCtQueueApi {
  export interface CtQueue {
    id?: number;
    queueNo?: string;
    appointmentId?: number;
    patientId?: number;
    patientName?: string;
    queueDate?: string;
    queueSeq?: number;
    signInTime?: string;
    callTime?: string;
    finishTime?: string;
    queueStatus?: string;
    windowName?: string;
    ctRoomName?: string;
    remark?: string;
    createTime?: Date;
  }
}

export function getCtQueuePage(params: PageParam) {
  return requestClient.get<PageResult<HospitalCtQueueApi.CtQueue>>('/hospital/ct-queue/page', { params });
}

export function callCtQueue(id: number) {
  return requestClient.put(`/hospital/ct-queue/call?id=${id}`);
}

export function skipCtQueue(id: number) {
  return requestClient.put(`/hospital/ct-queue/skip?id=${id}`);
}

export function finishCtQueue(id: number) {
  return requestClient.put(`/hospital/ct-queue/finish?id=${id}`);
}
