<!-- 消息列表为空时，展示 prompt 列表 -->
<script setup lang="ts">
const emits = defineEmits(['onPrompt']);

const promptList = [
  {
    prompt: '帮我整理今日门诊待办',
  },
  {
    prompt: '生成一份患者回访提醒清单',
  },
];

async function handlerPromptClick(prompt: any) {
  emits('onPrompt', prompt.prompt);
}
</script>
<template>
  <div class="relative flex h-full w-full flex-row justify-center">
    <div class="flex flex-col justify-center">
      <div class="text-center text-3xl font-bold">医院智能助手</div>
      <div class="mt-5 flex w-96 flex-wrap items-center justify-center">
        <div
          v-for="prompt in promptList"
          :key="prompt.prompt"
          class="m-2.5 flex w-44 cursor-pointer justify-center rounded-lg border border-gray-200 leading-10 hover:bg-gray-100"
          @click="handlerPromptClick(prompt)"
        >
          {{ prompt.prompt }}
        </div>
      </div>
    </div>
  </div>
</template>
