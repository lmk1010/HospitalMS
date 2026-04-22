package cn.iocoder.yudao.module.hospital.dal.mysql.ctqueue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.ctqueue.vo.CtQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctqueue.CtQueueDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

@Mapper
public interface CtQueueMapper extends BaseMapperX<CtQueueDO> {

    default PageResult<CtQueueDO> selectPage(CtQueuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CtQueueDO>()
                .eqIfPresent(CtQueueDO::getId, reqVO.getId())
                .likeIfPresent(CtQueueDO::getQueueNo, reqVO.getQueueNo())
                .likeIfPresent(CtQueueDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(CtQueueDO::getQueueDate, reqVO.getQueueDate())
                .eqIfPresent(CtQueueDO::getQueueStatus, reqVO.getQueueStatus())
                .likeIfPresent(CtQueueDO::getCtRoomName, reqVO.getCtRoomName())
                .orderByDesc(CtQueueDO::getQueueDate)
                .orderByAsc(CtQueueDO::getQueueSeq)
                .orderByDesc(CtQueueDO::getId));
    }

    default Long selectCountByQueueDate(LocalDate queueDate) {
        return selectCount(CtQueueDO::getQueueDate, queueDate);
    }

    default CtQueueDO selectByAppointmentId(Long appointmentId) {
        return selectFirstOne(CtQueueDO::getAppointmentId, appointmentId);
    }

}
