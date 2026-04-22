<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalDoctorApi } from '#/api/hospital/doctor';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDoctor, getDoctorPage } from '#/api/hospital/doctor';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

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

function handleEdit(row: HospitalDoctorApi.Doctor) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: HospitalDoctorApi.Doctor) {
  const hideLoading = message.loading({
    content: `正在删除医生【${row.name}】`,
    duration: 0,
  });
  try {
    await deleteDoctor(row.id!);
    message.success('删除成功');
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
          return await getDoctorPage({
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
  } as VxeTableGridOptions<HospitalDoctorApi.Doctor>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="医生档案列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '新增医生',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['hospital:doctor:create'],
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
              auth: ['hospital:doctor:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '删除',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['hospital:doctor:delete'],
              popConfirm: {
                title: `确认删除医生【${row.name}】吗？`,
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
