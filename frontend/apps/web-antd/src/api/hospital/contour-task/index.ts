import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalContourTaskApi {
  export interface ContourTask {
    id?: number;
    bizNo?: string;
    patientId?: number;
    patientName?: string;
    ctAppointmentId?: number;
    contourDoctorId?: number;
    contourDoctorName?: string;
    treatmentSite?: string;
    versionNo?: string;
    attachmentUrl?: string;
    submitTime?: Date;
    workflowStatus?: string;
    processInstanceId?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
    displayName?: string;
  }
}

export function getContourTaskPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalContourTaskApi.ContourTask>>('/hospital/contour-task/page', { params });
}

export function getSimpleContourTaskList() {
  return requestClient.get<HospitalContourTaskApi.ContourTask[]>('/hospital/contour-task/simple-list');
}

export function getContourTask(id: number) {
  return requestClient.get<HospitalContourTaskApi.ContourTask>(`/hospital/contour-task/get?id=${id}`);
}

export function createContourTask(data: HospitalContourTaskApi.ContourTask) {
  return requestClient.post('/hospital/contour-task/create', data);
}

export function updateContourTask(data: HospitalContourTaskApi.ContourTask) {
  return requestClient.put('/hospital/contour-task/update', data);
}

export function submitContourTask(id: number) {
  return requestClient.put(`/hospital/contour-task/submit?id=${id}`);
}

export function deleteContourTask(id: number) {
  return requestClient.delete(`/hospital/contour-task/delete?id=${id}`);
}
