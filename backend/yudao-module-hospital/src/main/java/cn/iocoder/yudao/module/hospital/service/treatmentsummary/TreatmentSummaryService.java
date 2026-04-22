package cn.iocoder.yudao.module.hospital.service.treatmentsummary;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummaryPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummarySaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentsummary.TreatmentSummaryDO;

public interface TreatmentSummaryService {

    Long createTreatmentSummary(TreatmentSummarySaveReqVO createReqVO);

    void updateTreatmentSummary(TreatmentSummarySaveReqVO updateReqVO);

    void syncFromTreatmentExecute(TreatmentExecuteDO execute);

    TreatmentSummaryDO getTreatmentSummary(Long id);

    PageResult<TreatmentSummaryDO> getTreatmentSummaryPage(TreatmentSummaryPageReqVO reqVO);

}
