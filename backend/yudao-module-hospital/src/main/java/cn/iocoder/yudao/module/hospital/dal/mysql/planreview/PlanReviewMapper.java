package cn.iocoder.yudao.module.hospital.dal.mysql.planreview;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planreview.PlanReviewDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlanReviewMapper extends BaseMapperX<PlanReviewDO> {

    default PageResult<PlanReviewDO> selectPage(PlanReviewPageReqVO reqVO, String reviewStage) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PlanReviewDO>()
                .eqIfPresent(PlanReviewDO::getId, reqVO.getId())
                .likeIfPresent(PlanReviewDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(PlanReviewDO::getPlanDesignId, reqVO.getPlanDesignId())
                .likeIfPresent(PlanReviewDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanReviewDO::getReviewDoctorId, reqVO.getReviewDoctorId())
                .eqIfPresent(PlanReviewDO::getReviewResult, reqVO.getReviewResult())
                .eqIfPresent(PlanReviewDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .eqIfPresent(PlanReviewDO::getStatus, reqVO.getStatus())
                .eq(PlanReviewDO::getReviewStage, reviewStage)
                .orderByDesc(PlanReviewDO::getId));
    }

    default java.util.List<PlanReviewDO> selectListByReqVO(PlanReviewPageReqVO reqVO, String reviewStage) {
        return selectList(new LambdaQueryWrapperX<PlanReviewDO>()
                .eqIfPresent(PlanReviewDO::getId, reqVO.getId())
                .likeIfPresent(PlanReviewDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(PlanReviewDO::getPlanDesignId, reqVO.getPlanDesignId())
                .likeIfPresent(PlanReviewDO::getPatientName, reqVO.getPatientName())
                .eqIfPresent(PlanReviewDO::getReviewDoctorId, reqVO.getReviewDoctorId())
                .eqIfPresent(PlanReviewDO::getReviewResult, reqVO.getReviewResult())
                .eqIfPresent(PlanReviewDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .eqIfPresent(PlanReviewDO::getStatus, reqVO.getStatus())
                .eq(PlanReviewDO::getReviewStage, reviewStage)
                .orderByDesc(PlanReviewDO::getId));
    }

    default PlanReviewDO selectByBizNo(String bizNo) {
        return selectOne(PlanReviewDO::getBizNo, bizNo);
    }

}
