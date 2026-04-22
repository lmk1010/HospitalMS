package cn.iocoder.yudao.module.hospital.dal.mysql.planverify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlanVerifyMapper extends BaseMapperX<PlanVerifyDO> {

    default PageResult<PlanVerifyDO> selectPage(PlanVerifyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PlanVerifyDO>()
                .likeIfPresent(PlanVerifyDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(PlanVerifyDO::getPlanDesignId, reqVO.getPlanDesignId())
                .likeIfPresent(PlanVerifyDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanVerifyDO::getVerifyUserId, reqVO.getVerifyUserId())
                .eqIfPresent(PlanVerifyDO::getVerifyResult, reqVO.getVerifyResult())
                .eqIfPresent(PlanVerifyDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .eqIfPresent(PlanVerifyDO::getStatus, reqVO.getStatus())
                .orderByDesc(PlanVerifyDO::getId));
    }

    default java.util.List<PlanVerifyDO> selectListByReqVO(PlanVerifyPageReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PlanVerifyDO>()
                .likeIfPresent(PlanVerifyDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(PlanVerifyDO::getPlanDesignId, reqVO.getPlanDesignId())
                .likeIfPresent(PlanVerifyDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanVerifyDO::getVerifyUserId, reqVO.getVerifyUserId())
                .eqIfPresent(PlanVerifyDO::getVerifyResult, reqVO.getVerifyResult())
                .eqIfPresent(PlanVerifyDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .eqIfPresent(PlanVerifyDO::getStatus, reqVO.getStatus())
                .orderByDesc(PlanVerifyDO::getId));
    }

    default PlanVerifyDO selectByBizNo(String bizNo) {
        return selectOne(PlanVerifyDO::getBizNo, bizNo);
    }

}
