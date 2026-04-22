<script lang="ts" setup>
import type { HospitalTreatmentAppointmentApi } from '#/api/hospital/treatment-appointment';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createTreatmentAppointment,
  getTreatmentAppointment,
  updateTreatmentAppointment,
} from '#/api/hospital/treatment-appointment';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalTreatmentAppointmentApi.TreatmentAppointment>();
const getTitle = computed(() => (formData.value?.id ? '编辑治疗预约' : '新增治疗预约'));

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
      const data = (await formApi.getValues()) as HospitalTreatmentAppointmentApi.TreatmentAppointment;
      await (formData.value?.id ? updateTreatmentAppointment(data) : createTreatmentAppointment(data));
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
    const data = modalApi.getData<HospitalTreatmentAppointmentApi.TreatmentAppointment>();
    if (!data || !data.id) {
      await formApi.setValues({ fractionNo: 1, status: 0 });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getTreatmentAppointment(data.id);
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
