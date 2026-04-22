<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import FormCreate from '@form-create/ant-design-vue';

import { getCustomForm } from '#/api/hospital/custom-form';
import { setConfAndFields2 } from '#/components/form-create';
import HospitalFormSheet from '#/views/hospital/_shared/hospital-form-sheet.vue';
import {
  normalizeHospitalFormOption,
  normalizeHospitalFormRules,
} from '#/views/hospital/_shared/hospital-form-style';

const formConfig = ref<any>({});

const [Modal, modalApi] = useVbenModal({
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    const data = modalApi.getData();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formConfig.value = await getCustomForm(data.id);
      setConfAndFields2(
        formConfig.value,
        formConfig.value.conf,
        formConfig.value.fields,
      );
      formConfig.value.rule = normalizeHospitalFormRules(
        formConfig.value.rule,
        {
          nodeName: formConfig.value.nodeName,
          pageCode: formConfig.value.pageCode,
        },
      );
      formConfig.value.option = normalizeHospitalFormOption({
        ...formConfig.value.option,
        resetBtn: false,
        submitBtn: false,
      });
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-[1100px] max-w-[94vw]" title="医院表单预览">
    <HospitalFormSheet
      :code="formConfig.pageCode || ''"
      :meta-text="formConfig.nodeName ? `节点：${formConfig.nodeName}` : ''"
      :title="formConfig.name || '医院表单'"
    >
      <FormCreate :option="formConfig.option" :rule="formConfig.rule" />
    </HospitalFormSheet>
  </Modal>
</template>
