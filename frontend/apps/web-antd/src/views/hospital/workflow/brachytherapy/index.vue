<script lang="ts" setup>
import WorkflowModelPage from '../modules/workflow-model-page.vue';

const config = {
  formPath: '/hospital-workflow/brachytherapy',
  modelKey: 'hospital_radiotherapy_brachytherapy',
  subtitle: '用于展示后装治疗业务节点，并可直接进入 BPM 设计器维护。',
  summary:
    '后装流程覆盖术前评估、后装申请、后装定位、通道勾画、计划设计、医师核准与治疗执行。',
  title: '后装治疗业务流程',
  workflowKey: 'brachytherapy' as const,
};
</script>
<template><WorkflowModelPage v-bind="config" /></template>
