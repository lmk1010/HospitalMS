<script lang="ts" setup>
import type { HospitalCtAppointmentApi } from '#/api/hospital/ct-appointment';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createCtAppointment,
  getCtAppointment,
  updateCtAppointment,
} from '#/api/hospital/ct-appointment';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalCtAppointmentApi.CtAppointment>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑CT预约' : '新增CT预约',
);

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
      const data =
        (await formApi.getValues()) as HospitalCtAppointmentApi.CtAppointment;
      await (formData.value?.id
        ? updateCtAppointment(data)
        : createCtAppointment(data));
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
    const data = modalApi.getData<HospitalCtAppointmentApi.CtAppointment>();
    if (!data || !data.id) {
      formData.value = data;
      await formApi.setValues({
        appointmentSlot: '上午',
        priority: 'NORMAL',
        status: 0,
        ...data,
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getCtAppointment(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[820px]">
    <div class="max-h-[72vh] overflow-y-auto pr-2">
      <Form class="mx-4" />
    </div>
  </Modal>
</template>
