<script lang="ts" setup>
import type { HospitalContourTaskApi } from '#/api/hospital/contour-task';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createContourTask,
  getContourTask,
  updateContourTask,
} from '#/api/hospital/contour-task';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalContourTaskApi.ContourTask>();
const getTitle = computed(() => (formData.value?.id ? '编辑靶区勾画' : '新增靶区勾画'));

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
      const data = (await formApi.getValues()) as HospitalContourTaskApi.ContourTask;
      await (formData.value?.id ? updateContourTask(data) : createContourTask(data));
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
    const data = modalApi.getData<HospitalContourTaskApi.ContourTask>();
    if (!data || !data.id) {
      await formApi.setValues({ versionNo: 'V1', status: 0 });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getContourTask(data.id);
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
