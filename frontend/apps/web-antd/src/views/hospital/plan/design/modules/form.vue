<script lang="ts" setup>
import type { HospitalPlanDesignApi } from '#/api/hospital/plan-design';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createPlanDesign,
  getPlanDesign,
  updatePlanDesign,
} from '#/api/hospital/plan-design';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalPlanDesignApi.PlanDesign>();
const getTitle = computed(() => (formData.value?.id ? '编辑计划设计' : '新增计划设计'));

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    try {
      const data = (await formApi.getValues()) as HospitalPlanDesignApi.PlanDesign;
      await (formData.value?.id ? updatePlanDesign(data) : createPlanDesign(data));
      await modalApi.close();
      emit('success');
      message.success('保存成功');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    const data = modalApi.getData<HospitalPlanDesignApi.PlanDesign>();
    if (!data || !data.id) {
      await formApi.setValues({ versionNo: 'V1', status: 0 });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getPlanDesign(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[860px]">
    <div class="max-h-[72vh] overflow-y-auto pr-2">
      <Form class="mx-4" />
    </div>
  </Modal>
</template>
