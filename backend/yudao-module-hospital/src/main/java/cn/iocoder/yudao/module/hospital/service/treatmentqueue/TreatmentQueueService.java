package cn.iocoder.yudao.module.hospital.service.treatmentqueue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentqueue.vo.TreatmentQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;

public interface TreatmentQueueService {

    void createQueueFromAppointment(TreatmentAppointmentDO appointment);

    PageResult<TreatmentQueueDO> getTreatmentQueuePage(TreatmentQueuePageReqVO reqVO);

    void callTreatmentQueue(Long id);

    void skipTreatmentQueue(Long id);

    void finishTreatmentQueue(Long id);

}
