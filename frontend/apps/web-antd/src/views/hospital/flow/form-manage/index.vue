<script lang="ts" setup>
import type { HospitalCustomFormApi } from '#/api/hospital/custom-form';

import { computed, onActivated, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  Button,
  InputSearch,
  message,
  Popconfirm,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  deleteCustomForm,
  getCustomFormPage,
} from '#/api/hospital/custom-form';
import { router } from '#/router';

import { resolvePagePathByCode } from './data';
import Detail from './modules/detail.vue';

defineOptions({ name: 'HospitalCustomForm' });

const route = useRoute();
const loading = ref(false);
const keyword = ref('');
const rows = ref<HospitalCustomFormApi.CustomForm[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(20);
const filterProcessKey = computed(
  () => `${route.query.processKey || ''}` || undefined,
);
const filterProcessName = computed(() => `${route.query.processName || ''}`);
const columns = [
  { title: '表单名称', dataIndex: 'name', key: 'name', width: 220 },
  { title: '表单编码', dataIndex: 'code', key: 'code', width: 220 },
  {
    title: '业务分类',
    dataIndex: 'bizCategory',
    key: 'bizCategory',
    width: 120,
  },
  { title: '所属科室', dataIndex: 'deptName', key: 'deptName', width: 120 },
  {
    title: '流程名称',
    dataIndex: 'processName',
    key: 'processName',
    width: 180,
  },
  { title: '节点名称', dataIndex: 'nodeName', key: 'nodeName', width: 140 },
  { title: '页面编码', dataIndex: 'pageCode', key: 'pageCode', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '更新时间', key: 'updateTime', width: 180 },
  { title: '操作', key: 'actions', width: 220, fixed: 'right' },
];

const [DetailModal, detailModalApi] = useVbenModal({
  connectedComponent: Detail,
  destroyOnClose: true,
});

async function loadList(reset = false) {
  if (reset) pageNo.value = 1;
  loading.value = true;
  try {
    const data = await getCustomFormPage({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      name: keyword.value.trim() || undefined,
      processKey: filterProcessKey.value,
    });
    rows.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

function handleCreate() {
  router.push({
    name: 'HospitalCustomFormDesigner',
    query: {
      type: 'create',
      processKey: route.query.processKey,
      processName: route.query.processName,
    },
  });
}
function handleEdit(row: HospitalCustomFormApi.CustomForm) {
  router.push({
    name: 'HospitalCustomFormDesigner',
    query: { id: row.id, type: 'edit' },
  });
}
function handleCopy(row: HospitalCustomFormApi.CustomForm) {
  router.push({
    name: 'HospitalCustomFormDesigner',
    query: { copyId: row.id, type: 'copy' },
  });
}
function handlePreview(row: HospitalCustomFormApi.CustomForm) {
  detailModalApi.setData(row).open();
}
function handleOpenPage(row: HospitalCustomFormApi.CustomForm) {
  const pagePath = row.pagePath || resolvePagePathByCode(row.pageCode);
  if (!pagePath) {
    message.warning('当前表单未配置业务页面');
    return;
  }
  router.push(pagePath);
}
async function handleDelete(row: HospitalCustomFormApi.CustomForm) {
  await deleteCustomForm(row.id!);
  message.success(`已删除 ${row.name}`);
  loadList();
}
function handleTableChange(page: any) {
  pageNo.value = page.current || 1;
  pageSize.value = page.pageSize || 20;
  loadList();
}
function clearFilter() {
  router.push({ path: '/hospital-flow/form-manage' });
}

watch(filterProcessKey, () => loadList(true));
onMounted(() => loadList());
onActivated(() => loadList());
</script>

<template>
  <Page auto-content-height>
    <DetailModal />
    <div class="bg-white px-4 py-3">
      <div class="mb-3 flex items-center justify-between gap-3">
        <div class="flex items-center gap-2">
          <div class="text-base font-semibold">表单管理</div>
          <Tag v-if="filterProcessName" color="blue">
            {{ filterProcessName }}
          </Tag>
          <Button
            v-if="filterProcessName"
            type="link"
            size="small"
            @click="clearFilter"
          >
            清除筛选
          </Button>
        </div>
        <Space :size="8">
          <InputSearch
            v-model:value="keyword"
            allow-clear
            placeholder="请输入表单名称关键字"
            style="width: 240px"
            @search="loadList(true)"
          />
          <Button type="primary" @click="handleCreate">新建</Button>
        </Space>
      </div>
      <Table
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="{
          current: pageNo,
          pageSize,
          total,
          showSizeChanger: false,
        }"
        :scroll="{ x: 1520 }"
        :locale="{ emptyText: '暂无表单数据' }"
        row-key="id"
        size="small"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'code'">
            <span class="font-mono text-[12px] text-gray-600">{{
              record.code
            }}</span>
          </template>
          <template v-else-if="column.key === 'bizCategory'">
            <Tag color="blue">{{ record.bizCategory || '--' }}</Tag>
          </template>
          <template v-else-if="column.key === 'deptName'">
            {{ record.deptName || '放疗科' }}
          </template>
          <template v-else-if="column.key === 'processName'">
            {{ record.processName || '--' }}
          </template>
          <template v-else-if="column.key === 'nodeName'">
            {{ record.nodeName || '--' }}
          </template>
          <template v-else-if="column.key === 'status'">
            <Tag :color="record.status === 0 ? 'processing' : 'default'">
              {{ record.status === 0 ? '启用' : '停用' }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'updateTime'">
            {{ formatDateTime(record.updateTime || record.createTime) || '--' }}
          </template>
          <template v-else-if="column.key === 'actions'">
            <Space :size="2" wrap>
              <Button type="link" size="small" @click="handleEdit(record)">
                设计
              </Button>
              <Button type="link" size="small" @click="handleCopy(record)">
                复制
              </Button>
              <Button type="link" size="small" @click="handlePreview(record)">
                预览
              </Button>
              <Button type="link" size="small" @click="handleOpenPage(record)">
                打开页面
              </Button>
              <Popconfirm
                :title="`确认删除 ${record.name} 吗？`"
                @confirm="handleDelete(record)"
              >
                <Button danger type="link" size="small">删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </div>
  </Page>
</template>
