package cn.iocoder.yudao.module.hospital.service.planreview;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planreview.PlanReviewDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.plandesign.PlanDesignMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planreview.PlanReviewMapper;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import cn.iocoder.yudao.module.hospital.service.planverify.PlanVerifyService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_DESIGN_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_REVIEW_AUDIT_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_REVIEW_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_REVIEW_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_REVIEW_STAGE_INVALID;

@Service
@Validated
public class PlanReviewServiceImpl implements PlanReviewService {

    public static final String STAGE_GROUP_LEADER = "GROUP_LEADER";
    public static final String STAGE_DOCTOR_APPROVAL = "DOCTOR_APPROVAL";
    public static final String STAGE_RECHECK = "RECHECK";

    private static final String REVIEW_RESULT_APPROVED = "APPROVED";
    private static final String REVIEW_RESULT_REJECTED = "REJECTED";
    private static final String WORKFLOW_STATUS_REVIEWING = "REVIEWING";
    private static final String WORKFLOW_STATUS_APPROVED = "APPROVED";
    private static final String WORKFLOW_STATUS_REJECTED = "REJECTED";
    private static final String CURRENT_NODE_PLAN_GROUP_REVIEW = "PLAN_GROUP_REVIEW";
    private static final String CURRENT_NODE_PLAN_DOCTOR_APPROVAL = "PLAN_DOCTOR_APPROVAL";
    private static final String CURRENT_NODE_PLAN_RECHECK = "PLAN_RECHECK";

    @Resource
    private PlanReviewMapper planReviewMapper;
    @Resource
    private PlanDesignMapper planDesignMapper;
    @Resource
    private DoctorService doctorService;
    @Resource
    private PlanVerifyService planVerifyService;
    @Resource
    private BpmTaskService bpmTaskService;
    @Resource
    private org.flowable.engine.TaskService flowableTaskService;
    @Resource
    private PatientService patientService;

    @Override
    public void createGroupLeaderReviewFromDesign(PlanDesignDO planDesign) {
        createPlanReview(planDesign, STAGE_GROUP_LEADER, planDesign.getProcessInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePlanReview(String reviewStage, PlanReviewAuditReqVO reqVO) {
        PlanReviewDO existing = validatePlanReviewForAudit(reqVO.getId(), reviewStage);
        doctorService.validateDoctorExists(reqVO.getReviewDoctorId());
        DoctorDO doctor = doctorService.getDoctor(reqVO.getReviewDoctorId());
        syncBpmReview(existing, doctor.getId(), reqVO.getReviewComment(), true);
        PlanReviewDO updateObj = buildAuditUpdate(existing, doctor, reqVO, REVIEW_RESULT_APPROVED);
        planReviewMapper.updateById(updateObj);
        handleApproveSuccess(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectPlanReview(String reviewStage, PlanReviewAuditReqVO reqVO) {
        PlanReviewDO existing = validatePlanReviewForAudit(reqVO.getId(), reviewStage);
        doctorService.validateDoctorExists(reqVO.getReviewDoctorId());
        DoctorDO doctor = doctorService.getDoctor(reqVO.getReviewDoctorId());
        syncBpmReview(existing, doctor.getId(), reqVO.getReviewComment(), false);
        PlanReviewDO updateObj = buildAuditUpdate(existing, doctor, reqVO, REVIEW_RESULT_REJECTED);
        planReviewMapper.updateById(updateObj);
        updatePlanDesignStatus(existing.getPlanDesignId(), WORKFLOW_STATUS_REJECTED);
    }

    @Override
    public PlanReviewDO getPlanReview(Long id) {
        return planReviewMapper.selectById(id);
    }

    @Override
    public PageResult<PlanReviewDO> getPlanReviewPage(PlanReviewPageReqVO reqVO, String reviewStage) {
        List<PlanReviewDO> records = planReviewMapper.selectListByReqVO(reqVO, reviewStage);
        if (records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        Set<Long> patientIds = records.stream()
                .map(PlanReviewDO::getPatientId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> currentNodeCodeMap = patientService.getCurrentNodeCodeMap(patientIds);
        String currentNodeCode = resolveCurrentNodeCode(reviewStage);
        List<PlanReviewDO> filtered = records.stream()
                .filter(item -> Objects.equals(currentNodeCode, currentNodeCodeMap.get(item.getPatientId())))
                .collect(Collectors.toList());
        return buildPageResult(filtered, reqVO);
    }

    private PageResult<PlanReviewDO> buildPageResult(List<PlanReviewDO> rows, PlanReviewPageReqVO reqVO) {
        long total = rows.size();
        int fromIndex = Math.min((Math.max(reqVO.getPageNo(), 1) - 1) * Math.max(reqVO.getPageSize(), 1), rows.size());
        int toIndex = Math.min(fromIndex + Math.max(reqVO.getPageSize(), 1), rows.size());
        if (fromIndex >= toIndex) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        return new PageResult<>(new ArrayList<>(rows.subList(fromIndex, toIndex)), total);
    }

    private PlanReviewDO buildAuditUpdate(PlanReviewDO existing, DoctorDO doctor,
                                          PlanReviewAuditReqVO reqVO, String reviewResult) {
        PlanReviewDO updateObj = new PlanReviewDO();
        updateObj.setId(existing.getId());
        updateObj.setReviewDoctorId(doctor.getId());
        updateObj.setReviewDoctorName(doctor.getName());
        updateObj.setReviewResult(reviewResult);
        updateObj.setReviewComment(reqVO.getReviewComment());
        updateObj.setReviewTime(LocalDateTime.now());
        updateObj.setWorkflowStatus(reviewResult);
        updateObj.setRemark(reqVO.getRemark());
        return updateObj;
    }

    private void handleApproveSuccess(PlanReviewDO review) {
        if (Objects.equals(review.getReviewStage(), STAGE_GROUP_LEADER)) {
            PlanDesignDO design = validatePlanDesignExists(review.getPlanDesignId());
            createPlanReview(design, STAGE_DOCTOR_APPROVAL, review.getProcessInstanceId());
            updatePlanDesignStatus(review.getPlanDesignId(), WORKFLOW_STATUS_REVIEWING);
            return;
        }
        if (Objects.equals(review.getReviewStage(), STAGE_DOCTOR_APPROVAL)) {
            PlanDesignDO design = validatePlanDesignExists(review.getPlanDesignId());
            createPlanReview(design, STAGE_RECHECK, review.getProcessInstanceId());
            updatePlanDesignStatus(review.getPlanDesignId(), WORKFLOW_STATUS_REVIEWING);
            return;
        }
        if (Objects.equals(review.getReviewStage(), STAGE_RECHECK)) {
            PlanDesignDO design = validatePlanDesignExists(review.getPlanDesignId());
            updatePlanDesignStatus(review.getPlanDesignId(), WORKFLOW_STATUS_APPROVED);
            planVerifyService.createPlanVerifyFromDesign(design);
            return;
        }
        throw exception(PLAN_REVIEW_STAGE_INVALID);
    }

    private void syncBpmReview(PlanReviewDO review, Long reviewDoctorId, String reason, boolean approved) {
        if (StrUtil.isBlank(review.getProcessInstanceId())) {
            return;
        }
        List<Task> runningTasks = bpmTaskService.getRunningTaskListByProcessInstanceId(
                review.getProcessInstanceId(), null, resolveReviewTaskDefineKey(review.getReviewStage()));
        if (runningTasks.isEmpty()) {
            throw new IllegalStateException("未找到计划审核 BPM 节点，流程实例=" + review.getProcessInstanceId());
        }
        Task currentTask = runningTasks.get(0);
        assignTask(currentTask.getId(), reviewDoctorId);
        if (approved) {
            BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
            approveReqVO.setId(currentTask.getId());
            approveReqVO.setReason(StrUtil.blankToDefault(reason, "计划审核通过"));
            bpmTaskService.approveTask(reviewDoctorId, approveReqVO);
            return;
        }
        BpmTaskRejectReqVO rejectReqVO = new BpmTaskRejectReqVO();
        rejectReqVO.setId(currentTask.getId());
        rejectReqVO.setReason(StrUtil.blankToDefault(reason, "计划审核退回"));
        bpmTaskService.rejectTask(reviewDoctorId, rejectReqVO);
    }

    private String resolveCurrentNodeCode(String reviewStage) {
        if (Objects.equals(reviewStage, STAGE_GROUP_LEADER)) {
            return CURRENT_NODE_PLAN_GROUP_REVIEW;
        }
        if (Objects.equals(reviewStage, STAGE_DOCTOR_APPROVAL)) {
            return CURRENT_NODE_PLAN_DOCTOR_APPROVAL;
        }
        if (Objects.equals(reviewStage, STAGE_RECHECK)) {
            return CURRENT_NODE_PLAN_RECHECK;
        }
        throw exception(PLAN_REVIEW_STAGE_INVALID);
    }

    private String resolveReviewTaskDefineKey(String reviewStage) {
        if (Objects.equals(reviewStage, STAGE_GROUP_LEADER)) {
            return HospitalWorkflowConstants.RADIOTHERAPY_GZ_GROUP_REVIEW_KEY;
        }
        if (Objects.equals(reviewStage, STAGE_DOCTOR_APPROVAL)) {
            return HospitalWorkflowConstants.RADIOTHERAPY_GZ_DOCTOR_APPROVAL_KEY;
        }
        if (Objects.equals(reviewStage, STAGE_RECHECK)) {
            return HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_RECHECK_KEY;
        }
        throw exception(PLAN_REVIEW_STAGE_INVALID);
    }

    private void assignTask(String taskId, Long userId) {
        if (userId == null) {
            return;
        }
        flowableTaskService.setAssignee(taskId, String.valueOf(userId));
    }

    private PlanReviewDO validatePlanReviewForAudit(Long id, String reviewStage) {
        PlanReviewDO review = planReviewMapper.selectById(id);
        if (review == null) {
            throw exception(PLAN_REVIEW_NOT_EXISTS);
        }
        if (!Objects.equals(review.getReviewStage(), reviewStage)) {
            throw exception(PLAN_REVIEW_STAGE_INVALID);
        }
        if (!Objects.equals(review.getWorkflowStatus(), WORKFLOW_STATUS_REVIEWING)) {
            throw exception(PLAN_REVIEW_AUDIT_FAIL_STATUS);
        }
        return review;
    }

    private PlanDesignDO validatePlanDesignExists(Long id) {
        PlanDesignDO design = planDesignMapper.selectById(id);
        if (design == null) {
            throw exception(PLAN_DESIGN_NOT_EXISTS);
        }
        return design;
    }

    private void updatePlanDesignStatus(Long planDesignId, String workflowStatus) {
        validatePlanDesignExists(planDesignId);
        PlanDesignDO updateObj = new PlanDesignDO();
        updateObj.setId(planDesignId);
        updateObj.setWorkflowStatus(workflowStatus);
        planDesignMapper.updateById(updateObj);
    }

    private void createPlanReview(PlanDesignDO planDesign, String reviewStage, String processInstanceId) {
        PlanReviewDO review = new PlanReviewDO();
        review.setBizNo(prepareBizNo());
        review.setPlanDesignId(planDesign.getId());
        review.setPatientId(planDesign.getPatientId());
        review.setPatientName(planDesign.getPatientName());
        review.setReviewStage(reviewStage);
        review.setReviewDoctorId(0L);
        review.setReviewDoctorName("");
        review.setReviewResult("");
        review.setWorkflowStatus(WORKFLOW_STATUS_REVIEWING);
        review.setProcessInstanceId(StrUtil.blankToDefault(processInstanceId, ""));
        review.setStatus(CommonStatusEnum.ENABLE.getStatus());
        review.setRemark("");
        planReviewMapper.insert(review);
    }

    private String prepareBizNo() {
        for (int i = 0; i < 10; i++) {
            String generated = "PS" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            if (planReviewMapper.selectByBizNo(generated) == null) {
                return generated;
            }
        }
        String generated = "PS" + System.currentTimeMillis();
        if (planReviewMapper.selectByBizNo(generated) != null) {
            throw exception(PLAN_REVIEW_BIZ_NO_DUPLICATE);
        }
        return generated;
    }
}
