<script lang="ts" setup>
import { Page } from '@vben/common-ui';

interface CardItem {
  label: string;
  remark?: string;
  value: string;
}

interface Props {
  actions?: string[];
  cards?: CardItem[];
  columns?: string[];
  fields?: string[];
  summary: string;
  title: string;
  workflow?: string[];
}

const props = defineProps<Props>();
</script>

<template>
  <Page :title="props.title">
    <div class="space-y-4">
      <div class="card-box rounded-2xl p-6">
        <div class="text-xl font-semibold">{{ props.title }}</div>
        <div class="text-foreground/70 mt-2 leading-7">{{ props.summary }}</div>
        <div v-if="props.actions?.length" class="mt-4 flex flex-wrap gap-2">
          <span
            v-for="item in props.actions"
            :key="item"
            class="rounded-full bg-sky-50 px-3 py-1 text-xs text-sky-700 dark:bg-sky-500/10 dark:text-sky-300"
          >
            {{ item }}
          </span>
        </div>
        <div v-if="props.workflow?.length" class="mt-4 flex flex-wrap items-center gap-2">
          <template v-for="(item, index) in props.workflow" :key="item">
            <span class="rounded-lg border px-3 py-1 text-sm">{{ item }}</span>
            <span v-if="index < props.workflow.length - 1" class="text-foreground/40">→</span>
          </template>
        </div>
      </div>

      <div v-if="props.cards?.length" class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <div v-for="item in props.cards" :key="item.label" class="card-box rounded-2xl p-5">
          <div class="text-foreground/60 text-sm">{{ item.label }}</div>
          <div class="mt-2 text-lg font-semibold">{{ item.value }}</div>
          <div v-if="item.remark" class="text-foreground/50 mt-2 text-xs">{{ item.remark }}</div>
        </div>
      </div>

      <div v-if="props.fields?.length" class="card-box rounded-2xl p-6">
        <div class="mb-4 text-lg font-semibold">重点字段</div>
        <div class="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-5">
          <div v-for="item in props.fields" :key="item" class="rounded-xl border px-4 py-3 text-sm">
            {{ item }}
          </div>
        </div>
      </div>

      <div v-if="props.columns?.length" class="card-box rounded-2xl p-6">
        <div class="mb-4 text-lg font-semibold">列表字段</div>
        <div class="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-5">
          <div v-for="item in props.columns" :key="item" class="rounded-xl border px-4 py-3 text-sm">
            {{ item }}
          </div>
        </div>
      </div>
    </div>
  </Page>
</template>
