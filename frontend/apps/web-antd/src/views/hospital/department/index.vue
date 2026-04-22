<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { HospitalDepartmentApi } from '#/api/hospital/department';

import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteDepartment, getDepartmentList } from '#/api/hospital/department';

import { useGridColumns } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const isExpanded = ref(true);
function handleExpand() {
  isExpanded.value = !isExpanded.value;
  gridApi.grid.setAllTreeExpand(isExpanded.value);
}

function handleRefresh() {
  gridApi.query();
}

function handleCreate() {
  formModalApi.setData(null).open();
}

function handleAppend(row: HospitalDepartmentApi.Department) {
  formModalApi.setData({ parentId: row.id }).open();
}

function handleEdit(row: HospitalDepartmentApi.Department) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: HospitalDepartmentApi.Department) {
  const hideLoading = message.loading({
    content: `正在删除科室【${row.name}】`,
    duration: 0,
  });
  try {
    await deleteDepartment(row.id!);
    message.success('删除成功');
    handleRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async () => {
          return await getDepartmentList();
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
    },
    treeConfig: {
      parentField: 'parentId',
      rowField: 'id',
      transform: true,
      expandAll: true,
      reserve: true,
    },
  } as VxeTableGridOptions<HospitalDepartmentApi.Department>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="医院科室列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '新增科室',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['hospital:department:create'],
              onClick: handleCreate,
            },
            {
              label: isExpanded ? '收缩层级' : '展开层级',
              type: 'primary',
              onClick: handleExpand,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '新增下级',
              type: 'link',
              icon: ACTION_ICON.ADD,
              auth: ['hospital:department:create'],
              onClick: handleAppend.bind(null, row),
            },
            {
              label: '编辑',
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['hospital:department:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '删除',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['hospital:department:delete'],
              popConfirm: {
                title: `确认删除科室【${row.name}】吗？`,
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
