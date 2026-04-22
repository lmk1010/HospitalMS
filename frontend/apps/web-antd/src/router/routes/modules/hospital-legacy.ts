import type { RouteRecordRaw } from 'vue-router';

const legacyRouteRedirects = [
  ['/hospital-flow/hospital-patient', '/hospital-flow/hospital-patient/list'],
  [
    '/hospital-flow/hospital-position',
    '/hospital-flow/hospital-position/ct-appointment',
  ],
  ['/hospital-flow/hospital-contour', '/hospital-flow/hospital-contour/task'],
  ['/hospital-flow/hospital-plan', '/hospital-flow/hospital-plan/apply'],
  [
    '/hospital-flow/hospital-treatment',
    '/hospital-flow/hospital-treatment/appointment',
  ],
  ['/hospital-flow/hospital-fee', '/hospital-flow/hospital-fee/record'],
  ['/hospital-patient', '/hospital-flow/hospital-patient/list'],
  ['/hospital-position', '/hospital-flow/hospital-position/ct-appointment'],
  ['/hospital-contour', '/hospital-flow/hospital-contour/task'],
  ['/hospital-plan', '/hospital-flow/hospital-plan/apply'],
  ['/hospital-treatment', '/hospital-flow/hospital-treatment/appointment'],
  ['/hospital-fee', '/hospital-flow/hospital-fee/record'],
  [
    '/hospital-patient/registration',
    '/hospital-flow/hospital-patient/registration',
  ],
  ['/hospital-patient/list', '/hospital-flow/hospital-patient/list'],
  [
    '/hospital-position/ct-appointment',
    '/hospital-flow/hospital-position/ct-appointment',
  ],
  ['/hospital-position/ct-queue', '/hospital-flow/hospital-position/ct-queue'],
  ['/hospital-contour/task', '/hospital-flow/hospital-contour/task'],
  ['/hospital-contour/review', '/hospital-flow/hospital-contour/review'],
  ['/hospital-plan/apply', '/hospital-flow/hospital-plan/apply'],
  ['/hospital-plan/design', '/hospital-flow/hospital-plan/design'],
  ['/hospital-plan/group-review', '/hospital-flow/hospital-plan/group-review'],
  [
    '/hospital-plan/doctor-approval',
    '/hospital-flow/hospital-plan/doctor-approval',
  ],
  ['/hospital-plan/recheck', '/hospital-flow/hospital-plan/recheck'],
  ['/hospital-plan/verify', '/hospital-flow/hospital-plan/verify'],
  [
    '/hospital-treatment/appointment',
    '/hospital-flow/hospital-treatment/appointment',
  ],
  ['/hospital-treatment/queue', '/hospital-flow/hospital-treatment/queue'],
  ['/hospital-treatment/execute', '/hospital-flow/hospital-treatment/execute'],
  ['/hospital-treatment/summary', '/hospital-flow/hospital-treatment/summary'],
  ['/hospital-fee/record', '/hospital-flow/hospital-fee/record'],
  ['/hospital-fee/settlement', '/hospital-flow/hospital-fee/settlement'],
  ['/hospital-fee/query', '/hospital-flow/hospital-fee/query'],
] as const;

const routes: RouteRecordRaw[] = legacyRouteRedirects.map(
  ([path, target], index) => ({
    path,
    name: `HospitalLegacyRedirect${index + 1}`,
    meta: {
      title: '医院旧路由兼容',
      hideInBreadcrumb: true,
      hideInMenu: true,
      hideInTab: true,
    },
    redirect: (route) => ({
      path: target,
      query: route.query,
    }),
  }),
);

export default routes;
