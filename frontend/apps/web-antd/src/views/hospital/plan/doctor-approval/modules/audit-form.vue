<script lang="ts" setup>
import type { HospitalPlanDoctorApprovalApi } from '#/api/hospital/plan-doctor-approval';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  approvePlanDoctorApproval,
  getPlanDoctorApproval,
  rejectPlanDoctorApproval,
} from '#/api/hospital/plan-doctor-approval';

import { useAuditFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<HospitalPlanDoctorApprovalApi.PlanReview>();
const actionType = ref<'approve' | 'reject'>('approve');
const getTitle = computed(() => (actionType.value === 'approve' ? '医师核准通过' : '医师核准退回'));

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
      const data = await formApi.getValues<HospitalPlanDoctorApprovalApi.PlanReviewAudit>();
      if (actionType.value === 'approve') {
        await approvePlanDoctorApproval(data);
      } else {
        await rejectPlanDoctorApproval(data);
      }
      await modalApi.close();
      emit('success');
      message.success(actionType.value === 'approve' ? '核准通过成功' : '退回成功');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    const data = modalApi.getData<{ mode: 'approve' | 'reject'; row: HospitalPlanDoctorApprovalApi.PlanReview }>();
    if (!data?.row?.id) {
      return;
    }
    actionType.value = data.mode;
    modalApi.lock();
    try {
      formData.value = await getPlanDoctorApproval(data.row.id);
      await formApi.setValues({
        id: formData.value.id,
        reviewDoctorId: formData.value.reviewDoctorId && formData.value.reviewDoctorId > 0 ? formData.value.reviewDoctorId : undefined,
        reviewComment: formData.value.reviewComment,
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
