package cn.iocoder.yudao.module.hospital.dal.mysql.treatmentexecute;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute.vo.TreatmentExecutePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TreatmentExecuteMapper extends BaseMapperX<TreatmentExecuteDO> {

    default PageResult<TreatmentExecuteDO> selectPage(TreatmentExecutePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreatmentExecuteDO>()
                .eqIfPresent(TreatmentExecuteDO::getId, reqVO.getId())
                .likeIfPresent(TreatmentExecuteDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(TreatmentExecuteDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(TreatmentExecuteDO::getTreatmentDate, reqVO.getTreatmentDate())
                .eqIfPresent(TreatmentExecuteDO::getExecutorId, reqVO.getExecutorId())
                .eqIfPresent(TreatmentExecuteDO::getExecuteResult, reqVO.getExecuteResult())
                .eqIfPresent(TreatmentExecuteDO::getStatus, reqVO.getStatus())
                .orderByDesc(TreatmentExecuteDO::getTreatmentDate)
                .orderByDesc(TreatmentExecuteDO::getId));
    }

    default TreatmentExecuteDO selectByBizNo(String bizNo) {
        return selectOne(TreatmentExecuteDO::getBizNo, bizNo);
    }

    default TreatmentExecuteDO selectByTreatmentAppointmentId(Long treatmentAppointmentId) {
        return selectFirstOne(TreatmentExecuteDO::getTreatmentAppointmentId, treatmentAppointmentId);
    }

    default List<TreatmentExecuteDO> selectDoneListByPatientId(Long patientId, String executeResult) {
        return selectList(new LambdaQueryWrapperX<TreatmentExecuteDO>()
                .eq(TreatmentExecuteDO::getPatientId, patientId)
                .eq(TreatmentExecuteDO::getExecuteResult, executeResult)
                .orderByAsc(TreatmentExecuteDO::getTreatmentDate)
                .orderByAsc(TreatmentExecuteDO::getId));
    }

}
