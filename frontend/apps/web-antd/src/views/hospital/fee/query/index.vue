<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalFeeRecordApi } from '#/api/hospital/fee-record';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getFeeQueryPage } from '#/api/hospital/fee-query';

import { useGridColumns, useGridFormSchema } from './data';

const [Grid] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: { ajax: { query: async ({ page }, formValues) => await getFeeQueryPage({ pageNo: page.currentPage, pageSize: page.pageSize, ...formValues }) } },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<HospitalFeeRecordApi.FeeRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="费用查询" />
  </Page>
</template>
