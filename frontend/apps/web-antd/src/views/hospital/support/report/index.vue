<script lang="ts" setup>
import type { EchartsUIType } from '@vben/plugins/echarts';

import { onMounted, ref } from 'vue';

import { ComparisonCard, Page } from '@vben/common-ui';
import { EchartsUI, useEcharts } from '@vben/plugins/echarts';

import { Card, Col, Row, Table, Tag } from 'ant-design-vue';

const chartRef = ref<EchartsUIType>();
const { renderEcharts } = useEcharts(chartRef);

const summaryCards = [
  { title: '本周治疗人次', value: 286, todayCount: 42, icon: 'dashboard', iconColor: 'text-sky-500' },
  { title: '本周流程办结', value: 94, todayCount: 18, icon: 'audit', iconColor: 'text-violet-500' },
  { title: '本周结算金额', value: 182600, todayCount: 35600, icon: 'money-collect', iconColor: 'text-amber-500' },
  { title: '设备在线率', value: 98.7, todayCount: 0.3, icon: 'wifi', iconColor: 'text-emerald-500' },
];

const wardColumns = [
  { title: '科室/机房', dataIndex: 'name', key: 'name' },
  { title: '预约量', dataIndex: 'appointmentCount', key: 'appointmentCount' },
  { title: '执行量', dataIndex: 'executeCount', key: 'executeCount' },
  { title: '异常数', dataIndex: 'abnormalCount', key: 'abnormalCount' },
  { title: '完成率', dataIndex: 'rate', key: 'rate' },
];

const wardData = [
  { key: 1, name: '一号治疗室', appointmentCount: 36, executeCount: 32, abnormalCount: 1, rate: '88.9%' },
  { key: 2, name: '二号治疗室', appointmentCount: 30, executeCount: 29, abnormalCount: 0, rate: '96.7%' },
  { key: 3, name: 'CT定位室', appointmentCount: 24, executeCount: 22, abnormalCount: 1, rate: '91.7%' },
  { key: 4, name: '计划物理组', appointmentCount: 18, executeCount: 17, abnormalCount: 0, rate: '94.4%' },
];

onMounted(() => {
  renderEcharts({
    color: ['#1677ff', '#52c41a', '#fa8c16'],
    grid: { left: '3%', right: '3%', bottom: '3%', containLabel: true },
    legend: { top: 0 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: [{ type: 'value', name: '人次/单' }, { type: 'value', name: '金额', splitLine: { show: false } }],
    series: [
      { name: '治疗执行', type: 'bar', barMaxWidth: 36, data: [36, 42, 39, 44, 47, 38, 40] },
      { name: '流程办结', type: 'line', smooth: true, data: [12, 14, 11, 16, 15, 13, 13] },
      { name: '结算金额', type: 'line', smooth: true, yAxisIndex: 1, data: [2.8, 3.2, 2.6, 3.8, 4.1, 3.3, 3.5] },
    ],
  });
});
</script>

<template>
  <Page>
    <Row :gutter="16" class="mb-4">
      <Col v-for="card in summaryCards" :key="card.title" :lg="6" :md="12" :sm="12" :xs="24">
        <ComparisonCard :title="card.title" :value="card.value" :today-count="card.todayCount" :icon="card.icon" :icon-color="card.iconColor" />
      </Col>
    </Row>

    <Row :gutter="16">
      <Col :lg="16" :md="24" :sm="24" :xs="24">
        <Card title="周运营趋势">
          <EchartsUI ref="chartRef" style="height: 320px" />
        </Card>
      </Col>
      <Col :lg="8" :md="24" :sm="24" :xs="24">
        <Card title="关键指标">
          <div class="flex flex-col gap-4 text-[14px]">
            <div class="flex items-center justify-between"><span>CT 到检率</span><Tag color="processing">92.5%</Tag></div>
            <div class="flex items-center justify-between"><span>计划审核通过率</span><Tag color="success">95.1%</Tag></div>
            <div class="flex items-center justify-between"><span>治疗准点率</span><Tag color="warning">89.3%</Tag></div>
            <div class="flex items-center justify-between"><span>费用冲正占比</span><Tag color="error">1.2%</Tag></div>
          </div>
        </Card>
      </Col>
    </Row>

    <Card class="mt-4" title="科室/机房产能分布">
      <Table :columns="wardColumns" :data-source="wardData" :pagination="false" size="small" />
    </Card>
  </Page>
</template>
