package cn.iocoder.yudao.module.hospital.dal.mysql.feesettlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feesettlement.FeeSettlementDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeeSettlementMapper extends BaseMapperX<FeeSettlementDO> {

    default PageResult<FeeSettlementDO> selectPage(FeeSettlementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FeeSettlementDO>()
                .likeIfPresent(FeeSettlementDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(FeeSettlementDO::getPatientId, reqVO.getPatientId())
                .likeIfPresent(FeeSettlementDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(FeeSettlementDO::getPaymentType, reqVO.getPaymentType())
                .eqIfPresent(FeeSettlementDO::getCashierId, reqVO.getCashierId())
                .eqIfPresent(FeeSettlementDO::getSettlementStatus, reqVO.getSettlementStatus())
                .eqIfPresent(FeeSettlementDO::getStatus, reqVO.getStatus())
                .orderByDesc(FeeSettlementDO::getPayTime)
                .orderByDesc(FeeSettlementDO::getId));
    }

    default FeeSettlementDO selectByBizNo(String bizNo) {
        return selectOne(FeeSettlementDO::getBizNo, bizNo);
    }

}
