import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace HospitalCustomFormApi {
  export interface CustomForm {
    id?: number;
    name: string;
    code: string;
    deptId?: number;
    deptName?: string;
    bizCategory?: string;
    processKey?: string;
    processName?: string;
    nodeKey?: string;
    nodeName?: string;
    pageCode: string;
    pagePath?: string;
    conf: string;
    fields: string[];
    status: number;
    remark?: string;
    creator?: string;
    updater?: string;
    createTime?: string;
    updateTime?: string;
  }
}

export function getCustomFormPage(params: PageParam) {
  return requestClient.get<PageResult<HospitalCustomFormApi.CustomForm>>(
    '/hospital/custom-form/page',
    { params },
  );
}

export function getCustomForm(id: number) {
  return requestClient.get<HospitalCustomFormApi.CustomForm>(
    `/hospital/custom-form/get?id=${id}`,
  );
}

export function createCustomForm(data: HospitalCustomFormApi.CustomForm) {
  return requestClient.post('/hospital/custom-form/create', data);
}

export function updateCustomForm(data: HospitalCustomFormApi.CustomForm) {
  return requestClient.put('/hospital/custom-form/update', data);
}

export function deleteCustomForm(id: number) {
  return requestClient.delete(`/hospital/custom-form/delete?id=${id}`);
}

export function getCustomFormSimpleList() {
  return requestClient.get<HospitalCustomFormApi.CustomForm[]>(
    '/hospital/custom-form/simple-list',
  );
}

export function getCustomFormByPage(params: {
  pageCode: string;
  processKey?: string;
  nodeKey?: string;
}) {
  return requestClient.get<HospitalCustomFormApi.CustomForm>(
    '/hospital/custom-form/get-by-page',
    { params },
  );
}
