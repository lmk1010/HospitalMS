package cn.iocoder.yudao.module.hospital.service.planapply;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplyPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplySaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourtask.ContourTaskMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planapply.PlanApplyMapper;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import cn.iocoder.yudao.module.hospital.service.plandesign.PlanDesignService;
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
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_TASK_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_APPLY_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_APPLY_CONTOUR_STATUS_INVALID;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_APPLY_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_APPLY_SUBMIT_FAIL_STATUS;

@Service
@Validated
public class PlanApplyServiceImpl implements PlanApplyService {

    private static final String DEFAULT_PRIORITY = "NORMAL";
    private static final String WORKFLOW_STATUS_DRAFT = "DRAFT";
    private static final String WORKFLOW_STATUS_APPROVED = "APPROVED";
    private static final String WORKFLOW_STATUS_REJECTED = "REJECTED";
    private static final String CONTOUR_STATUS_APPROVED = "APPROVED";
    private static final String CURRENT_NODE_PLAN_APPLY = "PLAN_APPLY";

    @Resource
    private PlanApplyMapper planApplyMapper;
    @Resource
    private ContourTaskMapper contourTaskMapper;
    @Resource
    private DoctorService doctorService;
    @Resource
    private BpmTaskService bpmTaskService;
    @Resource
    private org.flowable.engine.TaskService flowableTaskService;
    @Resource
    private PlanDesignService planDesignService;
    @Resource
    private PatientService patientService;

    @Override
    public Long createPlanApply(PlanApplySaveReqVO createReqVO) {
        String bizNo = prepareBizNo(null, createReqVO.getBizNo());
        ContourTaskDO contourTask = validateCreateOrUpdate(null, bizNo, createReqVO);
        DoctorDO doctor = doctorService.getDoctor(createReqVO.getApplyDoctorId());
        PlanApplyDO apply = BeanUtils.toBean(createReqVO, PlanApplyDO.class);
        apply.setBizNo(bizNo);
        apply.setPatientId(contourTask.getPatientId());
        apply.setPatientName(contourTask.getPatientName());
        apply.setApplyDoctorName(doctor.getName());
        apply.setProcessInstanceId(StrUtil.blankToDefault(contourTask.getProcessInstanceId(), ""));
        normalizeApply(apply, contourTask);
        planApplyMapper.insert(apply);
        return apply.getId();
    }

    @Override
    public void updatePlanApply(PlanApplySaveReqVO updateReqVO) {
        PlanApplyDO existing = validatePlanApplyExists(updateReqVO.getId());
        String bizNo = prepareBizNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getBizNo(), existing.getBizNo()));
        ContourTaskDO contourTask = validateCreateOrUpdate(updateReqVO.getId(), bizNo, updateReqVO);
        DoctorDO doctor = doctorService.getDoctor(updateReqVO.getApplyDoctorId());
        PlanApplyDO apply = BeanUtils.toBean(updateReqVO, PlanApplyDO.class);
        apply.setBizNo(bizNo);
        apply.setPatientId(contourTask.getPatientId());
        apply.setPatientName(contourTask.getPatientName());
        apply.setApplyDoctorName(doctor.getName());
        apply.setSubmitTime(existing.getSubmitTime());
        apply.setWorkflowStatus(existing.getWorkflowStatus());
        apply.setProcessInstanceId(StrUtil.blankToDefault(existing.getProcessInstanceId(), contourTask.getProcessInstanceId()));
        normalizeApply(apply, contourTask);
        planApplyMapper.updateById(apply);
    }

    @Override
    public void createFromContourTask(ContourTaskDO contourTask) {
        if (contourTask == null || contourTask.getId() == null) {
            return;
        }
        if (planApplyMapper.selectByContourTaskId(contourTask.getId()) != null) {
            return;
        }
        Long applyDoctorId = resolveAutoApplyDoctorId(contourTask);
        DoctorDO applyDoctor = applyDoctorId != null && applyDoctorId > 0
                ? doctorService.getDoctor(applyDoctorId)
                : null;

        PlanApplyDO apply = new PlanApplyDO();
        apply.setBizNo(prepareBizNo(null, null));
        apply.setPatientId(contourTask.getPatientId());
        apply.setPatientName(contourTask.getPatientName());
        apply.setContourTaskId(contourTask.getId());
        apply.setApplyDoctorId(applyDoctor == null ? 0L : applyDoctor.getId());
        apply.setApplyDoctorName(applyDoctor == null ? "" : applyDoctor.getName());
        apply.setTreatmentSite(StrUtil.blankToDefault(contourTask.getTreatmentSite(), ""));
        apply.setClinicalDiagnosis("");
        apply.setPrescriptionDose(BigDecimal.ZERO);
        apply.setTotalFractions(0);
        apply.setPriority(DEFAULT_PRIORITY);
        apply.setWorkflowStatus(WORKFLOW_STATUS_DRAFT);
        apply.setProcessInstanceId(StrUtil.blankToDefault(contourTask.getProcessInstanceId(), ""));
        apply.setStatus(CommonStatusEnum.ENABLE.getStatus());
        apply.setRemark(StrUtil.blankToDefault(contourTask.getRemark(), ""));
        normalizeApply(apply, contourTask);
        planApplyMapper.insert(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPlanApply(Long id) {
        PlanApplyDO existing = validatePlanApplyExists(id);
        if (!Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_DRAFT)
                && !Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_REJECTED)) {
            throw exception(PLAN_APPLY_SUBMIT_FAIL_STATUS);
        }
        String processInstanceId = resolveProcessInstanceId(existing);
        syncPlanApplyBpm(existing, processInstanceId, resolveSubmitUserId(existing));

        PlanApplyDO updateObj = new PlanApplyDO();
        updateObj.setId(id);
        updateObj.setSubmitTime(LocalDateTime.now());
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_APPROVED);
        updateObj.setProcessInstanceId(processInstanceId);
        planApplyMapper.updateById(updateObj);

        existing.setSubmitTime(updateObj.getSubmitTime());
        existing.setWorkflowStatus(WORKFLOW_STATUS_APPROVED);
        existing.setProcessInstanceId(processInstanceId);
        planDesignService.createFromPlanApply(existing);
    }

    @Override
    public void deletePlanApply(Long id) {
        validatePlanApplyExists(id);
        planApplyMapper.deleteById(id);
    }

    @Override
    public PlanApplyDO getPlanApply(Long id) {
        return planApplyMapper.selectById(id);
    }

    @Override
    public java.util.List<PlanApplyDO> getPlanApplySimpleList() {
        return planApplyMapper.selectSimpleList();
    }

    @Override
    public PageResult<PlanApplyDO> getPlanApplyPage(PlanApplyPageReqVO reqVO) {
        List<PlanApplyDO> records = planApplyMapper.selectListByReqVO(reqVO);
        if (records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        Set<Long> patientIds = records.stream()
                .map(PlanApplyDO::getPatientId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> currentNodeCodeMap = patientService.getCurrentNodeCodeMap(patientIds);
        List<PlanApplyDO> filtered = records.stream()
                .filter(item -> Objects.equals(CURRENT_NODE_PLAN_APPLY, currentNodeCodeMap.get(item.getPatientId())))
                .collect(Collectors.toList());
        return buildPageResult(filtered, reqVO);
    }

    private PageResult<PlanApplyDO> buildPageResult(List<PlanApplyDO> rows, PlanApplyPageReqVO reqVO) {
        long total = rows.size();
        int fromIndex = Math.min((Math.max(reqVO.getPageNo(), 1) - 1) * Math.max(reqVO.getPageSize(), 1), rows.size());
        int toIndex = Math.min(fromIndex + Math.max(reqVO.getPageSize(), 1), rows.size());
        if (fromIndex >= toIndex) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        return new PageResult<>(new ArrayList<>(rows.subList(fromIndex, toIndex)), total);
    }

    private ContourTaskDO validateCreateOrUpdate(Long id, String bizNo, PlanApplySaveReqVO reqVO) {
        if (id != null) {
            validatePlanApplyExists(id);
        }
        doctorService.validateDoctorExists(reqVO.getApplyDoctorId());
        ContourTaskDO contourTask = contourTaskMapper.selectById(reqVO.getContourTaskId());
        if (contourTask == null) {
            throw exception(CONTOUR_TASK_NOT_EXISTS);
        }
        if (!Objects.equals(contourTask.getWorkflowStatus(), CONTOUR_STATUS_APPROVED)) {
            throw exception(PLAN_APPLY_CONTOUR_STATUS_INVALID);
        }
        validateBizNoUnique(id, bizNo);
        return contourTask;
    }

    private PlanApplyDO validatePlanApplyExists(Long id) {
        PlanApplyDO apply = planApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(PLAN_APPLY_NOT_EXISTS);
        }
        return apply;
    }

    private void validateBizNoUnique(Long id, String bizNo) {
        PlanApplyDO apply = planApplyMapper.selectByBizNo(bizNo);
        if (apply == null) {
            return;
        }
        if (!Objects.equals(apply.getId(), id)) {
            throw exception(PLAN_APPLY_BIZ_NO_DUPLICATE);
        }
    }

    private String prepareBizNo(Long id, String bizNo) {
        if (StrUtil.isNotBlank(bizNo)) {
            return bizNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "FA" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            PlanApplyDO apply = planApplyMapper.selectByBizNo(generated);
            if (apply == null || Objects.equals(apply.getId(), id)) {
                return generated;
            }
        }
        return "FA" + System.currentTimeMillis();
    }

    private Long resolveAutoApplyDoctorId(ContourTaskDO contourTask) {
        if (contourTask.getContourDoctorId() != null && contourTask.getContourDoctorId() > 0) {
            return contourTask.getContourDoctorId();
        }
        List<DoctorDO> doctors = doctorService.getDoctorSimpleList();
        return doctors.isEmpty() ? 0L : doctors.get(0).getId();
    }

    private String resolveProcessInstanceId(PlanApplyDO apply) {
        if (StrUtil.isNotBlank(apply.getProcessInstanceId())) {
            return apply.getProcessInstanceId();
        }
        ContourTaskDO contourTask = contourTaskMapper.selectById(apply.getContourTaskId());
        return contourTask == null ? "" : StrUtil.blankToDefault(contourTask.getProcessInstanceId(), "");
    }

    private Long resolveSubmitUserId(PlanApplyDO apply) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        return loginUserId != null ? loginUserId : apply.getApplyDoctorId();
    }

    private void syncPlanApplyBpm(PlanApplyDO apply, String processInstanceId, Long submitUserId) {
        if (StrUtil.isBlank(processInstanceId)) {
            return;
        }
        List<Task> runningTasks = bpmTaskService.getRunningTaskListByProcessInstanceId(
                processInstanceId, null, HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_APPLY_KEY);
        if (runningTasks.isEmpty()) {
            throw new IllegalStateException("未找到计划申请 BPM 节点，流程实例=" + processInstanceId);
        }
        Task currentTask = runningTasks.get(0);
        assignTask(currentTask.getId(), submitUserId);
        BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
        approveReqVO.setId(currentTask.getId());
        approveReqVO.setReason("计划申请提交");
        bpmTaskService.approveTask(submitUserId, approveReqVO);
    }

    private void assignTask(String taskId, Long userId) {
        if (userId == null) {
            return;
        }
        flowableTaskService.setAssignee(taskId, String.valueOf(userId));
    }

    private void normalizeApply(PlanApplyDO apply, ContourTaskDO contourTask) {
        if (StrUtil.isBlank(apply.getTreatmentSite())) {
            apply.setTreatmentSite(StrUtil.blankToDefault(contourTask.getTreatmentSite(), ""));
        }
        if (apply.getPrescriptionDose() == null) {
            apply.setPrescriptionDose(BigDecimal.ZERO);
        }
        if (apply.getTotalFractions() == null) {
            apply.setTotalFractions(0);
        }
        if (StrUtil.isBlank(apply.getPriority())) {
            apply.setPriority(DEFAULT_PRIORITY);
        }
        if (StrUtil.isBlank(apply.getWorkflowStatus())) {
            apply.setWorkflowStatus(WORKFLOW_STATUS_DRAFT);
        }
        if (apply.getStatus() == null) {
            apply.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (apply.getProcessInstanceId() == null) {
            apply.setProcessInstanceId("");
        }
        if (apply.getClinicalDiagnosis() == null) {
            apply.setClinicalDiagnosis("");
        }
    }
}
