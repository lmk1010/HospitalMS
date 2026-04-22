<script lang="ts" setup>
import type { FcDesigner } from '@form-create/antd-designer';

import type { HospitalCustomFormApi } from '#/api/hospital/custom-form';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createCustomForm, updateCustomForm } from '#/api/hospital/custom-form';
import { encodeConf, encodeFields } from '#/components/form-create';

import { resolvePagePathByCode, useFormSchema } from '../data';

const emit = defineEmits(['success']);
const designerComponent = ref<InstanceType<typeof FcDesigner>>();
const formData = ref<HospitalCustomFormApi.CustomForm>();
const editorAction = ref<string>();
const getTitle = computed(() => {
  if (!formData.value?.id) {
    return '新建医院表单';
  }
  return editorAction.value === 'copy' ? '复制医院表单' : '编辑医院表单';
});
const [Form, formApi] = useVbenForm({
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
      const data =
        (await formApi.getValues()) as HospitalCustomFormApi.CustomForm;
      data.conf = encodeConf(designerComponent);
      data.fields = encodeFields(designerComponent);
      data.pagePath =
        data.pagePath || resolvePagePathByCode(data.pageCode) || '';
      await (formData.value?.id && editorAction.value !== 'copy'
        ? updateCustomForm(data)
        : createCustomForm(data));
      await modalApi.close();
      emit('success');
      message.success('表单保存成功');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      designerComponent.value = undefined;
      return;
    }
    const data = modalApi.getData<any>();
    if (!data) return;
    modalApi.lock();
    designerComponent.value = data.designer;
    formData.value = data.formConfig;
    editorAction.value = data.action;
    if (editorAction.value === 'copy' && formData.value)
      formData.value = {
        ...formData.value,
        name: `${formData.value.name}_copy`,
        code: `${formData.value.code}_copy`,
        id: undefined,
      };
    try {
      await formApi.setValues(formData.value || data.initialFormData || {});
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <Form class="mx-4" />
  </Modal>
</template>
