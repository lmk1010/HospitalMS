package cn.iocoder.yudao.module.hospital.service.plandesign;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.planapply.PlanApplyMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.plandesign.PlanDesignMapper;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import cn.iocoder.yudao.module.hospital.service.planreview.PlanReviewService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_APPLY_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_DESIGN_APPLY_STATUS_INVALID;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_DESIGN_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_DESIGN_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_DESIGN_SUBMIT_FAIL_STATUS;

@Service
@Validated
public class PlanDesignServiceImpl implements PlanDesignService {

    private static final String DEFAULT_VERSION_NO = "V1";
    private static final String WORKFLOW_STATUS_DRAFT = "DRAFT";
    private static final String WORKFLOW_STATUS_REJECTED = "REJECTED";
    private static final String WORKFLOW_STATUS_REVIEWING = "REVIEWING";
    private static final String CURRENT_NODE_PLAN_DESIGN = "PLAN_DESIGN";

    @Resource
    private PlanDesignMapper planDesignMapper;
    @Resource
    private PlanApplyMapper planApplyMapper;
    @Resource
    private DoctorService doctorService;
    @Resource
    private PlanReviewService planReviewService;
    @Resource
    private BpmTaskService bpmTaskService;
    @Resource
    private org.flowable.engine.TaskService flowableTaskService;
    @Resource
    private PatientService patientService;

    @Override
    public Long createPlanDesign(PlanDesignSaveReqVO createReqVO) {
        String bizNo = prepareBizNo(null, createReqVO.getBizNo());
        PlanApplyDO planApply = validateCreateOrUpdate(null, bizNo, createReqVO);
        DoctorDO doctor = doctorService.getDoctor(createReqVO.getDesignUserId());
        PlanDesignDO design = BeanUtils.toBean(createReqVO, PlanDesignDO.class);
        design.setBizNo(bizNo);
        design.setPatientId(planApply.getPatientId());
        design.setPatientName(planApply.getPatientName());
        design.setDesignUserName(doctor.getName());
        design.setProcessInstanceId(StrUtil.blankToDefault(planApply.getProcessInstanceId(), ""));
        normalizeDesign(design, planApply);
        planDesignMapper.insert(design);
        return design.getId();
    }

    @Override
    public void updatePlanDesign(PlanDesignSaveReqVO updateReqVO) {
        PlanDesignDO existing = validatePlanDesignExists(updateReqVO.getId());
        String bizNo = prepareBizNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getBizNo(), existing.getBizNo()));
        PlanApplyDO planApply = validateCreateOrUpdate(updateReqVO.getId(), bizNo, updateReqVO);
        DoctorDO doctor = doctorService.getDoctor(updateReqVO.getDesignUserId());
        PlanDesignDO design = BeanUtils.toBean(updateReqVO, PlanDesignDO.class);
        design.setBizNo(bizNo);
        design.setPatientId(planApply.getPatientId());
        design.setPatientName(planApply.getPatientName());
        design.setDesignUserName(doctor.getName());
        design.setDesignTime(existing.getDesignTime());
        design.setSubmitTime(existing.getSubmitTime());
        design.setWorkflowStatus(existing.getWorkflowStatus());
        design.setProcessInstanceId(StrUtil.blankToDefault(existing.getProcessInstanceId(), planApply.getProcessInstanceId()));
        normalizeDesign(design, planApply);
        planDesignMapper.updateById(design);
    }

    @Override
    public void createFromPlanApply(PlanApplyDO planApply) {
        if (planApply == null || planApply.getId() == null) {
            return;
        }
        if (planDesignMapper.selectByPlanApplyId(planApply.getId()) != null) {
            return;
        }
        Long designUserId = resolveAutoDesignUserId(planApply);
        DoctorDO designUser = designUserId != null && designUserId > 0
                ? doctorService.getDoctor(designUserId)
                : null;

        PlanDesignDO design = new PlanDesignDO();
        design.setBizNo(prepareBizNo(null, null));
        design.setPlanApplyId(planApply.getId());
        design.setPatientId(planApply.getPatientId());
        design.setPatientName(planApply.getPatientName());
        design.setDesignUserId(designUser == null ? 0L : designUser.getId());
        design.setDesignUserName(designUser == null ? "" : designUser.getName());
        design.setPlanName(StrUtil.blankToDefault(planApply.getPatientName(), "") + "计划");
        design.setVersionNo(DEFAULT_VERSION_NO);
        design.setTotalDose(planApply.getPrescriptionDose());
        design.setFractions(planApply.getTotalFractions());
        design.setWorkflowStatus(WORKFLOW_STATUS_DRAFT);
        design.setProcessInstanceId(StrUtil.blankToDefault(planApply.getProcessInstanceId(), ""));
        design.setStatus(CommonStatusEnum.ENABLE.getStatus());
        design.setRemark(StrUtil.blankToDefault(planApply.getRemark(), ""));
        normalizeDesign(design, planApply);
        planDesignMapper.insert(design);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPlanDesign(Long id) {
        PlanDesignDO existing = validatePlanDesignExists(id);
        if (!Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_DRAFT)
                && !Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_REJECTED)) {
            throw exception(PLAN_DESIGN_SUBMIT_FAIL_STATUS);
        }
        String processInstanceId = resolveProcessInstanceId(existing);
        syncPlanDesignBpm(existing, processInstanceId, resolveSubmitUserId(existing));

        PlanDesignDO updateObj = new PlanDesignDO();
        updateObj.setId(id);
        updateObj.setSubmitTime(LocalDateTime.now());
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_REVIEWING);
        updateObj.setProcessInstanceId(processInstanceId);
        planDesignMapper.updateById(updateObj);

        PlanApplyDO applyUpdate = new PlanApplyDO();
        applyUpdate.setId(existing.getPlanApplyId());
        applyUpdate.setWorkflowStatus("APPROVED");
        planApplyMapper.updateById(applyUpdate);

        existing.setWorkflowStatus(WORKFLOW_STATUS_REVIEWING);
        existing.setProcessInstanceId(processInstanceId);
        planReviewService.createGroupLeaderReviewFromDesign(existing);
    }

    @Override
    public void deletePlanDesign(Long id) {
        validatePlanDesignExists(id);
        planDesignMapper.deleteById(id);
    }

    @Override
    public PlanDesignDO getPlanDesign(Long id) {
        return planDesignMapper.selectById(id);
    }

    @Override
    public PageResult<PlanDesignDO> getPlanDesignPage(PlanDesignPageReqVO reqVO) {
        List<PlanDesignDO> records = planDesignMapper.selectListByReqVO(reqVO);
        if (records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        Set<Long> patientIds = records.stream()
                .map(PlanDesignDO::getPatientId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> currentNodeCodeMap = patientService.getCurrentNodeCodeMap(patientIds);
        List<PlanDesignDO> filtered = records.stream()
                .filter(item -> Objects.equals(CURRENT_NODE_PLAN_DESIGN, currentNodeCodeMap.get(item.getPatientId())))
                .collect(Collectors.toList());
        return buildPageResult(filtered, reqVO);
    }

    private PageResult<PlanDesignDO> buildPageResult(List<PlanDesignDO> rows, PlanDesignPageReqVO reqVO) {
        long total = rows.size();
        int fromIndex = Math.min((Math.max(reqVO.getPageNo(), 1) - 1) * Math.max(reqVO.getPageSize(), 1), rows.size());
        int toIndex = Math.min(fromIndex + Math.max(reqVO.getPageSize(), 1), rows.size());
        if (fromIndex >= toIndex) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        return new PageResult<>(new ArrayList<>(rows.subList(fromIndex, toIndex)), total);
    }

    private PlanApplyDO validateCreateOrUpdate(Long id, String bizNo, PlanDesignSaveReqVO reqVO) {
        if (id != null) {
            validatePlanDesignExists(id);
        }
        doctorService.validateDoctorExists(reqVO.getDesignUserId());
        PlanApplyDO planApply = planApplyMapper.selectById(reqVO.getPlanApplyId());
        if (planApply == null) {
            throw exception(PLAN_APPLY_NOT_EXISTS);
        }
        if (!Objects.equals(planApply.getWorkflowStatus(), "REVIEWING")
                && !Objects.equals(planApply.getWorkflowStatus(), "APPROVED")) {
            throw exception(PLAN_DESIGN_APPLY_STATUS_INVALID);
        }
        validateBizNoUnique(id, bizNo);
        return planApply;
    }

    private PlanDesignDO validatePlanDesignExists(Long id) {
        PlanDesignDO design = planDesignMapper.selectById(id);
        if (design == null) {
            throw exception(PLAN_DESIGN_NOT_EXISTS);
        }
        return design;
    }

    private void validateBizNoUnique(Long id, String bizNo) {
        PlanDesignDO design = planDesignMapper.selectByBizNo(bizNo);
        if (design == null) {
            return;
        }
        if (!Objects.equals(design.getId(), id)) {
            throw exception(PLAN_DESIGN_BIZ_NO_DUPLICATE);
        }
    }

    private String prepareBizNo(Long id, String bizNo) {
        if (StrUtil.isNotBlank(bizNo)) {
            return bizNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "SJ" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            PlanDesignDO design = planDesignMapper.selectByBizNo(generated);
            if (design == null || Objects.equals(design.getId(), id)) {
                return generated;
            }
        }
        return "SJ" + System.currentTimeMillis();
    }

    private Long resolveAutoDesignUserId(PlanApplyDO planApply) {
        if (planApply.getApplyDoctorId() != null && planApply.getApplyDoctorId() > 0) {
            return planApply.getApplyDoctorId();
        }
        List<DoctorDO> doctors = doctorService.getDoctorSimpleList();
        return doctors.isEmpty() ? 0L : doctors.get(0).getId();
    }

    private String resolveProcessInstanceId(PlanDesignDO design) {
        if (StrUtil.isNotBlank(design.getProcessInstanceId())) {
            return design.getProcessInstanceId();
        }
        PlanApplyDO planApply = planApplyMapper.selectById(design.getPlanApplyId());
        return planApply == null ? "" : StrUtil.blankToDefault(planApply.getProcessInstanceId(), "");
    }

    private Long resolveSubmitUserId(PlanDesignDO design) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        return loginUserId != null ? loginUserId : design.getDesignUserId();
    }

    private void syncPlanDesignBpm(PlanDesignDO design, String processInstanceId, Long submitUserId) {
        if (StrUtil.isBlank(processInstanceId)) {
            return;
        }
        List<Task> runningTasks = bpmTaskService.getRunningTaskListByProcessInstanceId(
                processInstanceId, null, HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_DESIGN_KEY);
        if (runningTasks.isEmpty()) {
            throw new IllegalStateException("未找到计划设计 BPM 节点，流程实例=" + processInstanceId);
        }
        Task currentTask = runningTasks.get(0);
        assignTask(currentTask.getId(), submitUserId);
        BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
        approveReqVO.setId(currentTask.getId());
        approveReqVO.setReason("计划设计提交");
        bpmTaskService.approveTask(submitUserId, approveReqVO);
    }

    private void assignTask(String taskId, Long userId) {
        if (userId == null) {
            return;
        }
        flowableTaskService.setAssignee(taskId, String.valueOf(userId));
    }

    private void normalizeDesign(PlanDesignDO design, PlanApplyDO planApply) {
        if (StrUtil.isBlank(design.getPlanName())) {
            design.setPlanName(planApply.getPatientName() + "计划");
        }
        if (StrUtil.isBlank(design.getVersionNo())) {
            design.setVersionNo(DEFAULT_VERSION_NO);
        }
        if (design.getTotalDose() == null) {
            design.setTotalDose(planApply.getPrescriptionDose() == null ? BigDecimal.ZERO : planApply.getPrescriptionDose());
        }
        if (design.getFractions() == null) {
            design.setFractions(planApply.getTotalFractions() == null ? 0 : planApply.getTotalFractions());
        }
        if (design.getSingleDose() == null) {
            if (design.getFractions() != null && design.getFractions() > 0) {
                design.setSingleDose(design.getTotalDose().divide(new BigDecimal(design.getFractions()), 2, BigDecimal.ROUND_HALF_UP));
            } else {
                design.setSingleDose(BigDecimal.ZERO);
            }
        }
        if (design.getDesignTime() == null) {
            design.setDesignTime(LocalDateTime.now());
        }
        if (StrUtil.isBlank(design.getWorkflowStatus())) {
            design.setWorkflowStatus(WORKFLOW_STATUS_DRAFT);
        }
        if (design.getStatus() == null) {
            design.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (design.getProcessInstanceId() == null) {
            design.setProcessInstanceId("");
        }
    }
}
