<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalTreatmentSummaryApi } from '#/api/hospital/treatment-summary';

import { onMounted } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { useRoute } from 'vue-router';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getTreatmentSummary, getTreatmentSummaryPage } from '#/api/hospital/treatment-summary';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const route = useRoute();

function getFocusId() {
  const rawValue = Array.isArray(route.query.focusId)
    ? route.query.focusId[0]
    : route.query.focusId;
  const focusId = Number(rawValue);
  return Number.isFinite(focusId) && focusId > 0 ? focusId : undefined;
}

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

function handleRefresh() {
  gridApi.query();
}

function handleCreate() {
  formModalApi.setData(null).open();
}

function handleEdit(row: HospitalTreatmentSummaryApi.TreatmentSummary) {
  formModalApi.setData(row).open();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getTreatmentSummaryPage({
            id: getFocusId(),
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<HospitalTreatmentSummaryApi.TreatmentSummary>,
});

onMounted(async () => {
  const focusId = getFocusId();
  if (!focusId) return;
  const summary = await getTreatmentSummary(focusId).catch(() => undefined);
  if (summary) {
    formModalApi.setData(summary).open();
  }
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="治疗小结列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '新增治疗小结',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['hospital:treatment-summary:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '编辑',
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['hospital:treatment-summary:update'],
              onClick: handleEdit.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
