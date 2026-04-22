package cn.iocoder.yudao.module.hospital.service.ctappointment;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.ctappointment.CtAppointmentMapper;
import cn.iocoder.yudao.module.hospital.service.ctqueue.CtQueueService;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_APPOINTMENT_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_APPOINTMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_APPOINTMENT_SUBMIT_FAIL_STATUS;

@Service
@Validated
public class CtAppointmentServiceImpl implements CtAppointmentService {

    private static final String DEFAULT_APPOINTMENT_SLOT = "上午";
    private static final String DEFAULT_PRIORITY = "NORMAL";
    private static final String WORKFLOW_STATUS_PENDING = "PENDING";
    private static final String WORKFLOW_STATUS_BOOKED = "BOOKED";

    @Resource
    private CtAppointmentMapper ctAppointmentMapper;
    @Resource
    private PatientService patientService;
    @Resource
    private DoctorService doctorService;
    @Resource
    private CtQueueService ctQueueService;

    @Override
    public Long createCtAppointment(CtAppointmentSaveReqVO createReqVO) {
        String bizNo = prepareBizNo(null, createReqVO.getBizNo());
        validateCtAppointmentForCreateOrUpdate(null, bizNo, createReqVO);
        PatientDO patient = patientService.getPatient(createReqVO.getPatientId());
        DoctorDO doctor = doctorService.getDoctor(createReqVO.getApplyDoctorId());
        CtAppointmentDO appointment = BeanUtils.toBean(createReqVO, CtAppointmentDO.class);
        appointment.setBizNo(bizNo);
        appointment.setPatientName(patient.getName());
        appointment.setApplyDoctorName(doctor.getName());
        normalizeAppointment(appointment);
        ctAppointmentMapper.insert(appointment);
        return appointment.getId();
    }

    @Override
    public void updateCtAppointment(CtAppointmentSaveReqVO updateReqVO) {
        CtAppointmentDO existing = validateCtAppointmentExists(updateReqVO.getId());
        String bizNo = prepareBizNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getBizNo(), existing.getBizNo()));
        validateCtAppointmentForCreateOrUpdate(updateReqVO.getId(), bizNo, updateReqVO);
        PatientDO patient = patientService.getPatient(updateReqVO.getPatientId());
        DoctorDO doctor = doctorService.getDoctor(updateReqVO.getApplyDoctorId());
        CtAppointmentDO appointment = BeanUtils.toBean(updateReqVO, CtAppointmentDO.class);
        appointment.setBizNo(bizNo);
        appointment.setPatientName(patient.getName());
        appointment.setApplyDoctorName(doctor.getName());
        appointment.setWorkflowStatus(existing.getWorkflowStatus());
        appointment.setProcessInstanceId(existing.getProcessInstanceId());
        normalizeAppointment(appointment);
        ctAppointmentMapper.updateById(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCtAppointment(Long id) {
        CtAppointmentDO existing = validateCtAppointmentExists(id);
        if (!Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_PENDING)) {
            throw exception(CT_APPOINTMENT_SUBMIT_FAIL_STATUS);
        }
        CtAppointmentDO updateObj = new CtAppointmentDO();
        updateObj.setId(id);
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_BOOKED);
        ctAppointmentMapper.updateById(updateObj);
        existing.setWorkflowStatus(WORKFLOW_STATUS_BOOKED);
        ctQueueService.createQueueFromAppointment(existing);
    }

    @Override
    public void deleteCtAppointment(Long id) {
        validateCtAppointmentExists(id);
        ctAppointmentMapper.deleteById(id);
    }

    @Override
    public CtAppointmentDO getCtAppointment(Long id) {
        return ctAppointmentMapper.selectById(id);
    }

    @Override
    public PageResult<CtAppointmentDO> getCtAppointmentPage(CtAppointmentPageReqVO reqVO) {
        return ctAppointmentMapper.selectPage(reqVO);
    }

    @Override
    public List<CtAppointmentDO> getCtAppointmentScheduleList(CtAppointmentScheduleReqVO reqVO) {
        return ctAppointmentMapper.selectScheduleList(reqVO);
    }

    private void validateCtAppointmentForCreateOrUpdate(Long id, String bizNo, CtAppointmentSaveReqVO reqVO) {
        if (id != null) {
            validateCtAppointmentExists(id);
        }
        patientService.validatePatientExists(reqVO.getPatientId());
        doctorService.validateDoctorExists(reqVO.getApplyDoctorId());
        validateBizNoUnique(id, bizNo);
    }

    private CtAppointmentDO validateCtAppointmentExists(Long id) {
        CtAppointmentDO appointment = ctAppointmentMapper.selectById(id);
        if (appointment == null) {
            throw exception(CT_APPOINTMENT_NOT_EXISTS);
        }
        return appointment;
    }

    private void validateBizNoUnique(Long id, String bizNo) {
        CtAppointmentDO appointment = ctAppointmentMapper.selectByBizNo(bizNo);
        if (appointment == null) {
            return;
        }
        if (!Objects.equals(appointment.getId(), id)) {
            throw exception(CT_APPOINTMENT_BIZ_NO_DUPLICATE);
        }
    }

    private String prepareBizNo(Long id, String bizNo) {
        if (StrUtil.isNotBlank(bizNo)) {
            return bizNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "CT" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            CtAppointmentDO appointment = ctAppointmentMapper.selectByBizNo(generated);
            if (appointment == null || Objects.equals(appointment.getId(), id)) {
                return generated;
            }
        }
        return "CT" + System.currentTimeMillis();
    }

    private void normalizeAppointment(CtAppointmentDO appointment) {
        if (StrUtil.isBlank(appointment.getAppointmentSlot())) {
            appointment.setAppointmentSlot(DEFAULT_APPOINTMENT_SLOT);
        }
        if (StrUtil.isBlank(appointment.getPriority())) {
            appointment.setPriority(DEFAULT_PRIORITY);
        }
        if (StrUtil.isBlank(appointment.getWorkflowStatus())) {
            appointment.setWorkflowStatus(WORKFLOW_STATUS_PENDING);
        }
        if (appointment.getStatus() == null) {
            appointment.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (appointment.getProcessInstanceId() == null) {
            appointment.setProcessInstanceId("");
        }
        if (appointment.getCtRoomName() == null) {
            appointment.setCtRoomName("");
        }
        if (appointment.getCtDeviceName() == null) {
            appointment.setCtDeviceName("");
        }
        if (appointment.getPositionNote() == null) {
            appointment.setPositionNote("");
        }
    }

}
