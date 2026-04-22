package cn.iocoder.yudao.module.hospital.service.ctqueue;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.ctqueue.vo.CtQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctqueue.CtQueueDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.ctappointment.CtAppointmentMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.ctqueue.CtQueueMapper;
import cn.iocoder.yudao.module.hospital.service.contourtask.ContourTaskService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_QUEUE_CALL_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_QUEUE_FINISH_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_QUEUE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.CT_QUEUE_SKIP_FAIL_STATUS;

@Service
@Validated
public class CtQueueServiceImpl implements CtQueueService {

    private static final String APPOINTMENT_STATUS_DONE = "DONE";
    private static final String DEFAULT_WINDOW_NAME = "CT排队屏";
    private static final String QUEUE_STATUS_CALLED = "CALLED";
    private static final String QUEUE_STATUS_DONE = "DONE";
    private static final String QUEUE_STATUS_QUEUING = "QUEUING";
    private static final String QUEUE_STATUS_SKIPPED = "SKIPPED";

    @Resource
    private CtQueueMapper ctQueueMapper;
    @Resource
    private CtAppointmentMapper ctAppointmentMapper;
    @Lazy
    @Resource
    private ContourTaskService contourTaskService;

    @Override
    public void createQueueFromAppointment(CtAppointmentDO appointment) {
        if (appointment == null || appointment.getId() == null) {
            return;
        }
        if (ctQueueMapper.selectByAppointmentId(appointment.getId()) != null) {
            return;
        }
        LocalDate queueDate = appointment.getAppointmentDate() == null ? LocalDate.now() : appointment.getAppointmentDate();
        int queueSeq = Math.toIntExact(ctQueueMapper.selectCountByQueueDate(queueDate)) + 1;
        CtQueueDO queue = new CtQueueDO();
        queue.setQueueNo(generateQueueNo(queueDate, queueSeq));
        queue.setAppointmentId(appointment.getId());
        queue.setPatientId(appointment.getPatientId());
        queue.setPatientName(appointment.getPatientName());
        queue.setQueueDate(queueDate);
        queue.setQueueSeq(queueSeq);
        queue.setSignInTime(LocalDateTime.now());
        queue.setQueueStatus(QUEUE_STATUS_QUEUING);
        queue.setWindowName(StrUtil.blankToDefault(appointment.getCtRoomName(), DEFAULT_WINDOW_NAME));
        queue.setCtRoomName(StrUtil.blankToDefault(appointment.getCtRoomName(), ""));
        queue.setRemark(appointment.getRemark());
        ctQueueMapper.insert(queue);
    }

    @Override
    public PageResult<CtQueueDO> getCtQueuePage(CtQueuePageReqVO reqVO) {
        return ctQueueMapper.selectPage(reqVO);
    }

    @Override
    public void callCtQueue(Long id) {
        CtQueueDO queue = validateCtQueueExists(id);
        if (!Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_QUEUING)
                && !Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_SKIPPED)) {
            throw exception(CT_QUEUE_CALL_FAIL_STATUS);
        }
        CtQueueDO updateObj = new CtQueueDO();
        updateObj.setId(id);
        updateObj.setQueueStatus(QUEUE_STATUS_CALLED);
        updateObj.setCallTime(LocalDateTime.now());
        ctQueueMapper.updateById(updateObj);
    }

    @Override
    public void skipCtQueue(Long id) {
        CtQueueDO queue = validateCtQueueExists(id);
        if (!Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_QUEUING)
                && !Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_CALLED)) {
            throw exception(CT_QUEUE_SKIP_FAIL_STATUS);
        }
        CtQueueDO updateObj = new CtQueueDO();
        updateObj.setId(id);
        updateObj.setQueueStatus(QUEUE_STATUS_SKIPPED);
        ctQueueMapper.updateById(updateObj);
    }

    @Override
    public void finishCtQueue(Long id) {
        CtQueueDO queue = validateCtQueueExists(id);
        if (!Objects.equals(queue.getQueueStatus(), QUEUE_STATUS_CALLED)) {
            throw exception(CT_QUEUE_FINISH_FAIL_STATUS);
        }
        CtQueueDO updateObj = new CtQueueDO();
        updateObj.setId(id);
        updateObj.setQueueStatus(QUEUE_STATUS_DONE);
        updateObj.setFinishTime(LocalDateTime.now());
        ctQueueMapper.updateById(updateObj);
        if (queue.getAppointmentId() != null && queue.getAppointmentId() > 0) {
            CtAppointmentDO appointment = ctAppointmentMapper.selectById(queue.getAppointmentId());
            if (appointment != null) {
                CtAppointmentDO updateAppointment = new CtAppointmentDO();
                updateAppointment.setId(appointment.getId());
                updateAppointment.setWorkflowStatus(APPOINTMENT_STATUS_DONE);
                ctAppointmentMapper.updateById(updateAppointment);
                appointment.setWorkflowStatus(APPOINTMENT_STATUS_DONE);
                contourTaskService.createFromCtAppointment(appointment);
            }
        }
    }

    private CtQueueDO validateCtQueueExists(Long id) {
        CtQueueDO queue = ctQueueMapper.selectById(id);
        if (queue == null) {
            throw exception(CT_QUEUE_NOT_EXISTS);
        }
        return queue;
    }

    private String generateQueueNo(LocalDate queueDate, int queueSeq) {
        return "Q" + queueDate.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%03d", queueSeq);
    }

}
