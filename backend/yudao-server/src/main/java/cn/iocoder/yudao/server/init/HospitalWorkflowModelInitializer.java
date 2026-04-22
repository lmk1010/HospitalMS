package cn.iocoder.yudao.server.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.category.BpmCategorySaveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.service.definition.BpmCategoryService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.hospital.dal.dataobject.customform.CustomFormDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.customform.CustomFormMapper;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.MenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@Profile("local")
@Slf4j
public class HospitalWorkflowModelInitializer implements ApplicationRunner {

    private static final Long HOSPITAL_MENU_ROOT_ID = 22900L;
    private static final Long WORKFLOW_MENU_ID = 22510L;
    private static final Long CUSTOM_FORM_MENU_ID = 22550L;

    private static final List<Long> BPM_PERMISSION_MENU_IDS = Arrays.asList(
            1194L, 1195L, 1197L, 1198L, 1199L, 1215L, 1216L, 1217L, 1218L
    );

    private static final List<Long> CUSTOM_FORM_PERMISSION_MENU_IDS = Arrays.asList(
            CUSTOM_FORM_MENU_ID, 22551L, 22552L, 22553L, 22554L
    );

    private static final List<CategorySeed> CATEGORY_SEEDS = Arrays.asList(
            new CategorySeed("HOSPITAL_WORKFLOW", "放疗业务流程", 1),
            new CategorySeed("HOSPITAL_POSITION", "定位管理", 2),
            new CategorySeed("HOSPITAL_TREATMENT", "治疗管理", 3),
            new CategorySeed("HOSPITAL_FEE", "费用管理", 4)
    );

    private static final List<String> LEGACY_MODEL_KEYS = Arrays.asList(
            "hospital_ct_appointment", "hospital_ct_queue", "hospital_contour_task", "hospital_contour_review",
            "hospital_plan_apply", "hospital_plan_design", "hospital_plan_group_review", "hospital_plan_doctor_approval",
            "hospital_plan_recheck", "hospital_plan_verify", "hospital_treatment_appointment", "hospital_treatment_queue",
            "hospital_treatment_execute", "hospital_treatment_summary", "hospital_fee_record", "hospital_fee_settlement"
    );

    private static final List<String> LEGACY_CUSTOM_FORM_CODES = Arrays.asList(
            "CT_QUEUE_FORM", "CONTOUR_REVIEW_FORM", "PLAN_DESIGN_FORM"
    );

    private static final String DEFAULT_FORM_CONF =
            "{\"form\":{\"labelWidth\":\"110px\",\"labelPosition\":\"right\",\"size\":\"default\"}}";

    private static final List<ModelSeed> MODEL_SEEDS = Arrays.asList(
            new ModelSeed(HospitalWorkflowConstants.RADIOTHERAPY_GZ_PROCESS_KEY, "外照射业务流程（GZ）", "HOSPITAL_WORKFLOW", "/hospital-flow/hospital-contour/task",
                    HospitalWorkflowConstants.RADIOTHERAPY_GZ_TASKS),
            new ModelSeed("hospital_radiotherapy_external_beam_px", "外照射业务流程（PX）", "HOSPITAL_WORKFLOW", "/hospital-workflow/external-beam-px",
                    Arrays.asList("诊疗建档", "影像采集", "定位执行", "靶区勾画", "勾画审核", "计划申请", "剂量设计", "计划审核", "计划验证", "治疗执行", "疗程评估")),
            new ModelSeed("hospital_radiotherapy_external_beam_sj", "外照射业务流程（SJ）", "HOSPITAL_WORKFLOW", "/hospital-workflow/external-beam-sj",
                    Arrays.asList("诊疗建档", "定位申请", "CT定位", "影像融合", "靶区勾画", "勾画审核", "计划设计", "医师核准", "计划验证", "治疗执行", "治疗小结")),
            new ModelSeed("hospital_radiotherapy_brachytherapy", "后装治疗业务流程", "HOSPITAL_WORKFLOW", "/hospital-workflow/brachytherapy",
                    Arrays.asList("诊疗建档", "术前评估", "后装申请", "后装定位", "通道勾画", "计划设计", "医师核准", "治疗执行", "治疗小结"))
    );

    @Resource
    private TenantService tenantService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private PermissionService permissionService;
    @Resource
    private BpmCategoryService bpmCategoryService;
    @Resource
    private BpmModelService bpmModelService;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private CustomFormMapper customFormMapper;

    @Override
    public void run(ApplicationArguments args) {
        TenantUtils.executeIgnore(() -> {
            alignWorkflowMenu();
            List<Long> tenantIds = tenantService.getTenantIdList();
            if (CollUtil.isEmpty(tenantIds)) {
                tenantIds = Collections.singletonList(1L);
            }
            tenantIds.forEach(tenantId -> TenantUtils.execute(tenantId, () -> initTenantModels(tenantId)));
        });
    }

    private void alignWorkflowMenu() {
        MenuDO menu = menuMapper.selectById(WORKFLOW_MENU_ID);
        if (menu == null) {
            ensureCustomFormMenus(HOSPITAL_MENU_ROOT_ID);
            return;
        }
        boolean changed = false;
        if (!Objects.equals(menu.getName(), "流程管理")) {
            menu.setName("流程管理");
            changed = true;
        }
        if (!Objects.equals(menu.getPath(), "manage-list")) {
            menu.setPath("manage-list");
            changed = true;
        }
        if (!Objects.equals(menu.getComponent(), "hospital/flow/process-manage/index")) {
            menu.setComponent("hospital/flow/process-manage/index");
            changed = true;
        }
        if (!Objects.equals(menu.getComponentName(), "HospitalWorkflowManage")) {
            menu.setComponentName("HospitalWorkflowManage");
            changed = true;
        }
        if (changed) {
            menuMapper.updateById(menu);
            log.info("[HospitalWorkflowModelInitializer][流程管理菜单已对齐医院流程页入口]");
        }
        ensureCustomFormMenus(Objects.equals(menu.getParentId(), MenuDO.ID_ROOT) ? HOSPITAL_MENU_ROOT_ID : menu.getParentId());
    }

    private void ensureCustomFormMenus(Long parentMenuId) {
        ensureCustomFormMenu(parentMenuId);
        ensureCustomFormButton(22551L, "表单查询", "hospital:custom-form:query", 1);
        ensureCustomFormButton(22552L, "表单新增", "hospital:custom-form:create", 2);
        ensureCustomFormButton(22553L, "表单修改", "hospital:custom-form:update", 3);
        ensureCustomFormButton(22554L, "表单删除", "hospital:custom-form:delete", 4);
    }

    private void ensureCustomFormMenu(Long parentMenuId) {
        MenuDO menu = menuMapper.selectById(CUSTOM_FORM_MENU_ID);
        boolean created = menu == null;
        if (created) {
            menu = new MenuDO();
            menu.setId(CUSTOM_FORM_MENU_ID);
        }
        boolean changed = false;
        if (!Objects.equals(menu.getName(), "表单管理")) {
            menu.setName("表单管理");
            changed = true;
        }
        if (!Objects.equals(menu.getPermission(), "hospital:custom-form:query")) {
            menu.setPermission("hospital:custom-form:query");
            changed = true;
        }
        if (!Objects.equals(menu.getType(), MenuTypeEnum.MENU.getType())) {
            menu.setType(MenuTypeEnum.MENU.getType());
            changed = true;
        }
        if (!Objects.equals(menu.getSort(), 11)) {
            menu.setSort(11);
            changed = true;
        }
        if (!Objects.equals(menu.getParentId(), parentMenuId)) {
            menu.setParentId(parentMenuId);
            changed = true;
        }
        if (!Objects.equals(menu.getPath(), "form-manage")) {
            menu.setPath("form-manage");
            changed = true;
        }
        if (!Objects.equals(menu.getIcon(), "ant-design:form-outlined")) {
            menu.setIcon("ant-design:form-outlined");
            changed = true;
        }
        if (!Objects.equals(menu.getComponent(), "hospital/flow/form-manage/index")) {
            menu.setComponent("hospital/flow/form-manage/index");
            changed = true;
        }
        if (!Objects.equals(menu.getComponentName(), "HospitalCustomForm")) {
            menu.setComponentName("HospitalCustomForm");
            changed = true;
        }
        if (!Objects.equals(menu.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            menu.setStatus(CommonStatusEnum.ENABLE.getStatus());
            changed = true;
        }
        if (!Objects.equals(menu.getVisible(), Boolean.TRUE)) {
            menu.setVisible(Boolean.TRUE);
            changed = true;
        }
        if (!Objects.equals(menu.getKeepAlive(), Boolean.TRUE)) {
            menu.setKeepAlive(Boolean.TRUE);
            changed = true;
        }
        if (!Objects.equals(menu.getAlwaysShow(), Boolean.FALSE)) {
            menu.setAlwaysShow(Boolean.FALSE);
            changed = true;
        }
        if (created) {
            menuMapper.insert(menu);
            log.info("[HospitalWorkflowModelInitializer][创建医院表单管理菜单成功]");
        } else if (changed) {
            menuMapper.updateById(menu);
            log.info("[HospitalWorkflowModelInitializer][医院表单管理菜单已对齐]");
        }
    }

    private void ensureCustomFormButton(Long id, String name, String permission, Integer sort) {
        MenuDO menu = menuMapper.selectById(id);
        boolean created = menu == null;
        if (created) {
            menu = new MenuDO();
            menu.setId(id);
        }
        boolean changed = false;
        if (!Objects.equals(menu.getName(), name)) {
            menu.setName(name);
            changed = true;
        }
        if (!Objects.equals(menu.getPermission(), permission)) {
            menu.setPermission(permission);
            changed = true;
        }
        if (!Objects.equals(menu.getType(), MenuTypeEnum.BUTTON.getType())) {
            menu.setType(MenuTypeEnum.BUTTON.getType());
            changed = true;
        }
        if (!Objects.equals(menu.getSort(), sort)) {
            menu.setSort(sort);
            changed = true;
        }
        if (!Objects.equals(menu.getParentId(), CUSTOM_FORM_MENU_ID)) {
            menu.setParentId(CUSTOM_FORM_MENU_ID);
            changed = true;
        }
        if (menu.getPath() != null) {
            menu.setPath(null);
            changed = true;
        }
        if (menu.getIcon() != null) {
            menu.setIcon(null);
            changed = true;
        }
        if (menu.getComponent() != null) {
            menu.setComponent(null);
            changed = true;
        }
        if (menu.getComponentName() != null) {
            menu.setComponentName(null);
            changed = true;
        }
        if (!Objects.equals(menu.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
            menu.setStatus(CommonStatusEnum.ENABLE.getStatus());
            changed = true;
        }
        if (!Objects.equals(menu.getVisible(), Boolean.TRUE)) {
            menu.setVisible(Boolean.TRUE);
            changed = true;
        }
        if (!Objects.equals(menu.getKeepAlive(), Boolean.FALSE)) {
            menu.setKeepAlive(Boolean.FALSE);
            changed = true;
        }
        if (!Objects.equals(menu.getAlwaysShow(), Boolean.FALSE)) {
            menu.setAlwaysShow(Boolean.FALSE);
            changed = true;
        }
        if (created) {
            menuMapper.insert(menu);
        } else if (changed) {
            menuMapper.updateById(menu);
        }
    }

    private void ensureCategories() {
        Set<String> categoryCodes = new LinkedHashSet<>();
        for (CategorySeed seed : CATEGORY_SEEDS) {
            categoryCodes.add(seed.getCode());
        }
        Map<String, BpmCategoryDO> categoryMap = bpmCategoryService.getCategoryMap(categoryCodes);
        for (CategorySeed seed : CATEGORY_SEEDS) {
            String expectedDescription = seed.getName() + "相关流程";
            BpmCategoryDO category = categoryMap.get(seed.getCode());
            if (category == null) {
                BpmCategorySaveReqVO reqVO = new BpmCategorySaveReqVO();
                reqVO.setName(seed.getName());
                reqVO.setCode(seed.getCode());
                reqVO.setDescription(expectedDescription);
                reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
                reqVO.setSort(seed.getSort());
                bpmCategoryService.createCategory(reqVO);
                log.info("[HospitalWorkflowModelInitializer][创建流程分类({})成功]", seed.getName());
                continue;
            }
            if (Objects.equals(category.getName(), seed.getName())
                    && Objects.equals(category.getDescription(), expectedDescription)
                    && Objects.equals(category.getSort(), seed.getSort())
                    && Objects.equals(category.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                continue;
            }
            BpmCategorySaveReqVO reqVO = new BpmCategorySaveReqVO();
            reqVO.setId(category.getId());
            reqVO.setName(seed.getName());
            reqVO.setCode(seed.getCode());
            reqVO.setDescription(expectedDescription);
            reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
            reqVO.setSort(seed.getSort());
            bpmCategoryService.updateCategory(reqVO);
            log.info("[HospitalWorkflowModelInitializer][流程分类({})已对齐]", seed.getName());
        }
    }

    private void initTenantModels(Long tenantId) {
        ensureCategories();
        grantWorkflowPermissions();
        AdminUserDO managerUser = resolveManagerUser(tenantId);
        if (managerUser == null) {
            log.warn("[HospitalWorkflowModelInitializer][租户({}) 未找到可用管理员，跳过流程模型初始化]", tenantId);
            return;
        }
        cleanupLegacyModels(managerUser);
        for (ModelSeed seed : MODEL_SEEDS) {
            ensureModel(seed, managerUser);
        }
        ensureCustomForms();
    }

    private void cleanupLegacyModels(AdminUserDO managerUser) {
        for (String legacyKey : LEGACY_MODEL_KEYS) {
            Model legacyModel = repositoryService.createModelQuery()
                    .modelTenantId(String.valueOf(managerUser.getTenantId()))
                    .modelKey(legacyKey)
                    .singleResult();
            if (legacyModel == null) {
                continue;
            }
            bpmModelService.deleteModel(managerUser.getId(), legacyModel.getId());
            log.info("[HospitalWorkflowModelInitializer][租户({}) 已清理旧页面流程模型({})]", managerUser.getTenantId(), legacyKey);
        }
    }

    private void grantWorkflowPermissions() {
        List<RoleMenuDO> roleMenus = roleMenuMapper.selectListByMenuId(WORKFLOW_MENU_ID);
        if (CollUtil.isEmpty(roleMenus)) {
            return;
        }
        Set<Long> requiredMenuIds = new LinkedHashSet<>(BPM_PERMISSION_MENU_IDS);
        requiredMenuIds.addAll(CUSTOM_FORM_PERMISSION_MENU_IDS);
        for (RoleMenuDO roleMenu : roleMenus) {
            Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(roleMenu.getRoleId());
            if (menuIds.containsAll(requiredMenuIds)) {
                continue;
            }
            Set<Long> mergedMenuIds = new LinkedHashSet<>(menuIds);
            mergedMenuIds.addAll(requiredMenuIds);
            permissionService.assignRoleMenu(roleMenu.getRoleId(), mergedMenuIds);
            log.info("[HospitalWorkflowModelInitializer][角色({}) 已补齐 BPM 模型与医院表单权限]", roleMenu.getRoleId());
        }
    }

    private AdminUserDO resolveManagerUser(Long tenantId) {
        TenantDO tenant = tenantService.getTenant(tenantId);
        if (tenant != null && tenant.getContactUserId() != null) {
            AdminUserDO contactUser = adminUserService.getUser(tenant.getContactUserId());
            if (contactUser != null) {
                return contactUser;
            }
        }
        List<AdminUserDO> users = adminUserService.getUserListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return CollUtil.isEmpty(users) ? null : users.get(0);
    }

    private void ensureModel(ModelSeed seed, AdminUserDO managerUser) {
        Model model = repositoryService.createModelQuery()
                .modelTenantId(String.valueOf(managerUser.getTenantId()))
                .modelKey(seed.getKey())
                .singleResult();
        String modelId;
        boolean changed = false;
        if (model == null) {
            modelId = bpmModelService.createModel(buildModelSaveReq(seed, managerUser));
            changed = true;
            log.info("[HospitalWorkflowModelInitializer][租户({}) 创建流程模型({})成功]", managerUser.getTenantId(), seed.getName());
        } else {
            modelId = model.getId();
            changed = syncLegacyModel(model, seed, managerUser);
        }
        ensureModelDeployed(seed, managerUser, modelId, changed);
    }

    private boolean syncLegacyModel(Model model, ModelSeed seed, AdminUserDO managerUser) {
        List<String> existingTaskNames = extractTaskNames(bpmModelService.getSimpleModel(model.getId()));
        if (CollUtil.isNotEmpty(existingTaskNames) && existingTaskNames.size() > 1) {
            return false;
        }
        if (Objects.equals(existingTaskNames, seed.getTaskNames())) {
            return false;
        }
        BpmModelSaveReqVO reqVO = buildModelSaveReq(seed, managerUser);
        reqVO.setId(model.getId());
        bpmModelService.updateModel(managerUser.getId(), reqVO);
        log.info("[HospitalWorkflowModelInitializer][租户({}) 同步默认流程模型({})步骤成功]", managerUser.getTenantId(), seed.getName());
        return true;
    }

    private void ensureModelDeployed(ModelSeed seed, AdminUserDO managerUser, String modelId, boolean forceDeploy) {
        long activeDefinitionCount = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(String.valueOf(managerUser.getTenantId()))
                .processDefinitionKey(seed.getKey())
                .active()
                .count();
        if (!forceDeploy && activeDefinitionCount > 0) {
            return;
        }
        if (!forceDeploy) {
            BpmModelSaveReqVO reqVO = buildModelSaveReq(seed, managerUser);
            reqVO.setId(modelId);
            bpmModelService.updateModel(managerUser.getId(), reqVO);
        }
        bpmModelService.deployModel(managerUser.getId(), modelId);
        log.info("[HospitalWorkflowModelInitializer][租户({}) 发布流程模型({})成功]", managerUser.getTenantId(), seed.getName());
    }

    private BpmModelSaveReqVO buildModelSaveReq(ModelSeed seed, AdminUserDO managerUser) {
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO();
        reqVO.setKey(seed.getKey());
        reqVO.setName(seed.getName());
        reqVO.setCategory(seed.getCategoryCode());
        reqVO.setDescription(seed.getName() + "医院流程模型");
        reqVO.setType(BpmModelTypeEnum.SIMPLE.getType());
        reqVO.setFormType(BpmModelFormTypeEnum.CUSTOM.getType());
        reqVO.setFormCustomCreatePath(seed.getFormPath());
        reqVO.setFormCustomViewPath(seed.getFormPath());
        reqVO.setVisible(Boolean.TRUE);
        reqVO.setManagerUserIds(Collections.singletonList(managerUser.getId()));
        reqVO.setAllowCancelRunningProcess(Boolean.TRUE);
        reqVO.setSimpleModel(buildSimpleModel(seed, managerUser));
        return reqVO;
    }

    private BpmSimpleModelNodeVO buildSimpleModel(ModelSeed seed, AdminUserDO managerUser) {
        BpmSimpleModelNodeVO nextNode = new BpmSimpleModelNodeVO();
        nextNode.setId(seed.getKey() + "_end");
        nextNode.setType(BpmSimpleModelNodeTypeEnum.END_NODE.getType());
        nextNode.setName("结束");

        for (int i = seed.getTaskNames().size() - 1; i >= 0; i--) {
            String taskName = seed.getTaskNames().get(i);
            BpmSimpleModelNodeVO approveNode = new BpmSimpleModelNodeVO();
            approveNode.setId(seed.getKey() + "_step_" + i);
            approveNode.setType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType());
            approveNode.setName(taskName);
            approveNode.setShowText(buildTaskDescription(taskName));
            approveNode.setApproveType(BpmUserTaskApproveTypeEnum.USER.getType());
            approveNode.setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy());
            approveNode.setCandidateParam(String.valueOf(managerUser.getId()));
            approveNode.setApproveMethod(BpmUserTaskApproveMethodEnum.RANDOM.getMethod());
            approveNode.setHospitalSetting(buildHospitalSetting(seed, i, taskName));
            approveNode.setChildNode(nextNode);
            nextNode = approveNode;
        }

        BpmSimpleModelNodeVO startUserNode = new BpmSimpleModelNodeVO();
        startUserNode.setId("StartUserNode");
        startUserNode.setType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType());
        startUserNode.setName("发起人");
        startUserNode.setShowText("全员可发起");
        startUserNode.setChildNode(nextNode);
        return startUserNode;
    }

    private List<String> extractTaskNames(BpmSimpleModelNodeVO node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<String> taskNames = new java.util.ArrayList<>();
        collectTaskNames(node, taskNames);
        return taskNames;
    }

    private void collectTaskNames(BpmSimpleModelNodeVO node, List<String> taskNames) {
        if (node == null) {
            return;
        }
        if (Objects.equals(node.getType(), BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                && StrUtil.isNotBlank(node.getName())) {
            taskNames.add(node.getName());
        }
        if (node.getConditionNodes() != null) {
            node.getConditionNodes().forEach(child -> collectTaskNames(child, taskNames));
        }
        collectTaskNames(node.getChildNode(), taskNames);
    }

    private void ensureCustomForms() {
        cleanupLegacyCustomForms();
        for (ModelSeed seed : MODEL_SEEDS) {
            for (int i = 0; i < seed.getTaskNames().size(); i++) {
                ensureCustomForm(seed, i, seed.getTaskNames().get(i));
            }
        }
    }

    private void cleanupLegacyCustomForms() {
        for (String code : LEGACY_CUSTOM_FORM_CODES) {
            CustomFormDO legacyForm = customFormMapper.selectByCode(code);
            if (legacyForm != null) {
                customFormMapper.deleteById(legacyForm.getId());
            }
        }
    }

    private void ensureCustomForm(ModelSeed seed, int stepIndex, String taskName) {
        NodeFormSeed formSeed = buildNodeFormSeed(seed, stepIndex, taskName);
        CustomFormDO customForm = customFormMapper.selectByCode(formSeed.getCode());
        boolean created = customForm == null;
        if (created) {
            customForm = new CustomFormDO();
        }
        customForm.setName(formSeed.getName());
        customForm.setCode(formSeed.getCode());
        customForm.setDeptId(null);
        customForm.setBizCategory(formSeed.getBizCategory());
        customForm.setProcessKey(seed.getKey());
        customForm.setProcessName(seed.getName());
        customForm.setNodeKey(seed.getKey() + "_step_" + stepIndex);
        customForm.setNodeName(taskName);
        customForm.setPageCode(formSeed.getPageCode());
        customForm.setPagePath(formSeed.getPagePath());
        customForm.setStatus(CommonStatusEnum.ENABLE.getStatus());
        customForm.setConf(DEFAULT_FORM_CONF);
        customForm.setFields(buildFieldsForTask(taskName));
        customForm.setRemark("系统预置节点表单");
        if (created) {
            customFormMapper.insert(customForm);
            return;
        }
        customFormMapper.updateById(customForm);
    }

    private NodeFormSeed buildNodeFormSeed(ModelSeed seed, int stepIndex, String taskName) {
        PageSeed pageSeed = resolvePageSeed(seed, taskName);
        return new NodeFormSeed(
                seed.getName() + "-" + taskName + "表单",
                seed.getKey() + "_step_" + stepIndex + "_form",
                pageSeed.getBizCategory(),
                pageSeed.getPageCode(),
                pageSeed.getPagePath());
    }

    private PageSeed resolvePageSeed(ModelSeed seed, String taskName) {
        switch (taskName) {
            case "患者建档":
            case "诊疗建档":
                return new PageSeed("综合业务", "medical-record", seed.getFormPath());
            case "CT预约":
                return new PageSeed("定位管理", "ct-appointment", "/hospital-flow/hospital-position/ct-appointment");
            case "影像采集":
                return new PageSeed("定位管理", "image-collect", seed.getFormPath());
            case "定位申请":
                return new PageSeed("定位管理", "position-apply", seed.getFormPath());
            case "CT定位":
            case "定位执行":
            case "排队取号":
            case "排队叫号":
            case "叫号到检":
            case "定位完成":
                return new PageSeed("定位管理", "ct-queue", "/hospital-flow/hospital-position/ct-queue");
            case "术前评估":
                return new PageSeed("综合业务", "preoperative-evaluate", seed.getFormPath());
            case "后装申请":
                return new PageSeed("综合业务", "brachy-apply", seed.getFormPath());
            case "后装定位":
                return new PageSeed("定位管理", "brachy-position", seed.getFormPath());
            case "影像融合":
                return new PageSeed("勾画管理", "image-fusion", seed.getFormPath());
            case "靶区勾画":
            case "通道勾画":
                return new PageSeed("勾画管理", "contour-task", "/hospital-flow/hospital-contour/task");
            case "勾画审核":
            case "审核确认":
                return new PageSeed("勾画管理", "contour-review", "/hospital-flow/hospital-contour/review");
            case "计划申请":
            case "申请受理":
                return new PageSeed("计划管理", "plan-apply", "/hospital-flow/hospital-plan/apply");
            case "剂量设计":
            case "计划设计":
                return new PageSeed("计划管理", "plan-design", "/hospital-flow/hospital-plan/design");
            case "计划审核":
            case "组长审核":
                return new PageSeed("计划管理", "plan-group-review", "/hospital-flow/hospital-plan/group-review");
            case "医师核准":
                return new PageSeed("计划管理", "plan-doctor-approval", "/hospital-flow/hospital-plan/doctor-approval");
            case "计划复核":
                return new PageSeed("计划管理", "plan-recheck", "/hospital-flow/hospital-plan/recheck");
            case "计划验证":
                return new PageSeed("计划管理", "plan-verify", "/hospital-flow/hospital-plan/verify");
            case "治疗预约":
            case "预约确认":
                return new PageSeed("治疗管理", "treatment-appointment", "/hospital-flow/hospital-treatment/appointment");
            case "排队签到":
            case "叫号入室":
                return new PageSeed("治疗管理", "treatment-queue", "/hospital-flow/hospital-treatment/queue");
            case "治疗执行":
                return new PageSeed("治疗管理", "treatment-execute", "/hospital-flow/hospital-treatment/execute");
            case "治疗小结":
            case "小结归档":
                return new PageSeed("治疗管理", "treatment-summary", "/hospital-flow/hospital-treatment/summary");
            case "疗程评估":
                return new PageSeed("治疗管理", "course-evaluate", "/hospital-flow/hospital-treatment/summary");
            case "费用登记":
            case "费用确认":
                return new PageSeed("费用管理", "fee-record", "/hospital-flow/hospital-fee/record");
            case "费用结算":
                return new PageSeed("费用管理", "fee-settlement", "/hospital-flow/hospital-fee/settlement");
            default:
                return new PageSeed("综合业务", "medical-record", seed.getFormPath());
        }
    }

    private BpmSimpleModelNodeVO.HospitalSetting buildHospitalSetting(ModelSeed seed, int stepIndex, String taskName) {
        PageSeed pageSeed = resolvePageSeed(seed, taskName);
        BpmSimpleModelNodeVO.HospitalSetting setting = new BpmSimpleModelNodeVO.HospitalSetting();
        setting.setBizCategory(pageSeed.getBizCategory());
        setting.setPageCode(pageSeed.getPageCode());
        setting.setPagePath(pageSeed.getPagePath());
        setting.setFlowRecordPageCode(pageSeed.getPageCode());
        setting.setFunctionSystemCode(pageSeed.getBizCategory());
        setting.setPreviousNodeId(stepIndex > 0 ? seed.getKey() + "_step_" + (stepIndex - 1) : "StartUserNode");
        setting.setAutoSkip(Boolean.FALSE);
        setting.setEndNode(Boolean.FALSE);
        setting.setPrintSubmitterSignature(Boolean.TRUE);
        setting.setActionButtonIds(new ArrayList<>());
        setting.setSecondaryConfirmButtonIds(new ArrayList<>());
        setting.setButtonRoleIds(new ArrayList<>());
        setting.setEditRoleIds(new ArrayList<>());
        setting.setDocumentFormIds(new ArrayList<>());
        setting.setTimeoutRules(new ArrayList<>());
        return setting;
    }

    private List<String> buildFieldsForTask(String taskName) {
        List<String> fields = new ArrayList<>();
        fields.add(buildInputField("patientName", "患者姓名", "请输入患者姓名"));
        fields.add(buildInputField("patientNo", "患者编号", "请输入患者编号"));
        if ("患者建档".equals(taskName) || "诊疗建档".equals(taskName)) {
            fields.add(buildInputField("diagnosis", "临床诊断", "请输入临床诊断"));
            fields.add(buildTextareaField("archiveRemark", "建档备注", "请输入建档备注"));
            return fields;
        }
        if (taskName.contains("评估")) {
            fields.add(buildInputField("evaluateDoctor", "评估人", "请输入评估人"));
            fields.add(buildSelectField("evaluateLevel", "评估等级", new String[][]{{"优", "GOOD"}, {"良", "NORMAL"}, {"待补充", "PENDING"}}));
            fields.add(buildTextareaField("evaluateRemark", taskName + "备注", "请输入" + taskName + "备注"));
            return fields;
        }
        if (taskName.contains("审核") || taskName.contains("核准") || taskName.contains("复核") || taskName.contains("验证")) {
            fields.add(buildInputField("handlerName", "处理人", "请输入处理人"));
            fields.add(buildRadioField("handleResult", "处理结果", new String[][]{{"通过", "PASS"}, {"退回", "REJECT"}}));
            fields.add(buildTextareaField("handleOpinion", "处理意见", "请输入处理意见"));
            return fields;
        }
        if (taskName.contains("勾画")) {
            fields.add(buildInputField("contourDoctor", "勾画医生", "请输入勾画医生"));
            fields.add(buildInputField("targetSite", "靶区部位", "请输入靶区部位"));
            fields.add(buildTextareaField("contourRemark", taskName + "说明", "请输入" + taskName + "说明"));
            return fields;
        }
        if (taskName.contains("计划") || taskName.contains("剂量")) {
            fields.add(buildInputField("planName", "计划名称", "请输入计划名称"));
            fields.add(buildNumberField("totalDose", "总剂量(Gy)"));
            fields.add(buildTextareaField("planRemark", taskName + "说明", "请输入" + taskName + "说明"));
            return fields;
        }
        if (taskName.contains("治疗") || taskName.contains("预约") || taskName.contains("排队") || taskName.contains("叫号")) {
            fields.add(buildDateField("scheduleTime", "计划时间"));
            fields.add(buildInputField("executePosition", "机房/队列", "请输入机房或队列信息"));
            fields.add(buildTextareaField("treatmentRemark", taskName + "备注", "请输入" + taskName + "备注"));
            return fields;
        }
        if (taskName.contains("费用")) {
            fields.add(buildInputField("feeItem", "费用项目", "请输入费用项目"));
            fields.add(buildNumberField("amount", "金额(元)"));
            fields.add(buildTextareaField("feeRemark", taskName + "备注", "请输入" + taskName + "备注"));
            return fields;
        }
        if (taskName.contains("定位") || taskName.contains("CT") || taskName.contains("影像")) {
            fields.add(buildDateField("appointTime", "安排时间"));
            fields.add(buildInputField("resourceName", "机房/设备", "请输入机房或设备"));
            fields.add(buildTextareaField("positionRemark", taskName + "备注", "请输入" + taskName + "备注"));
            return fields;
        }
        fields.add(buildInputField("handlerName", "责任人", "请输入责任人"));
        fields.add(buildInputField("taskContent", taskName + "内容", "请输入" + taskName + "内容"));
        fields.add(buildTextareaField("taskRemark", taskName + "备注", "请输入" + taskName + "备注"));
        return fields;
    }

    private String buildInputField(String field, String title, String placeholder) {
        return "{\"type\":\"input\",\"field\":\"" + field + "\",\"title\":\"" + title
                + "\",\"props\":{\"placeholder\":\"" + placeholder + "\"}}";
    }

    private String buildTextareaField(String field, String title, String placeholder) {
        return "{\"type\":\"textarea\",\"field\":\"" + field + "\",\"title\":\"" + title
                + "\",\"props\":{\"rows\":4,\"placeholder\":\"" + placeholder + "\"}}";
    }

    private String buildNumberField(String field, String title) {
        return "{\"type\":\"inputNumber\",\"field\":\"" + field + "\",\"title\":\"" + title
                + "\",\"props\":{\"min\":0,\"precision\":2}}";
    }

    private String buildDateField(String field, String title) {
        return "{\"type\":\"datePicker\",\"field\":\"" + field + "\",\"title\":\"" + title + "\"}";
    }

    private String buildRadioField(String field, String title, String[][] options) {
        return "{\"type\":\"radio\",\"field\":\"" + field + "\",\"title\":\"" + title
                + "\",\"options\":" + buildOptionsJson(options) + "}";
    }

    private String buildSelectField(String field, String title, String[][] options) {
        return "{\"type\":\"select\",\"field\":\"" + field + "\",\"title\":\"" + title
                + "\",\"options\":" + buildOptionsJson(options) + "}";
    }

    private String buildOptionsJson(String[][] options) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < options.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append("{\"label\":\"")
                    .append(options[i][0])
                    .append("\",\"value\":\"")
                    .append(options[i][1])
                    .append("\"}");
        }
        builder.append(']');
        return builder.toString();
    }

    private String buildTaskDescription(String taskName) {
        switch (taskName) {
            case "患者建档":
            case "诊疗建档":
                return "完成患者基础信息建档并建立治疗档案";
            case "CT预约":
                return "登记 CT 预约并确认机房与时间";
            case "CT定位":
                return "安排 CT 定位并确认扫描条件";
            case "定位执行":
                return "完成体位固定、扫描与定位数据采集";
            case "排队取号":
                return "生成排队序号并等待叫号";
            case "排队叫号":
                return "按队列顺序完成排队与叫号";
            case "叫号到检":
                return "患者按叫号进入检查或定位";
            case "定位完成":
                return "定位完成并回传定位结果";
            case "靶区勾画":
            case "勾画处理":
                return "医生完成靶区及危及器官勾画";
            case "勾画审核":
            case "审核确认":
                return "审核医生确认当前结果并给出意见";
            case "计划申请":
            case "申请受理":
                return "提交物理计划制作申请并安排处理";
            case "计划设计":
                return "物理师制定治疗计划与剂量分布";
            case "组长审核":
                return "组长复核计划设计参数与结果";
            case "医师核准":
                return "主管医师核准计划执行方案";
            case "计划复核":
                return "复核剂量参数与执行条件";
            case "计划验证":
                return "治疗前完成计划验证与记录";
            case "治疗预约":
            case "预约确认":
                return "安排患者治疗时间与机房资源";
            case "排队签到":
                return "患者签到并进入候诊队列";
            case "叫号入室":
                return "按叫号进入治疗机房";
            case "治疗执行":
                return "按批准计划实施治疗";
            case "治疗小结":
            case "小结归档":
                return "治疗完成后归档并形成小结";
            case "费用登记":
            case "费用确认":
                return "登记费用项目并确认收费信息";
            case "费用结算":
                return "完成费用结算与收款确认";
            default:
                return taskName + "处理";
        }
    }

    @Getter
    @AllArgsConstructor
    private static class CategorySeed {
        private final String code;
        private final String name;
        private final Integer sort;
    }

    @Getter
    @AllArgsConstructor
    private static class ModelSeed {
        private final String key;
        private final String name;
        private final String categoryCode;
        private final String formPath;
        private final List<String> taskNames;
    }

    @Getter
    @AllArgsConstructor
    private static class PageSeed {
        private final String bizCategory;
        private final String pageCode;
        private final String pagePath;
    }

    @Getter
    @AllArgsConstructor
    private static class NodeFormSeed {
        private final String name;
        private final String code;
        private final String bizCategory;
        private final String pageCode;
        private final String pagePath;
    }
}

