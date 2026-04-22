<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue';

import FormCreate from '@form-create/ant-design-vue';

import { getCustomFormByPage } from '#/api/hospital/custom-form';
import { setConfAndFields2 } from '#/components/form-create';
import HospitalFormSheet from '#/views/hospital/_shared/hospital-form-sheet.vue';
import {
  normalizeHospitalFormOption,
  normalizeHospitalFormRules,
} from '#/views/hospital/_shared/hospital-form-style';

const props = withDefaults(
  defineProps<{
    emptyText?: string;
    formValues?: Record<string, any>;
    nodeKey?: string;
    pageCode: string;
    processKey?: string;
    title?: string;
  }>(),
  {
    emptyText: '当前节点未配置表单',
    formValues: () => ({}),
    processKey: undefined,
    nodeKey: undefined,
    title: '节点表单',
  },
);

const loading = ref(false);
const loaded = ref(false);
const formMeta = ref<any>();
const preview = reactive<{
  option: Record<string, any>;
  rule: any[];
  value: Record<string, any>;
}>({
  option: {},
  rule: [],
  value: {},
});

const hasForm = computed(() => preview.rule.length > 0);
const titleText = computed(() => formMeta.value?.name || props.title);
const nodeText = computed(() => formMeta.value?.nodeName || '');

function makeReadonlyRules(rules: any[] = []) {
  return rules.map((item) => {
    if (!item || typeof item !== 'object') {
      return item;
    }
    const nextItem = {
      ...item,
      props: {
        ...item.props,
      },
    };
    if (!['aDivider', 'fcTitle', 'hidden'].includes(nextItem.type)) {
      nextItem.props.disabled = true;
      nextItem.props.readonly = true;
    }
    if (Array.isArray(nextItem.children)) {
      nextItem.children = makeReadonlyRules(nextItem.children);
    }
    if (Array.isArray(nextItem.control)) {
      nextItem.control = nextItem.control.map((control: any) => ({
        ...control,
        rule: Array.isArray(control.rule)
          ? makeReadonlyRules(control.rule)
          : control.rule,
      }));
    }
    return nextItem;
  });
}

function resetPreview() {
  formMeta.value = undefined;
  preview.option = {};
  preview.rule = [];
  preview.value = {};
}

function applyPreview(form: any) {
  formMeta.value = form;
  setConfAndFields2(
    preview,
    form.conf,
    form.fields || [],
    props.formValues || {},
  );
  preview.rule = normalizeHospitalFormRules(makeReadonlyRules(preview.rule), {
    nodeName: form.nodeName,
    pageCode: form.pageCode,
  });
  preview.option = normalizeHospitalFormOption({
    ...preview.option,
    resetBtn: false,
    submitBtn: false,
  });
  preview.value = {
    ...props.formValues,
  };
}

async function loadForm() {
  if (!props.pageCode) {
    resetPreview();
    loaded.value = true;
    return;
  }
  loading.value = true;
  loaded.value = false;
  resetPreview();
  try {
    const form = await getCustomFormByPage({
      pageCode: props.pageCode,
      processKey: props.processKey || undefined,
      nodeKey: props.nodeKey || undefined,
    });
    if (form?.id) {
      applyPreview(form);
    }
  } catch {
    resetPreview();
  } finally {
    loaded.value = true;
    loading.value = false;
  }
}

watch(
  () => [props.pageCode, props.processKey, props.nodeKey].join('|'),
  () => {
    void loadForm();
  },
  { immediate: true },
);

watch(
  () => props.formValues,
  (value) => {
    preview.value = {
      ...value,
    };
  },
  { deep: true },
);
</script>

<template>
  <HospitalFormSheet
    :code="formMeta?.pageCode || ''"
    :meta-text="nodeText ? `节点：${nodeText}` : ''"
    :title="titleText"
  >
    <a-spin :spinning="loading">
      <div v-if="hasForm" class="node-custom-form__body">
        <FormCreate
          v-model:value="preview.value"
          :option="preview.option"
          :rule="preview.rule"
        />
      </div>
      <div v-else-if="loaded" class="node-custom-form__empty">
        {{ emptyText }}
      </div>
    </a-spin>
  </HospitalFormSheet>
</template>

<style scoped>
.node-custom-form__body {
  min-width: 0;
}

.node-custom-form__empty {
  padding: 48px 0 32px;
  color: #94a3b8;
  font-size: 12px;
  text-align: center;
}
</style>
