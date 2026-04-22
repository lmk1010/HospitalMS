<script lang="ts" setup>
import type { HospitalPlanVerifyApi } from '#/api/hospital/plan-verify';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  getPlanVerify,
  rejectPlanVerify,
  verifyPlan,
} from '#/api/hospital/plan-verify';

import { useAuditFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalPlanVerifyApi.PlanVerify>();
const actionType = ref<'approve' | 'reject'>('approve');
const getTitle = computed(() => (actionType.value === 'approve' ? '计划验证通过' : '计划验证不通过'));

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: useAuditFormSchema(),
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
      const data = await formApi.getValues<HospitalPlanVerifyApi.PlanVerifyAudit>();
      if (actionType.value === 'approve') {
        await verifyPlan(data);
      } else {
        await rejectPlanVerify(data);
      }
      await modalApi.close();
      emit('success');
      message.success(actionType.value === 'approve' ? '验证通过成功' : '已标记为不通过');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    const data = modalApi.getData<{ mode: 'approve' | 'reject'; row: HospitalPlanVerifyApi.PlanVerify }>();
    if (!data?.row?.id) {
      return;
    }
    actionType.value = data.mode;
    modalApi.lock();
    try {
      formData.value = await getPlanVerify(data.row.id);
      await formApi.setValues({
        id: formData.value.id,
        verifyUserId: formData.value.verifyUserId && formData.value.verifyUserId > 0 ? formData.value.verifyUserId : undefined,
        verifyDeviceName: formData.value.verifyDeviceName,
        reportUrl: formData.value.reportUrl,
        remark: formData.value.remark,
      });
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[760px]">
    <div class="max-h-[72vh] overflow-y-auto pr-2">
      <Form class="mx-4" />
    </div>
  </Modal>
</template>
