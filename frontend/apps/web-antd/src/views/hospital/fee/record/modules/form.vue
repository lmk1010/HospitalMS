<script lang="ts" setup>
import type { HospitalFeeRecordApi } from '#/api/hospital/fee-record';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createFeeRecord, getFeeRecord, updateFeeRecord } from '#/api/hospital/fee-record';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalFeeRecordApi.FeeRecord>();
const getTitle = computed(() => (formData.value?.id ? '编辑费用登记' : '新增费用登记'));

const [Form, formApi] = useVbenForm({
  commonConfig: { componentProps: { class: 'w-full' }, formItemClass: 'col-span-2', labelWidth: 90 },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = (await formApi.getValues()) as HospitalFeeRecordApi.FeeRecord;
      await (formData.value?.id ? updateFeeRecord(data) : createFeeRecord(data));
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
    const data = modalApi.getData<HospitalFeeRecordApi.FeeRecord>();
    if (!data || !data.id) {
      formData.value = undefined;
      await formApi.setValues({
        quantity: 1,
        status: 0,
        sourceType: 'OTHER',
        ...data,
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getFeeRecord(data.id);
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
