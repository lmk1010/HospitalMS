package cn.iocoder.yudao.module.hospital.service.planverify;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.plandesign.PlanDesignMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planverify.PlanVerifyMapper;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import cn.iocoder.yudao.module.hospital.service.treatmentappointment.TreatmentAppointmentService;
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
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_VERIFY_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_VERIFY_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_VERIFY_VERIFY_FAIL_STATUS;

@Service
@Validated
public class PlanVerifyServiceImpl implements PlanVerifyService {

    private static final String VERIFY_RESULT_APPROVED = "APPROVED";
    private static final String VERIFY_RESULT_REJECTED = "REJECTED";
    private static final String WORKFLOW_STATUS_DRAFT = "DRAFT";
    private static final String WORKFLOW_STATUS_REJECTED = "REJECTED";
    private static final String WORKFLOW_STATUS_VERIFIED = "VERIFIED";
    private static final String CURRENT_NODE_PLAN_VERIFY = "PLAN_VERIFY";

    @Resource
    private PlanVerifyMapper planVerifyMapper;
    @Resource
    private PlanDesignMapper planDesignMapper;
    @Resource
    private DoctorService doctorService;
    @Resource
    private BpmTaskService bpmTaskService;
    @Resource
    private org.flowable.engine.TaskService flowableTaskService;
    @Resource
    private TreatmentAppointmentService treatmentAppointmentService;
    @Resource
    private PatientService patientService;

    @Override
    public void createPlanVerifyFromDesign(PlanDesignDO planDesign) {
        PlanVerifyDO verify = new PlanVerifyDO();
        verify.setBizNo(prepareBizNo());
        verify.setPlanDesignId(planDesign.getId());
        verify.setPatientId(planDesign.getPatientId());
        verify.setPatientName(planDesign.getPatientName());
        verify.setVerifyUserId(0L);
        verify.setVerifyUserName("");
        verify.setVerifyDeviceName("");
        verify.setVerifyResult("");
        verify.setReportUrl("");
        verify.setWorkflowStatus(WORKFLOW_STATUS_DRAFT);
        verify.setProcessInstanceId(StrUtil.blankToDefault(planDesign.getProcessInstanceId(), ""));
        verify.setStatus(CommonStatusEnum.ENABLE.getStatus());
        verify.setRemark("");
        planVerifyMapper.insert(verify);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyPlan(PlanVerifyAuditReqVO reqVO) {
        PlanVerifyDO existing = validatePlanVerifyForAudit(reqVO.getId());
        doctorService.validateDoctorExists(reqVO.getVerifyUserId());
        DoctorDO doctor = doctorService.getDoctor(reqVO.getVerifyUserId());
        syncBpmVerify(existing, doctor.getId(), reqVO.getRemark(), true);
        PlanVerifyDO updateObj = buildAuditUpdate(existing, doctor, reqVO, VERIFY_RESULT_APPROVED, WORKFLOW_STATUS_VERIFIED);
        planVerifyMapper.updateById(updateObj);
        updatePlanDesignStatus(existing.getPlanDesignId(), WORKFLOW_STATUS_VERIFIED);

        existing.setVerifyUserId(updateObj.getVerifyUserId());
        existing.setVerifyUserName(updateObj.getVerifyUserName());
        existing.setVerifyDeviceName(updateObj.getVerifyDeviceName());
        existing.setVerifyResult(updateObj.getVerifyResult());
        existing.setReportUrl(updateObj.getReportUrl());
        existing.setVerifyTime(updateObj.getVerifyTime());
        existing.setWorkflowStatus(updateObj.getWorkflowStatus());
        existing.setRemark(updateObj.getRemark());
        treatmentAppointmentService.createFromPlanVerify(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectPlanVerify(PlanVerifyAuditReqVO reqVO) {
        PlanVerifyDO existing = validatePlanVerifyForAudit(reqVO.getId());
        doctorService.validateDoctorExists(reqVO.getVerifyUserId());
        DoctorDO doctor = doctorService.getDoctor(reqVO.getVerifyUserId());
        syncBpmVerify(existing, doctor.getId(), reqVO.getRemark(), false);
        PlanVerifyDO updateObj = buildAuditUpdate(existing, doctor, reqVO, VERIFY_RESULT_REJECTED, WORKFLOW_STATUS_REJECTED);
        planVerifyMapper.updateById(updateObj);
        updatePlanDesignStatus(existing.getPlanDesignId(), WORKFLOW_STATUS_REJECTED);
    }

    @Override
    public PlanVerifyDO getPlanVerify(Long id) {
        return planVerifyMapper.selectById(id);
    }

    @Override
    public PageResult<PlanVerifyDO> getPlanVerifyPage(PlanVerifyPageReqVO reqVO) {
        List<PlanVerifyDO> records = planVerifyMapper.selectListByReqVO(reqVO);
        if (records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        Set<Long> patientIds = records.stream()
                .map(PlanVerifyDO::getPatientId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> currentNodeCodeMap = patientService.getCurrentNodeCodeMap(patientIds);
        List<PlanVerifyDO> filtered = records.stream()
                .filter(item -> Objects.equals(CURRENT_NODE_PLAN_VERIFY, currentNodeCodeMap.get(item.getPatientId())))
                .collect(Collectors.toList());
        return buildPageResult(filtered, reqVO);
    }

    private PageResult<PlanVerifyDO> buildPageResult(List<PlanVerifyDO> rows, PlanVerifyPageReqVO reqVO) {
        long total = rows.size();
        int fromIndex = Math.min((Math.max(reqVO.getPageNo(), 1) - 1) * Math.max(reqVO.getPageSize(), 1), rows.size());
        int toIndex = Math.min(fromIndex + Math.max(reqVO.getPageSize(), 1), rows.size());
        if (fromIndex >= toIndex) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        return new PageResult<>(new ArrayList<>(rows.subList(fromIndex, toIndex)), total);
    }

    private void syncBpmVerify(PlanVerifyDO verify, Long verifyUserId, String reason, boolean approved) {
        if (StrUtil.isBlank(verify.getProcessInstanceId())) {
            return;
        }
        List<Task> runningTasks = bpmTaskService.getRunningTaskListByProcessInstanceId(
                verify.getProcessInstanceId(), null, HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_VERIFY_KEY);
        if (runningTasks.isEmpty()) {
            throw new IllegalStateException("未找到计划验证 BPM 节点，流程实例=" + verify.getProcessInstanceId());
        }
        Task currentTask = runningTasks.get(0);
        assignTask(currentTask.getId(), verifyUserId);
        if (approved) {
            BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
            approveReqVO.setId(currentTask.getId());
            approveReqVO.setReason(StrUtil.blankToDefault(reason, "计划验证通过"));
            bpmTaskService.approveTask(verifyUserId, approveReqVO);
            return;
        }
        BpmTaskRejectReqVO rejectReqVO = new BpmTaskRejectReqVO();
        rejectReqVO.setId(currentTask.getId());
        rejectReqVO.setReason(StrUtil.blankToDefault(reason, "计划验证退回"));
        bpmTaskService.rejectTask(verifyUserId, rejectReqVO);
    }

    private void assignTask(String taskId, Long userId) {
        if (userId == null) {
            return;
        }
        flowableTaskService.setAssignee(taskId, String.valueOf(userId));
    }

    private PlanVerifyDO buildAuditUpdate(PlanVerifyDO existing, DoctorDO doctor,
                                          PlanVerifyAuditReqVO reqVO, String verifyResult, String workflowStatus) {
        PlanVerifyDO updateObj = new PlanVerifyDO();
        updateObj.setId(existing.getId());
        updateObj.setVerifyUserId(doctor.getId());
        updateObj.setVerifyUserName(doctor.getName());
        updateObj.setVerifyDeviceName(StrUtil.blankToDefault(reqVO.getVerifyDeviceName(), existing.getVerifyDeviceName()));
        updateObj.setVerifyResult(verifyResult);
        updateObj.setReportUrl(StrUtil.blankToDefault(reqVO.getReportUrl(), existing.getReportUrl()));
        updateObj.setVerifyTime(LocalDateTime.now());
        updateObj.setWorkflowStatus(workflowStatus);
        updateObj.setRemark(reqVO.getRemark());
        return updateObj;
    }

    private PlanVerifyDO validatePlanVerifyForAudit(Long id) {
        PlanVerifyDO verify = planVerifyMapper.selectById(id);
        if (verify == null) {
            throw exception(PLAN_VERIFY_NOT_EXISTS);
        }
        if (!Objects.equals(verify.getWorkflowStatus(), WORKFLOW_STATUS_DRAFT)) {
            throw exception(PLAN_VERIFY_VERIFY_FAIL_STATUS);
        }
        return verify;
    }

    private void updatePlanDesignStatus(Long planDesignId, String workflowStatus) {
        PlanDesignDO design = planDesignMapper.selectById(planDesignId);
        if (design == null) {
            throw exception(PLAN_DESIGN_NOT_EXISTS);
        }
        PlanDesignDO updateObj = new PlanDesignDO();
        updateObj.setId(planDesignId);
        updateObj.setWorkflowStatus(workflowStatus);
        planDesignMapper.updateById(updateObj);
    }

    private String prepareBizNo() {
        for (int i = 0; i < 10; i++) {
            String generated = "YZ" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            if (planVerifyMapper.selectByBizNo(generated) == null) {
                return generated;
            }
        }
        String generated = "YZ" + System.currentTimeMillis();
        if (planVerifyMapper.selectByBizNo(generated) != null) {
            throw exception(PLAN_VERIFY_BIZ_NO_DUPLICATE);
        }
        return generated;
    }
}
