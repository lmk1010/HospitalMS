<script lang="ts" setup>
import type { HospitalDoctorApi } from '#/api/hospital/doctor';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createDoctor, getDoctor, updateDoctor } from '#/api/hospital/doctor';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalDoctorApi.Doctor>();
const getTitle = computed(() => (formData.value?.id ? '编辑医生' : '新增医生'));

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
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
      const data = (await formApi.getValues()) as HospitalDoctorApi.Doctor;
      await (formData.value?.id ? updateDoctor(data) : createDoctor(data));
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
    const data = modalApi.getData<HospitalDoctorApi.Doctor>();
    if (!data || !data.id) {
      await formApi.setValues({
        gender: 1,
        sort: 0,
        status: 0,
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDoctor(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[760px]">
    <Form class="mx-4" />
  </Modal>
</template>
