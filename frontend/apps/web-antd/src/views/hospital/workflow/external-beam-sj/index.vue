<script lang="ts" setup>
import WorkflowModelPage from '../modules/workflow-model-page.vue';

const config = {
  formPath: '/hospital-workflow/external-beam-sj',
  modelKey: 'hospital_radiotherapy_external_beam_sj',
  subtitle: '用于展示 SJ 业务流程节点，并可直接进入 BPM 设计器维护。',
  summary:
    'SJ 流程强调定位申请、影像融合、靶区勾画、计划设计、医师核准和治疗小结。',
  title: '外照射业务流程（SJ）',
  workflowKey: 'radiotherapy-sj' as const,
};
</script>
<template><WorkflowModelPage v-bind="config" /></template>
