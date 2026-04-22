package cn.iocoder.yudao.module.hospital.dal.mysql.treatmentsummary;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummaryPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentsummary.TreatmentSummaryDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TreatmentSummaryMapper extends BaseMapperX<TreatmentSummaryDO> {

    default PageResult<TreatmentSummaryDO> selectPage(TreatmentSummaryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreatmentSummaryDO>()
                .eqIfPresent(TreatmentSummaryDO::getId, reqVO.getId())
                .likeIfPresent(TreatmentSummaryDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(TreatmentSummaryDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(TreatmentSummaryDO::getSummaryDoctorId, reqVO.getSummaryDoctorId())
                .eqIfPresent(TreatmentSummaryDO::getStatus, reqVO.getStatus())
                .orderByDesc(TreatmentSummaryDO::getSummaryTime)
                .orderByDesc(TreatmentSummaryDO::getId));
    }

    default TreatmentSummaryDO selectByBizNo(String bizNo) {
        return selectOne(TreatmentSummaryDO::getBizNo, bizNo);
    }

    default TreatmentSummaryDO selectLatestByPatientId(Long patientId) {
        return selectOne(new LambdaQueryWrapperX<TreatmentSummaryDO>()
                .eq(TreatmentSummaryDO::getPatientId, patientId)
                .orderByDesc(TreatmentSummaryDO::getSummaryTime)
                .orderByDesc(TreatmentSummaryDO::getId)
                .last("LIMIT 1"));
    }

}
