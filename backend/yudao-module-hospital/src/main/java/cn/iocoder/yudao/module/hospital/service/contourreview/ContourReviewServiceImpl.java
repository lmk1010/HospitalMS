package cn.iocoder.yudao.module.hospital.service.contourreview;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourreview.ContourReviewDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourreview.ContourReviewMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourtask.ContourTaskMapper;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.planapply.PlanApplyService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_REVIEW_AUDIT_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_REVIEW_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_REVIEW_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_TASK_NOT_EXISTS;

@Service
@Validated
public class ContourReviewServiceImpl implements ContourReviewService {

    private static final String REVIEW_RESULT_APPROVED = "APPROVED";
    private static final String REVIEW_RESULT_REJECTED = "REJECTED";
    private static final String WORKFLOW_STATUS_REVIEWING = "REVIEWING";
    private static final String BPM_REVIEW_TASK_DEFINE_KEY = HospitalWorkflowConstants.RADIOTHERAPY_GZ_CONTOUR_REVIEW_KEY;

    @Resource
    private ContourReviewMapper contourReviewMapper;
    @Resource
    private ContourTaskMapper contourTaskMapper;
    @Resource
    private DoctorService doctorService;
    @Resource
    private BpmTaskService bpmTaskService;
    @Resource
    private org.flowable.engine.TaskService flowableTaskService;
    @Resource
    private PlanApplyService planApplyService;

    @Override
    public void createContourReviewFromTask(ContourTaskDO contourTask) {
        ContourReviewDO review = new ContourReviewDO();
        review.setBizNo(prepareBizNo());
        review.setContourTaskId(contourTask.getId());
        review.setPatientId(contourTask.getPatientId());
        review.setPatientName(contourTask.getPatientName());
        review.setReviewDoctorId(0L);
        review.setReviewDoctorName("");
        review.setReviewResult("");
        review.setWorkflowStatus(WORKFLOW_STATUS_REVIEWING);
        review.setProcessInstanceId(StrUtil.blankToDefault(contourTask.getProcessInstanceId(), ""));
        review.setStatus(CommonStatusEnum.ENABLE.getStatus());
        review.setRemark("");
        contourReviewMapper.insert(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveContourReview(ContourReviewAuditReqVO reqVO) {
        ContourReviewDO existing = validateContourReviewForAudit(reqVO.getId());
        doctorService.validateDoctorExists(reqVO.getReviewDoctorId());
        DoctorDO doctor = doctorService.getDoctor(reqVO.getReviewDoctorId());
        syncBpmReview(existing, doctor.getId(), reqVO.getReviewComment(), true);
        ContourReviewDO updateObj = new ContourReviewDO();
        updateObj.setId(existing.getId());
        updateObj.setReviewDoctorId(doctor.getId());
        updateObj.setReviewDoctorName(doctor.getName());
        updateObj.setReviewResult(REVIEW_RESULT_APPROVED);
        updateObj.setReviewComment(reqVO.getReviewComment());
        updateObj.setReviewTime(LocalDateTime.now());
        updateObj.setWorkflowStatus(REVIEW_RESULT_APPROVED);
        updateObj.setRemark(reqVO.getRemark());
        contourReviewMapper.updateById(updateObj);
        updateContourTaskStatus(existing.getContourTaskId(), REVIEW_RESULT_APPROVED);
        ContourTaskDO contourTask = contourTaskMapper.selectById(existing.getContourTaskId());
        if (contourTask != null) {
            contourTask.setWorkflowStatus(REVIEW_RESULT_APPROVED);
            planApplyService.createFromContourTask(contourTask);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectContourReview(ContourReviewAuditReqVO reqVO) {
        ContourReviewDO existing = validateContourReviewForAudit(reqVO.getId());
        doctorService.validateDoctorExists(reqVO.getReviewDoctorId());
        DoctorDO doctor = doctorService.getDoctor(reqVO.getReviewDoctorId());
        syncBpmReview(existing, doctor.getId(), reqVO.getReviewComment(), false);
        ContourReviewDO updateObj = new ContourReviewDO();
        updateObj.setId(existing.getId());
        updateObj.setReviewDoctorId(doctor.getId());
        updateObj.setReviewDoctorName(doctor.getName());
        updateObj.setReviewResult(REVIEW_RESULT_REJECTED);
        updateObj.setReviewComment(reqVO.getReviewComment());
        updateObj.setReviewTime(LocalDateTime.now());
        updateObj.setWorkflowStatus(REVIEW_RESULT_REJECTED);
        updateObj.setRemark(reqVO.getRemark());
        contourReviewMapper.updateById(updateObj);
        updateContourTaskStatus(existing.getContourTaskId(), REVIEW_RESULT_REJECTED);
    }

    @Override
    public ContourReviewDO getContourReview(Long id) {
        return contourReviewMapper.selectById(id);
    }

    @Override
    public PageResult<ContourReviewDO> getContourReviewPage(ContourReviewPageReqVO reqVO) {
        return contourReviewMapper.selectPage(reqVO);
    }

    private void syncBpmReview(ContourReviewDO review, Long reviewDoctorId, String reason, boolean approved) {
        if (StrUtil.isBlank(review.getProcessInstanceId())) {
            return;
        }
        List<Task> runningTasks = bpmTaskService.getRunningTaskListByProcessInstanceId(
                review.getProcessInstanceId(), null, BPM_REVIEW_TASK_DEFINE_KEY);
        if (runningTasks.isEmpty()) {
            throw new IllegalStateException("未找到勾画审核 BPM 节点，流程实例=" + review.getProcessInstanceId());
        }
        Task currentTask = runningTasks.get(0);
        assignTask(currentTask.getId(), reviewDoctorId);
        if (approved) {
            BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
            approveReqVO.setId(currentTask.getId());
            approveReqVO.setReason(StrUtil.blankToDefault(reason, "勾画审核通过"));
            bpmTaskService.approveTask(reviewDoctorId, approveReqVO);
            return;
        }
        BpmTaskRejectReqVO rejectReqVO = new BpmTaskRejectReqVO();
        rejectReqVO.setId(currentTask.getId());
        rejectReqVO.setReason(StrUtil.blankToDefault(reason, "勾画审核退回"));
        bpmTaskService.rejectTask(reviewDoctorId, rejectReqVO);
    }

    private void assignTask(String taskId, Long userId) {
        if (userId == null) {
            return;
        }
        flowableTaskService.setAssignee(taskId, String.valueOf(userId));
    }

    private ContourReviewDO validateContourReviewForAudit(Long id) {
        ContourReviewDO review = contourReviewMapper.selectById(id);
        if (review == null) {
            throw exception(CONTOUR_REVIEW_NOT_EXISTS);
        }
        if (!Objects.equals(review.getWorkflowStatus(), WORKFLOW_STATUS_REVIEWING)) {
            throw exception(CONTOUR_REVIEW_AUDIT_FAIL_STATUS);
        }
        return review;
    }

    private void updateContourTaskStatus(Long contourTaskId, String workflowStatus) {
        ContourTaskDO task = contourTaskMapper.selectById(contourTaskId);
        if (task == null) {
            throw exception(CONTOUR_TASK_NOT_EXISTS);
        }
        ContourTaskDO updateObj = new ContourTaskDO();
        updateObj.setId(contourTaskId);
        updateObj.setWorkflowStatus(workflowStatus);
        contourTaskMapper.updateById(updateObj);
    }

    private String prepareBizNo() {
        for (int i = 0; i < 10; i++) {
            String generated = "SH" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            if (contourReviewMapper.selectByBizNo(generated) == null) {
                return generated;
            }
        }
        String generated = "SH" + System.currentTimeMillis();
        if (contourReviewMapper.selectByBizNo(generated) != null) {
            throw exception(CONTOUR_REVIEW_BIZ_NO_DUPLICATE);
        }
        return generated;
    }

}
