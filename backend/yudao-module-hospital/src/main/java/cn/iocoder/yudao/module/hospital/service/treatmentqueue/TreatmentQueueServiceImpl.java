package cn.iocoder.yudao.module.hospital.service.treatmentqueue;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentqueue.vo.TreatmentQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentqueue.TreatmentQueueMapper;
import cn.iocoder.yudao.module.hospital.service.treatmentexecute.TreatmentExecuteService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_QUEUE_CALL_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_QUEUE_FINISH_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_QUEUE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_QUEUE_SKIP_FAIL_STATUS;

@Service
@Validated
public class TreatmentQueueServiceImpl implements TreatmentQueueService {

    private static final String QUEUE_STATUS_WAIT_SIGN = "WAIT_SIGN";
    private static final String QUEUE_STATUS_QUEUING = "QUEUING";
    private static final String QUEUE_STATUS_CALLED = "CALLED";
    private static final String QUEUE_STATUS_SKIPPED = "SKIPPED";
    private static final String QUEUE_STATUS_DONE = "DONE";

    @Resource
    private TreatmentQueueMapper treatmentQueueMapper;
    @Resource
    private TreatmentExecuteService treatmentExecuteService;

    @Override
    public void createQueueFromAppointment(TreatmentAppointmentDO appointment) {
        if (appointment == null || appointment.getId() == null) {
            return;
        }
        if (treatmentQueueMapper.selectByTreatmentAppointmentId(appointment.getId()) != null) {
            return;
        }
        LocalDate queueDate = appointment.getAppointmentDate() == null ? LocalDate.now() : appointment.getAppointmentDate();
        int queueSeq = Math.toIntExact(treatmentQueueMapper.selectCountByQueueDate(queueDate)) + 1;
        TreatmentQueueDO queue = new TreatmentQueueDO();
        queue.setQueueNo(generateQueueNo(queueDate, queueSeq));
        queue.setTreatmentAppointmentId(appointment.getId());
        queue.setPatientId(appointment.getPatientId());
        queue.setPatientName(appointment.getPatientName());
        queue.setQueueDate(queueDate);
        queue.setQueueSeq(queueSeq);
        queue.setQueueStatus(QUEUE_STATUS_WAIT_SIGN);
        queue.setTreatmentRoomName(StrUtil.blankToDefault(appointment.getTreatmentRoomName(), ""));
        queue.setDeviceName(StrUtil.blankToDefault(appointment.getDeviceName(), ""));
        queue.setRemark(appointment.getRemark());
        treatmentQueueMapper.insert(queue);
    }

    @Override
    public PageResult<TreatmentQueueDO> getTreatmentQueuePage(TreatmentQueuePageReqVO reqVO) {
        return treatmentQueueMapper.selectPage(reqVO);
    }

    @Override
    public void callTreatmentQueue(Long id) {
        TreatmentQueueDO queue = validateTreatmentQueueExists(id);
        if (!Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_WAIT_SIGN)
                && !Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_QUEUING)
                && !Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_SKIPPED)) {
            throw exception(TREATMENT_QUEUE_CALL_FAIL_STATUS);
        }
        TreatmentQueueDO updateObj = new TreatmentQueueDO();
        updateObj.setId(id);
        updateObj.setQueueStatus(QUEUE_STATUS_CALLED);
        if (queue.getSignInTime() == null) {
            updateObj.setSignInTime(LocalDateTime.now());
        }
        updateObj.setCallTime(LocalDateTime.now());
        if (queue.getStartTime() == null) {
            updateObj.setStartTime(LocalDateTime.now());
        }
        treatmentQueueMapper.updateById(updateObj);
    }

    @Override
    public void skipTreatmentQueue(Long id) {
        TreatmentQueueDO queue = validateTreatmentQueueExists(id);
        if (!Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_WAIT_SIGN)
                && !Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_QUEUING)
                && !Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_CALLED)) {
            throw exception(TREATMENT_QUEUE_SKIP_FAIL_STATUS);
        }
        TreatmentQueueDO updateObj = new TreatmentQueueDO();
        updateObj.setId(id);
        updateObj.setQueueStatus(QUEUE_STATUS_SKIPPED);
        treatmentQueueMapper.updateById(updateObj);
    }

    @Override
    public void finishTreatmentQueue(Long id) {
        TreatmentQueueDO queue = validateTreatmentQueueExists(id);
        if (!Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_CALLED)) {
            throw exception(TREATMENT_QUEUE_FINISH_FAIL_STATUS);
        }
        TreatmentQueueDO updateObj = new TreatmentQueueDO();
        updateObj.setId(id);
        updateObj.setQueueStatus(QUEUE_STATUS_DONE);
        if (queue.getStartTime() == null) {
            updateObj.setStartTime(queue.getCallTime() == null ? LocalDateTime.now() : queue.getCallTime());
        }
        updateObj.setFinishTime(LocalDateTime.now());
        treatmentQueueMapper.updateById(updateObj);
        treatmentExecuteService.createFromQueue(queue);
    }

    private TreatmentQueueDO validateTreatmentQueueExists(Long id) {
        TreatmentQueueDO queue = treatmentQueueMapper.selectById(id);
        if (queue == null) {
            throw exception(TREATMENT_QUEUE_NOT_EXISTS);
        }
        return queue;
    }

    private String generateQueueNo(LocalDate queueDate, int queueSeq) {
        return "TQ" + queueDate.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%03d", queueSeq);
    }

}
