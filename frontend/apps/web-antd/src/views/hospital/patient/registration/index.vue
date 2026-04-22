<script lang="ts" setup>
import type { FormInstance } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';

import type { HospitalDoctorApi } from '#/api/hospital/doctor';
import type { HospitalPatientApi } from '#/api/hospital/patient';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  Button,
  DatePicker,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Select,
  Table,
  Tag,
  Textarea,
} from 'ant-design-vue';

import { getSimpleDoctorList } from '#/api/hospital/doctor';
import {
  createPatient,
  getPatient,
  getPatientPage,
  updatePatient,
} from '#/api/hospital/patient';
import { uploadFile } from '#/api/infra/file';

const router = useRouter();
const formRef = ref<FormInstance>();
const formLoading = ref(false);
const latestPatientNo = ref('');
const doctorList = ref<HospitalDoctorApi.Doctor[]>([]);
const photoUploading = ref(false);
const captureInputRef = ref<HTMLInputElement>();
const localInputRef = ref<HTMLInputElement>();
const pickerOpen = ref(false);
const pickerLoading = ref(false);
const pickerMode = ref<'admit' | 'fetch-his'>('fetch-his');
const pickerRows = ref<HospitalPatientApi.Patient[]>([]);
const pickerPagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0,
});
const pickerQuery = reactive({
  hospitalizationNo: '',
  idNo: '',
  name: '',
  patientNo: '',
  patientPhone: '',
  patientType: undefined as string | undefined,
  socialSecurityNo: '',
});

const idTypeOptions = [
  { label: '身份证', value: '身份证' },
  { label: '护照', value: '护照' },
  { label: '军官证', value: '军官证' },
  { label: '港澳台证件', value: '港澳台证件' },
];
const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 },
];
const maritalStatusOptions = [
  { label: '未婚', value: '未婚' },
  { label: '已婚', value: '已婚' },
  { label: '离异', value: '离异' },
  { label: '丧偶', value: '丧偶' },
];
const relationOptions = [
  { label: '本人', value: '本人' },
  { label: '配偶', value: '配偶' },
  { label: '子女', value: '子女' },
  { label: '父母', value: '父母' },
  { label: '其他', value: '其他' },
];
const patientTypeOptions = [
  { label: '门诊', value: '门诊' },
  { label: '住院', value: '住院' },
  { label: '会诊', value: '会诊' },
];
const paymentTypeOptions = [
  { label: '医保', value: 'INSURANCE' },
  { label: '自费', value: 'SELF_PAY' },
  { label: '商保', value: 'COMMERCIAL' },
  { label: '公费', value: 'PUBLIC_PAY' },
];
const campusOptions = [
  { label: '本部院区', value: '本部院区' },
  { label: '南区院区', value: '南区院区' },
  { label: '北区院区', value: '北区院区' },
];
const quickActions = [
  { key: 'fetch-his', label: '调取HIS' },
  { key: 'read-social-card', label: '读社保卡' },
  { key: 'read-id-card', label: '读身份证' },
  { key: 'admit', label: '从入院登记' },
] as const;
const pickerColumns = [
  { dataIndex: 'patientNo', key: 'patientNo', title: '档案号', width: 150 },
  { dataIndex: 'name', key: 'name', title: '患者姓名', width: 120 },
  {
    customRender: ({ text }: { text?: number }) => {
      if (text === 1) return '男';
      if (text === 2) return '女';
      return '-';
    },
    dataIndex: 'gender',
    key: 'gender',
    title: '性别',
    width: 80,
  },
  {
    dataIndex: 'patientPhone',
    key: 'patientPhone',
    title: '联系电话',
    width: 140,
  },
  {
    dataIndex: 'hospitalizationNo',
    key: 'hospitalizationNo',
    title: '住院号',
    width: 140,
  },
  {
    dataIndex: 'patientType',
    key: 'patientType',
    title: '患者类型',
    width: 100,
  },
  { dataIndex: 'campus', key: 'campus', title: '院区', width: 120 },
  { dataIndex: 'wardName', key: 'wardName', title: '病区', width: 120 },
  { key: 'action', title: '操作', width: 90 },
];

function createDefaultFormModel(): HospitalPatientApi.Patient {
  return {
    campus: '本部院区',
    gender: 1,
    idType: '身份证',
    name: '',
    patientType: '住院',
    paymentType: 'INSURANCE',
    photoUrl: '',
    status: 0,
  };
}

const formModel = reactive<HospitalPatientApi.Patient>(
  createDefaultFormModel(),
);

const doctorOptions = computed(() =>
  doctorList.value.map((item) => ({
    label: item.name,
    value: item.id!,
  })),
);

const pickerTitle = computed(() => {
  if (pickerMode.value === 'admit') {
    return '从入院登记导入';
  }
  return '调取 HIS 档案';
});

const rules: Record<string, Rule[]> = {
  campus: [{ message: '请选择院区', required: true, trigger: 'change' }],
  contactPhone: [
    { message: '请输入联系人电话', required: true, trigger: 'blur' },
  ],
  gender: [{ message: '请选择性别', required: true, trigger: 'change' }],
  idNo: [{ message: '请输入证件号', required: true, trigger: 'blur' }],
  idType: [{ message: '请选择证件类型', required: true, trigger: 'change' }],
  managerDoctorId: [
    { message: '请选择主管医生', required: true, trigger: 'change' },
  ],
  name: [{ message: '请输入患者姓名', required: true, trigger: 'blur' }],
  patientPhone: [
    { message: '请输入患者电话', required: true, trigger: 'blur' },
  ],
  patientType: [
    { message: '请选择患者类型', required: true, trigger: 'change' },
  ],
};

function handleBack() {
  void router.push('/hospital-flow/hospital-patient/list');
}

function calculateAge(birthday?: string) {
  if (!birthday) return undefined;
  const date = new Date(birthday);
  if (Number.isNaN(date.getTime())) return undefined;
  const now = new Date();
  let age = now.getFullYear() - date.getFullYear();
  const monthOffset = now.getMonth() - date.getMonth();
  if (
    monthOffset < 0 ||
    (monthOffset === 0 && now.getDate() < date.getDate())
  ) {
    age -= 1;
  }
  return age < 0 ? undefined : age;
}

function handleBirthdayChange(_value: unknown, dateString: string | string[]) {
  const birthday = Array.isArray(dateString) ? dateString[0] : dateString;
  formModel.age = calculateAge(birthday);
}

function buildPickerParams(pageNo = 1) {
  return {
    hospitalizationNo: pickerQuery.hospitalizationNo.trim() || undefined,
    idNo: pickerQuery.idNo.trim() || undefined,
    managerDoctorId: undefined,
    name: pickerQuery.name.trim() || undefined,
    pageNo,
    pageSize: pickerPagination.pageSize,
    patientNo: pickerQuery.patientNo.trim() || undefined,
    patientPhone: pickerQuery.patientPhone.trim() || undefined,
    patientType: pickerQuery.patientType || undefined,
    socialSecurityNo: pickerQuery.socialSecurityNo.trim() || undefined,
    status: 0,
  };
}

function fillForm(data?: HospitalPatientApi.Patient | null) {
  Object.assign(formModel, createDefaultFormModel(), data || {});
}

function resetPickerQuery() {
  Object.assign(pickerQuery, {
    hospitalizationNo: '',
    idNo: '',
    name: '',
    patientNo: '',
    patientPhone: '',
    patientType: undefined,
    socialSecurityNo: '',
  });
}

async function loadPickerPage(pageNo = 1) {
  pickerLoading.value = true;
  try {
    const data = await getPatientPage(buildPickerParams(pageNo));
    pickerRows.value = data.list || [];
    pickerPagination.current = pageNo;
    pickerPagination.total = data.total || 0;
  } finally {
    pickerLoading.value = false;
  }
}

function openPicker(mode: 'admit' | 'fetch-his') {
  pickerMode.value = mode;
  resetPickerQuery();
  Object.assign(pickerQuery, {
    hospitalizationNo: formModel.hospitalizationNo || '',
    idNo: formModel.idNo || '',
    name: formModel.name || '',
    patientNo: formModel.patientNo || '',
    patientPhone: formModel.patientPhone || '',
    patientType: mode === 'admit' ? '住院' : undefined,
    socialSecurityNo: formModel.socialSecurityNo || '',
  });
  pickerOpen.value = true;
  void loadPickerPage(1);
}

function isChineseIdCard(idNo?: string) {
  return /^[1-9]\d{16}[\dX]$/i.test(idNo || '');
}

function applyIdCardProfile(idNo: string) {
  if (!isChineseIdCard(idNo)) return;
  const birthday = `${idNo.slice(6, 10)}-${idNo.slice(10, 12)}-${idNo.slice(12, 14)}`;
  formModel.birthday = birthday;
  formModel.gender = Number(idNo.slice(16, 17)) % 2 === 0 ? 2 : 1;
  formModel.age = calculateAge(birthday);
  formModel.idType = '身份证';
}

async function importPatientDetail(id?: number) {
  if (!id) return;
  const detail = await getPatient(id);
  fillForm(detail);
  latestPatientNo.value = detail.patientNo || '';
  pickerOpen.value = false;
  message.success(`已回填患者档案：${detail.patientNo || detail.name}`);
}

async function handleReadIdCard() {
  const idNo = formModel.idNo?.trim();
  if (!idNo) {
    message.warning('请先输入身份证号');
    return;
  }
  applyIdCardProfile(idNo);
  const page = await getPatientPage({
    idNo,
    pageNo: 1,
    pageSize: 1,
    status: 0,
  });
  if (page.total > 0 && page.list[0]?.id) {
    await importPatientDetail(page.list[0].id);
    return;
  }
  if (isChineseIdCard(idNo)) {
    message.success('已读取身份证基础信息，可继续登记');
    return;
  }
  message.info('未命中历史档案，可继续新建登记');
}

async function handleReadSocialSecurityCard() {
  const socialSecurityNo = formModel.socialSecurityNo?.trim();
  if (!socialSecurityNo) {
    message.warning('请先输入社保卡号');
    return;
  }
  const page = await getPatientPage({
    pageNo: 1,
    pageSize: 1,
    socialSecurityNo,
    status: 0,
  });
  if (page.total > 0 && page.list[0]?.id) {
    await importPatientDetail(page.list[0].id);
    return;
  }
  message.info('未命中社保卡档案，可继续新建登记');
}

function handleQuickAction(key: (typeof quickActions)[number]['key']) {
  switch (key) {
    case 'admit': {
      openPicker('admit');
      return;
    }
    case 'fetch-his': {
      openPicker('fetch-his');
      return;
    }
    case 'read-id-card': {
      void handleReadIdCard();
      return;
    }
    default: {
      void handleReadSocialSecurityCard();
    }
  }
}

async function handlePhotoUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;
  photoUploading.value = true;
  try {
    const res = await uploadFile({ directory: 'hospital/patient', file });
    formModel.photoUrl = res?.url || res?.data || res;
    message.success('患者照片上传成功');
  } finally {
    photoUploading.value = false;
    target.value = '';
  }
}

function triggerPhotoInput(type: 'capture' | 'local') {
  if (type === 'capture') {
    captureInputRef.value?.click();
    return;
  }
  localInputRef.value?.click();
}

async function loadDoctors() {
  doctorList.value = await getSimpleDoctorList();
}

function normalizePayload(): HospitalPatientApi.Patient {
  return {
    ...formModel,
    age:
      formModel.age || formModel.age === 0 ? Number(formModel.age) : undefined,
    attendingDoctorId: formModel.attendingDoctorId || undefined,
    contactName: formModel.contactName?.trim(),
    contactPhone: formModel.contactPhone?.trim(),
    currentAddress: formModel.currentAddress?.trim(),
    firstDoctorName: formModel.firstDoctorName?.trim(),
    hospitalizationNo: formModel.hospitalizationNo?.trim(),
    idNo: formModel.idNo?.trim(),
    medicalRecordNo: formModel.medicalRecordNo?.trim(),
    name: formModel.name?.trim() || '',
    nativePlace: formModel.nativePlace?.trim(),
    outpatientNo: formModel.outpatientNo?.trim(),
    patientPhone: formModel.patientPhone?.trim(),
    photoUrl: formModel.photoUrl?.trim(),
    radiotherapyNo: formModel.radiotherapyNo?.trim(),
    remark: formModel.remark?.trim(),
    socialSecurityNo: formModel.socialSecurityNo?.trim(),
    tags: formModel.tags?.trim(),
    visitNo: formModel.visitNo?.trim(),
    wardName: formModel.wardName?.trim(),
  };
}

async function handleSubmit(redirect = false) {
  await formRef.value?.validate();
  formLoading.value = true;
  try {
    const payload = normalizePayload();
    const isUpdate = Boolean(payload.id);
    let patientId = payload.id;
    if (isUpdate) {
      await updatePatient(payload);
    } else {
      patientId = await createPatient(payload);
    }
    const detail = await getPatient(patientId!);
    latestPatientNo.value = detail.patientNo || '';
    message.success(
      isUpdate
        ? `患者档案已更新：${detail.patientNo || patientId}`
        : `患者登记成功，档案号：${detail.patientNo || patientId}`,
    );
    if (redirect) {
      await router.push({
        path: '/hospital-flow/hospital-patient/list',
        query: { openId: String(patientId) },
      });
      return;
    }
    if (isUpdate) {
      fillForm(detail);
      return;
    }
    handleReset(false);
  } finally {
    formLoading.value = false;
  }
}

function handleReset(clearLatest = true) {
  fillForm();
  formRef.value?.clearValidate();
  if (clearLatest) {
    latestPatientNo.value = '';
  }
}

onMounted(async () => {
  handleReset();
  await loadDoctors();
});
</script>

<template>
  <Page auto-content-height>
    <section class="patient-registration-page">
      <div class="patient-registration-topbar">
        <div class="patient-registration-topbar__left">
          <Button
            class="patient-registration-topbar__back"
            type="link"
            @click="handleBack"
          >
            <IconifyIcon icon="mdi:chevron-left" />
            返回列表
          </Button>
          <div class="patient-registration-topbar__title-wrap">
            <div class="patient-registration-topbar__title">患者登记</div>
            <div class="patient-registration-topbar__meta">
              <Tag :color="formModel.id ? 'processing' : 'default'">
                {{
                  formModel.id
                    ? `编辑档案：${formModel.patientNo || formModel.id}`
                    : '新建档案'
                }}
              </Tag>
              <Tag v-if="latestPatientNo" color="success">
                最近档案：{{ latestPatientNo }}
              </Tag>
            </div>
          </div>
        </div>
        <div class="patient-registration-topbar__actions">
          <Button
            v-for="item in quickActions"
            :key="item.key"
            size="small"
            @click="handleQuickAction(item.key)"
          >
            {{ item.label }}
          </Button>
        </div>
      </div>

      <div class="patient-registration-shell">
        <aside class="patient-registration-shell__aside">
          <div class="patient-photo-panel">
            <div class="patient-photo-panel__title">患者照片</div>
            <div v-if="formModel.photoUrl" class="patient-photo-panel__frame">
              <img
                :src="formModel.photoUrl"
                alt="患者照片"
                class="patient-photo-panel__image"
              />
            </div>
            <div
              v-else
              class="patient-photo-panel__frame patient-photo-panel__frame--placeholder"
            >
              <IconifyIcon icon="mdi:account-outline" />
            </div>
            <div class="patient-photo-panel__actions">
              <Button
                size="small"
                :loading="photoUploading"
                @click="triggerPhotoInput('capture')"
              >
                拍摄照片
              </Button>
              <Button
                size="small"
                type="primary"
                ghost
                :loading="photoUploading"
                @click="triggerPhotoInput('local')"
              >
                本地上传
              </Button>
            </div>
            <Button
              v-if="formModel.photoUrl"
              block
              danger
              ghost
              size="small"
              @click="formModel.photoUrl = ''"
            >
              清除照片
            </Button>
            <input
              ref="captureInputRef"
              accept="image/*"
              capture="environment"
              class="hidden-input"
              type="file"
              @change="handlePhotoUpload"
            />
            <input
              ref="localInputRef"
              accept="image/*"
              class="hidden-input"
              type="file"
              @change="handlePhotoUpload"
            />
            <div class="patient-photo-panel__tip">
              照片用于腕带、病历打印与治疗核验。
            </div>
          </div>
        </aside>

        <div class="patient-registration-shell__main">
          <div class="patient-form-shell">
            <Form
              ref="formRef"
              :label-col="{ style: { width: '84px' } }"
              :model="formModel"
              :rules="rules"
              class="patient-registration-form"
              layout="horizontal"
            >
              <div class="patient-form-section">
                <div class="patient-form-section__title">基本信息</div>
                <div class="patient-form-grid">
                  <Form.Item
                    class="is-span-2 patient-form-item--compound"
                    label="证件号"
                    required
                  >
                    <div class="patient-compound-field">
                      <Form.Item name="idType" no-style>
                        <Select
                          v-model:value="formModel.idType"
                          :options="idTypeOptions"
                          class="patient-compound-field__type"
                          placeholder="类型"
                        />
                      </Form.Item>
                      <Form.Item name="idNo" no-style>
                        <Input
                          v-model:value="formModel.idNo"
                          allow-clear
                          placeholder="请输入证件号码"
                        />
                      </Form.Item>
                      <Button
                        size="small"
                        type="primary"
                        @click="handleReadIdCard"
                      >
                        查询
                      </Button>
                    </div>
                  </Form.Item>

                  <Form.Item
                    class="is-span-2 patient-form-item--compound"
                    label="病案号"
                  >
                    <div
                      class="patient-compound-field patient-compound-field--two"
                    >
                      <Form.Item name="medicalRecordNo" no-style>
                        <Input
                          v-model:value="formModel.medicalRecordNo"
                          allow-clear
                          placeholder="请输入病案号"
                        />
                      </Form.Item>
                      <Button size="small" @click="openPicker('fetch-his')">
                        调HIS
                      </Button>
                    </div>
                  </Form.Item>

                  <Form.Item label="档案号">
                    <Input
                      :value="
                        formModel.patientNo ||
                        latestPatientNo ||
                        '系统保存后自动生成'
                      "
                      disabled
                    />
                  </Form.Item>
                  <Form.Item label="就诊号" name="visitNo">
                    <Input
                      v-model:value="formModel.visitNo"
                      allow-clear
                      placeholder="请输入就诊号"
                    />
                  </Form.Item>
                  <Form.Item label="放疗号" name="radiotherapyNo">
                    <Input
                      v-model:value="formModel.radiotherapyNo"
                      allow-clear
                      placeholder="请输入放疗号"
                    />
                  </Form.Item>
                  <Form.Item
                    class="patient-form-item--compound"
                    label="社保卡号"
                  >
                    <div
                      class="patient-compound-field patient-compound-field--two"
                    >
                      <Form.Item name="socialSecurityNo" no-style>
                        <Input
                          v-model:value="formModel.socialSecurityNo"
                          allow-clear
                          placeholder="请输入社保卡号"
                        />
                      </Form.Item>
                      <Button
                        size="small"
                        @click="handleReadSocialSecurityCard"
                      >
                        读卡
                      </Button>
                    </div>
                  </Form.Item>

                  <Form.Item label="姓名" name="name">
                    <Input
                      v-model:value="formModel.name"
                      allow-clear
                      placeholder="请输入姓名"
                    />
                  </Form.Item>
                  <Form.Item label="性别" name="gender">
                    <Select
                      v-model:value="formModel.gender"
                      :options="genderOptions"
                      placeholder="请选择"
                    />
                  </Form.Item>
                  <Form.Item label="出生日期" name="birthday">
                    <DatePicker
                      v-model:value="formModel.birthday"
                      format="YYYY-MM-DD"
                      placeholder="请选择出生日期"
                      value-format="YYYY-MM-DD"
                      @change="handleBirthdayChange"
                    />
                  </Form.Item>
                  <Form.Item label="年龄" name="age">
                    <InputNumber
                      v-model:value="formModel.age"
                      :min="0"
                      class="w-full"
                      placeholder="年龄"
                    />
                  </Form.Item>

                  <Form.Item label="婚姻" name="maritalStatus">
                    <Select
                      v-model:value="formModel.maritalStatus"
                      :options="maritalStatusOptions"
                      allow-clear
                      placeholder="请选择"
                    />
                  </Form.Item>
                  <Form.Item label="籍贯" name="nativePlace">
                    <Input
                      v-model:value="formModel.nativePlace"
                      allow-clear
                      placeholder="请输入籍贯"
                    />
                  </Form.Item>
                  <Form.Item
                    class="is-span-2"
                    label="现住址"
                    name="currentAddress"
                  >
                    <Input
                      v-model:value="formModel.currentAddress"
                      allow-clear
                      placeholder="请输入现住址"
                    />
                  </Form.Item>

                  <Form.Item label="患者电话" name="patientPhone">
                    <Input
                      v-model:value="formModel.patientPhone"
                      allow-clear
                      placeholder="请输入患者电话"
                    />
                  </Form.Item>
                  <Form.Item label="联系人" name="contactName">
                    <Input
                      v-model:value="formModel.contactName"
                      allow-clear
                      placeholder="请输入联系人姓名"
                    />
                  </Form.Item>
                  <Form.Item label="关系" name="contactRelation">
                    <Select
                      v-model:value="formModel.contactRelation"
                      :options="relationOptions"
                      allow-clear
                      placeholder="请选择"
                    />
                  </Form.Item>
                  <Form.Item label="联系人电话" name="contactPhone">
                    <Input
                      v-model:value="formModel.contactPhone"
                      allow-clear
                      placeholder="请输入联系人电话"
                    />
                  </Form.Item>
                </div>
              </div>

              <div class="patient-form-section">
                <div class="patient-form-section__title">诊疗信息</div>
                <div class="patient-form-grid">
                  <Form.Item label="主管医生" name="managerDoctorId">
                    <Select
                      v-model:value="formModel.managerDoctorId"
                      :options="doctorOptions"
                      allow-clear
                      option-filter-prop="label"
                      placeholder="请选择主管医生"
                      show-search
                    />
                  </Form.Item>
                  <Form.Item label="主诊医生" name="attendingDoctorId">
                    <Select
                      v-model:value="formModel.attendingDoctorId"
                      :options="doctorOptions"
                      allow-clear
                      option-filter-prop="label"
                      placeholder="请选择主诊医生"
                      show-search
                    />
                  </Form.Item>
                  <Form.Item label="患者类型" name="patientType">
                    <Select
                      v-model:value="formModel.patientType"
                      :options="patientTypeOptions"
                      placeholder="请选择患者类型"
                    />
                  </Form.Item>
                  <Form.Item label="院区" name="campus">
                    <Select
                      v-model:value="formModel.campus"
                      :options="campusOptions"
                      placeholder="请选择院区"
                    />
                  </Form.Item>

                  <Form.Item label="病区" name="wardName">
                    <Input
                      v-model:value="formModel.wardName"
                      allow-clear
                      placeholder="请输入病区"
                    />
                  </Form.Item>
                  <Form.Item label="付费方式" name="paymentType">
                    <Select
                      v-model:value="formModel.paymentType"
                      :options="paymentTypeOptions"
                      allow-clear
                      placeholder="请选择付费方式"
                    />
                  </Form.Item>
                  <Form.Item label="住院号" name="hospitalizationNo">
                    <Input
                      v-model:value="formModel.hospitalizationNo"
                      allow-clear
                      placeholder="请输入住院号"
                    />
                  </Form.Item>
                  <Form.Item label="门诊号" name="outpatientNo">
                    <Input
                      v-model:value="formModel.outpatientNo"
                      allow-clear
                      placeholder="请输入门诊号"
                    />
                  </Form.Item>

                  <Form.Item label="首诊医生" name="firstDoctorName">
                    <Input
                      v-model:value="formModel.firstDoctorName"
                      allow-clear
                      placeholder="请输入首诊医生"
                    />
                  </Form.Item>
                  <Form.Item class="is-span-2" label="标签" name="tags">
                    <Input
                      v-model:value="formModel.tags"
                      allow-clear
                      placeholder="多个标签用逗号分隔"
                    />
                  </Form.Item>
                  <Form.Item label="备注" name="remark">
                    <Input
                      v-model:value="formModel.remark"
                      allow-clear
                      placeholder="请输入备注信息"
                    />
                  </Form.Item>

                  <Form.Item
                    class="is-span-2 patient-form-item--textarea"
                    label="过敏史"
                    name="allergyHistory"
                  >
                    <Textarea
                      v-model:value="formModel.allergyHistory"
                      :rows="4"
                      placeholder="请输入过敏史"
                    />
                  </Form.Item>
                  <Form.Item
                    class="is-span-2 patient-form-item--textarea"
                    label="既往史"
                    name="pastHistory"
                  >
                    <Textarea
                      v-model:value="formModel.pastHistory"
                      :rows="4"
                      placeholder="请输入既往史"
                    />
                  </Form.Item>
                </div>
              </div>
            </Form>
          </div>
        </div>
      </div>

      <div class="patient-registration-footer">
        <div class="patient-registration-footer__hint">
          保存后自动生成档案号，提交后直接回到患者列表并打开详情。
        </div>
        <div class="patient-registration-footer__actions">
          <Button @click="handleReset()">清空</Button>
          <Button
            type="primary"
            ghost
            :loading="formLoading"
            @click="handleSubmit()"
          >
            保存
          </Button>
          <Button
            type="primary"
            :loading="formLoading"
            @click="handleSubmit(true)"
          >
            提交并进入详情
          </Button>
        </div>
      </div>

      <Modal
        v-model:open="pickerOpen"
        :confirm-loading="pickerLoading"
        :footer="null"
        :title="pickerTitle"
        destroy-on-close
        width="1180px"
      >
        <div class="patient-picker-toolbar">
          <Input
            v-model:value="pickerQuery.patientNo"
            allow-clear
            placeholder="档案号"
          />
          <Input
            v-model:value="pickerQuery.name"
            allow-clear
            placeholder="患者姓名"
          />
          <Input
            v-model:value="pickerQuery.idNo"
            allow-clear
            placeholder="身份证号"
          />
          <Input
            v-model:value="pickerQuery.hospitalizationNo"
            allow-clear
            placeholder="住院号"
          />
          <Input
            v-model:value="pickerQuery.patientPhone"
            allow-clear
            placeholder="联系电话"
          />
          <Button type="primary" @click="loadPickerPage(1)">查询</Button>
          <Button @click="resetPickerQuery()">重置</Button>
        </div>
        <Table
          :columns="pickerColumns"
          :data-source="pickerRows"
          :loading="pickerLoading"
          :pagination="{
            current: pickerPagination.current,
            pageSize: pickerPagination.pageSize,
            showSizeChanger: false,
            size: 'small',
            total: pickerPagination.total,
          }"
          row-key="id"
          size="small"
          @change="(page) => loadPickerPage(page.current || 1)"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <Button type="link" @click="importPatientDetail(record.id)">
                导入
              </Button>
            </template>
          </template>
        </Table>
      </Modal>
    </section>
  </Page>
</template>

<style scoped>
.patient-registration-page {
  --pr-border: #d9d9d9;
  --pr-panel: #ffffff;
  --pr-panel-soft: #fafcff;
  --pr-primary: #1677ff;
  --pr-text: rgb(31 35 41);
  --pr-text-soft: rgb(107 114 128);
  display: flex;
  flex-direction: column;
  gap: 12px;
  color: var(--pr-text);
}

.patient-registration-topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.patient-registration-topbar__left {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.patient-registration-topbar__back {
  padding-inline: 0;
  color: var(--pr-text-soft);
}

.patient-registration-topbar__title {
  font-size: 20px;
  font-weight: 600;
  line-height: 28px;
}

.patient-registration-topbar__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.patient-registration-topbar__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.patient-registration-shell {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.patient-registration-shell__aside {
  position: sticky;
  top: 12px;
}

.patient-photo-panel,
.patient-form-shell,
.patient-registration-footer {
  border: 1px solid var(--pr-border);
  border-radius: 8px;
  background: var(--pr-panel);
}

.patient-photo-panel {
  padding: 12px;
}

.patient-photo-panel__title,
.patient-form-section__title {
  margin-bottom: 10px;
  padding-left: 8px;
  border-left: 3px solid var(--pr-primary);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.2;
}

.patient-photo-panel__frame {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  aspect-ratio: 4 / 5;
  border: 1px solid var(--pr-border);
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.patient-photo-panel__frame--placeholder {
  background: linear-gradient(180deg, #f9fbff 0%, #eef5ff 100%);
  color: #91a6c6;
  font-size: 56px;
}

.patient-photo-panel__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.patient-photo-panel__actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin: 10px 0 8px;
}

.patient-photo-panel__tip {
  margin-top: 10px;
  color: var(--pr-text-soft);
  font-size: 12px;
  line-height: 1.5;
}

.patient-form-shell {
  padding: 12px 12px 14px;
}

.patient-registration-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.patient-form-section {
  border: 1px solid var(--pr-border);
  border-radius: 8px;
  background: var(--pr-panel-soft);
  padding: 12px 12px 4px;
}

.patient-form-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 12px;
}

.patient-registration-form :deep(.ant-form-item) {
  margin-bottom: 10px;
}

.patient-registration-form :deep(.ant-form-item.is-span-2) {
  grid-column: span 2;
}

.patient-registration-form :deep(.ant-form-item-label > label) {
  color: #374151;
  font-size: 12px;
  height: 30px;
}

.patient-registration-form :deep(.ant-input),
.patient-registration-form :deep(.ant-select-selector),
.patient-registration-form :deep(.ant-picker),
.patient-registration-form :deep(.ant-input-number),
.patient-registration-form :deep(textarea.ant-input),
.patient-picker-toolbar :deep(.ant-input),
.patient-picker-toolbar :deep(.ant-btn) {
  border-radius: 4px;
  font-size: 12px;
}

.patient-registration-form :deep(.ant-input),
.patient-registration-form :deep(.ant-picker),
.patient-registration-form :deep(.ant-input-number),
.patient-registration-form :deep(.ant-select-selector) {
  min-height: 30px;
}

.patient-registration-form :deep(.ant-input),
.patient-registration-form :deep(.ant-picker),
.patient-registration-form :deep(.ant-input-number) {
  width: 100%;
}

.patient-registration-form :deep(.ant-input-number-input) {
  height: 28px;
}

.patient-registration-form :deep(textarea.ant-input) {
  min-height: 92px;
}

.patient-form-item--compound :deep(.ant-form-item-control-input) {
  min-height: 30px;
}

.patient-compound-field {
  display: grid;
  grid-template-columns: 118px minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
}

.patient-compound-field--two {
  grid-template-columns: minmax(0, 1fr) auto;
}

.patient-form-item--textarea :deep(.ant-form-item-label > label) {
  align-self: flex-start;
  padding-top: 6px;
}

.patient-registration-footer {
  position: sticky;
  bottom: 0;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  box-shadow: 0 -6px 14px rgb(15 23 42 / 0.04);
}

.patient-registration-footer__hint {
  color: var(--pr-text-soft);
  font-size: 12px;
}

.patient-registration-footer__actions {
  display: flex;
  gap: 8px;
}

.patient-picker-toolbar {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 12px;
}

.hidden-input {
  display: none;
}

@media (max-width: 1500px) {
  .patient-form-grid,
  .patient-picker-toolbar {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .patient-registration-form :deep(.ant-form-item.is-span-2) {
    grid-column: span 3;
  }
}

@media (max-width: 1180px) {
  .patient-registration-shell {
    grid-template-columns: 1fr;
  }

  .patient-registration-shell__aside {
    position: static;
  }

  .patient-photo-panel__frame {
    max-width: 180px;
  }

  .patient-form-grid,
  .patient-picker-toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .patient-registration-form :deep(.ant-form-item.is-span-2) {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .patient-registration-topbar,
  .patient-registration-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .patient-registration-topbar__left {
    flex-direction: column;
    gap: 4px;
  }

  .patient-registration-topbar__actions,
  .patient-registration-footer__actions {
    width: 100%;
  }

  .patient-registration-topbar__actions :deep(.ant-btn),
  .patient-registration-footer__actions :deep(.ant-btn) {
    flex: 1;
  }

  .patient-form-grid,
  .patient-picker-toolbar,
  .patient-photo-panel__actions,
  .patient-compound-field,
  .patient-compound-field--two {
    grid-template-columns: 1fr;
  }

  .patient-registration-form :deep(.ant-form-item.is-span-2) {
    grid-column: span 1;
  }
}
</style>
