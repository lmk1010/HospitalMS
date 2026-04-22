import { requestClient } from '#/api/request';

export namespace HospitalDepartmentApi {
  export interface Department {
    id?: number;
    name: string;
    code: string;
    parentId?: number;
    type?: number;
    directorName?: string;
    phone?: string;
    sort?: number;
    status?: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getDepartmentList(params?: Record<string, any>) {
  return requestClient.get<HospitalDepartmentApi.Department[]>('/hospital/department/list', { params });
}

export function getSimpleDepartmentList() {
  return requestClient.get<HospitalDepartmentApi.Department[]>('/hospital/department/simple-list');
}

export function getDepartment(id: number) {
  return requestClient.get<HospitalDepartmentApi.Department>(`/hospital/department/get?id=${id}`);
}

export function createDepartment(data: HospitalDepartmentApi.Department) {
  return requestClient.post('/hospital/department/create', data);
}

export function updateDepartment(data: HospitalDepartmentApi.Department) {
  return requestClient.put('/hospital/department/update', data);
}

export function deleteDepartment(id: number) {
  return requestClient.delete(`/hospital/department/delete?id=${id}`);
}
