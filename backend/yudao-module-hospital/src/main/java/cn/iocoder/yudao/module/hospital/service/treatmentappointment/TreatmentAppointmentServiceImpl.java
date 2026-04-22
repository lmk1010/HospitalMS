package cn.iocoder.yudao.module.hospital.service.treatmentappointment;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.planverify.PlanVerifyMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentappointment.TreatmentAppointmentMapper;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.treatmentqueue.TreatmentQueueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PLAN_VERIFY_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_APPOINTMENT_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_APPOINTMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_APPOINTMENT_PLAN_VERIFY_STATUS_INVALID;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_APPOINTMENT_SUBMIT_FAIL_STATUS;

@Service
@Validated
public class TreatmentAppointmentServiceImpl implements TreatmentAppointmentService {

    private static final String WORKFLOW_STATUS_WAIT_APPOINTMENT = "WAIT_APPOINTMENT";
    private static final String WORKFLOW_STATUS_TREATING = "TREATING";

    @Resource
    private TreatmentAppointmentMapper treatmentAppointmentMapper;
    @Resource
    private PlanVerifyMapper planVerifyMapper;
    @Resource
    private DoctorService doctorService;
    @Resource
    private TreatmentQueueService treatmentQueueService;

    @Override
    public Long createTreatmentAppointment(TreatmentAppointmentSaveReqVO createReqVO) {
        String bizNo = prepareBizNo(null, createReqVO.getBizNo());
        PlanVerifyDO planVerify = validateCreateOrUpdate(null, bizNo, createReqVO);
        DoctorDO doctor = doctorService.getDoctor(createReqVO.getDoctorId());
        TreatmentAppointmentDO appointment = BeanUtils.toBean(createReqVO, TreatmentAppointmentDO.class);
        appointment.setBizNo(bizNo);
        appointment.setPatientId(planVerify.getPatientId());
        appointment.setPatientName(planVerify.getPatientName());
        appointment.setDoctorName(doctor.getName());
        appointment.setProcessInstanceId(planVerify.getProcessInstanceId());
        normalizeAppointment(appointment);
        treatmentAppointmentMapper.insert(appointment);
        return appointment.getId();
    }

    @Override
    public void updateTreatmentAppointment(TreatmentAppointmentSaveReqVO updateReqVO) {
        TreatmentAppointmentDO existing = validateTreatmentAppointmentExists(updateReqVO.getId());
        String bizNo = prepareBizNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getBizNo(), existing.getBizNo()));
        PlanVerifyDO planVerify = validateCreateOrUpdate(updateReqVO.getId(), bizNo, updateReqVO);
        DoctorDO doctor = doctorService.getDoctor(updateReqVO.getDoctorId());
        TreatmentAppointmentDO appointment = BeanUtils.toBean(updateReqVO, TreatmentAppointmentDO.class);
        appointment.setBizNo(bizNo);
        appointment.setPatientId(planVerify.getPatientId());
        appointment.setPatientName(planVerify.getPatientName());
        appointment.setDoctorName(doctor.getName());
        appointment.setWorkflowStatus(existing.getWorkflowStatus());
        appointment.setProcessInstanceId(existing.getProcessInstanceId());
        normalizeAppointment(appointment);
        treatmentAppointmentMapper.updateById(appointment);
    }

    @Override
    public void createFromPlanVerify(PlanVerifyDO planVerify) {
        if (planVerify == null || planVerify.getId() == null) {
            return;
        }
        if (treatmentAppointmentMapper.selectByPlanVerifyId(planVerify.getId()) != null) {
            return;
        }
        Long doctorId = resolveAutoTreatmentDoctorId(planVerify);
        DoctorDO doctor = doctorId != null && doctorId > 0 ? doctorService.getDoctor(doctorId) : null;

        TreatmentAppointmentDO appointment = new TreatmentAppointmentDO();
        appointment.setBizNo(prepareBizNo(null, null));
        appointment.setPatientId(planVerify.getPatientId());
        appointment.setPatientName(planVerify.getPatientName());
        appointment.setPlanVerifyId(planVerify.getId());
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setFractionNo(1);
        appointment.setDoctorId(doctor == null ? 0L : doctor.getId());
        appointment.setDoctorName(doctor == null ? "" : doctor.getName());
        appointment.setWorkflowStatus(WORKFLOW_STATUS_WAIT_APPOINTMENT);
        appointment.setProcessInstanceId(StrUtil.blankToDefault(planVerify.getProcessInstanceId(), ""));
        appointment.setStatus(CommonStatusEnum.ENABLE.getStatus());
        appointment.setRemark(StrUtil.blankToDefault(planVerify.getRemark(), ""));
        normalizeAppointment(appointment);
        treatmentAppointmentMapper.insert(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTreatmentAppointment(Long id) {
        TreatmentAppointmentDO existing = validateTreatmentAppointmentExists(id);
        if (!Objects.equals(existing.getWorkflowStatus(), WORKFLOW_STATUS_WAIT_APPOINTMENT)) {
            throw exception(TREATMENT_APPOINTMENT_SUBMIT_FAIL_STATUS);
        }
        TreatmentAppointmentDO updateObj = new TreatmentAppointmentDO();
        updateObj.setId(id);
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_TREATING);
        treatmentAppointmentMapper.updateById(updateObj);
        existing.setWorkflowStatus(WORKFLOW_STATUS_TREATING);
        treatmentQueueService.createQueueFromAppointment(existing);
    }

    @Override
    public void deleteTreatmentAppointment(Long id) {
        validateTreatmentAppointmentExists(id);
        treatmentAppointmentMapper.deleteById(id);
    }

    @Override
    public TreatmentAppointmentDO getTreatmentAppointment(Long id) {
        return treatmentAppointmentMapper.selectById(id);
    }

    @Override
    public PageResult<TreatmentAppointmentDO> getTreatmentAppointmentPage(TreatmentAppointmentPageReqVO reqVO) {
        return treatmentAppointmentMapper.selectPage(reqVO);
    }

    @Override
    public List<TreatmentAppointmentDO> getTreatmentAppointmentScheduleList(TreatmentAppointmentScheduleReqVO reqVO) {
        return treatmentAppointmentMapper.selectScheduleList(reqVO);
    }

    private PlanVerifyDO validateCreateOrUpdate(Long id, String bizNo, TreatmentAppointmentSaveReqVO reqVO) {
        if (id != null) {
            validateTreatmentAppointmentExists(id);
        }
        doctorService.validateDoctorExists(reqVO.getDoctorId());
        PlanVerifyDO planVerify = planVerifyMapper.selectById(reqVO.getPlanVerifyId());
        if (planVerify == null) {
            throw exception(PLAN_VERIFY_NOT_EXISTS);
        }
        if (!Objects.equals(planVerify.getWorkflowStatus(), "VERIFIED")) {
            throw exception(TREATMENT_APPOINTMENT_PLAN_VERIFY_STATUS_INVALID);
        }
        validateBizNoUnique(id, bizNo);
        return planVerify;
    }

    private Long resolveAutoTreatmentDoctorId(PlanVerifyDO planVerify) {
        if (planVerify.getVerifyUserId() != null && planVerify.getVerifyUserId() > 0) {
            return planVerify.getVerifyUserId();
        }
        List<DoctorDO> doctors = doctorService.getDoctorSimpleList();
        return doctors.isEmpty() ? 0L : doctors.get(0).getId();
    }

    private TreatmentAppointmentDO validateTreatmentAppointmentExists(Long id) {
        TreatmentAppointmentDO appointment = treatmentAppointmentMapper.selectById(id);
        if (appointment == null) {
            throw exception(TREATMENT_APPOINTMENT_NOT_EXISTS);
        }
        return appointment;
    }

    private void validateBizNoUnique(Long id, String bizNo) {
        TreatmentAppointmentDO appointment = treatmentAppointmentMapper.selectByBizNo(bizNo);
        if (appointment == null) {
            return;
        }
        if (!Objects.equals(appointment.getId(), id)) {
            throw exception(TREATMENT_APPOINTMENT_BIZ_NO_DUPLICATE);
        }
    }

    private String prepareBizNo(Long id, String bizNo) {
        if (StrUtil.isNotBlank(bizNo)) {
            return bizNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "ZL" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            TreatmentAppointmentDO appointment = treatmentAppointmentMapper.selectByBizNo(generated);
            if (appointment == null || Objects.equals(appointment.getId(), id)) {
                return generated;
            }
        }
        return "ZL" + System.currentTimeMillis();
    }

    private void normalizeAppointment(TreatmentAppointmentDO appointment) {
        if (appointment.getAppointmentDate() == null) {
            appointment.setAppointmentDate(LocalDate.now());
        }
        if (appointment.getFractionNo() == null || appointment.getFractionNo() <= 0) {
            appointment.setFractionNo(1);
        }
        if (StrUtil.isBlank(appointment.getWorkflowStatus())) {
            appointment.setWorkflowStatus(WORKFLOW_STATUS_WAIT_APPOINTMENT);
        }
        if (appointment.getStatus() == null) {
            appointment.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (appointment.getProcessInstanceId() == null) {
            appointment.setProcessInstanceId("");
        }
        if (appointment.getTreatmentRoomName() == null) {
            appointment.setTreatmentRoomName("");
        }
        if (appointment.getDeviceName() == null) {
            appointment.setDeviceName("");
        }
        if (appointment.getRemark() == null) {
            appointment.setRemark("");
        }
    }

}
