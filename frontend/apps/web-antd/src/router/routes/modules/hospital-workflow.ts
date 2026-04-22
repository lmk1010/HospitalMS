import type { RouteRecordRaw } from 'vue-router';
const routes: RouteRecordRaw[] = [
  {
    path: '/hospital-workflow',
    name: 'HospitalWorkflowPages',
    meta: { title: '医院流程', hideInMenu: true },
    children: [
      {
        path: 'external-beam-px',
        name: 'HospitalWorkflowExternalBeamPx',
        meta: {
          activePath: '/bpm/manager/model',
          hideInMenu: true,
          title: '外照射业务流程（PX）',
        },
        component: () =>
          import('#/views/hospital/workflow/external-beam-px/index.vue'),
      },
      {
        path: 'external-beam-sj',
        name: 'HospitalWorkflowExternalBeamSj',
        meta: {
          activePath: '/bpm/manager/model',
          hideInMenu: true,
          title: '外照射业务流程（SJ）',
        },
        component: () =>
          import('#/views/hospital/workflow/external-beam-sj/index.vue'),
      },
      {
        path: 'brachytherapy',
        name: 'HospitalWorkflowBrachytherapy',
        meta: {
          activePath: '/bpm/manager/model',
          hideInMenu: true,
          title: '后装治疗业务流程',
        },
        component: () =>
          import('#/views/hospital/workflow/brachytherapy/index.vue'),
      },
    ],
  },
];
export default routes;
