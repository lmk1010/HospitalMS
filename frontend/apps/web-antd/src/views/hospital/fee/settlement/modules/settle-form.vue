<script lang="ts" setup>
import type { HospitalFeeSettlementApi } from '#/api/hospital/fee-settlement';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { settleFee } from '#/api/hospital/fee-settlement';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);

const modalData = ref<HospitalFeeSettlementApi.FeeSettlement>({});

const summaryItems = computed(() => [
  { label: '患者', value: modalData.value.patientName || '--' },
  { label: '待结算项目', value: `${modalData.value.recordIds?.length || 0} 项` },
  { label: '应收合计', value: `${Number(modalData.value.totalAmount || 0).toFixed(2)} 元` },
  { label: '待收金额', value: `${Number(modalData.value.payableAmount || modalData.value.totalAmount || 0).toFixed(2)} 元` },
]);

const [Form, formApi] = useVbenForm({
  commonConfig: { componentProps: { class: 'w-full' }, formItemClass: 'col-span-2', labelWidth: 90 },
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
      const data = (await formApi.getValues()) as HospitalFeeSettlementApi.FeeSettlement;
      await settleFee({
        ...data,
        recordIds: modalData.value.recordIds,
      });
      await modalApi.close();
      emit('success');
      message.success('结算成功');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) return;
    const data = modalApi.getData<HospitalFeeSettlementApi.FeeSettlement>() || {};
    modalData.value = data;
    await formApi.setValues({
      discountAmount: 0,
      paidAmount: data.payableAmount ?? data.totalAmount,
      paymentType: 'SELF_PAY',
      status: 0,
      ...data,
    });
  },
});
</script>

<template>
  <Modal title="收费确认" class="w-[820px]">
    <div class="max-h-[72vh] overflow-y-auto pr-2">
      <div class="settle-summary mx-4 mb-4">
        <div
          v-for="item in summaryItems"
          :key="item.label"
          class="settle-summary__item"
        >
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
      <Form class="mx-4" />
    </div>
  </Modal>
</template>

<style scoped>
.settle-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.settle-summary__item {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f8fafc;
}

.settle-summary__item span {
  display: block;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.settle-summary__item strong {
  display: block;
  margin-top: 6px;
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.4;
}

@media (max-width: 900px) {
  .settle-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
