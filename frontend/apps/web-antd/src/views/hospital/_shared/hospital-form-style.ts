interface HospitalFormMeta {
  nodeName?: string;
  pageCode?: string;
}

interface HospitalTemplateField {
  field: string;
  title: string;
  col?: 6 | 8 | 12 | 24;
  display?: boolean;
  options?: Array<{ label: string; value: string }>;
  props?: Record<string, any>;
  type?: string;
}

interface HospitalTemplateSection {
  fields: HospitalTemplateField[];
  title: string;
}

const FULL_WIDTH_TYPES = new Set([
  'aDivider',
  'array',
  'button',
  'divider',
  'editor',
  'fc-editor',
  'fcRow',
  'fcTable',
  'fcTitle',
  'frame',
  'group',
  'hidden',
  'row',
  'subForm',
  'table',
  'textarea',
  'title',
  'upload',
  'uploadFile',
  'uploadImage',
  'uploadImages',
]);

const STRUCTURED_TYPES = new Set(['aDivider', 'fcRow', 'fcTable', 'fcTitle']);

const OPTION_SETS = {
  evaluate: [
    { label: '优', value: 'GOOD' },
    { label: '良', value: 'NORMAL' },
    { label: '需关注', value: 'WATCH' },
  ],
  gender: [
    { label: '男', value: 'MALE' },
    { label: '女', value: 'FEMALE' },
  ],
  guide: [
    { label: 'CBCT', value: 'CBCT' },
    { label: 'EPID', value: 'EPID' },
    { label: '激光摆位', value: 'LASER' },
  ],
  machine: [
    { label: '直线加速器1', value: 'LINAC-1' },
    { label: '直线加速器2', value: 'LINAC-2' },
    { label: 'TOMO', value: 'TOMO' },
  ],
  queue: [
    { label: '上午', value: 'AM' },
    { label: '下午', value: 'PM' },
    { label: '夜间', value: 'NIGHT' },
  ],
  result: [
    { label: '通过', value: 'PASS' },
    { label: '退回', value: 'REJECT' },
  ],
  status: [
    { label: '待执行', value: 'PENDING' },
    { label: '执行中', value: 'RUNNING' },
    { label: '已完成', value: 'DONE' },
  ],
  technique: [
    { label: '3D-CRT', value: '3DCRT' },
    { label: 'IMRT', value: 'IMRT' },
    { label: 'VMAT', value: 'VMAT' },
    { label: 'SBRT', value: 'SBRT' },
  ],
  yesNo: [
    { label: '是', value: 'YES' },
    { label: '否', value: 'NO' },
  ],
};

function cloneRule(rule: any) {
  if (!rule || typeof rule !== 'object') return rule;
  return {
    ...rule,
    children: Array.isArray(rule.children)
      ? rule.children.map((item: any) => cloneRule(item))
      : rule.children,
    control: Array.isArray(rule.control)
      ? rule.control.map((item: any) => ({
          ...item,
          rule: Array.isArray(item.rule)
            ? item.rule.map((child: any) => cloneRule(child))
            : item.rule,
        }))
      : rule.control,
    options: Array.isArray(rule.options) ? [...rule.options] : rule.options,
    props: rule.props ? { ...rule.props } : rule.props,
  };
}

function normalizeText(text?: string) {
  return String(text || '')
    .trim()
    .toLowerCase();
}

function getDefaultCol(span = 6) {
  if (span === 24) return { span: 24 };
  if (span === 12) return { lg: 12, md: 12, sm: 24, span: 12, xs: 24 };
  if (span === 8) return { lg: 8, md: 12, sm: 24, span: 8, xs: 24 };
  return { lg: 6, md: 8, sm: 12, span: 6, xs: 24 };
}

function resolveRuleCol(rule: any) {
  if (rule.col !== undefined) return rule.col;
  if (FULL_WIDTH_TYPES.has(rule.type)) return { span: 24 };
  if (rule.props?.rows && Number(rule.props.rows) > 1) return { span: 24 };
  return getDefaultCol(6);
}

function resolveRuleSpan(col: any) {
  if (typeof col === 'number') return col;
  if (!col || typeof col !== 'object') return 6;
  return Number(col.span || col.lg || col.md || col.sm || 6);
}

function appendRuleClassName(rule: any, className: string) {
  const current = rule.className;
  if (Array.isArray(current)) {
    if (!current.includes(className)) current.push(className);
    rule.className = current;
    return;
  }
  const next = String(current || '')
    .split(/\s+/)
    .filter(Boolean);
  if (!next.includes(className)) next.push(className);
  rule.className = next.join(' ');
}

function appendColClassName(col: any, className: string) {
  const nextCol =
    typeof col === 'number'
      ? { ...getDefaultCol(col) }
      : col && typeof col === 'object'
        ? { ...col }
        : { ...getDefaultCol(6) };
  const current = nextCol.class;
  if (Array.isArray(current)) {
    const next = current.filter(
      (item: string) => !String(item).startsWith('hospital-grid-col-span-'),
    );
    next.push(className);
    nextCol.class = next;
    return nextCol;
  }
  const next = String(current || '')
    .split(/\s+/)
    .filter(Boolean)
    .filter((item) => !item.startsWith('hospital-grid-col-span-'));
  next.push(className);
  nextCol.class = next.join(' ');
  return nextCol;
}

function normalizeRule(rule: any, depth = 0): any {
  const nextRule = cloneRule(rule);
  if (!nextRule || typeof nextRule !== 'object') return nextRule;
  nextRule.col = resolveRuleCol(nextRule);
  const span = resolveRuleSpan(nextRule.col);
  nextRule.col = appendColClassName(
    nextRule.col,
    `hospital-grid-col-span-${span}`,
  );
  if (depth === 0) {
    appendRuleClassName(nextRule, `hospital-grid-span-${span}`);
  }
  if (Array.isArray(nextRule.children)) {
    nextRule.children = nextRule.children.map((item: any) =>
      normalizeRule(item, depth + 1),
    );
  }
  if (Array.isArray(nextRule.control)) {
    nextRule.control = nextRule.control.map((item: any) => ({
      ...item,
      rule: Array.isArray(item.rule)
        ? item.rule.map((child: any) => normalizeRule(child, depth + 1))
        : item.rule,
    }));
  }
  return nextRule;
}

const PASS_THROUGH_TYPES = new Set([
  'array',
  'col',
  'fcRow',
  'fcTable',
  'group',
  'hidden',
  'row',
  'subForm',
  'table',
]);

const SECTION_BREAK_TYPES = new Set([
  'aDivider',
  'divider',
  'fcTitle',
  'title',
]);

function stripGridSpanClassName(rule: any) {
  if (!rule || typeof rule !== 'object' || !rule.className) return;
  if (Array.isArray(rule.className)) {
    const next = rule.className.filter(
      (item: string) => !String(item).startsWith('hospital-grid-span-'),
    );
    if (next.length > 0) {
      rule.className = next;
    } else {
      delete rule.className;
    }
    return;
  }
  const next = String(rule.className)
    .split(/\s+/)
    .filter(Boolean)
    .filter((item) => !item.startsWith('hospital-grid-span-'));
  if (next.length > 0) {
    rule.className = next.join(' ');
  } else {
    delete rule.className;
  }
}

function makeHospitalRowRule(items: Array<{ rule: any; span: number }>) {
  const rowRule = {
    children: items.map(({ rule, span }) => {
      const childRule = cloneRule(rule);
      childRule.col = { show: false };
      stripGridSpanClassName(childRule);
      return {
        children: [childRule],
        class: `hospital-grid-col hospital-grid-col-span-${span}`,
        props: getDefaultCol(span),
        type: 'col',
      };
    }),
    props: { align: 'top', gutter: 8 },
    type: 'fcRow',
  };
  appendRuleClassName(rowRule, 'hospital-grid-span-24');
  return rowRule;
}

function convertHospitalRulesToRows(rules: any[] = []) {
  const nextRules: any[] = [];
  let rowItems: Array<{ rule: any; span: number }> = [];
  let currentSpan = 0;

  const flushRow = () => {
    if (rowItems.length === 0) return;
    nextRules.push(makeHospitalRowRule(rowItems));
    rowItems = [];
    currentSpan = 0;
  };

  rules.forEach((item) => {
    if (!item || typeof item !== 'object') {
      flushRow();
      nextRules.push(item);
      return;
    }

    const type = item.type;
    if (type === 'hidden') {
      nextRules.push(cloneRule(item));
      return;
    }
    if (type && PASS_THROUGH_TYPES.has(type)) {
      flushRow();
      nextRules.push(cloneRule(item));
      return;
    }
    if (type && SECTION_BREAK_TYPES.has(type)) {
      flushRow();
      nextRules.push(cloneRule(item));
      return;
    }

    const span = FULL_WIDTH_TYPES.has(type)
      ? 24
      : Math.min(24, Math.max(1, resolveRuleSpan(item.col)));

    if (span >= 24) {
      flushRow();
      rowItems.push({ rule: item, span: 24 });
      flushRow();
      return;
    }

    if (currentSpan + span > 24) {
      flushRow();
    }
    rowItems.push({ rule: item, span });
    currentSpan += span;

    if (currentSpan >= 24) {
      flushRow();
    }
  });

  flushRow();
  return nextRules;
}

function hasClassName(rule: any, className: string) {
  if (!rule || typeof rule !== 'object') return false;
  const current = rule.className;
  if (Array.isArray(current)) return current.includes(className);
  return String(current || '')
    .split(/\s+/)
    .filter(Boolean)
    .includes(className);
}

function isLegacyHospitalRow(rule: any) {
  return Boolean(
    rule?.type === 'fcRow' &&
    Array.isArray(rule.children) &&
    rule.children.length > 0 &&
    hasClassName(rule, 'hospital-grid-span-24') &&
    rule.children.every(
      (child: any) =>
        child?.type === 'col' &&
        Array.isArray(child.children) &&
        child.children.length === 1 &&
        child.children[0] &&
        typeof child.children[0] === 'object' &&
        child.children[0].col?.show === false,
    ),
  );
}

function unwrapLegacyHospitalRows(rules: any[] = []) {
  return rules.flatMap((item) => {
    if (!item || typeof item !== 'object') return [item];
    if (isLegacyHospitalRow(item)) {
      return item.children.flatMap((child: any) => {
        const innerRule = cloneRule(child.children[0]);
        innerRule.col = getDefaultCol(resolveRuleSpan(child.props));
        return unwrapLegacyHospitalRows([innerRule]);
      });
    }
    const nextItem = cloneRule(item);
    if (Array.isArray(nextItem.children) && nextItem.type !== 'fcRow') {
      nextItem.children = unwrapLegacyHospitalRows(nextItem.children);
    }
    if (Array.isArray(nextItem.control)) {
      nextItem.control = nextItem.control.map((control: any) => ({
        ...control,
        rule: Array.isArray(control.rule)
          ? unwrapLegacyHospitalRows(control.rule)
          : control.rule,
      }));
    }
    return [nextItem];
  });
}

function field(
  title: string,
  key: string,
  extras: Partial<HospitalTemplateField> = {},
) {
  return {
    col: 6,
    field: key,
    title,
    type: 'input',
    ...extras,
  } satisfies HospitalTemplateField;
}

function displayField(
  title: string,
  key: string,
  extras: Partial<HospitalTemplateField> = {},
) {
  return field(title, key, {
    display: true,
    ...extras,
  });
}

function makeDivider(title: string) {
  return {
    children: [title],
    col: { span: 24 },
    props: { orientation: 'left', plain: true },
    type: 'aDivider',
  };
}

function basicPatientSection(): HospitalTemplateSection {
  return {
    title: '基本信息',
    fields: [
      displayField('姓名', 'patientName'),
      displayField('性别', 'gender', {
        options: OPTION_SETS.gender,
        type: 'select',
      }),
      displayField('年龄/岁', 'patientAge', { type: 'inputNumber' }),
      displayField('患者编号', 'patientNo'),
      displayField('放疗号', 'radiotherapyNo'),
      displayField('病案号', 'medicalRecordNo'),
      displayField('联系电话', 'patientPhone'),
      displayField('临床诊断', 'diagnosis', { col: 12 }),
    ],
  };
}

function sectionLibrary(key: string): HospitalTemplateSection[] {
  switch (key) {
    case 'position':
      return [
        basicPatientSection(),
        {
          title: '定位信息',
          fields: [
            field('预约日期', 'appointDate', { type: 'datePicker' }),
            field('定位设备', 'positionDevice'),
            field('扫描部位', 'scanSite'),
            field('机房/队列', 'queueName'),
            field('体位', 'patientPosition'),
            field('固定方式', 'fixMode'),
            field('增强扫描', 'enhanceScan', {
              options: OPTION_SETS.yesNo,
              type: 'radio',
            }),
            field('备注', 'positionRemark', { col: 24, type: 'textarea' }),
          ],
        },
      ];
    case 'contour':
      return [
        basicPatientSection(),
        {
          title: '勾画信息',
          fields: [
            field('勾画医生', 'contourDoctor'),
            field('勾画版本', 'contourVersion'),
            field('靶区部位', 'targetSite'),
            field('危及器官', 'organAtRisk', { col: 12 }),
            field('审核结果', 'reviewResult', {
              options: OPTION_SETS.result,
              type: 'radio',
            }),
            field('审核医生', 'reviewDoctor'),
            field('处理说明', 'contourRemark', { col: 24, type: 'textarea' }),
          ],
        },
      ];
    case 'plan':
      return [
        basicPatientSection(),
        {
          title: '计划信息',
          fields: [
            field('计划名称', 'planName'),
            field('治疗技术', 'technique', {
              options: OPTION_SETS.technique,
              type: 'select',
            }),
            field('治疗机器', 'treatmentMachine', {
              options: OPTION_SETS.machine,
              type: 'select',
            }),
            field('申请医生', 'requestDoctor'),
            field('分次剂量[Gy]', 'fractionDose', { type: 'inputNumber' }),
            field('分次数[F]', 'fractionCount', { type: 'inputNumber' }),
            field('总剂量[Gy]', 'totalDose', { type: 'inputNumber' }),
            field('计划说明', 'planRemark', { col: 24, type: 'textarea' }),
          ],
        },
      ];
    case 'treatment-summary':
      return [
        basicPatientSection(),
        {
          title: '治疗信息',
          fields: [
            field('治疗机器', 'treatmentMachine', {
              options: OPTION_SETS.machine,
              type: 'select',
            }),
            field('治疗技术', 'technique', {
              options: OPTION_SETS.technique,
              type: 'select',
            }),
            field('开始日期', 'startDate', { type: 'datePicker' }),
            field('结束日期', 'endDate', { type: 'datePicker' }),
            field('累计分次', 'finishedFractions', { type: 'inputNumber' }),
            field('累计剂量[Gy]', 'finishedDose', { type: 'inputNumber' }),
            field('疗效评估', 'evaluateLevel', {
              options: OPTION_SETS.evaluate,
              type: 'select',
            }),
            field('不良反应', 'adverseReaction', { col: 12 }),
            field('小结备注', 'summaryRemark', { col: 24, type: 'textarea' }),
          ],
        },
      ];
    case 'fee':
      return [
        basicPatientSection(),
        {
          title: '费用信息',
          fields: [
            field('费用项目', 'feeItem'),
            field('金额[元]', 'amount', { type: 'inputNumber' }),
            field('收费日期', 'feeDate', { type: 'datePicker' }),
            field('收费状态', 'feeStatus', {
              options: OPTION_SETS.status,
              type: 'select',
            }),
            field('说明', 'feeRemark', { col: 24, type: 'textarea' }),
          ],
        },
      ];
    case 'treatment-execute':
      return [
        basicPatientSection(),
        {
          title: '计划信息',
          fields: [
            field('治疗机器', 'treatmentMachine', {
              options: OPTION_SETS.machine,
              type: 'select',
            }),
            displayField('计划中心', 'planCenter'),
            displayField('治疗技术', 'technique', {
              options: OPTION_SETS.technique,
              type: 'select',
            }),
            displayField('治疗部位', 'targetSite'),
          ],
        },
        {
          title: '靶区处方剂量',
          fields: [
            field('治疗靶区', 'targetArea'),
            field('分次剂量[Gy]', 'fractionDose', { type: 'inputNumber' }),
            field('分次数[F]', 'fractionCount', { type: 'inputNumber' }),
            field('总剂量[Gy]', 'totalDose', { type: 'inputNumber' }),
          ],
        },
        {
          title: '摆位要求',
          fields: [
            displayField('扫描体位', 'scanPosition'),
            displayField('摆位流程', 'positionFlow'),
            displayField('体位', 'patientPosition'),
            displayField('固定装置', 'fixDevice'),
            displayField('双手位置', 'armPosition'),
            displayField('同步要求', 'breathingMode'),
            displayField('脚架体位', 'footSupport'),
            displayField('图像引导', 'imageGuide', {
              options: OPTION_SETS.guide,
              type: 'select',
            }),
          ],
        },
        {
          title: '治疗执行',
          fields: [
            field('治疗日期', 'executeDate', { type: 'datePicker' }),
            field('治疗时段', 'executeShift', {
              options: OPTION_SETS.queue,
              type: 'select',
            }),
            field('治疗状态', 'executeStatus', {
              options: OPTION_SETS.status,
              type: 'select',
            }),
            field('执行技师', 'operatorName'),
            field('复核医师', 'reviewDoctor'),
            field('执行结果', 'executeResult', {
              options: OPTION_SETS.result,
              type: 'radio',
            }),
          ],
        },
        {
          title: '备注',
          fields: [
            field('特别说明', 'treatmentRemark', { col: 24, type: 'textarea' }),
          ],
        },
      ];
    default:
      return [
        basicPatientSection(),
        {
          title: '业务信息',
          fields: [
            field('责任人', 'handlerName'),
            field('安排时间', 'scheduleTime', { type: 'datePicker' }),
            field('机房/资源', 'resourceName'),
            field('处理状态', 'bizStatus', {
              options: OPTION_SETS.status,
              type: 'select',
            }),
            field('说明', 'taskRemark', { col: 24, type: 'textarea' }),
          ],
        },
      ];
  }
}

function resolveTemplateKey(meta: HospitalFormMeta = {}) {
  const pageCode = normalizeText(meta.pageCode);
  const nodeName = String(meta.nodeName || '');
  if (
    pageCode.includes('treatment-summary') ||
    nodeName.includes('小结') ||
    nodeName.includes('评估')
  ) {
    return 'treatment-summary';
  }
  if (
    pageCode.includes('treatment-execute') ||
    pageCode.includes('treatment-queue') ||
    pageCode.includes('treatment-appointment') ||
    nodeName.includes('治疗')
  ) {
    return 'treatment-execute';
  }
  if (
    pageCode.includes('plan-') ||
    nodeName.includes('计划') ||
    nodeName.includes('剂量')
  ) {
    return 'plan';
  }
  if (
    pageCode.includes('contour') ||
    nodeName.includes('勾画') ||
    nodeName.includes('审核')
  ) {
    return 'contour';
  }
  if (
    pageCode.includes('position') ||
    pageCode.includes('ct-') ||
    nodeName.includes('定位') ||
    nodeName.includes('影像')
  ) {
    return 'position';
  }
  if (pageCode.includes('fee') || nodeName.includes('费用')) {
    return 'fee';
  }
  return 'generic';
}

function collectFieldRules(rules: any[] = [], result = new Map<string, any>()) {
  rules.forEach((item) => {
    if (!item || typeof item !== 'object') return;
    if (item.field && !result.has(item.field)) result.set(item.field, item);
    if (Array.isArray(item.children)) collectFieldRules(item.children, result);
  });
  return result;
}

function makePlaceholder(type: string, title: string) {
  if (type === 'datePicker') return `请选择${title}`;
  if (type === 'select') return `请选择${title}`;
  return `请输入${title}`;
}

function buildFieldRule(template: HospitalTemplateField, sourceRule?: any) {
  const type = template.type || sourceRule?.type || 'input';
  const props = {
    ...(sourceRule?.props || {}),
    ...(template.props || {}),
  };
  if (template.display) {
    props.bordered ??= false;
    props.controls ??= false;
    props.disabled ??= true;
    props.inputReadOnly ??= true;
    props.placeholder = '--';
    props.readonly ??= true;
  }
  if (!props.placeholder && !['radio', 'checkbox', 'switch'].includes(type)) {
    props.placeholder = makePlaceholder(type, template.title);
  }
  if (type === 'inputNumber') {
    props.min ??= 0;
    props.precision ??= 2;
  }
  if (type === 'textarea') {
    props.rows ??= 3;
  }
  const nextRule = {
    ...cloneRule(sourceRule || {}),
    col: getDefaultCol(template.col || (type === 'textarea' ? 24 : 6)),
    field: template.field,
    options: template.options || sourceRule?.options,
    props,
    title: template.title,
    type,
  };
  if (template.display) {
    appendRuleClassName(nextRule, 'hospital-display-field');
  }
  return nextRule;
}

function needsPresetTemplate(rules: any[] = [], meta: HospitalFormMeta = {}) {
  if (!rules.length) return true;
  if (rules.some((item) => item?.type && STRUCTURED_TYPES.has(item.type))) {
    return false;
  }
  const fieldRules = rules.filter(
    (item) => item?.field && item.type !== 'hidden',
  );
  return Boolean(meta.pageCode || meta.nodeName) && fieldRules.length <= 12;
}

function buildHospitalPresetRules(
  sourceRules: any[] = [],
  meta: HospitalFormMeta = {},
) {
  const sections = sectionLibrary(resolveTemplateKey(meta));
  const fieldRuleMap = collectFieldRules(sourceRules);
  const usedFields = new Set<string>();
  const nextRules: any[] = [];
  sections.forEach((section) => {
    nextRules.push(makeDivider(section.title));
    section.fields.forEach((item) => {
      usedFields.add(item.field);
      nextRules.push(buildFieldRule(item, fieldRuleMap.get(item.field)));
    });
  });
  const extras = sourceRules.filter(
    (item) => item?.field && !usedFields.has(item.field),
  );
  if (extras.length > 0) {
    nextRules.push(makeDivider('其他信息'));
    nextRules.push(...extras.map((item) => cloneRule(item)));
  }
  return nextRules;
}

function buildTemplateFieldMap(meta: HospitalFormMeta = {}) {
  const fieldMap = new Map<string, HospitalTemplateField>();
  sectionLibrary(resolveTemplateKey(meta)).forEach((section) => {
    section.fields.forEach((item) => {
      fieldMap.set(item.field, item);
    });
  });
  return fieldMap;
}

function applyTemplateFieldPreset(
  rule: any,
  fieldMap: Map<string, HospitalTemplateField>,
): any {
  const nextRule = cloneRule(rule);
  if (!nextRule || typeof nextRule !== 'object') return nextRule;
  const template = nextRule.field ? fieldMap.get(nextRule.field) : undefined;
  if (template?.display) {
    nextRule.props = {
      ...(nextRule.props || {}),
      bordered: nextRule.props?.bordered ?? false,
      controls: nextRule.props?.controls ?? false,
      disabled: nextRule.props?.disabled ?? true,
      inputReadOnly: nextRule.props?.inputReadOnly ?? true,
      placeholder: '--',
      readonly: nextRule.props?.readonly ?? true,
    };
    appendRuleClassName(nextRule, 'hospital-display-field');
  }
  if (Array.isArray(nextRule.children)) {
    nextRule.children = nextRule.children.map((item: any) =>
      applyTemplateFieldPreset(item, fieldMap),
    );
  }
  if (Array.isArray(nextRule.control)) {
    nextRule.control = nextRule.control.map((item: any) => ({
      ...item,
      rule: Array.isArray(item.rule)
        ? item.rule.map((child: any) =>
            applyTemplateFieldPreset(child, fieldMap),
          )
        : item.rule,
    }));
  }
  return nextRule;
}

export function normalizeHospitalFormOption(option?: Record<string, any>) {
  const nextOption = {
    ...(option || {}),
    col: {
      lg: 6,
      md: 8,
      sm: 12,
      span: 6,
      xs: 24,
      ...((option || {}).col || {}),
    },
    row: {
      gutter: 8,
      ...((option || {}).row || {}),
    },
  };
  nextOption.form = {
    ...((option || {}).form || {}),
    colon: false,
    labelPosition: 'right',
    labelWidth: '72px',
    size: 'small',
  };
  return nextOption;
}

export function normalizeHospitalFormRules(
  rules: any[] = [],
  meta: HospitalFormMeta = {},
) {
  const sourceRules = unwrapLegacyHospitalRows(rules);
  const nextRules = needsPresetTemplate(sourceRules, meta)
    ? buildHospitalPresetRules(sourceRules, meta)
    : sourceRules;
  const fieldMap = buildTemplateFieldMap(meta);
  return nextRules.map((item) =>
    applyTemplateFieldPreset(normalizeRule(item), fieldMap),
  );
}
