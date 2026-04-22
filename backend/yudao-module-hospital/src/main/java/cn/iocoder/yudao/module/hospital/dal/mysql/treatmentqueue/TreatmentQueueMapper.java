package cn.iocoder.yudao.module.hospital.dal.mysql.treatmentqueue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentqueue.vo.TreatmentQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

@Mapper
public interface TreatmentQueueMapper extends BaseMapperX<TreatmentQueueDO> {

    default PageResult<TreatmentQueueDO> selectPage(TreatmentQueuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreatmentQueueDO>()
                .eqIfPresent(TreatmentQueueDO::getId, reqVO.getId())
                .likeIfPresent(TreatmentQueueDO::getQueueNo, reqVO.getQueueNo())
                .likeIfPresent(TreatmentQueueDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(TreatmentQueueDO::getQueueDate, reqVO.getQueueDate())
                .eqIfPresent(TreatmentQueueDO::getQueueStatus, reqVO.getQueueStatus())
                .likeIfPresent(TreatmentQueueDO::getTreatmentRoomName, reqVO.getTreatmentRoomName())
                .orderByDesc(TreatmentQueueDO::getQueueDate)
                .orderByAsc(TreatmentQueueDO::getQueueSeq)
                .orderByDesc(TreatmentQueueDO::getId));
    }

    default Long selectCountByQueueDate(LocalDate queueDate) {
        return selectCount(TreatmentQueueDO::getQueueDate, queueDate);
    }

    default TreatmentQueueDO selectByTreatmentAppointmentId(Long treatmentAppointmentId) {
        return selectFirstOne(TreatmentQueueDO::getTreatmentAppointmentId, treatmentAppointmentId);
    }

}
