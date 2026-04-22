<script lang="ts" setup>
import type { HospitalPatientApi } from '#/api/hospital/patient';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createPatient, getPatient, updatePatient } from '#/api/hospital/patient';

import { usePatientFormSchema } from '../../shared';

const emit = defineEmits(['success']);
const formData = ref<HospitalPatientApi.Patient>();
const getTitle = computed(() => (formData.value?.id ? '编辑患者' : '新增患者'));

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: usePatientFormSchema(),
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
      const data = (await formApi.getValues()) as HospitalPatientApi.Patient;
      await (formData.value?.id ? updatePatient(data) : createPatient(data));
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
    const data = modalApi.getData<HospitalPatientApi.Patient>();
    if (!data || !data.id) {
      await formApi.setValues({
        gender: 1,
        idType: '身份证',
        status: 0,
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getPatient(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[960px]">
    <div class="max-h-[72vh] overflow-y-auto pr-2">
      <Form class="mx-4" />
    </div>
  </Modal>
</template>
