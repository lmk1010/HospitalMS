package cn.iocoder.yudao.module.hospital.dal.mysql.planapply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplyPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlanApplyMapper extends BaseMapperX<PlanApplyDO> {

    default PageResult<PlanApplyDO> selectPage(PlanApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PlanApplyDO>()
                .eqIfPresent(PlanApplyDO::getId, reqVO.getId())
                .likeIfPresent(PlanApplyDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(PlanApplyDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanApplyDO::getContourTaskId, reqVO.getContourTaskId())
                .eqIfPresent(PlanApplyDO::getApplyDoctorId, reqVO.getApplyDoctorId())
                .eqIfPresent(PlanApplyDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .inIfPresent(PlanApplyDO::getWorkflowStatus, reqVO.getWorkflowStatusList())
                .eqIfPresent(PlanApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(PlanApplyDO::getId));
    }


    default java.util.List<PlanApplyDO> selectListByReqVO(PlanApplyPageReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PlanApplyDO>()
                .eqIfPresent(PlanApplyDO::getId, reqVO.getId())
                .likeIfPresent(PlanApplyDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(PlanApplyDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanApplyDO::getContourTaskId, reqVO.getContourTaskId())
                .eqIfPresent(PlanApplyDO::getApplyDoctorId, reqVO.getApplyDoctorId())
                .eqIfPresent(PlanApplyDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .inIfPresent(PlanApplyDO::getWorkflowStatus, reqVO.getWorkflowStatusList())
                .eqIfPresent(PlanApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(PlanApplyDO::getId));
    }

    default PlanApplyDO selectByBizNo(String bizNo) {
        return selectOne(PlanApplyDO::getBizNo, bizNo);
    }


    default PlanApplyDO selectByContourTaskId(Long contourTaskId) {
        return selectFirstOne(PlanApplyDO::getContourTaskId, contourTaskId);
    }
    default java.util.List<PlanApplyDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<PlanApplyDO>()
                .eq(PlanApplyDO::getStatus, 0)
                .in(PlanApplyDO::getWorkflowStatus, java.util.Arrays.asList("REVIEWING", "APPROVED"))
                .orderByDesc(PlanApplyDO::getId));
    }

}
