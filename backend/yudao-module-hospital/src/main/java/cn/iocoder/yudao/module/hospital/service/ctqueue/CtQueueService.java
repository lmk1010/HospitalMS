package cn.iocoder.yudao.module.hospital.service.ctqueue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.ctqueue.vo.CtQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctqueue.CtQueueDO;

public interface CtQueueService {

    void createQueueFromAppointment(CtAppointmentDO appointment);

    PageResult<CtQueueDO> getCtQueuePage(CtQueuePageReqVO reqVO);

    void callCtQueue(Long id);

    void skipCtQueue(Long id);

    void finishCtQueue(Long id);

}
