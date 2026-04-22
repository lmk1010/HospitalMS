<script lang="ts" setup>
import { $t } from '@vben/locales';

import { useVbenModal } from '@vben-core/popup-ui';
import { Badge } from '@vben-core/shadcn-ui';

import { useMagicKeys, whenever } from '@vueuse/core';

defineOptions({
  name: 'Help',
});

const keys = useMagicKeys();
whenever(keys['Alt+KeyH']!, () => {
  modalApi.open();
});

const [Modal, modalApi] = useVbenModal({
  draggable: true,
  overlayBlur: 5,
  footer: false,
  onCancel() {
    modalApi.close();
  },
});
</script>
<template>
  <Modal class="w-1/3" :title="$t('ui.widgets.qa')">
    <div class="space-y-4 py-2">
      <div class="rounded-lg border border-gray-200 p-4 dark:border-gray-700">
        <div class="text-base font-medium">院内使用说明</div>
        <div class="text-foreground/70 mt-2 text-sm leading-7">
          当前页面为医院管理平台内部帮助面板，不展示外部站点、开源仓库或推广链接。
        </div>
      </div>
      <div class="rounded-lg border border-gray-200 p-4 dark:border-gray-700">
        <div class="text-sm leading-7">如需协助，请联系本院信息科或系统实施人员。</div>
      </div>
      <div class="flex items-center justify-center pt-2 text-sm italic">
        <span>院内部署版本</span>
        <Badge class="mx-2" variant="secondary">LOCAL</Badge>
      </div>
    </div>
  </Modal>
</template>
