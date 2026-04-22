<script setup lang="ts">
import type { Ref } from 'vue';

import type { SimpleFlowNode } from '../../consts';

import { computed, inject, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useTaskStatusClass, useWatchNode } from '../../helpers';
import ProcessInstanceModal from './modules/process-instance-modal.vue';

defineOptions({ name: 'EndEventNode' });
const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    default: () => null,
  },
});
// 监控节点变化
const currentNode = useWatchNode(props);
// 是否只读
const readonly = inject<Boolean>('readonly');
const processInstance = inject<Ref<any>>('processInstance', ref({}));
const selectedNode = inject<Ref<SimpleFlowNode | undefined>>(
  'selectedNode',
  ref(),
);
const designerConfigMode = inject<Ref<string>>(
  'designerConfigMode',
  ref('drawer'),
);
const isSidebarMode = computed(() => designerConfigMode.value === 'sidebar');
const isSelected = computed(
  () => selectedNode.value?.id === currentNode.value?.id,
);

const [Modal, modalApi] = useVbenModal({
  connectedComponent: ProcessInstanceModal,
  destroyOnClose: true,
});

function nodeClick() {
  if (!readonly && isSidebarMode.value) {
    selectedNode.value = currentNode.value;
    return;
  }
  if (readonly && processInstance && processInstance.value) {
    const processInstanceInfo = [
      {
        startUser: processInstance.value.startUser,
        createTime: processInstance.value.startTime,
        endTime: processInstance.value.endTime,
        status: processInstance.value.status,
        durationInMillis: processInstance.value.durationInMillis,
      },
    ];
    modalApi
      .setData(processInstanceInfo)
      .setState({ title: '流程信息' })
      .open();
  }
}
</script>
<template>
  <div class="end-node-wrapper">
    <div
      class="end-node-box cursor-pointer"
      :class="[
        `${useTaskStatusClass(currentNode?.activityStatus)}`,
        { 'node-selected': isSelected },
      ]"
      @click="nodeClick"
    >
      <span class="node-fixed-name" title="结束">结束</span>
    </div>
  </div>
  <!-- 流程信息弹窗 -->
  <Modal />
</template>
