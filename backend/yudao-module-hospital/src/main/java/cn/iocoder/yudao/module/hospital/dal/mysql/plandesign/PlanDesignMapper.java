package cn.iocoder.yudao.module.hospital.dal.mysql.plandesign;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlanDesignMapper extends BaseMapperX<PlanDesignDO> {

    default PageResult<PlanDesignDO> selectPage(PlanDesignPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PlanDesignDO>()
                .eqIfPresent(PlanDesignDO::getId, reqVO.getId())
                .likeIfPresent(PlanDesignDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(PlanDesignDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanDesignDO::getPlanApplyId, reqVO.getPlanApplyId())
                .eqIfPresent(PlanDesignDO::getDesignUserId, reqVO.getDesignUserId())
                .eqIfPresent(PlanDesignDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .inIfPresent(PlanDesignDO::getWorkflowStatus, reqVO.getWorkflowStatusList())
                .eqIfPresent(PlanDesignDO::getStatus, reqVO.getStatus())
                .orderByDesc(PlanDesignDO::getId));
    }

    default java.util.List<PlanDesignDO> selectListByReqVO(PlanDesignPageReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PlanDesignDO>()
                .eqIfPresent(PlanDesignDO::getId, reqVO.getId())
                .likeIfPresent(PlanDesignDO::getBizNo, reqVO.getBizNo())
                .likeIfPresent(PlanDesignDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanDesignDO::getPlanApplyId, reqVO.getPlanApplyId())
                .eqIfPresent(PlanDesignDO::getDesignUserId, reqVO.getDesignUserId())
                .eqIfPresent(PlanDesignDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .inIfPresent(PlanDesignDO::getWorkflowStatus, reqVO.getWorkflowStatusList())
                .eqIfPresent(PlanDesignDO::getStatus, reqVO.getStatus())
                .orderByDesc(PlanDesignDO::getId));
    }

    default PlanDesignDO selectByBizNo(String bizNo) {
        return selectOne(PlanDesignDO::getBizNo, bizNo);
    }

    default PlanDesignDO selectByPlanApplyId(Long planApplyId) {
        return selectFirstOne(PlanDesignDO::getPlanApplyId, planApplyId);
    }

}
