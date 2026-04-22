<script lang="ts" setup>
import WorkflowModelPage from '../modules/workflow-model-page.vue';

const config = {
  formPath: '/hospital-workflow/external-beam-px',
  modelKey: 'hospital_radiotherapy_external_beam_px',
  subtitle: '用于展示 PX 业务流程节点，并可直接进入 BPM 设计器维护。',
  summary:
    'PX 流程覆盖影像采集、靶区勾画、剂量设计、计划审核、计划验证与疗程评估。',
  title: '外照射业务流程（PX）',
  workflowKey: 'radiotherapy-px' as const,
};
</script>
<template><WorkflowModelPage v-bind="config" /></template>
