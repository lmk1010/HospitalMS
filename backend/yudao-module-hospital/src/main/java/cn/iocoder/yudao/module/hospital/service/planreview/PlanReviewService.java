package cn.iocoder.yudao.module.hospital.service.planreview;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planreview.PlanReviewDO;

public interface PlanReviewService {

    void createGroupLeaderReviewFromDesign(PlanDesignDO planDesign);

    void approvePlanReview(String reviewStage, PlanReviewAuditReqVO reqVO);

    void rejectPlanReview(String reviewStage, PlanReviewAuditReqVO reqVO);

    PlanReviewDO getPlanReview(Long id);

    PageResult<PlanReviewDO> getPlanReviewPage(PlanReviewPageReqVO reqVO, String reviewStage);

}
