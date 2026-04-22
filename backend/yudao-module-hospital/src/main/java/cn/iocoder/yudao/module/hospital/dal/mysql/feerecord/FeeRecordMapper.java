package cn.iocoder.yudao.module.hospital.dal.mysql.feerecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feerecord.FeeRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FeeRecordMapper extends BaseMapperX<FeeRecordDO> {

    default PageResult<FeeRecordDO> selectPage(FeeRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FeeRecordDO>()
                .likeIfPresent(FeeRecordDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(FeeRecordDO::getPatientId, reqVO.getPatientId())
                .likeIfPresent(FeeRecordDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(FeeRecordDO::getSourceType, reqVO.getSourceType())
                .likeIfPresent(FeeRecordDO::getItemName, reqVO.getItemName())
                .eqIfPresent(FeeRecordDO::getChargeUserId, reqVO.getChargeUserId())
                .eqIfPresent(FeeRecordDO::getSettlementStatus, reqVO.getSettlementStatus())
                .eqIfPresent(FeeRecordDO::getStatus, reqVO.getStatus())
                .eqIfPresent(FeeRecordDO::getChargeTime, reqVO.getChargeTime())
                .orderByDesc(FeeRecordDO::getChargeTime)
                .orderByDesc(FeeRecordDO::getId));
    }

    default FeeRecordDO selectByBizNo(String bizNo) {
        return selectOne(FeeRecordDO::getBizNo, bizNo);
    }

    default List<FeeRecordDO> selectListByPatientIdAndSettlementStatus(Long patientId, String settlementStatus) {
        return selectListByPatientIdAndIds(patientId, settlementStatus, null);
    }

    default List<FeeRecordDO> selectListByPatientIdAndIds(Long patientId, String settlementStatus, List<Long> ids) {
        return selectList(new LambdaQueryWrapperX<FeeRecordDO>()
                .eq(FeeRecordDO::getPatientId, patientId)
                .eq(FeeRecordDO::getSettlementStatus, settlementStatus)
                .inIfPresent(FeeRecordDO::getId, ids)
                .orderByAsc(FeeRecordDO::getId));
    }

}
