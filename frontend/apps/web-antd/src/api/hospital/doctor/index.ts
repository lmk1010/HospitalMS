import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalDoctorApi {
  export interface Doctor {
    id?: number;
    deptId: number;
    deptName?: string;
    userId?: number;
    doctorCode: string;
    name: string;
    gender?: number;
    phone?: string;
    title?: string;
    practicingLicenseNo?: string;
    specialty?: string;
    sort?: number;
    status?: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getDoctorPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalDoctorApi.Doctor>>('/hospital/doctor/page', { params });
}

export function getSimpleDoctorList() {
  return requestClient.get<HospitalDoctorApi.Doctor[]>('/hospital/doctor/simple-list');
}

export function getDoctor(id: number) {
  return requestClient.get<HospitalDoctorApi.Doctor>(`/hospital/doctor/get?id=${id}`);
}

export function createDoctor(data: HospitalDoctorApi.Doctor) {
  return requestClient.post('/hospital/doctor/create', data);
}

export function updateDoctor(data: HospitalDoctorApi.Doctor) {
  return requestClient.put('/hospital/doctor/update', data);
}

export function deleteDoctor(id: number) {
  return requestClient.delete(`/hospital/doctor/delete?id=${id}`);
}
