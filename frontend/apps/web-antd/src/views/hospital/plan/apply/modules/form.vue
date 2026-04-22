<script lang="ts" setup>
import type { HospitalPlanApplyApi } from '#/api/hospital/plan-apply';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createPlanApply,
  getPlanApply,
  updatePlanApply,
} from '#/api/hospital/plan-apply';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalPlanApplyApi.PlanApply>();
const getTitle = computed(() => (formData.value?.id ? '编辑计划申请' : '新增计划申请'));

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
      const data = (await formApi.getValues()) as HospitalPlanApplyApi.PlanApply;
      await (formData.value?.id ? updatePlanApply(data) : createPlanApply(data));
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
    const data = modalApi.getData<HospitalPlanApplyApi.PlanApply>();
    if (!data || !data.id) {
      await formApi.setValues({ priority: 'NORMAL', status: 0 });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getPlanApply(data.id);
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
