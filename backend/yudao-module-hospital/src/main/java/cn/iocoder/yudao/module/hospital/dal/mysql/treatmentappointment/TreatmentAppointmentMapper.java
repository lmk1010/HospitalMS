package cn.iocoder.yudao.module.hospital.dal.mysql.treatmentappointment;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo.TreatmentAppointmentScheduleReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TreatmentAppointmentMapper extends BaseMapperX<TreatmentAppointmentDO> {

    default PageResult<TreatmentAppointmentDO> selectPage(TreatmentAppointmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreatmentAppointmentDO>()
                .eqIfPresent(TreatmentAppointmentDO::getId, reqVO.getId())
                .likeIfPresent(TreatmentAppointmentDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(TreatmentAppointmentDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(TreatmentAppointmentDO::getPlanVerifyId, reqVO.getPlanVerifyId())
                .eqIfPresent(TreatmentAppointmentDO::getAppointmentDate, reqVO.getAppointmentDate())
                .eqIfPresent(TreatmentAppointmentDO::getDoctorId, reqVO.getDoctorId())
                .eqIfPresent(TreatmentAppointmentDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .eqIfPresent(TreatmentAppointmentDO::getStatus, reqVO.getStatus())
                .orderByDesc(TreatmentAppointmentDO::getId));
    }

    default List<TreatmentAppointmentDO> selectScheduleList(TreatmentAppointmentScheduleReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<TreatmentAppointmentDO>()
                .eqIfPresent(TreatmentAppointmentDO::getId, reqVO.getId())
                .betweenIfPresent(TreatmentAppointmentDO::getAppointmentDate, reqVO.getStartDate(), reqVO.getEndDate())
                .eqIfPresent(TreatmentAppointmentDO::getDoctorId, reqVO.getDoctorId())
                .likeIfPresent(TreatmentAppointmentDO::getTreatmentRoomName, reqVO.getTreatmentRoomName())
                .and(StrUtil.isNotBlank(reqVO.getKeyword()), wrapper -> wrapper
                        .like(TreatmentAppointmentDO::getBizNo, reqVO.getKeyword())
                        .or().like(TreatmentAppointmentDO::getPatientName, reqVO.getKeyword())
                        .or().like(TreatmentAppointmentDO::getDoctorName, reqVO.getKeyword())
                        .or().like(TreatmentAppointmentDO::getTreatmentRoomName, reqVO.getKeyword())
                        .or().like(TreatmentAppointmentDO::getDeviceName, reqVO.getKeyword()))
                .orderByAsc(TreatmentAppointmentDO::getAppointmentDate)
                .orderByAsc(TreatmentAppointmentDO::getId));
    }

    default TreatmentAppointmentDO selectByBizNo(String bizNo) {
        return selectOne(TreatmentAppointmentDO::getBizNo, bizNo);
    }

    default TreatmentAppointmentDO selectByPlanVerifyId(Long planVerifyId) {
        return selectFirstOne(TreatmentAppointmentDO::getPlanVerifyId, planVerifyId);
    }

}
