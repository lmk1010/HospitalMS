<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import { Badge, Card, Col, Row, Tag, Timeline } from 'ant-design-vue';

const roomStatus = [
  { name: '一号治疗室', status: 'online', detail: '设备在线，支持远程协助' },
  { name: '二号治疗室', status: 'busy', detail: '当前治疗中，暂不允许接管' },
  { name: 'CT定位室', status: 'online', detail: '支持阅片会诊与参数复核' },
  { name: '计划物理组', status: 'warning', detail: '发现 1 条待确认告警' },
];

const statusMap: Record<string, { color: string; text: string }> = {
  online: { color: 'success', text: '可连接' },
  busy: { color: 'processing', text: '使用中' },
  warning: { color: 'warning', text: '待确认' },
};
</script>

<template>
  <Page>
    <Row :gutter="16">
      <Col :lg="8" :md="24" :sm="24" :xs="24">
        <Card title="远程协助能力">
          <div class="flex flex-col gap-4 text-[14px]">
            <div class="flex items-center justify-between"><span>在线机房</span><Tag color="success">3 / 4</Tag></div>
            <div class="flex items-center justify-between"><span>会诊通道</span><Tag color="processing">已开启</Tag></div>
            <div class="flex items-center justify-between"><span>录屏审计</span><Tag color="success">已启用</Tag></div>
            <div class="flex items-center justify-between"><span>应急预案</span><Tag color="warning">人工确认</Tag></div>
          </div>
        </Card>
      </Col>
      <Col :lg="16" :md="24" :sm="24" :xs="24">
        <Card title="机房连通状态">
          <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <div v-for="item in roomStatus" :key="item.name" class="rounded-xl border border-[var(--ant-color-border-secondary)] p-4">
              <div class="flex items-center justify-between">
                <div class="font-medium">{{ item.name }}</div>
                <Badge :status="statusMap[item.status]?.color as any" :text="statusMap[item.status]?.text" />
              </div>
              <div class="mt-2 text-[13px] text-[var(--ant-color-text-description)]">{{ item.detail }}</div>
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <Row :gutter="16" class="mt-4">
      <Col :span="24">
        <Card title="操作预案">
          <Timeline>
            <Timeline.Item color="blue">远程接入前先确认治疗室未在出束状态，避免误操作。</Timeline.Item>
            <Timeline.Item color="green">计划参数复核应双人确认，并留存审计记录与操作截图。</Timeline.Item>
            <Timeline.Item color="orange">设备告警时优先切换人工模式，再通知物理师和值班工程师。</Timeline.Item>
            <Timeline.Item color="red">若涉及患者治疗中断，需同步记录异常并触发后续 BPM 处理流程。</Timeline.Item>
          </Timeline>
        </Card>
      </Col>
    </Row>
  </Page>
</template>
