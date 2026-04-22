package cn.iocoder.yudao.module.hospital.dal.mysql.ctappointment;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CtAppointmentMapper extends BaseMapperX<CtAppointmentDO> {

    default PageResult<CtAppointmentDO> selectPage(CtAppointmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CtAppointmentDO>()
                .likeIfPresent(CtAppointmentDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(CtAppointmentDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(CtAppointmentDO::getApplyDoctorId, reqVO.getApplyDoctorId())
                .eqIfPresent(CtAppointmentDO::getAppointmentDate, reqVO.getAppointmentDate())
                .eqIfPresent(CtAppointmentDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .eqIfPresent(CtAppointmentDO::getStatus, reqVO.getStatus())
                .orderByDesc(CtAppointmentDO::getAppointmentDate)
                .orderByDesc(CtAppointmentDO::getId));
    }

    default List<CtAppointmentDO> selectScheduleList(CtAppointmentScheduleReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CtAppointmentDO>()
                .betweenIfPresent(CtAppointmentDO::getAppointmentDate, reqVO.getStartDate(), reqVO.getEndDate())
                .eqIfPresent(CtAppointmentDO::getApplyDoctorId, reqVO.getApplyDoctorId())
                .likeIfPresent(CtAppointmentDO::getCtRoomName, reqVO.getCtRoomName())
                .and(StrUtil.isNotBlank(reqVO.getKeyword()), wrapper -> wrapper
                        .like(CtAppointmentDO::getBizNo, reqVO.getKeyword())
                        .or().like(CtAppointmentDO::getPatientName, reqVO.getKeyword())
                        .or().like(CtAppointmentDO::getApplyDoctorName, reqVO.getKeyword())
                        .or().like(CtAppointmentDO::getCtRoomName, reqVO.getKeyword())
                        .or().like(CtAppointmentDO::getCtDeviceName, reqVO.getKeyword()))
                .orderByAsc(CtAppointmentDO::getAppointmentDate)
                .orderByAsc(CtAppointmentDO::getId));
    }

    default CtAppointmentDO selectByBizNo(String bizNo) {
        return selectOne(CtAppointmentDO::getBizNo, bizNo);
    }

}
