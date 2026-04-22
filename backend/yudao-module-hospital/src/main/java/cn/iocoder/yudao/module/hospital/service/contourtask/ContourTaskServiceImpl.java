package cn.iocoder.yudao.module.hospital.service.contourtask;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskPageReqVO;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourtask.ContourTaskMapper;
import cn.iocoder.yudao.module.hospital.service.contourreview.ContourReviewService;
import cn.iocoder.yudao.module.hospital.service.ctappointment.CtAppointmentService;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_TASK_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_TASK_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CONTOUR_TASK_SUBMIT_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_APPOINTMENT_NOT_EXISTS;

@Service
@Validated
public class ContourTaskServiceImpl implements ContourTaskService {

    private static final String DEFAULT_VERSION_NO = "V1";
    private static final String WORKFLOW_STATUS_WAIT_CONTOUR = "WAIT_CONTOUR";
    private static final String WORKFLOW_STATUS_REVIEWING = "REVIEWING";
    private static final String WORKFLOW_STATUS_REJECTED = "REJECTED";
    private static final String BPM_PROCESS_KEY = HospitalWorkflowConstants.RADIOTHERAPY_GZ_PROCESS_KEY;
    private static final String BPM_REVIEW_TASK_DEFINE_KEY = HospitalWorkflowConstants.RADIOTHERAPY_GZ_CONTOUR_REVIEW_KEY;
    private static final int BPM_ADVANCE_MAX_ROUNDS = 12;

    @Resource
    private ContourTaskMapper contourTaskMapper;
    @Resource
    private PatientService patientService;
    @Resource
    private DoctorService doctorService;
    @Resource
    private CtAppointmentService ctAppointmentService;
    @Resource
    private ContourReviewService contourReviewService;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private BpmTaskService bpmTaskService;
    @Resource
    private org.flowable.engine.TaskService flowableTaskService;

    @Override
    public Long createContourTask(ContourTaskSaveReqVO createReqVO) {
        String bizNo = prepareBizNo(null, createReqVO.getBizNo());
        validateContourTaskForCreateOrUpdate(null, bizNo, createReqVO);
        PatientDO patient = patientService.getPatient(createReqVO.getPatientId());
        DoctorDO doctor = doctorService.getDoctor(createReqVO.getContourDoctorId());
        ContourTaskDO task = BeanUtils.toBean(createReqVO, ContourTaskDO.class);
        task.setBizNo(bizNo);
        task.setPatientName(patient.getName());
        task.setContourDoctorName(doctor.getName());
        normalizeTask(task);
        contourTaskMapper.insert(task);
        return task.getId();
    }

    @Override
    public void updateContourTask(ContourTaskSaveReqVO updateReqVO) {
        ContourTaskDO existing = validateContourTaskExists(updateReqVO.getId());
        String bizNo = prepareBizNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getBizNo(), existing.getBizNo()));
        validateContourTaskForCreateOrUpdate(updateReqVO.getId(), bizNo, updateReqVO);
        PatientDO patient = patientService.getPatient(updateReqVO.getPatientId());
        DoctorDO doctor = doctorService.getDoctor(updateReqVO.getContourDoctorId());
        ContourTaskDO task = BeanUtils.toBean(updateReqVO, ContourTaskDO.class);
        task.setBizNo(bizNo);
        task.setPatientName(patient.getName());
        task.setContourDoctorName(doctor.getName());
        task.setSubmitTime(existing.getSubmitTime());
        task.setWorkflowStatus(existing.getWorkflowStatus());
        task.setProcessInstanceId(existing.getProcessInstanceId());
        normalizeTask(task);
        contourTaskMapper.updateById(task);
    }

    @Override
    public void createFromCtAppointment(CtAppointmentDO appointment) {
        if (appointment == null || appointment.getId() == null) {
            return;
        }
        if (contourTaskMapper.selectByCtAppointmentId(appointment.getId()) != null) {
            return;
        }
        PatientDO patient = patientService.getPatient(appointment.getPatientId());
        Long contourDoctorId = resolveAutoContourDoctorId(appointment, patient);
        DoctorDO contourDoctor = contourDoctorId != null && contourDoctorId > 0
                ? doctorService.getDoctor(contourDoctorId)
                : null;

        ContourTaskDO task = new ContourTaskDO();
        task.setBizNo(prepareBizNo(null, null));
        task.setPatientId(appointment.getPatientId());
        task.setPatientName(StrUtil.blankToDefault(appointment.getPatientName(), patient == null ? "" : patient.getName()));
        task.setCtAppointmentId(appointment.getId());
        task.setContourDoctorId(contourDoctor == null ? 0L : contourDoctor.getId());
        task.setContourDoctorName(contourDoctor == null ? "" : contourDoctor.getName());
        task.setTreatmentSite(StrUtil.blankToDefault(appointment.getPositionNote(), ""));
        task.setVersionNo(DEFAULT_VERSION_NO);
        task.setAttachmentUrl("");
        task.setWorkflowStatus(WORKFLOW_STATUS_WAIT_CONTOUR);
        task.setProcessInstanceId(StrUtil.blankToDefault(appointment.getProcessInstanceId(), ""));
        task.setStatus(CommonStatusEnum.ENABLE.getStatus());
        task.setRemark(StrUtil.blankToDefault(appointment.getRemark(), ""));
        normalizeTask(task);
        contourTaskMapper.insert(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitContourTask(Long id) {
        ContourTaskDO existing = validateContourTaskExists(id);
        if (!Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_WAIT_CONTOUR)
                && !Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_REJECTED)) {
            throw exception(CONTOUR_TASK_SUBMIT_FAIL_STATUS);
        }
        Long startUserId = resolveStartUserId(existing);
        String processInstanceId = startContourWorkflow(existing, startUserId);
        advanceWorkflowToReview(processInstanceId, startUserId);

        ContourTaskDO updateObj = new ContourTaskDO();
        updateObj.setId(id);
        updateObj.setSubmitTime(LocalDateTime.now());
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_REVIEWING);
        updateObj.setProcessInstanceId(processInstanceId);
        contourTaskMapper.updateById(updateObj);

        existing.setSubmitTime(updateObj.getSubmitTime());
        existing.setWorkflowStatus(WORKFLOW_STATUS_REVIEWING);
        existing.setProcessInstanceId(processInstanceId);
        contourReviewService.createContourReviewFromTask(existing);
    }

    @Override
    public void deleteContourTask(Long id) {
        validateContourTaskExists(id);
        contourTaskMapper.deleteById(id);
    }

    @Override
    public ContourTaskDO getContourTask(Long id) {
        return contourTaskMapper.selectById(id);
    }

    @Override
    public java.util.List<ContourTaskDO> getContourTaskSimpleList() {
        return contourTaskMapper.selectSimpleList();
    }

    
    public PageResult<ContourTaskDO> getContourTaskPage(ContourTaskPageReqVO reqVO) {
        return contourTaskMapper.selectPage(reqVO);
    }

    private void validateContourTaskForCreateOrUpdate(Long id, String bizNo, ContourTaskSaveReqVO reqVO) {
        if (id != null) {
            validateContourTaskExists(id);
        }
        patientService.validatePatientExists(reqVO.getPatientId());
        doctorService.validateDoctorExists(reqVO.getContourDoctorId());
        if (reqVO.getCtAppointmentId() != null && reqVO.getCtAppointmentId() > 0) {
            CtAppointmentDO appointment = ctAppointmentService.getCtAppointment(reqVO.getCtAppointmentId());
            if (appointment == null) {
                throw exception(CT_APPOINTMENT_NOT_EXISTS);
            }
        }
        validateBizNoUnique(id, bizNo);
    }

    private ContourTaskDO validateContourTaskExists(Long id) {
        ContourTaskDO task = contourTaskMapper.selectById(id);
        if (task == null) {
            throw exception(CONTOUR_TASK_NOT_EXISTS);
        }
        return task;
    }

    private void validateBizNoUnique(Long id, String bizNo) {
        ContourTaskDO task = contourTaskMapper.selectByBizNo(bizNo);
        if (task == null) {
            return;
        }
        if (!Objects.equals(task.getId(), id)) {
            throw exception(CONTOUR_TASK_BIZ_NO_DUPLICATE);
        }
    }

    private String prepareBizNo(Long id, String bizNo) {
        if (StrUtil.isNotBlank(bizNo)) {
            return bizNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "TH" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            ContourTaskDO task = contourTaskMapper.selectByBizNo(generated);
            if (task == null || Objects.equals(task.getId(), id)) {
                return generated;
            }
        }
        return "TH" + System.currentTimeMillis();
    }

    private Long resolveAutoContourDoctorId(CtAppointmentDO appointment, PatientDO patient) {
        Long managerDoctorId = patient == null ? null : patient.getManagerDoctorId();
        if (managerDoctorId != null && managerDoctorId > 0) {
            return managerDoctorId;
        }
        if (appointment.getApplyDoctorId() != null && appointment.getApplyDoctorId() > 0) {
            return appointment.getApplyDoctorId();
        }
        List<DoctorDO> doctors = doctorService.getDoctorSimpleList();
        return doctors.isEmpty() ? 0L : doctors.get(0).getId();
    }

    private Long resolveStartUserId(ContourTaskDO task) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        return loginUserId != null ? loginUserId : task.getContourDoctorId();
    }

    private String startContourWorkflow(ContourTaskDO task, Long startUserId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("bizNo", task.getBizNo());
        variables.put("patientId", task.getPatientId());
        variables.put("patientName", task.getPatientName());
        variables.put("contourTaskId", task.getId());
        variables.put("contourDoctorId", task.getContourDoctorId());
        variables.put("contourDoctorName", task.getContourDoctorName());
        return processInstanceApi.createProcessInstance(startUserId,
                new BpmProcessInstanceCreateReqDTO()
                        .setProcessDefinitionKey(BPM_PROCESS_KEY)
                        .setBusinessKey(String.valueOf(task.getId()))
                        .setVariables(variables));
    }

    private void advanceWorkflowToReview(String processInstanceId, Long approverUserId) {
        for (int i = 0; i < BPM_ADVANCE_MAX_ROUNDS; i++) {
            List<Task> runningTasks = bpmTaskService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
            if (runningTasks.isEmpty()) {
                throw new IllegalStateException("勾画流程未找到可执行节点，流程实例=" + processInstanceId);
            }
            Task currentTask = runningTasks.get(0);
            if (StrUtil.equals(currentTask.getTaskDefinitionKey(), BPM_REVIEW_TASK_DEFINE_KEY)) {
                return;
            }
            assignTask(currentTask.getId(), approverUserId);
            BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
            approveReqVO.setId(currentTask.getId());
            approveReqVO.setReason("根据勾画业务进度自动流转");
            bpmTaskService.approveTask(approverUserId, approveReqVO);
        }
        throw new IllegalStateException("勾画流程推进到审核节点失败，流程实例=" + processInstanceId);
    }

    private void assignTask(String taskId, Long userId) {
        if (userId == null) {
            return;
        }
        flowableTaskService.setAssignee(taskId, String.valueOf(userId));
    }

    private void normalizeTask(ContourTaskDO task) {
        if (task.getCtAppointmentId() == null) {
            task.setCtAppointmentId(0L);
        }
        if (StrUtil.isBlank(task.getVersionNo())) {
            task.setVersionNo(DEFAULT_VERSION_NO);
        }
        if (StrUtil.isBlank(task.getWorkflowStatus())) {
            task.setWorkflowStatus(WORKFLOW_STATUS_WAIT_CONTOUR);
        }
        if (task.getStatus() == null) {
            task.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (task.getAttachmentUrl() == null) {
            task.setAttachmentUrl("");
        }
        if (task.getProcessInstanceId() == null) {
            task.setProcessInstanceId("");
        }
        if (task.getTreatmentSite() == null) {
            task.setTreatmentSite("");
        }
    }

}
