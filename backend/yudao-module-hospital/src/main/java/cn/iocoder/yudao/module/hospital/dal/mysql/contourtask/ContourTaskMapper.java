package cn.iocoder.yudao.module.hospital.dal.mysql.contourtask;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContourTaskMapper extends BaseMapperX<ContourTaskDO> {

    default PageResult<ContourTaskDO> selectPage(ContourTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContourTaskDO>()
                .eqIfPresent(ContourTaskDO::getId, reqVO.getId())
                .likeIfPresent(ContourTaskDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(ContourTaskDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(ContourTaskDO::getContourDoctorId, reqVO.getContourDoctorId())
                .likeIfPresent(ContourTaskDO::getTreatmentSite, reqVO.getTreatmentSite())
                .eqIfPresent(ContourTaskDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .inIfPresent(ContourTaskDO::getWorkflowStatus, reqVO.getWorkflowStatusList())
                .eqIfPresent(ContourTaskDO::getStatus, reqVO.getStatus())
                .orderByDesc(ContourTaskDO::getId));
    }

    default ContourTaskDO selectByBizNo(String bizNo) {
        return selectOne(ContourTaskDO::getBizNo, bizNo);
    }


    default ContourTaskDO selectByCtAppointmentId(Long ctAppointmentId) {
        return selectFirstOne(ContourTaskDO::getCtAppointmentId, ctAppointmentId);
    }
    default java.util.List<ContourTaskDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<ContourTaskDO>()
                .eq(ContourTaskDO::getWorkflowStatus, "APPROVED")
                .eq(ContourTaskDO::getStatus, 0)
                .orderByDesc(ContourTaskDO::getId));
    }

}
