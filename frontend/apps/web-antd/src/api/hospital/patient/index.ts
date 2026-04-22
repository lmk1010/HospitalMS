import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalPatientApi {
  export interface Patient {
    id?: number;
    patientNo?: string;
    name: string;
    gender?: number;
    birthday?: string;
    age?: number;
    idType?: string;
    idNo?: string;
    outpatientNo?: string;
    hospitalizationNo?: string;
    radiotherapyNo?: string;
    medicalRecordNo?: string;
    maritalStatus?: string;
    nativePlace?: string;
    currentAddress?: string;
    patientPhone?: string;
    contactName?: string;
    contactRelation?: string;
    contactPhone?: string;
    managerDoctorId?: number;
    managerDoctorName?: string;
    attendingDoctorId?: number;
    attendingDoctorName?: string;
    patientType?: string;
    wardName?: string;
    paymentType?: string;
    visitNo?: string;
    campus?: string;
    firstDoctorName?: string;
    socialSecurityNo?: string;
    photoUrl?: null | string;
    allergyHistory?: string;
    pastHistory?: string;
    tags?: string;
    status?: number;
    remark?: string;
    createTime?: Date;
  }

  export interface PatientDashboardQuery extends PageParam {
    currentStage?: string;
    currentNodeCode?: string;
    currentNodeName?: string;
    currentRoutePath?: string;
    currentBizId?: number;
    keyword?: string;
    managerDoctorId?: number;
    patientType?: string;
    planPhysicistName?: string;
  }

  export interface PatientDashboardRow {
    id: number;
    patientNo?: string;
    name: string;
    gender?: number;
    age?: number;
    idNo?: string;
    photoUrl?: null | string;
    patientPhone?: string;
    medicalRecordNo?: string;
    radiotherapyNo?: string;
    hospitalizationNo?: string;
    diagnosis?: string;
    managerDoctorId?: number;
    managerDoctorName?: string;
    planPhysicistName?: string;
    currentStageCode?: string;
    currentStage?: string;
    currentNodeCode?: string;
    currentNodeName?: string;
    currentRoutePath?: string;
    currentBizId?: number;
    totalFractions?: number;
    completedFractions?: number;
    patientType?: string;
    wardName?: string;
    registrationUserName?: string;
    registrationTime?: Date | number | string;
  }

  export interface PatientDashboardSummary {
    totalCount: number;
    todayCount: number;
    monthCount: number;
    registerCount: number;
    positioningCount: number;
    contourCount: number;
    planningCount: number;
    reviewCount: number;
    treatmentCount: number;
  }
}

export function getPatientPage(
  params: PageParam & {
    attendingDoctorId?: number;
    hospitalizationNo?: string;
    idNo?: string;
    managerDoctorId?: number;
    name?: string;
    patientNo?: string;
    patientPhone?: string;
    patientType?: string;
    socialSecurityNo?: string;
    status?: number;
  },
) {
  return requestClient.get<PageResult<HospitalPatientApi.Patient>>(
    '/hospital/patient/page',
    { params },
  );
}

export function getPatientDashboardPage(
  params: HospitalPatientApi.PatientDashboardQuery,
) {
  return requestClient.get<PageResult<HospitalPatientApi.PatientDashboardRow>>(
    '/hospital/patient/dashboard-page',
    { params },
  );
}

export function getPatientDashboardSummary(
  params: Omit<HospitalPatientApi.PatientDashboardQuery, 'pageNo' | 'pageSize'>,
) {
  return requestClient.get<HospitalPatientApi.PatientDashboardSummary>(
    '/hospital/patient/dashboard-summary',
    { params },
  );
}

export function getSimplePatientList() {
  return requestClient.get<HospitalPatientApi.Patient[]>(
    '/hospital/patient/simple-list',
  );
}

export function getPatient(id: number) {
  return requestClient.get<HospitalPatientApi.Patient>(
    `/hospital/patient/get?id=${id}`,
  );
}

export function createPatient(data: HospitalPatientApi.Patient) {
  return requestClient.post('/hospital/patient/create', data);
}

export function updatePatient(data: HospitalPatientApi.Patient) {
  return requestClient.put('/hospital/patient/update', data);
}

export function deletePatient(id: number) {
  return requestClient.delete(`/hospital/patient/delete?id=${id}`);
}
