<script lang="ts" setup>
import { computed, nextTick, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import FcDesigner from '@form-create/antd-designer';
import { Button, message, Spin } from 'ant-design-vue';

import { getCustomForm } from '#/api/hospital/custom-form';
import hospitalLogo from '#/assets/imgs/hospital-logo.svg';
import {
  setConfAndFields,
  useFormCreateDesigner,
} from '#/components/form-create';
import { router } from '#/router';
import {
  normalizeHospitalFormOption,
  normalizeHospitalFormRules,
} from '#/views/hospital/_shared/hospital-form-style';

import Form from '../modules/form.vue';

defineOptions({ name: 'HospitalCustomFormDesigner' });

const props = defineProps<{
  backTo?: string;
  copyId?: number | string;
  focusNodeId?: number | string;
  id?: number | string;
  nodeKey?: string;
  nodeName?: string;
  pageCode?: string;
  processKey?: string;
  processName?: string;
  type: 'copy' | 'create' | 'edit';
  workflowId?: number | string;
  workflowType?: string;
}>();

const route = useRoute();
const loading = ref(false);
const tabs = useTabs();
const formConfig = ref();
const designerRef = ref<InstanceType<typeof FcDesigner>>();
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const designerConfig = ref({
  switchType: [],
  autoActive: true,
  useTemplate: false,
  formOptions: normalizeHospitalFormOption({
    form: { labelWidth: '76px' },
  }),
  fieldReadonly: false,
  hiddenDragMenu: false,
  hiddenDragBtn: false,
  hiddenMenu: [],
  hiddenItem: [],
  hiddenItemConfig: {},
  disabledItemConfig: {},
  showSaveBtn: false,
  showConfig: true,
  showBaseForm: true,
  showControl: true,
  showPropsForm: true,
  showEventForm: true,
  showValidateForm: true,
  showFormConfig: true,
  showInputData: true,
  showDevice: true,
  appendConfigData: [],
});

useFormCreateDesigner(designerRef);

const currentFormId = computed(() =>
  props.type === 'copy' ? props.copyId : props.id,
);
const initialFormData = computed(() => ({
  processKey: props.processKey,
  processName: props.processName,
  nodeKey: props.nodeKey,
  nodeName: props.nodeName,
  pageCode: props.pageCode,
}));
const designerPaperTitle = computed(() => {
  return formConfig.value?.name || initialFormData.value.nodeName || '医院表单';
});
const designerCssVars = computed(() => ({
  '--hospital-form-logo': `url(${hospitalLogo})`,
  '--hospital-form-title': JSON.stringify(designerPaperTitle.value),
}));

async function loadFormConfig(id: number) {
  loading.value = true;
  try {
    const detail = await getCustomForm(id);
    formConfig.value = detail;
    if (designerRef.value) {
      setConfAndFields(designerRef, detail.conf, detail.fields);
      designerRef.value.setOption(
        normalizeHospitalFormOption(designerRef.value.getOption()),
      );
      designerRef.value.setRule(
        normalizeHospitalFormRules(designerRef.value.getRule(), {
          nodeName: detail.nodeName,
          pageCode: detail.pageCode,
        }),
      );
    }
  } finally {
    loading.value = false;
  }
}

async function initializeDesigner() {
  await nextTick();
  const id = currentFormId.value;
  if (props.type === 'copy' && !id) return message.error('复制 ID 不能为空');
  if (id) return loadFormConfig(Number(id));
  if (designerRef.value) {
    designerRef.value.setRule(
      normalizeHospitalFormRules([], {
        nodeName: initialFormData.value.nodeName,
        pageCode: initialFormData.value.pageCode,
      }),
    );
    designerRef.value.setOption(
      normalizeHospitalFormOption(designerRef.value.getOption()),
    );
  }
}

function handleSave() {
  formModalApi
    .setData({
      designer: designerRef.value,
      formConfig: formConfig.value,
      action: props.type,
      initialFormData: initialFormData.value,
    })
    .open();
}
function handleBack() {
  tabs.closeCurrentTab();
  if (props.backTo === 'workflow-designer') {
    router.push({
      name: 'HospitalWorkflowDesigner',
      query: {
        id: props.workflowId,
        type: props.workflowType || 'update',
        nodeId: props.focusNodeId || props.nodeKey,
      },
    });
    return;
  }
  router.push({
    path: '/hospital-flow/form-manage',
    query: {
      processKey: route.query.processKey,
      processName: route.query.processName,
    },
  });
}

onMounted(() => {
  initializeDesigner();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleBack" />
    <Spin :spinning="loading">
      <div class="hospital-form-designer" :style="designerCssVars">
        <FcDesigner ref="designerRef" height="90vh" :config="designerConfig">
          <template #handle>
            <Button size="small" type="primary" @click="handleSave">
              <IconifyIcon icon="mdi:content-save" />保存
            </Button>
          </template>
        </FcDesigner>
      </div>
    </Spin>
  </Page>
</template>

<style scoped>
.hospital-form-designer :deep(._fc-m ._fc-m-con) {
  background: #eef3f8;
  padding: 20px 12px 34px;
}

.hospital-form-designer :deep(._fc-l) {
  width: 208px !important;
  min-width: 208px !important;
  flex: 0 0 208px !important;
}

.hospital-form-designer :deep(._fc-r) {
  width: 252px !important;
  min-width: 252px !important;
  flex: 0 0 252px !important;
}

.hospital-form-designer :deep(._fc-m-drag) {
  width: min(100%, 960px) !important;
  max-width: 960px;
  min-height: 1180px;
  background: #fff;
  border: 1px solid #dfe7f1;
  box-shadow: 0 16px 36px rgb(15 23 42 / 10%);
  padding: 92px 24px 22px;
}

.hospital-form-designer :deep(._fc-m-drag)::before {
  content: '上海XX医院\A RADIOTHERAPY CENTER';
  position: absolute;
  top: 22px;
  left: 24px;
  width: 156px;
  min-height: 40px;
  padding-left: 48px;
  background: var(--hospital-form-logo) left top / 40px 40px no-repeat;
  color: #0f172a;
  font-size: 11px;
  font-weight: 700;
  line-height: 1.12;
  white-space: pre;
}

.hospital-form-designer :deep(._fc-m-drag)::after {
  content: var(--hospital-form-title);
  position: absolute;
  top: 24px;
  left: 196px;
  right: 92px;
  text-align: center;
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.8px;
  line-height: 1.18;
  max-height: 42px;
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.hospital-form-designer :deep(._fc-m-drag > form) {
  position: relative;
  min-height: calc(100% - 28px);
  background: transparent;
  border: 0;
  padding: 0;
}

.hospital-form-designer :deep(._fc-m-drag > form)::before {
  content: '';
  position: absolute;
  top: -14px;
  left: 0;
  right: 0;
  border-top: 1px solid #e6edf5;
}

.hospital-form-designer :deep(._fc-designer ._fc-m-drag ._fd-draggable-drag) {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  align-items: flex-start;
  overflow: auto;
  padding: 2px 0 100px;
  row-gap: 4px;
}

.hospital-form-designer
  :deep(._fc-designer ._fc-m-drag ._fd-draggable-drag > ._fd-drag-item),
.hospital-form-designer
  :deep(._fc-designer ._fc-m-drag ._fd-draggable-drag > div[data-draggable]) {
  min-width: 0;
  box-sizing: border-box;
  margin: 0 !important;
  padding: 0;
}

.hospital-form-designer :deep(._fd-drag-tool) {
  min-width: 0;
  height: auto !important;
  padding: 2px;
  outline: none !important;
  overflow: visible;
}

.hospital-form-designer :deep(._fd-drag-tool ._fd-drag-tool) {
  height: auto !important;
  margin: 0;
}

.hospital-form-designer :deep(._fd-drag-tool + ._fd-drag-tool) {
  margin-top: 0;
}

.hospital-form-designer :deep(._fd-drag-tool:hover) {
  outline: none !important;
  z-index: 0;
}

.hospital-form-designer :deep(._fd-row ._fd-drag-tool:not(.active):hover),
.hospital-form-designer
  :deep(
    ._fc-designer
      ._fc-m-drag
      ._fd-draggable-drag
      > ._fd-drag-item
      > ._fd-drag-tool:not(.active):hover
  ) {
  outline: 1px dashed rgb(46 115 255 / 45%) !important;
  z-index: 1;
}

.hospital-form-designer :deep(._fd-drag-tool.active) {
  outline: 1px dashed #2e73ff !important;
  outline-offset: 0;
  z-index: 1;
}

.hospital-form-designer :deep(._fd-row),
.hospital-form-designer :deep(._fc-m-drag > form > .ant-row) {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  align-items: flex-start;
  row-gap: 4px;
}

.hospital-form-designer :deep(._fd-row .ant-col),
.hospital-form-designer :deep(._fd-row > ._fd-drag-tool),
.hospital-form-designer :deep(._fd-row > ._fd-drag-item),
.hospital-form-designer :deep(._fd-row > ._fd-drag-item > ._fd-drag-tool),
.hospital-form-designer
  :deep(._fc-designer ._fc-m-drag ._fd-draggable-drag > ._fd-drag-item),
.hospital-form-designer :deep(._fc-m-drag > form > .ant-row > ._fd-drag-tool) {
  min-width: 0;
  height: auto !important;
  margin: 0 !important;
  box-sizing: border-box;
}

.hospital-form-designer
  :deep(
    ._fc-designer
      ._fc-m-drag
      ._fd-draggable-drag
      > ._fd-drag-item
      > ._fd-drag-tool
  ) {
  width: 100%;
}

.hospital-form-designer
  :deep(
    ._fc-designer
      ._fc-m-drag
      ._fd-draggable-drag
      > ._fd-drag-item:has(.hospital-grid-col-span-6)
  ),
.hospital-form-designer
  :deep(
    ._fc-m-drag
      > form
      > .ant-row
      > ._fd-drag-tool:has(.hospital-grid-col-span-6)
  ) {
  flex: 0 0 25% !important;
  width: 25% !important;
  max-width: 25% !important;
}

.hospital-form-designer
  :deep(
    ._fc-designer
      ._fc-m-drag
      ._fd-draggable-drag
      > ._fd-drag-item:has(.hospital-grid-col-span-8)
  ),
.hospital-form-designer
  :deep(
    ._fc-m-drag
      > form
      > .ant-row
      > ._fd-drag-tool:has(.hospital-grid-col-span-8)
  ) {
  flex: 0 0 33.333333% !important;
  width: 33.333333% !important;
  max-width: 33.333333% !important;
}

.hospital-form-designer
  :deep(
    ._fc-designer
      ._fc-m-drag
      ._fd-draggable-drag
      > ._fd-drag-item:has(.hospital-grid-col-span-12)
  ),
.hospital-form-designer
  :deep(
    ._fc-m-drag
      > form
      > .ant-row
      > ._fd-drag-tool:has(.hospital-grid-col-span-12)
  ) {
  flex: 0 0 50% !important;
  width: 50% !important;
  max-width: 50% !important;
}

.hospital-form-designer
  :deep(
    ._fc-designer
      ._fc-m-drag
      ._fd-draggable-drag
      > ._fd-drag-item:has(.hospital-grid-col-span-24)
  ),
.hospital-form-designer
  :deep(
    ._fc-m-drag
      > form
      > .ant-row
      > ._fd-drag-tool:has(.hospital-grid-col-span-24)
  ) {
  flex: 0 0 100% !important;
  width: 100% !important;
  max-width: 100% !important;
}

.hospital-form-designer :deep(._fc-m .form-create .fc-form-row) {
  background: transparent;
  border: 0;
  margin-bottom: 0;
  padding: 0;
}

.hospital-form-designer
  :deep(._fc-m .form-create .fc-form-row:has(.ant-divider)) {
  padding-top: 2px;
}

.hospital-form-designer :deep(._fc-m .form-create .fc-form-col) {
  width: auto !important;
  min-width: 0;
  padding-inline: 0;
}

.hospital-form-designer :deep(._fc-m .form-create .hospital-grid-col-span-6) {
  flex: 0 0 25% !important;
  width: 25% !important;
  max-width: 25% !important;
}

.hospital-form-designer :deep(._fc-m .form-create .hospital-grid-col-span-8) {
  flex: 0 0 33.333333% !important;
  width: 33.333333% !important;
  max-width: 33.333333% !important;
}

.hospital-form-designer :deep(._fc-m .form-create .hospital-grid-col-span-12) {
  flex: 0 0 50% !important;
  width: 50% !important;
  max-width: 50% !important;
}

.hospital-form-designer :deep(._fc-m .form-create .hospital-grid-col-span-24) {
  flex: 0 0 100% !important;
  width: 100% !important;
  max-width: 100% !important;
}

.hospital-form-designer :deep(._fc-m .form-create .form-create .ant-form-item),
.hospital-form-designer :deep(._fc-m .form-create .ant-form-item) {
  width: 100%;
  margin-bottom: 3px;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-form-item-row) {
  display: block;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-form-item-label) {
  flex: none;
  width: 100% !important;
  max-width: none;
  padding-bottom: 2px;
  padding-inline-end: 0;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-form-item-control),
.hospital-form-designer :deep(._fc-m .form-create .ant-form-item-control-input),
.hospital-form-designer
  :deep(._fc-m .form-create .ant-form-item-control-input-content),
.hospital-form-designer :deep(._fc-m .form-create .ant-select),
.hospital-form-designer :deep(._fc-m .form-create .ant-picker),
.hospital-form-designer :deep(._fc-m .form-create .ant-input-number),
.hospital-form-designer :deep(._fc-m .form-create .ant-input-affix-wrapper),
.hospital-form-designer :deep(._fc-m .form-create textarea.ant-input) {
  width: 100%;
  min-width: 0;
  max-width: 100%;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-form-item-control) {
  flex: none;
}

.hospital-form-designer
  :deep(._fc-m .form-create .ant-form-item-label > label) {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  color: #5b6575;
  font-size: 11px;
  font-weight: 600;
  height: 18px;
  line-height: 18px;
}

.hospital-form-designer
  :deep(._fc-m .form-create .ant-form-item-control-input) {
  display: flex;
  align-items: center;
  min-height: 28px;
}

.hospital-form-designer
  :deep(._fc-m .form-create .ant-form-item-control-input-content) {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 28px;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-input),
.hospital-form-designer :deep(._fc-m .form-create .ant-input-number),
.hospital-form-designer :deep(._fc-m .form-create .ant-picker),
.hospital-form-designer :deep(._fc-m .form-create .ant-select-selector),
.hospital-form-designer :deep(._fc-m .form-create .ant-input-affix-wrapper) {
  width: 100%;
  min-height: 28px;
  height: 28px;
  border-color: #d8e0ea;
  border-radius: 2px;
  box-shadow: none;
  font-size: 11px;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-input-number),
.hospital-form-designer :deep(._fc-m .form-create .ant-picker),
.hospital-form-designer :deep(._fc-m .form-create .ant-select-selector),
.hospital-form-designer :deep(._fc-m .form-create .ant-input-affix-wrapper) {
  display: flex;
  align-items: center;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-input),
.hospital-form-designer :deep(._fc-m .form-create .ant-input-affix-wrapper),
.hospital-form-designer :deep(._fc-m .form-create .ant-input-number-input),
.hospital-form-designer :deep(._fc-m .form-create .ant-select-selection-item),
.hospital-form-designer
  :deep(._fc-m .form-create .ant-select-selection-placeholder),
.hospital-form-designer :deep(._fc-m .form-create .ant-picker-input > input) {
  height: 28px;
  line-height: 28px;
  font-size: 11px;
}

.hospital-form-designer :deep(._fc-m .form-create .ant-input-number-input) {
  padding-block: 0;
}

.hospital-form-designer :deep(._fc-m .form-create textarea.ant-input) {
  display: block;
  min-height: 72px;
  height: auto;
  line-height: 1.5;
  padding-block: 4px;
}

.hospital-form-designer :deep(.hospital-display-field .ant-input),
.hospital-form-designer :deep(.hospital-display-field .ant-input-number),
.hospital-form-designer :deep(.hospital-display-field .ant-picker),
.hospital-form-designer :deep(.hospital-display-field .ant-select-selector),
.hospital-form-designer
  :deep(.hospital-display-field .ant-input-affix-wrapper) {
  min-height: 26px;
  height: 26px;
  line-height: 26px;
  border: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  color: #475569;
  padding-inline: 0 !important;
  padding-block: 0 !important;
}

.hospital-form-designer :deep(.hospital-display-field .ant-input-number),
.hospital-form-designer :deep(.hospital-display-field .ant-picker),
.hospital-form-designer :deep(.hospital-display-field .ant-select-selector),
.hospital-form-designer
  :deep(.hospital-display-field .ant-input-affix-wrapper) {
  display: flex;
  align-items: center;
}

.hospital-form-designer :deep(.hospital-display-field .ant-input-number-input),
.hospital-form-designer
  :deep(.hospital-display-field .ant-select-selection-item),
.hospital-form-designer
  :deep(.hospital-display-field .ant-select-selection-placeholder),
.hospital-form-designer
  :deep(.hospital-display-field .ant-picker-input > input),
.hospital-form-designer :deep(.hospital-display-field .ant-input) {
  height: 26px;
  line-height: 26px;
  padding: 0 !important;
}

.hospital-form-designer :deep(.hospital-display-field .ant-select-arrow),
.hospital-form-designer :deep(.hospital-display-field .ant-picker-suffix),
.hospital-form-designer
  :deep(.hospital-display-field .ant-input-number-handler-wrap) {
  display: none;
}

.hospital-form-designer :deep(.hospital-display-field .ant-input::placeholder),
.hospital-form-designer
  :deep(.hospital-display-field .ant-select-selection-placeholder),
.hospital-form-designer
  :deep(.hospital-display-field .ant-picker-input > input::placeholder),
.hospital-form-designer
  :deep(.hospital-display-field .ant-input-number-input::placeholder) {
  color: #94a3b8;
}

.hospital-form-designer :deep(.hospital-display-field .ant-select-selector),
.hospital-form-designer :deep(.hospital-display-field .ant-picker),
.hospital-form-designer :deep(.hospital-display-field .ant-input-number) {
  padding-inline: 0 !important;
}

.hospital-form-designer :deep(.hospital-display-field .ant-form-item-row) {
  display: block;
}

.hospital-form-designer :deep(.hospital-display-field .ant-form-item-label) {
  flex: none;
  width: 100% !important;
  max-width: none;
  overflow: visible;
  text-align: left !important;
  white-space: nowrap;
  padding-inline-end: 0;
  padding-bottom: 2px;
}

.hospital-form-designer
  :deep(.hospital-display-field .ant-form-item-label > label) {
  justify-content: flex-start;
  width: 100%;
  height: 18px;
  line-height: 18px;
  white-space: nowrap;
}

.hospital-form-designer :deep(.hospital-display-field .ant-form-item-control) {
  flex: none;
  width: 100%;
}

.hospital-form-designer
  :deep(.hospital-display-field .ant-form-item-control-input),
.hospital-form-designer
  :deep(.hospital-display-field .ant-form-item-control-input-content) {
  align-items: center;
  justify-content: flex-start;
  min-height: 20px;
  width: 100%;
}

.hospital-form-designer :deep(.hospital-display-field .ant-input),
.hospital-form-designer :deep(.hospital-display-field .ant-input-number),
.hospital-form-designer :deep(.hospital-display-field .ant-picker),
.hospital-form-designer :deep(.hospital-display-field .ant-select-selector),
.hospital-form-designer
  :deep(.hospital-display-field .ant-input-affix-wrapper) {
  width: 100% !important;
  min-width: 0;
  height: 20px;
  min-height: 20px;
  line-height: 20px;
}

.hospital-form-designer :deep(.hospital-display-field .ant-input-number-input),
.hospital-form-designer
  :deep(.hospital-display-field .ant-select-selection-item),
.hospital-form-designer
  :deep(.hospital-display-field .ant-select-selection-placeholder),
.hospital-form-designer
  :deep(.hospital-display-field .ant-picker-input > input),
.hospital-form-designer :deep(.hospital-display-field .ant-input) {
  height: 20px;
  line-height: 20px;
}

.hospital-form-designer :deep(._fc-m .form-create .fc-form-title) {
  color: #5b6575;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.4;
}

.hospital-form-designer
  :deep(._fc-m .form-create .ant-divider.ant-divider-horizontal) {
  margin: 8px 0 6px;
  border-top-color: #d8e7f8;
}

.hospital-form-designer
  :deep(._fc-m .form-create .ant-divider .ant-divider-inner-text) {
  position: relative;
  color: #0f172a;
  font-size: 12px;
  font-weight: 700;
  padding-inline: 10px 0;
}

.hospital-form-designer
  :deep(._fc-m .form-create .ant-divider .ant-divider-inner-text)::before {
  content: '';
  position: absolute;
  top: 3px;
  left: 0;
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: #4ea4ff;
}

.hospital-form-designer :deep(._fc-l ._fc-l-label),
.hospital-form-designer :deep(._fc-r .ant-tabs-tab-btn) {
  font-size: 12px;
}

@media (max-width: 1200px) {
  .hospital-form-designer :deep(._fc-m-drag) {
    width: 100% !important;
    min-height: auto;
    padding: 84px 16px 16px;
  }

  .hospital-form-designer :deep(._fc-m-drag)::before {
    top: 20px;
    left: 16px;
    width: 136px;
    min-height: 38px;
    padding-left: 44px;
    background-size: 36px 36px;
    font-size: 10px;
  }

  .hospital-form-designer :deep(._fc-m-drag)::after {
    top: 22px;
    left: 164px;
    right: 16px;
    font-size: 16px;
  }
}
</style>
