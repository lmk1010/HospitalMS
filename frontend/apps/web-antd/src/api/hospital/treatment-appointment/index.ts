import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalTreatmentAppointmentApi {
  export interface TreatmentAppointment {
    id?: number;
    bizNo?: string;
    patientId?: number;
    patientName?: string;
    planVerifyId?: number;
    appointmentDate?: string;
    fractionNo?: number;
    treatmentRoomName?: string;
    deviceName?: string;
    doctorId?: number;
    doctorName?: string;
    workflowStatus?: string;
    processInstanceId?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }

  export interface TreatmentAppointmentScheduleReq {
    doctorId?: number;
    endDate?: string;
    keyword?: string;
    startDate?: string;
    treatmentRoomName?: string;
  }
}

export function getTreatmentAppointmentPage(params: PageParam) {
  return requestClient.get<
    PageResult<HospitalTreatmentAppointmentApi.TreatmentAppointment>
  >('/hospital/treatment-appointment/page', { params });
}

export function getTreatmentAppointment(id: number) {
  return requestClient.get<HospitalTreatmentAppointmentApi.TreatmentAppointment>(
    `/hospital/treatment-appointment/get?id=${id}`,
  );
}

export function getTreatmentAppointmentScheduleList(
  params: HospitalTreatmentAppointmentApi.TreatmentAppointmentScheduleReq,
) {
  return requestClient.get<
    HospitalTreatmentAppointmentApi.TreatmentAppointment[]
  >('/hospital/treatment-appointment/schedule-list', { params });
}

export function createTreatmentAppointment(
  data: HospitalTreatmentAppointmentApi.TreatmentAppointment,
) {
  return requestClient.post('/hospital/treatment-appointment/create', data);
}

export function updateTreatmentAppointment(
  data: HospitalTreatmentAppointmentApi.TreatmentAppointment,
) {
  return requestClient.put('/hospital/treatment-appointment/update', data);
}

export function submitTreatmentAppointment(id: number) {
  return requestClient.put(`/hospital/treatment-appointment/submit?id=${id}`);
}

export function deleteTreatmentAppointment(id: number) {
  return requestClient.delete(
    `/hospital/treatment-appointment/delete?id=${id}`,
  );
}
