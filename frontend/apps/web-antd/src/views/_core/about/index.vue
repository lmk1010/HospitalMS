<script lang="ts" setup>
import { Page } from '@vben/common-ui';

defineOptions({ name: 'About' });

const basicInfo = [
  { label: '系统名称', value: '上海XX医院管理平台' },
  { label: '部署架构', value: 'Spring Boot 单体 + Vue3 + MySQL' },
  { label: '流程能力', value: '已集成 BPM 审批与流程配置' },
  { label: '运行环境', value: '本地开发环境 / 医院内网环境' },
];

const scopeInfo = [
  '患者信息管理',
  '预约排班与流程审批',
  '系统权限与消息通知',
  '日志审计与基础运维',
];
</script>

<template>
  <Page title="关于系统">
    <div class="space-y-4">
      <div class="card-box rounded-2xl p-6">
        <div class="text-xl font-semibold">上海XX医院</div>
        <div class="text-foreground/70 mt-2 leading-7">
          当前系统用于医院业务管理与流程协同，前端品牌信息已按院内场景做本地化收口，
          不展示外部推广入口、文档引流或开源项目宣传信息。
        </div>
      </div>

      <div class="card-box rounded-2xl p-6">
        <div class="mb-4 text-lg font-semibold">基础信息</div>
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div
            v-for="item in basicInfo"
            :key="item.label"
            class="rounded-xl border border-gray-200 px-4 py-4 dark:border-gray-700"
          >
            <div class="text-foreground/60 text-sm">{{ item.label }}</div>
            <div class="mt-2 font-medium">{{ item.value }}</div>
          </div>
        </div>
      </div>

      <div class="card-box rounded-2xl p-6">
        <div class="mb-4 text-lg font-semibold">当前建设范围</div>
        <ul class="list-disc space-y-2 pl-5 text-sm leading-7">
          <li v-for="item in scopeInfo" :key="item">{{ item }}</li>
        </ul>
      </div>
    </div>
  </Page>
</template>
