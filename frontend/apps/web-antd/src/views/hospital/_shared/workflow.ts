import type { BpmModelApi } from '#/api/bpm/model';
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';
import type { SimpleFlowNode } from '#/views/bpm/components/simple-process-design';

import { BpmNodeTypeEnum } from '@vben/constants';

import { getModel, getModelList } from '#/api/bpm/model';

export type HospitalWorkflowKey =
  | 'radiotherapy-gz'
  | 'radiotherapy-px'
  | 'radiotherapy-sj'
  | 'brachytherapy'
  | 'contour-review'
  | 'contour-task'
  | 'ct-queue'
  | 'plan-apply'
  | 'plan-design'
  | 'plan-doctor-approval'
  | 'plan-group-review'
  | 'plan-recheck'
  | 'plan-verify'
  | 'treatment-appointment';

export type WorkflowStepTone = 'current' | 'done' | 'pending';

export interface HospitalWorkflowStep {
  actorText?: string;
  desc: string;
  key: string;
  reasonText?: string;
  statusText?: string;
  timeText?: string;
  title: string;
  tone: WorkflowStepTone;
}

const fallbackWorkflowMap: Record<HospitalWorkflowKey, string[]> = {
  'radiotherapy-gz': [
    '诊疗建档',
    'CT预约',
    'CT定位',
    '定位执行',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '组长审核',
    '医师核准',
    '计划复核',
    '计划验证',
    '治疗预约',
    '排队签到',
    '叫号入室',
    '治疗执行',
    '治疗小结',
  ],
  'radiotherapy-px': [
    '诊疗建档',
    '影像采集',
    '定位执行',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '剂量设计',
    '计划审核',
    '计划验证',
    '治疗执行',
    '疗程评估',
  ],
  'radiotherapy-sj': [
    '诊疗建档',
    '定位申请',
    'CT定位',
    '影像融合',
    '靶区勾画',
    '勾画审核',
    '计划设计',
    '医师核准',
    '计划验证',
    '治疗执行',
    '治疗小结',
  ],
  brachytherapy: [
    '诊疗建档',
    '术前评估',
    '后装申请',
    '后装定位',
    '通道勾画',
    '计划设计',
    '医师核准',
    '治疗执行',
    '治疗小结',
  ],
  'contour-review': [
    '诊疗建档',
    'CT定位',
    '定位执行',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '治疗执行',
    '计划验证',
  ],
  'contour-task': [
    '诊疗建档',
    'CT定位',
    '定位执行',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '治疗执行',
    '计划验证',
  ],
  'plan-apply': [
    '诊疗建档',
    'CT定位',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
  ],
  'plan-design': [
    '诊疗建档',
    'CT定位',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '组长审核',
  ],
  'plan-group-review': [
    '诊疗建档',
    'CT定位',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '组长审核',
    '医师核准',
  ],
  'plan-doctor-approval': [
    '诊疗建档',
    'CT定位',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '组长审核',
    '医师核准',
    '计划复核',
  ],
  'plan-recheck': [
    '诊疗建档',
    'CT定位',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '组长审核',
    '医师核准',
    '计划复核',
    '计划验证',
  ],
  'plan-verify': [
    '诊疗建档',
    'CT定位',
    '靶区勾画',
    '勾画审核',
    '计划申请',
    '计划设计',
    '组长审核',
    '医师核准',
    '计划复核',
    '计划验证',
    '治疗预约',
  ],
  'ct-queue': ['患者建档', 'CT预约', '排队取号', '叫号到检', '定位完成'],
  'treatment-appointment': [
    '计划验证',
    '治疗预约',
    '排队签到',
    '叫号入室',
    '治疗执行',
  ],
};

const workflowRoutePathMap: Record<HospitalWorkflowKey, string> = {
  'radiotherapy-gz': '/hospital-flow/hospital-contour/task',
  'radiotherapy-px': '/hospital-workflow/external-beam-px',
  'radiotherapy-sj': '/hospital-workflow/external-beam-sj',
  brachytherapy: '/hospital-workflow/brachytherapy',
  'contour-review': '/hospital-flow/hospital-contour/review',
  'contour-task': '/hospital-flow/hospital-contour/task',
  'plan-apply': '/hospital-flow/hospital-plan/apply',
  'plan-design': '/hospital-flow/hospital-plan/design',
  'plan-group-review': '/hospital-flow/hospital-plan/group-review',
  'plan-doctor-approval': '/hospital-flow/hospital-plan/doctor-approval',
  'plan-recheck': '/hospital-flow/hospital-plan/recheck',
  'plan-verify': '/hospital-flow/hospital-plan/verify',
  'ct-queue': '/hospital-flow/hospital-position/ct-queue',
  'treatment-appointment': '/hospital-flow/hospital-treatment/appointment',
};

const workflowModelKeyMap: Partial<Record<HospitalWorkflowKey, string>> = {
  'radiotherapy-gz': 'hospital_radiotherapy_external_beam_gz',
  'radiotherapy-px': 'hospital_radiotherapy_external_beam_px',
  'radiotherapy-sj': 'hospital_radiotherapy_external_beam_sj',
  brachytherapy: 'hospital_radiotherapy_brachytherapy',
};

const workflowCurrentNodeTitleMap: Record<HospitalWorkflowKey, string[]> = {
  'radiotherapy-gz': ['靶区勾画'],
  'radiotherapy-px': [],
  'radiotherapy-sj': [],
  brachytherapy: [],
  'contour-review': ['勾画审核', '审核确认'],
  'contour-task': ['靶区勾画', '勾画处理'],
  'plan-apply': ['计划申请', '申请受理'],
  'plan-design': ['计划设计'],
  'plan-group-review': ['组长审核'],
  'plan-doctor-approval': ['医师核准'],
  'plan-recheck': ['计划复核'],
  'plan-verify': ['计划验证'],
  'ct-queue': ['排队叫号', '叫号到检'],
  'treatment-appointment': ['治疗预约', '预约确认'],
};

const workflowNodeDescMap: Record<string, string> = {
  影像采集: '完成治疗前影像采集与资料归集',
  影像融合: '完成多模态影像融合与配准',
  定位申请: '发起定位申请并安排定位资源',
  术前评估: '完成治疗前评估并确认适应证',
  后装申请: '提交后装治疗申请并确认治疗方式',
  后装定位: '完成后装体位固定与定位数据采集',
  通道勾画: '完成后装通道与靶区范围勾画',
  剂量设计: '完成剂量优化与计划设计',
  计划审核: '完成计划参数审核与意见确认',
  疗程评估: '完成疗程效果评估与记录',
  患者建档: '完成患者基础信息建档并建立治疗档案',
  诊疗建档: '完成患者基础信息建档并建立治疗档案',
  CT预约: '登记 CT 预约并确认机房与时间',
  CT定位: '安排 CT 定位并确认扫描条件',
  定位执行: '完成体位固定、扫描与定位数据采集',
  排队取号: '生成排队序号并等待叫号',
  排队叫号: '按队列顺序完成排队与叫号',
  叫号到检: '患者按叫号进入检查或定位',
  定位完成: '定位完成并回传定位结果',
  靶区勾画: '医生完成靶区及危及器官勾画',
  勾画处理: '医生完成靶区及危及器官勾画',
  勾画审核: '审核医生确认勾画结果并给出意见',
  审核确认: '审核医生确认当前业务结果并给出意见',
  计划申请: '提交物理计划制作申请',
  申请受理: '接收计划制作申请并安排处理',
  计划设计: '物理师制定治疗计划与剂量分布',
  组长审核: '组长复核计划设计参数与结果',
  医师核准: '主管医师核准计划执行方案',
  计划复核: '复核剂量参数与执行条件',
  计划验证: '治疗前完成计划验证与记录',
  治疗预约: '安排患者治疗时间与机房资源',
  预约确认: '确认预约信息并通知执行',
  排队签到: '患者签到并进入候诊队列',
  叫号入室: '按叫号进入治疗机房',
  治疗执行: '按批准计划实施治疗',
  治疗小结: '完成治疗过程总结并归档本次业务记录',
};

const workflowModelStepsCache = new Map<
  string,
  Promise<HospitalWorkflowStep[]>
>();

function normalizeRoutePath(path?: string) {
  if (!path) return '';
  const normalized = path.split('?')[0]?.trim() || '';
  if (!normalized) return '';
  return normalized.length > 1 ? normalized.replace(/\/+$/, '') : normalized;
}

function normalizeText(value?: string) {
  return value?.replace(/\s+/g, '').trim() || '';
}

function formatStepTime(value?: Date | number | string) {
  if (!value) return undefined;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return undefined;
  const pad = (num: number) => `${num}`.padStart(2, '0');
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function getNodeStatusText(status?: number) {
  switch (status) {
    case -2:
      return '已跳过';
    case -1:
      return '未开始';
    case 0:
      return '待处理';
    case 1:
      return '处理中';
    case 2:
      return '已完成';
    case 3:
      return '已退回';
    case 4:
      return '已取消';
    case 5:
      return '已退回';
    case 7:
      return '处理中';
    default:
      return undefined;
  }
}

function getToneByStatus(status?: number): WorkflowStepTone {
  if (status === 0 || status === 1) return 'current';
  if (typeof status === 'number' && status > 1) return 'done';
  return 'pending';
}

function getModelTone(index: number, currentIndex: number): WorkflowStepTone {
  if (currentIndex < 0) return 'pending';
  if (index < currentIndex) return 'done';
  if (index === currentIndex) return 'current';
  return 'pending';
}

function inferNodeDesc(title?: string, showText?: string) {
  const normalizedTitle = title?.trim() || '';
  if (showText && !/请(配置|设置)/.test(showText)) {
    return showText;
  }
  if (normalizedTitle && workflowNodeDescMap[normalizedTitle]) {
    return workflowNodeDescMap[normalizedTitle];
  }
  if (!normalizedTitle) {
    return '待流转到当前节点';
  }
  if (normalizedTitle.includes('审核')) {
    return `完成${normalizedTitle}并记录结果`;
  }
  if (normalizedTitle.includes('预约')) {
    return `完成${normalizedTitle}并确认时间安排`;
  }
  if (normalizedTitle.includes('验证')) {
    return `完成${normalizedTitle}并输出验证记录`;
  }
  if (normalizedTitle.includes('设计')) {
    return `完成${normalizedTitle}并输出执行方案`;
  }
  return `按流程完成${normalizedTitle}`;
}

function buildNodeDesc(
  node: BpmProcessInstanceApi.ApprovalNodeInfo,
  fallbackDesc?: string,
) {
  return fallbackDesc || inferNodeDesc(node.name);
}

function buildNodeActorText(node: BpmProcessInstanceApi.ApprovalNodeInfo) {
  const taskUsers = Array.from(
    new Set(
      (node.tasks || [])
        .map((task) => task.assigneeUser?.nickname || task.ownerUser?.nickname)
        .filter(Boolean),
    ),
  );
  if (taskUsers.length > 0) {
    return `处理人：${taskUsers.join(' / ')}`;
  }
  const candidateUsers = Array.from(
    new Set(
      (node.candidateUsers || []).map((user) => user.nickname).filter(Boolean),
    ),
  );
  if (candidateUsers.length > 0) {
    return `待处理：${candidateUsers.join(' / ')}`;
  }
  return undefined;
}

function buildNodeReasonText(node: BpmProcessInstanceApi.ApprovalNodeInfo) {
  const reason = (node.tasks || [])
    .map((task) => task.reason?.trim())
    .find(Boolean);
  return reason ? `意见：${reason}` : undefined;
}

function buildNodeTimeText(node: BpmProcessInstanceApi.ApprovalNodeInfo) {
  if (node.endTime) return `完成：${formatStepTime(node.endTime)}`;
  if (node.startTime) return `开始：${formatStepTime(node.startTime)}`;
  return undefined;
}

function isVisibleWorkflowNode(node?: BpmProcessInstanceApi.ApprovalNodeInfo) {
  const name = node?.name?.trim();
  return !!name && !['发起人', '开始', '结束'].includes(name);
}

function isMatchedWorkflowStep(
  step: HospitalWorkflowStep,
  node: BpmProcessInstanceApi.ApprovalNodeInfo,
) {
  const stepKey = normalizeText(step.key);
  const stepTitle = normalizeText(step.title);
  const nodeId = normalizeText(node.id);
  const nodeName = normalizeText(node.name);
  if (stepKey && nodeId && stepKey === nodeId) {
    return true;
  }
  if (!stepTitle || !nodeName) {
    return false;
  }
  return (
    stepTitle === nodeName ||
    stepTitle.includes(nodeName) ||
    nodeName.includes(stepTitle)
  );
}

function buildWorkflowStepFromApprovalNode(
  key: HospitalWorkflowKey,
  node: BpmProcessInstanceApi.ApprovalNodeInfo,
  index: number,
  fallbackStep?: HospitalWorkflowStep,
): HospitalWorkflowStep {
  return {
    actorText: buildNodeActorText(node),
    desc: buildNodeDesc(node, fallbackStep?.desc),
    key: fallbackStep?.key || node.id || `${key}-${index}`,
    reasonText: buildNodeReasonText(node),
    statusText: getNodeStatusText(node.status),
    timeText: buildNodeTimeText(node),
    title: fallbackStep?.title || node.name,
    tone: getToneByStatus(node.status),
  };
}

function resolveCurrentIndex(key: HospitalWorkflowKey, titles: string[]) {
  const currentTitles = workflowCurrentNodeTitleMap[key] || [];
  if (currentTitles.length === 0 || titles.length === 0) return -1;
  return titles.findIndex((title) =>
    currentTitles.some(
      (currentTitle) =>
        title.includes(currentTitle) || currentTitle.includes(title),
    ),
  );
}

function buildFallbackWorkflowSteps(
  key: HospitalWorkflowKey,
): HospitalWorkflowStep[] {
  const titles = fallbackWorkflowMap[key];
  const currentIndex = resolveCurrentIndex(key, titles);
  return titles.map((title, index) => ({
    desc: inferNodeDesc(title),
    key: `${key}-${index}`,
    title,
    tone: getModelTone(index, currentIndex),
  }));
}

function collectSimpleModelSteps(
  node?: SimpleFlowNode,
  steps: Array<{ id: string; name: string; showText?: string }> = [],
) {
  if (!node) return steps;
  const includeNodeTypes = new Set<number>([
    BpmNodeTypeEnum.CHILD_PROCESS_NODE,
    BpmNodeTypeEnum.COPY_TASK_NODE,
    BpmNodeTypeEnum.DELAY_TIMER_NODE,
    BpmNodeTypeEnum.TRANSACTOR_NODE,
    BpmNodeTypeEnum.TRIGGER_NODE,
    BpmNodeTypeEnum.USER_TASK_NODE,
  ]);
  if (
    includeNodeTypes.has(node.type) &&
    node.name &&
    !['发起人', '开始', '结束'].includes(node.name)
  ) {
    steps.push({ id: node.id, name: node.name, showText: node.showText });
  }
  node.conditionNodes?.forEach((conditionNode) => {
    collectSimpleModelSteps(conditionNode, steps);
  });
  if (node.childNode) {
    collectSimpleModelSteps(node.childNode, steps);
  }
  return steps;
}

function buildWorkflowStepsFromTitles(
  key: HospitalWorkflowKey,
  items: Array<{ id: string; name: string; showText?: string }>,
) {
  if (items.length === 0) {
    return buildFallbackWorkflowSteps(key);
  }
  const uniqueItems = items.filter((item, index, list) => {
    return (
      list.findIndex((candidate) => {
        return candidate.id === item.id || candidate.name === item.name;
      }) === index
    );
  });
  const currentIndex = resolveCurrentIndex(
    key,
    uniqueItems.map((item) => item.name),
  );
  return uniqueItems.map((item, index) => ({
    desc: inferNodeDesc(item.name, item.showText),
    key: item.id || `${key}-model-${index}`,
    title: item.name,
    tone: getModelTone(index, currentIndex),
  }));
}

function parseWorkflowStepsFromSimpleModel(
  key: HospitalWorkflowKey,
  simpleModel?: SimpleFlowNode,
) {
  return buildWorkflowStepsFromTitles(
    key,
    collectSimpleModelSteps(simpleModel),
  );
}

function parseWorkflowStepsFromBpmnXml(
  key: HospitalWorkflowKey,
  bpmnXml?: string,
) {
  if (!bpmnXml) return [];
  const items: Array<{ id: string; name: string }> = [];
  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(bpmnXml, 'text/xml');
    [...doc.querySelectorAll('*')]
      .filter((element) => {
        return [
          'callActivity',
          'receiveTask',
          'serviceTask',
          'task',
          'userTask',
        ].includes(element.localName);
      })
      .forEach((element) => {
        const name = element.getAttribute('name') || '';
        if (!name || ['发起人', '开始', '结束'].includes(name)) return;
        items.push({ id: element.getAttribute('id') || '', name });
      });
  } catch {
    const tagPattern =
      /<(?:\w+:)?(?:userTask|serviceTask|receiveTask|callActivity|task)\b([^>]*)>/g;
    for (const match of bpmnXml.matchAll(tagPattern)) {
      const attrs = match[2] || '';
      const name = attrs.match(/\bname="([^"]+)"/)?.[1] || '';
      if (!name || ['发起人', '开始', '结束'].includes(name)) continue;
      items.push({
        id: attrs.match(/\bid="([^"]+)"/)?.[1] || '',
        name,
      });
    }
  }
  return buildWorkflowStepsFromTitles(key, items);
}

async function getWorkflowModelByRoute(
  key: HospitalWorkflowKey,
  formPath: string,
) {
  const normalizedPath = normalizeRoutePath(formPath);
  const modelList = (await getModelList()) || [];
  const explicitKey = workflowModelKeyMap[key];
  const matchedModel = modelList.find((model) => {
    if (explicitKey && model.key === explicitKey) {
      return true;
    }
    return normalizeRoutePath(model.formCustomCreatePath) === normalizedPath;
  });
  if (!matchedModel?.id) {
    return undefined;
  }
  return (await getModel(String(matchedModel.id))) as BpmModelApi.Model & {
    simpleModel?: SimpleFlowNode;
  };
}

export function getWorkflowRoutePath(key: HospitalWorkflowKey) {
  return workflowRoutePathMap[key];
}

export function getWorkflowModelKey(key: HospitalWorkflowKey) {
  return workflowModelKeyMap[key];
}

export async function getWorkflowStepsFromModelPath(
  key: HospitalWorkflowKey,
  formPath: string,
): Promise<HospitalWorkflowStep[]> {
  const cacheKey = `${key}:${normalizeRoutePath(formPath)}`;
  let stepsPromise = workflowModelStepsCache.get(cacheKey);
  if (!stepsPromise) {
    stepsPromise = (async () => {
      try {
        const modelDetail = await getWorkflowModelByRoute(key, formPath);
        if (modelDetail?.simpleModel) {
          const steps = parseWorkflowStepsFromSimpleModel(
            key,
            modelDetail.simpleModel,
          );
          if (steps.length > 0) {
            return steps;
          }
        }
        if (modelDetail?.bpmnXml) {
          const steps = parseWorkflowStepsFromBpmnXml(key, modelDetail.bpmnXml);
          if (steps.length > 0) {
            return steps;
          }
        }
      } catch (error) {
        console.warn('[hospital-workflow] load bpm model failed', error);
      }
      return buildFallbackWorkflowSteps(key);
    })();
    workflowModelStepsCache.set(cacheKey, stepsPromise);
  }
  return stepsPromise;
}

export function buildWorkflowStepsFromApprovalNodes(
  key: HospitalWorkflowKey,
  nodes?: BpmProcessInstanceApi.ApprovalNodeInfo[],
  modelSteps?: HospitalWorkflowStep[],
): HospitalWorkflowStep[] {
  const validNodes = (nodes || []).filter((node) =>
    isVisibleWorkflowNode(node),
  );
  if (validNodes.length > 0) {
    if (modelSteps && modelSteps.length > 0) {
      const matchedIndexes = new Set<number>();
      const mergedSteps = modelSteps.map((step, index) => {
        const matchedIndex = validNodes.findIndex(
          (node, nodeIndex) =>
            !matchedIndexes.has(nodeIndex) && isMatchedWorkflowStep(step, node),
        );
        if (matchedIndex < 0) {
          return step;
        }
        const matchedNode = validNodes[matchedIndex];
        if (!matchedNode) {
          return step;
        }
        matchedIndexes.add(matchedIndex);
        return buildWorkflowStepFromApprovalNode(key, matchedNode, index, step);
      });
      const extraSteps = validNodes
        .filter((_, index) => !matchedIndexes.has(index))
        .map((node, index) =>
          buildWorkflowStepFromApprovalNode(key, node, index),
        );
      return extraSteps.length > 0
        ? [...mergedSteps, ...extraSteps]
        : mergedSteps;
    }
    return validNodes.map((node, index) =>
      buildWorkflowStepFromApprovalNode(key, node, index),
    );
  }
  return modelSteps && modelSteps.length > 0
    ? modelSteps
    : buildFallbackWorkflowSteps(key);
}
