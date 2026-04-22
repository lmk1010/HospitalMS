<script setup lang="ts">
import type { SimpleFlowNode } from '../consts';

import { nextTick, onBeforeUnmount, onMounted, provide, ref } from 'vue';

import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { downloadFileFromBlob, isString } from '@vben/utils';

import { Button, ButtonGroup, Modal, Row } from 'ant-design-vue';

import { NODE_DEFAULT_TEXT } from '../consts';
import { useWatchNode } from '../helpers';
import ProcessNodeTree from './process-node-tree.vue';

defineOptions({
  name: 'SimpleProcessModel',
});

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
  readonly: {
    type: Boolean,
    required: false,
    default: true,
  },
  defaultScale: {
    type: Number,
    required: false,
    default: 100,
  },
});

const emits = defineEmits<{
  save: [node: SimpleFlowNode | undefined];
}>();

const processNodeTree = useWatchNode(props);
const containerRef = ref<HTMLElement>();

provide('readonly', props.readonly);

// TODO 可优化：拖拽有点卡顿
/** 拖拽、放大缩小等操作 */
const scaleValue = ref(props.defaultScale);
const MAX_SCALE_VALUE = 200;
const MIN_SCALE_VALUE = 50;
const isDragging = ref(false);
const startX = ref(0);
const startY = ref(0);
const currentX = ref(0);
const currentY = ref(0);
const initialX = ref(0);
const initialY = ref(0);
const PAN_STEP = 1;
const DRAG_BLOCK_SELECTOR =
  '.node-box, .node-content, .node-title, .node-toolbar, .node-handler-wrapper, .handler-item-wrapper, .branch-node-title-container, .branch-node-content, button, input, textarea, .ant-input, .ant-input-number, .ant-select, .ant-radio-group';

function setGrabCursor() {
  document.body.style.cursor = 'grab';
}

function resetCursor() {
  document.body.style.cursor = 'default';
}

function startDrag(e: MouseEvent) {
  if (e.button !== 0 && e.button !== 1) {
    return;
  }
  const target = e.target as HTMLElement | null;
  if (e.button === 0 && target?.closest(DRAG_BLOCK_SELECTOR)) {
    return;
  }
  isDragging.value = true;
  startX.value = e.clientX - currentX.value;
  startY.value = e.clientY - currentY.value;
  setGrabCursor();
}

function onDrag(e: MouseEvent) {
  if (!isDragging.value) return;
  e.preventDefault(); // 禁用文本选择

  // 使用 requestAnimationFrame 优化性能
  requestAnimationFrame(() => {
    currentX.value = e.clientX - startX.value;
    currentY.value = e.clientY - startY.value;
  });
}

function stopDrag() {
  isDragging.value = false;
  resetCursor(); // 重置光标
}

function handleWheel(e: WheelEvent) {
  if (e.ctrlKey || e.metaKey) {
    if (e.deltaY < 0) {
      zoomIn();
    } else {
      zoomOut();
    }
    return;
  }
  const horizontalDelta = Math.abs(e.deltaX) > 0 ? e.deltaX : 0;
  currentX.value -= horizontalDelta * PAN_STEP;
  currentY.value -= e.deltaY * PAN_STEP;
}

function zoomIn() {
  if (scaleValue.value === MAX_SCALE_VALUE) {
    return;
  }
  scaleValue.value += 10;
}

function zoomOut() {
  if (scaleValue.value === MIN_SCALE_VALUE) {
    return;
  }
  scaleValue.value -= 10;
}

function processReZoom() {
  scaleValue.value = props.defaultScale;
  nextTick(() => syncInitialPosition());
}

function resetPosition() {
  currentX.value = initialX.value;
  currentY.value = initialY.value;
}

function syncInitialPosition() {
  const container = containerRef.value;
  if (!container) return;
  const content = container.querySelector(
    '.simple-process-content',
  ) as HTMLElement | null;
  if (!content) return;
  const scaledContentWidth = content.offsetWidth * (scaleValue.value / 100);
  initialX.value =
    scaledContentWidth > container.clientWidth
      ? (container.clientWidth - scaledContentWidth) / 2
      : 0;
  initialY.value = 0;
  currentX.value = initialX.value;
  currentY.value = initialY.value;
}

/** 校验节点设置 */
const errorDialogVisible = ref(false);
let errorNodes: SimpleFlowNode[] = [];

function validateNode(
  node: SimpleFlowNode | undefined,
  errorNodes: SimpleFlowNode[],
) {
  if (node) {
    const { type, showText, conditionNodes } = node;
    if (type === BpmNodeTypeEnum.END_EVENT_NODE) {
      return;
    }
    if (type === BpmNodeTypeEnum.START_USER_NODE) {
      // 发起人节点暂时不用校验，直接校验孩子节点
      validateNode(node.childNode, errorNodes);
    }

    if (
      type === BpmNodeTypeEnum.USER_TASK_NODE ||
      type === BpmNodeTypeEnum.COPY_TASK_NODE ||
      type === BpmNodeTypeEnum.CONDITION_NODE
    ) {
      if (!showText) {
        errorNodes.push(node);
      }
      validateNode(node.childNode, errorNodes);
    }

    if (
      type === BpmNodeTypeEnum.CONDITION_BRANCH_NODE ||
      type === BpmNodeTypeEnum.PARALLEL_BRANCH_NODE ||
      type === BpmNodeTypeEnum.INCLUSIVE_BRANCH_NODE
    ) {
      // 分支节点
      // 1. 先校验各个分支
      conditionNodes?.forEach((item) => {
        validateNode(item, errorNodes);
      });
      // 2. 校验孩子节点
      validateNode(node.childNode, errorNodes);
    }
  }
}

/** 获取当前流程数据 */
async function getCurrentFlowData() {
  try {
    errorNodes = [];
    validateNode(processNodeTree.value, errorNodes);
    if (errorNodes.length > 0) {
      errorDialogVisible.value = true;
      return undefined;
    }
    return processNodeTree.value;
  } catch (error) {
    console.error('获取流程数据失败:', error);
    return undefined;
  }
}

defineExpose({
  getCurrentFlowData,
});

/** 导出 JSON */
function exportJson() {
  downloadFileFromBlob({
    fileName: 'model.json',
    source: new Blob([JSON.stringify(processNodeTree.value)]),
  });
}

/** 导入 JSON */
const refFile = ref();
function importJson() {
  refFile.value.click();
}
function importLocalFile() {
  const file = refFile.value.files[0];
  file.text().then((result: any) => {
    if (isString(result)) {
      processNodeTree.value = JSON.parse(result);
      emits('save', processNodeTree.value);
    }
  });
}

// 在组件初始化时记录初始位置
onMounted(() => {
  nextTick(() => {
    syncInitialPosition();
    window.addEventListener('resize', syncInitialPosition);
  });
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncInitialPosition);
  resetCursor();
});
</script>
<template>
  <div
    ref="containerRef"
    class="simple-process-model-container"
    @wheel.prevent="handleWheel"
  >
    <div class="simple-process-toolbar">
      <Row type="flex" justify="end">
        <ButtonGroup key="scale-control">
          <Button v-if="!readonly" @click="exportJson">
            <IconifyIcon icon="lucide:download" /> 导出
          </Button>
          <Button v-if="!readonly" @click="importJson">
            <IconifyIcon icon="lucide:upload" />导入
          </Button>
          <!-- 用于打开本地文件-->
          <input
            v-if="!readonly"
            type="file"
            id="files"
            ref="refFile"
            class="hidden"
            accept=".json"
            @change="importLocalFile"
          />
          <Button @click="processReZoom()">
            <IconifyIcon icon="lucide:table-columns-split" />
          </Button>
          <Button :plain="true" @click="zoomOut()">
            <IconifyIcon icon="lucide:zoom-out" />
          </Button>
          <Button class="w-20"> {{ scaleValue }}% </Button>
          <Button :plain="true" @click="zoomIn()">
            <IconifyIcon icon="lucide:zoom-in" />
          </Button>
          <Button @click="resetPosition">重置</Button>
        </ButtonGroup>
      </Row>
    </div>
    <div
      class="simple-process-model"
      :class="{ 'is-dragging': isDragging }"
      :style="`transform: translate(${currentX}px, ${currentY}px) scale(${scaleValue / 100});`"
      @mousedown="startDrag"
      @mousemove="onDrag"
      @mouseup="stopDrag"
      @mouseleave="stopDrag"
      @mouseenter="setGrabCursor"
    >
      <div v-if="processNodeTree" class="simple-process-content">
        <ProcessNodeTree v-model:flow-node="processNodeTree" />
      </div>
    </div>
  </div>

  <Modal
    v-model:open="errorDialogVisible"
    title="保存失败"
    width="400"
    :fullscreen="false"
  >
    <div class="mb-2">以下节点内容不完善，请修改后保存</div>
    <div
      class="line-height-normal mb-3 rounded p-2"
      v-for="(item, index) in errorNodes"
      :key="index"
    >
      {{ item.name }} : {{ NODE_DEFAULT_TEXT.get(item.type) }}
    </div>
    <template #footer>
      <Button type="primary" @click="errorDialogVisible = false">知道了</Button>
    </template>
  </Modal>
</template>

<style scoped>
.simple-process-model-container {
  position: relative;
  min-height: 100%;
  overflow: hidden;
  background: transparent;
}

.simple-process-toolbar {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 20;
}

.simple-process-toolbar :deep(.ant-btn-group) {
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgb(15 23 42 / 10%);
}

.simple-process-model {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-width: max-content;
  min-height: 100%;
  padding: 46px 24px 24px;
  background: transparent;
  transform-origin: top center;
  user-select: none;
  cursor: grab;
}

.simple-process-content {
  width: fit-content;
  min-width: fit-content;
}

.simple-process-model.is-dragging {
  cursor: grabbing;
}
</style>
