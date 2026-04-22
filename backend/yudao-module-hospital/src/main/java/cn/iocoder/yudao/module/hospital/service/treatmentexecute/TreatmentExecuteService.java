package cn.iocoder.yudao.module.hospital.service.treatmentexecute;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute.vo.TreatmentExecutePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;

public interface TreatmentExecuteService {

    void createFromQueue(TreatmentQueueDO queue);

    PageResult<TreatmentExecuteDO> getTreatmentExecutePage(TreatmentExecutePageReqVO reqVO);

    void executeTreatment(Long id);

    void completeTreatment(Long id);

}
