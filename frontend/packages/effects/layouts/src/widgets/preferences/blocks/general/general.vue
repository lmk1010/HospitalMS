<script setup lang="ts">
import { SUPPORT_LANGUAGES } from '@vben/constants';
import { $t } from '@vben/locales';

import SelectItem from '../select-item.vue';
import SwitchItem from '../switch-item.vue';

defineOptions({
  name: 'PreferenceGeneralConfig',
});

const appLocale = defineModel<string>('appLocale');
const appDynamicTitle = defineModel<boolean>('appDynamicTitle');
const appEnableCheckUpdates = defineModel<boolean>('appEnableCheckUpdates');
</script>

<template>
  <SelectItem v-model="appLocale" :items="SUPPORT_LANGUAGES">
    {{ $t('preferences.language') }}
  </SelectItem>
  <SwitchItem v-model="appDynamicTitle">
    {{ $t('preferences.dynamicTitle') }}
  </SwitchItem>
  <SwitchItem v-model="appEnableCheckUpdates">
    {{ $t('preferences.checkUpdates') }}
  </SwitchItem>
</template>
