<script lang="ts" setup>
import type { HospitalDepartmentApi } from '#/api/hospital/department';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDepartment,
  getDepartment,
  updateDepartment,
} from '#/api/hospital/department';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalDepartmentApi.Department>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑科室' : '新增科室',
);

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
      const data = (await formApi.getValues()) as HospitalDepartmentApi.Department;
      await (formData.value?.id ? updateDepartment(data) : createDepartment(data));
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
    const data = modalApi.getData<HospitalDepartmentApi.Department>();
    if (!data || !data.id) {
      await formApi.setValues({
        parentId: data?.parentId ?? 0,
        type: 1,
        sort: 0,
        status: 0,
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDepartment(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[720px]">
    <Form class="mx-4" />
  </Modal>
</template>
