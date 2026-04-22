<script lang="ts" setup>
import hospitalLogo from '#/assets/imgs/hospital-logo.svg';

withDefaults(
  defineProps<{
    code?: string;
    hospitalName?: string;
    hospitalSubtitle?: string;
    metaText?: string;
    title?: string;
  }>(),
  {
    code: '',
    hospitalName: '上海XX医院',
    hospitalSubtitle: 'RADIOTHERAPY CENTER',
    metaText: '',
    title: '医院表单',
  },
);
</script>

<template>
  <div class="hospital-form-sheet">
    <div class="hospital-form-sheet__paper">
      <div class="hospital-form-sheet__header">
        <div class="hospital-form-sheet__brand">
          <img :src="hospitalLogo" alt="hospital logo" />
          <div class="hospital-form-sheet__brand-text">
            <div class="hospital-form-sheet__brand-name">
              {{ hospitalName }}
            </div>
            <div class="hospital-form-sheet__brand-subtitle">
              {{ hospitalSubtitle }}
            </div>
          </div>
        </div>
        <div class="hospital-form-sheet__title-box">
          <div class="hospital-form-sheet__title">{{ title }}</div>
          <div class="hospital-form-sheet__title-caption">放疗业务单据</div>
          <div class="hospital-form-sheet__meta">
            <span v-if="metaText">{{ metaText }}</span>
          </div>
        </div>
        <div class="hospital-form-sheet__code">
          {{ code ? `编号：${code}` : '' }}
        </div>
      </div>
      <div class="hospital-form-sheet__divider"></div>
      <div class="hospital-form-sheet__body">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hospital-form-sheet {
  padding: 12px 0 18px;
}

.hospital-form-sheet__paper {
  width: min(100%, 794px);
  min-height: 1122px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid #dfe7f1;
  box-shadow: 0 16px 36px rgb(15 23 42 / 10%);
  padding: 28px 30px 24px;
}

.hospital-form-sheet__header {
  display: grid;
  grid-template-columns: 210px 1fr 110px;
  align-items: start;
  gap: 8px;
}

.hospital-form-sheet__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  padding-top: 2px;
}

.hospital-form-sheet__brand img {
  width: 44px;
  height: 44px;
  object-fit: contain;
  flex-shrink: 0;
}

.hospital-form-sheet__brand-text {
  min-width: 0;
}

.hospital-form-sheet__brand-name {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.2;
}

.hospital-form-sheet__brand-subtitle {
  color: #94a3b8;
  font-size: 10px;
  letter-spacing: 0.8px;
  line-height: 1.35;
  margin-top: 2px;
}

.hospital-form-sheet__title-box {
  min-width: 0;
  text-align: center;
}

.hospital-form-sheet__title {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  line-height: 1.2;
}

.hospital-form-sheet__title-caption {
  color: #94a3b8;
  font-size: 10px;
  letter-spacing: 1px;
  margin-top: 4px;
}

.hospital-form-sheet__meta {
  color: #94a3b8;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 2px 8px;
  font-size: 10px;
  line-height: 1.4;
  margin-top: 5px;
  min-height: 14px;
}

.hospital-form-sheet__code {
  color: #94a3b8;
  font-size: 10px;
  line-height: 1.5;
  min-height: 44px;
  text-align: right;
  padding-top: 6px;
}

.hospital-form-sheet__divider {
  border-top: 1px solid #e6edf5;
  margin: 16px 0 14px;
}

.hospital-form-sheet__body {
  min-width: 0;
}

.hospital-form-sheet__body :deep(.form-create) {
  background: transparent;
  padding: 0;
}

.hospital-form-sheet__body :deep(.form-create .fc-form-row) {
  background: #f8fafc;
  border: 1px solid #edf2f7;
  margin-bottom: 4px;
  padding: 5px 8px 1px;
}

.hospital-form-sheet__body :deep(.form-create .fc-form-row:has(.ant-divider)) {
  background: transparent;
  border: 0;
  margin-bottom: 2px;
  padding: 0;
}

.hospital-form-sheet__body :deep(.form-create .fc-form-col) {
  width: auto !important;
  padding-inline: 3px;
}

.hospital-form-sheet__body :deep(.form-create .hospital-grid-col-span-6) {
  flex: 0 0 25% !important;
  width: 25% !important;
  max-width: 25% !important;
}

.hospital-form-sheet__body :deep(.form-create .hospital-grid-col-span-8) {
  flex: 0 0 33.333333% !important;
  width: 33.333333% !important;
  max-width: 33.333333% !important;
}

.hospital-form-sheet__body :deep(.form-create .hospital-grid-col-span-12) {
  flex: 0 0 50% !important;
  width: 50% !important;
  max-width: 50% !important;
}

.hospital-form-sheet__body :deep(.form-create .hospital-grid-col-span-24) {
  flex: 0 0 100% !important;
  width: 100% !important;
  max-width: 100% !important;
}

.hospital-form-sheet__body :deep(.form-create .form-create .ant-form-item),
.hospital-form-sheet__body :deep(.form-create .ant-form-item) {
  margin-bottom: 3px;
}

.hospital-form-sheet__body :deep(.form-create .ant-form-item-row) {
  display: block;
}

.hospital-form-sheet__body :deep(.form-create .ant-form-item-label) {
  flex: none;
  width: 100% !important;
  max-width: none;
  padding-bottom: 2px;
  padding-inline-end: 0;
}

.hospital-form-sheet__body :deep(.form-create .ant-form-item-control),
.hospital-form-sheet__body :deep(.form-create .ant-form-item-control-input),
.hospital-form-sheet__body
  :deep(.form-create .ant-form-item-control-input-content),
.hospital-form-sheet__body :deep(.form-create .ant-select),
.hospital-form-sheet__body :deep(.form-create .ant-picker),
.hospital-form-sheet__body :deep(.form-create .ant-input-number),
.hospital-form-sheet__body :deep(.form-create .ant-input-affix-wrapper),
.hospital-form-sheet__body :deep(.form-create textarea.ant-input) {
  width: 100%;
  min-width: 0;
  max-width: 100%;
}

.hospital-form-sheet__body :deep(.form-create .ant-form-item-control) {
  flex: none;
}

.hospital-form-sheet__body :deep(.form-create .ant-form-item-label > label) {
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

.hospital-form-sheet__body :deep(.form-create .ant-form-item-control-input) {
  display: flex;
  align-items: center;
  min-height: 28px;
}

.hospital-form-sheet__body
  :deep(.form-create .ant-form-item-control-input-content) {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 28px;
}

.hospital-form-sheet__body :deep(.form-create .ant-input),
.hospital-form-sheet__body :deep(.form-create .ant-input-number),
.hospital-form-sheet__body :deep(.form-create .ant-picker),
.hospital-form-sheet__body :deep(.form-create .ant-select-selector),
.hospital-form-sheet__body :deep(.form-create .ant-input-affix-wrapper) {
  min-height: 28px;
  height: 28px;
  border-color: #d8e0ea;
  border-radius: 2px;
  box-shadow: none;
  font-size: 11px;
}

.hospital-form-sheet__body :deep(.form-create .ant-input-number),
.hospital-form-sheet__body :deep(.form-create .ant-picker),
.hospital-form-sheet__body :deep(.form-create .ant-select-selector),
.hospital-form-sheet__body :deep(.form-create .ant-input-affix-wrapper) {
  display: flex;
  align-items: center;
}

.hospital-form-sheet__body :deep(.form-create .ant-input),
.hospital-form-sheet__body :deep(.form-create .ant-input-affix-wrapper),
.hospital-form-sheet__body :deep(.form-create .ant-input-number-input),
.hospital-form-sheet__body :deep(.form-create .ant-select-selection-item),
.hospital-form-sheet__body
  :deep(.form-create .ant-select-selection-placeholder),
.hospital-form-sheet__body :deep(.form-create .ant-picker-input > input) {
  height: 28px;
  line-height: 28px;
  font-size: 11px;
}

.hospital-form-sheet__body :deep(.form-create .ant-input-number-input) {
  padding-block: 0;
}

.hospital-form-sheet__body :deep(.form-create textarea.ant-input) {
  display: block;
  min-height: 72px;
  height: auto;
  line-height: 1.5;
  padding-block: 4px;
}

.hospital-form-sheet__body :deep(.hospital-display-field .ant-input),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-input-number),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-picker),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-select-selector),
.hospital-form-sheet__body
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

.hospital-form-sheet__body :deep(.hospital-display-field .ant-input-number),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-picker),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-select-selector),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-input-affix-wrapper) {
  display: flex;
  align-items: center;
}

.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-input-number-input),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-select-selection-item),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-select-selection-placeholder),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-picker-input > input),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-input) {
  height: 26px;
  line-height: 26px;
  padding: 0 !important;
}

.hospital-form-sheet__body :deep(.hospital-display-field .ant-select-arrow),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-picker-suffix),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-input-number-handler-wrap) {
  display: none;
}

.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-input::placeholder),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-select-selection-placeholder),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-picker-input > input::placeholder),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-input-number-input::placeholder) {
  color: #94a3b8;
}

.hospital-form-sheet__body :deep(.hospital-display-field .ant-select-selector),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-picker),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-input-number) {
  padding-inline: 0 !important;
}

.hospital-form-sheet__body :deep(.hospital-display-field .ant-form-item-row) {
  display: block;
}

.hospital-form-sheet__body :deep(.hospital-display-field .ant-form-item-label) {
  flex: none;
  width: 100% !important;
  max-width: none;
  overflow: visible;
  text-align: left !important;
  white-space: nowrap;
  padding-inline-end: 0;
  padding-bottom: 2px;
}

.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-form-item-label > label) {
  justify-content: flex-start;
  width: 100%;
  height: 18px;
  line-height: 18px;
  white-space: nowrap;
}

.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-form-item-control) {
  flex: none;
  width: 100%;
}

.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-form-item-control-input),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-form-item-control-input-content) {
  align-items: center;
  justify-content: flex-start;
  min-height: 20px;
  width: 100%;
}

.hospital-form-sheet__body :deep(.hospital-display-field .ant-input),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-input-number),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-picker),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-select-selector),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-input-affix-wrapper) {
  width: 100% !important;
  min-width: 0;
  height: 20px;
  min-height: 20px;
  line-height: 20px;
}

.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-input-number-input),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-select-selection-item),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-select-selection-placeholder),
.hospital-form-sheet__body
  :deep(.hospital-display-field .ant-picker-input > input),
.hospital-form-sheet__body :deep(.hospital-display-field .ant-input) {
  height: 20px;
  line-height: 20px;
}

.hospital-form-sheet__body :deep(.form-create .ant-input-number-input),
.hospital-form-sheet__body :deep(.form-create .ant-radio-wrapper),
.hospital-form-sheet__body :deep(.form-create .ant-checkbox-wrapper),
.hospital-form-sheet__body
  :deep(.form-create .ant-form-item-control-input-content) {
  font-size: 11px;
}

.hospital-form-sheet__body :deep(.form-create .ant-select-selection-item),
.hospital-form-sheet__body
  :deep(.form-create .ant-select-selection-placeholder),
.hospital-form-sheet__body :deep(.form-create .ant-picker-input > input),
.hospital-form-sheet__body :deep(.form-create .ant-input::placeholder),
.hospital-form-sheet__body :deep(.form-create textarea.ant-input::placeholder) {
  font-size: 11px;
}

.hospital-form-sheet__body :deep(.form-create .ant-input[disabled]),
.hospital-form-sheet__body :deep(.form-create .ant-input-number-disabled),
.hospital-form-sheet__body :deep(.form-create .ant-picker.ant-picker-disabled),
.hospital-form-sheet__body
  :deep(.form-create .ant-select-disabled .ant-select-selector),
.hospital-form-sheet__body :deep(.form-create textarea.ant-input[disabled]) {
  color: #0f172a;
  background: #fff;
}

.hospital-form-sheet__body :deep(.form-create textarea.ant-input) {
  min-height: 56px;
}

.hospital-form-sheet__body :deep(.form-create .fc-form-title) {
  color: #5b6575;
  font-size: 11px;
  font-weight: 600;
  line-height: 1.4;
}

.hospital-form-sheet__body
  :deep(.form-create .ant-divider.ant-divider-horizontal) {
  margin: 8px 0 6px;
  border-top-color: #d8e7f8;
}

.hospital-form-sheet__body
  :deep(.form-create .ant-divider .ant-divider-inner-text) {
  position: relative;
  color: #0f172a;
  font-size: 12px;
  font-weight: 700;
  padding-inline: 10px 0;
}

.hospital-form-sheet__body
  :deep(.form-create .ant-divider .ant-divider-inner-text)::before {
  content: '';
  position: absolute;
  top: 3px;
  left: 0;
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: #4ea4ff;
}

@media (max-width: 900px) {
  .hospital-form-sheet__paper {
    width: 100%;
    min-height: auto;
    padding: 20px 16px 16px;
  }

  .hospital-form-sheet__header {
    grid-template-columns: 1fr;
  }

  .hospital-form-sheet__brand,
  .hospital-form-sheet__title-box,
  .hospital-form-sheet__code {
    justify-content: center;
    text-align: center;
  }
}

@media print {
  .hospital-form-sheet {
    padding: 0;
  }

  .hospital-form-sheet__paper {
    width: 210mm;
    min-height: 297mm;
    border: 0;
    box-shadow: none;
    margin: 0;
    padding: 14mm 12mm 12mm;
  }
}
</style>
