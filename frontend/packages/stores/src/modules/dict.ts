import { acceptHMRUpdate, defineStore } from 'pinia';

export interface DictItem {
  colorType?: string;
  cssClass?: string;
  label: string;
  value: string;
}

export type Dict = Record<string, DictItem[]>;

interface DictState {
  dictCache: Dict;
}

function unwrapApiList<T>(payload: any): T[] {
  if (Array.isArray(payload)) {
    return payload;
  }
  if (Array.isArray(payload?.data)) {
    return payload.data;
  }
  if (Array.isArray(payload?.data?.data)) {
    return payload.data.data;
  }
  return [];
}

export const useDictStore = defineStore('core-dict', {
  actions: {
    getDictData(dictType: string, value: any) {
      const dict = this.dictCache[dictType];
      if (!dict) {
        return undefined;
      }
      return (
        dict.find((d) => d.value === value || d.value === value.toString()) ??
        undefined
      );
    },
    getDictOptions(dictType: string) {
      const dictOptions = this.dictCache[dictType];
      if (!dictOptions) {
        return [];
      }
      return dictOptions;
    },
    setDictCache(dicts: Dict) {
      this.dictCache = dicts;
    },
    setDictCacheByApi(
      api: (params: Record<string, any>) => Promise<Record<string, any>[]>,
      params: Record<string, any> = {},
      labelField: string = 'label',
      valueField: string = 'value',
    ) {
      api(params).then((dicts) => {
        const normalizedDicts = unwrapApiList<Record<string, any>>(dicts);
        const dictCacheData: Dict = {};
        normalizedDicts.forEach((dict) => {
          dictCacheData[dict.dictType] = normalizedDicts
            .filter((d) => d.dictType === dict.dictType)
            .map((d) => ({
              colorType: d.colorType,
              cssClass: d.cssClass,
              label: d[labelField],
              value: d[valueField],
            }));
        });
        this.setDictCache(dictCacheData);
      });
    },
  },
  persist: {
    // 持久化
    pick: ['dictCache'],
  },
  state: (): DictState => ({
    dictCache: {},
  }),
});

// 解决热更新问题
const hot = import.meta.hot;
if (hot) {
  hot.accept(acceptHMRUpdate(useDictStore, hot));
}
