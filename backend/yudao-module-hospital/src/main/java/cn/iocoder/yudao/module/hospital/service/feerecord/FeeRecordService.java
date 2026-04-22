package cn.iocoder.yudao.module.hospital.service.feerecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feerecord.FeeRecordDO;

public interface FeeRecordService {

    Long createFeeRecord(FeeRecordSaveReqVO createReqVO);

    void updateFeeRecord(FeeRecordSaveReqVO updateReqVO);

    void deleteFeeRecord(Long id);

    FeeRecordDO getFeeRecord(Long id);

    PageResult<FeeRecordDO> getFeeRecordPage(FeeRecordPageReqVO reqVO);

}
