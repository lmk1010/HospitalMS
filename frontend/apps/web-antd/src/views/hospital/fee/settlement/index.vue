<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalFeeSettlementApi } from '#/api/hospital/fee-settlement';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getFeeSettlementPage, reverseFeeSettlement } from '#/api/hospital/fee-settlement';

import { useGridColumns, useGridFormSchema } from './data';
import SettleForm from './modules/settle-form.vue';

const [SettleModal, settleModalApi] = useVbenModal({ connectedComponent: SettleForm, destroyOnClose: true });

function handleRefresh() {
  gridApi.query();
}

function handleCreate() {
  settleModalApi.open();
}

async function handleReverse(row: HospitalFeeSettlementApi.FeeSettlement) {
  const hideLoading = message.loading({ content: `正在冲正【${row.bizNo}】`, duration: 0 });
  try {
    await reverseFeeSettlement(row.id!);
    message.success('冲正成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: { ajax: { query: async ({ page }, formValues) => await getFeeSettlementPage({ pageNo: page.currentPage, pageSize: page.pageSize, ...formValues }) } },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<HospitalFeeSettlementApi.FeeSettlement>,
});
</script>

<template>
  <Page auto-content-height>
    <SettleModal @success="handleRefresh" />
    <Grid table-title="费用结算列表">
      <template #toolbar-tools>
        <TableAction :actions="[{ label: '新增结算', type: 'primary', icon: ACTION_ICON.ADD, auth: ['hospital:fee-settlement:settle'], onClick: handleCreate }]" />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            { label: '冲正', type: 'link', danger: true, auth: ['hospital:fee-settlement:reverse'], ifShow: row.settlementStatus !== 'VOID', popConfirm: { title: `确认冲正结算单【${row.bizNo}】吗？`, confirm: handleReverse.bind(null, row) } },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
