import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalCtAppointmentApi {
  export interface CtAppointmentScheduleQuery {
    startDate?: string;
    endDate?: string;
    keyword?: string;
    ctRoomName?: string;
    applyDoctorId?: number;
  }

  export interface CtAppointment {
    id?: number;
    bizNo?: string;
    patientId: number;
    patientName?: string;
    appointmentDate: string;
    appointmentSlot?: string;
    ctRoomName?: string;
    ctDeviceName?: string;
    applyDoctorId: number;
    applyDoctorName?: string;
    priority?: string;
    positionNote?: string;
    workflowStatus?: string;
    processInstanceId?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getCtAppointmentPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalCtAppointmentApi.CtAppointment>>(
    '/hospital/ct-appointment/page',
    { params },
  );
}

export function getCtAppointmentScheduleList(
  params: HospitalCtAppointmentApi.CtAppointmentScheduleQuery,
) {
  return requestClient.get<HospitalCtAppointmentApi.CtAppointment[]>(
    '/hospital/ct-appointment/schedule-list',
    { params },
  );
}

export function getCtAppointment(id: number) {
  return requestClient.get<HospitalCtAppointmentApi.CtAppointment>(
    `/hospital/ct-appointment/get?id=${id}`,
  );
}

export function createCtAppointment(
  data: HospitalCtAppointmentApi.CtAppointment,
) {
  return requestClient.post('/hospital/ct-appointment/create', data);
}

export function updateCtAppointment(
  data: HospitalCtAppointmentApi.CtAppointment,
) {
  return requestClient.put('/hospital/ct-appointment/update', data);
}

export function submitCtAppointment(id: number) {
  return requestClient.put(`/hospital/ct-appointment/submit?id=${id}`);
}

export function deleteCtAppointment(id: number) {
  return requestClient.delete(`/hospital/ct-appointment/delete?id=${id}`);
}
