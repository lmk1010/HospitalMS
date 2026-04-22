<script setup lang="ts">
import type { AboutProps, DescriptionItem } from './about';

import { Page } from '../../components';

interface Props extends AboutProps {}

defineOptions({
  name: 'AboutUI',
});

withDefaults(defineProps<Props>(), {
  description:
    '面向医院业务管理、流程审批与日常运营协同的一体化后台系统。',
  name: '上海XX医院管理平台',
  title: '关于系统',
});

const systemInfo: DescriptionItem[] = [
  { content: '上海XX医院管理平台', title: '系统名称' },
  { content: '院内本地化部署', title: '部署方式' },
  { content: 'Spring Boot 单体 + Vue3', title: '技术架构' },
  { content: 'BPM 审批、基础权限、消息通知', title: '核心能力' },
];
</script>

<template>
  <Page :title="title">
    <template #description>
      <p class="text-foreground mt-3 text-sm leading-6">
        {{ name }}{{ description }}
      </p>
    </template>
    <div class="card-box p-5">
      <div>
        <h5 class="text-foreground text-lg">基础信息</h5>
      </div>
      <div class="mt-4">
        <dl class="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4">
          <template v-for="item in systemInfo" :key="item.title">
            <div class="border-border border-t px-4 py-6 sm:col-span-1 sm:px-0">
              <dt class="text-foreground text-sm font-medium leading-6">
                {{ item.title }}
              </dt>
              <dd class="text-foreground mt-1 text-sm leading-6 sm:mt-2">
                {{ item.content }}
              </dd>
            </div>
          </template>
        </dl>
      </div>
    </div>
  </Page>
</template>
