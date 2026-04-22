import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/hospital-flow',
    name: 'HospitalFlowSettings',
    meta: {
      title: '医院流程设置',
      hideInMenu: true,
    },
    children: [
      {
        path: 'process-manage/design',
        name: 'HospitalWorkflowDesigner',
        component: () =>
          import('#/views/hospital/flow/process-manage/designer/index.vue'),
        meta: {
          title: '设计医院流程',
          activePath: '/hospital-flow/process-manage',
          hideInMenu: true,
        },
        props: (route) => ({
          id: Array.isArray(route.query.id)
            ? route.query.id[0]
            : route.query.id,
          nodeId: Array.isArray(route.query.nodeId)
            ? route.query.nodeId[0]
            : route.query.nodeId,
          type: Array.isArray(route.query.type)
            ? route.query.type[0]
            : route.query.type,
        }),
      },
      {
        path: 'form-manage/design',
        name: 'HospitalCustomFormDesigner',
        component: () =>
          import('#/views/hospital/flow/form-manage/designer/index.vue'),
        meta: {
          title: '设计医院表单',
          activePath: '/hospital-flow/form-manage',
          hideInMenu: true,
        },
        props: (route) => ({
          id: route.query.id,
          type: route.query.type,
          copyId: route.query.copyId,
          processKey: route.query.processKey,
          processName: route.query.processName,
          nodeKey: route.query.nodeKey,
          nodeName: route.query.nodeName,
          pageCode: route.query.pageCode,
          backTo: route.query.backTo,
          workflowId: route.query.workflowId,
          workflowType: route.query.workflowType,
          focusNodeId: route.query.focusNodeId,
        }),
      },
    ],
  },
];

export default routes;
