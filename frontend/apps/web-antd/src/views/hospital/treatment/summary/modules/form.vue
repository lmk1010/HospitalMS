<script lang="ts" setup>
import type { HospitalTreatmentSummaryApi } from '#/api/hospital/treatment-summary';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createTreatmentSummary,
  getTreatmentSummary,
  updateTreatmentSummary,
} from '#/api/hospital/treatment-summary';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalTreatmentSummaryApi.TreatmentSummary>();
const getTitle = computed(() => (formData.value?.id ? '编辑治疗小结' : '新增治疗小结'));

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
      const data = (await formApi.getValues()) as HospitalTreatmentSummaryApi.TreatmentSummary;
      await (formData.value?.id ? updateTreatmentSummary(data) : createTreatmentSummary(data));
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
    const data = modalApi.getData<HospitalTreatmentSummaryApi.TreatmentSummary>();
    if (!data || !data.id) {
      await formApi.setValues({ status: 0 });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getTreatmentSummary(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[900px]">
    <div class="max-h-[72vh] overflow-y-auto pr-2">
      <Form class="mx-4" />
    </div>
  </Modal>
</template>
