<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalTreatmentExecuteApi } from '#/api/hospital/treatment-execute';

import { Page } from '@vben/common-ui';
import { useRoute } from 'vue-router';

import { message } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  completeTreatment,
  executeTreatment,
  getTreatmentExecutePage,
} from '#/api/hospital/treatment-execute';

import { useGridColumns, useGridFormSchema } from './data';

const route = useRoute();

function getFocusId() {
  const rawValue = Array.isArray(route.query.focusId)
    ? route.query.focusId[0]
    : route.query.focusId;
  const focusId = Number(rawValue);
  return Number.isFinite(focusId) && focusId > 0 ? focusId : undefined;
}

function handleRefresh() {
  gridApi.query();
}

async function handleExecute(row: HospitalTreatmentExecuteApi.TreatmentExecute) {
  const hideLoading = message.loading({
    content: `正在开始治疗【${row.bizNo}】`,
    duration: 0,
  });
  try {
    await executeTreatment(row.id!);
    message.success('开始成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

async function handleComplete(row: HospitalTreatmentExecuteApi.TreatmentExecute) {
  const hideLoading = message.loading({
    content: `正在完成治疗【${row.bizNo}】`,
    duration: 0,
  });
  try {
    await completeTreatment(row.id!);
    message.success('完成成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getTreatmentExecutePage({
            id: getFocusId(),
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<HospitalTreatmentExecuteApi.TreatmentExecute>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="治疗执行列表">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '开始执行',
              type: 'link',
              auth: ['hospital:treatment-execute:execute'],
              ifShow: row.executeResult === 'WAIT_EXECUTE',
              onClick: handleExecute.bind(null, row),
            },
            {
              label: '执行完成',
              type: 'link',
              auth: ['hospital:treatment-execute:complete'],
              ifShow:
                row.executeResult === 'WAIT_EXECUTE' ||
                row.executeResult === 'EXECUTING',
              onClick: handleComplete.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
