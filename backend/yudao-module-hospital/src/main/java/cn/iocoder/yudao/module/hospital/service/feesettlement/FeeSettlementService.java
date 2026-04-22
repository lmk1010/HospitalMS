package cn.iocoder.yudao.module.hospital.service.feesettlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementSettleReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feesettlement.FeeSettlementDO;

public interface FeeSettlementService {

    Long settleFee(FeeSettlementSettleReqVO reqVO);

    void reverseFeeSettlement(Long id);

    PageResult<FeeSettlementDO> getFeeSettlementPage(FeeSettlementPageReqVO reqVO);

}
