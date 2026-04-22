package cn.iocoder.yudao.module.hospital.service.treatmentexecute;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute.vo.TreatmentExecutePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentappointment.TreatmentAppointmentMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentexecute.TreatmentExecuteMapper;
import cn.iocoder.yudao.module.hospital.service.treatmentsummary.TreatmentSummaryService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_EXECUTE_COMPLETE_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_EXECUTE_EXECUTE_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_EXECUTE_NOT_EXISTS;

@Service
@Validated
public class TreatmentExecuteServiceImpl implements TreatmentExecuteService {

    private static final String APPOINTMENT_STATUS_DONE = "DONE";
    private static final String EXECUTE_RESULT_WAIT = "WAIT_EXECUTE";
    private static final String EXECUTE_RESULT_EXECUTING = "EXECUTING";
    private static final String EXECUTE_RESULT_DONE = "DONE";

    @Resource
    private TreatmentExecuteMapper treatmentExecuteMapper;
    @Resource
    private TreatmentAppointmentMapper treatmentAppointmentMapper;
    @Resource
    private TreatmentSummaryService treatmentSummaryService;

    @Override
    public void createFromQueue(TreatmentQueueDO queue) {
        if (queue == null || queue.getTreatmentAppointmentId() == null) {
            return;
        }
        if (treatmentExecuteMapper.selectByTreatmentAppointmentId(queue.getTreatmentAppointmentId()) != null) {
            return;
        }
        TreatmentAppointmentDO appointment = treatmentAppointmentMapper.selectById(queue.getTreatmentAppointmentId());
        if (appointment == null) {
            return;
        }
        TreatmentExecuteDO execute = new TreatmentExecuteDO();
        execute.setBizNo(generateBizNo());
        execute.setTreatmentAppointmentId(appointment.getId());
        execute.setPatientId(appointment.getPatientId());
        execute.setPatientName(appointment.getPatientName());
        execute.setTreatmentDate(appointment.getAppointmentDate() == null ? LocalDate.now() : appointment.getAppointmentDate());
        execute.setFractionNo(appointment.getFractionNo());
        execute.setExecutorId(appointment.getDoctorId());
        execute.setExecutorName(appointment.getDoctorName());
        execute.setPlannedDose(BigDecimal.ZERO);
        execute.setActualDose(BigDecimal.ZERO);
        execute.setExecuteResult(EXECUTE_RESULT_WAIT);
        execute.setStatus(CommonStatusEnum.ENABLE.getStatus());
        execute.setRemark(queue.getRemark());
        treatmentExecuteMapper.insert(execute);
    }

    @Override
    public PageResult<TreatmentExecuteDO> getTreatmentExecutePage(TreatmentExecutePageReqVO reqVO) {
        return treatmentExecuteMapper.selectPage(reqVO);
    }

    @Override
    public void executeTreatment(Long id) {
        TreatmentExecuteDO execute = validateTreatmentExecuteExists(id);
        if (!Objects.equals(execute.getExecuteResult(), EXECUTE_RESULT_WAIT)) {
            throw exception(TREATMENT_EXECUTE_EXECUTE_FAIL_STATUS);
        }
        TreatmentExecuteDO updateObj = new TreatmentExecuteDO();
        updateObj.setId(id);
        updateObj.setExecuteResult(EXECUTE_RESULT_EXECUTING);
        updateObj.setStartTime(LocalDateTime.now());
        treatmentExecuteMapper.updateById(updateObj);
    }

    @Override
    public void completeTreatment(Long id) {
        TreatmentExecuteDO execute = validateTreatmentExecuteExists(id);
        if (!Objects.equals(execute.getExecuteResult(), EXECUTE_RESULT_WAIT)
                && !Objects.equals(execute.getExecuteResult(), EXECUTE_RESULT_EXECUTING)) {
            throw exception(TREATMENT_EXECUTE_COMPLETE_FAIL_STATUS);
        }
        TreatmentExecuteDO updateObj = new TreatmentExecuteDO();
        updateObj.setId(id);
        updateObj.setExecuteResult(EXECUTE_RESULT_DONE);
        if (execute.getStartTime() == null) {
            updateObj.setStartTime(LocalDateTime.now());
        }
        updateObj.setFinishTime(LocalDateTime.now());
        if (execute.getActualDose() == null || BigDecimal.ZERO.compareTo(execute.getActualDose()) == 0) {
            updateObj.setActualDose(execute.getPlannedDose() == null ? BigDecimal.ZERO : execute.getPlannedDose());
        }
        treatmentExecuteMapper.updateById(updateObj);
        if (execute.getTreatmentAppointmentId() != null && execute.getTreatmentAppointmentId() > 0) {
            TreatmentAppointmentDO appointment = new TreatmentAppointmentDO();
            appointment.setId(execute.getTreatmentAppointmentId());
            appointment.setWorkflowStatus(APPOINTMENT_STATUS_DONE);
            treatmentAppointmentMapper.updateById(appointment);
        }
        execute.setExecuteResult(updateObj.getExecuteResult());
        execute.setFinishTime(updateObj.getFinishTime());
        if (updateObj.getStartTime() != null) {
            execute.setStartTime(updateObj.getStartTime());
        }
        if (updateObj.getActualDose() != null) {
            execute.setActualDose(updateObj.getActualDose());
        }
        treatmentSummaryService.syncFromTreatmentExecute(execute);
    }

    private TreatmentExecuteDO validateTreatmentExecuteExists(Long id) {
        TreatmentExecuteDO execute = treatmentExecuteMapper.selectById(id);
        if (execute == null) {
            throw exception(TREATMENT_EXECUTE_NOT_EXISTS);
        }
        return execute;
    }

    private String generateBizNo() {
        return "ZX" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
    }

}
