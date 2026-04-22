package cn.iocoder.yudao.module.hospital.controller.admin.plangroupreview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planreview.PlanReviewDO;
import cn.iocoder.yudao.module.hospital.service.planreview.PlanReviewService;
import cn.iocoder.yudao.module.hospital.service.planreview.PlanReviewServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 组长审核")
@RestController
@RequestMapping("/hospital/plan-group-review")
@Validated
public class PlanGroupReviewController {

    @Resource
    private PlanReviewService planReviewService;

    @GetMapping("/get")
    @Operation(summary = "获得组长审核详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-group-review:query')")
    public CommonResult<PlanReviewRespVO> getPlanGroupReview(@RequestParam("id") Long id) {
        PlanReviewDO review = planReviewService.getPlanReview(id);
        return success(BeanUtils.toBean(review, PlanReviewRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得组长审核分页")
    @PreAuthorize("@ss.hasPermission('hospital:plan-group-review:query')")
    public CommonResult<PageResult<PlanReviewRespVO>> getPlanGroupReviewPage(@Validated PlanReviewPageReqVO reqVO) {
        PageResult<PlanReviewDO> pageResult = planReviewService.getPlanReviewPage(reqVO, PlanReviewServiceImpl.STAGE_GROUP_LEADER);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), PlanReviewRespVO.class), pageResult.getTotal()));
    }

    @PutMapping("/approve")
    @Operation(summary = "组长审核通过")
    @PreAuthorize("@ss.hasPermission('hospital:plan-group-review:approve')")
    public CommonResult<Boolean> approvePlanGroupReview(@Valid @RequestBody PlanReviewAuditReqVO reqVO) {
        planReviewService.approvePlanReview(PlanReviewServiceImpl.STAGE_GROUP_LEADER, reqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "组长审核退回")
    @PreAuthorize("@ss.hasPermission('hospital:plan-group-review:reject')")
    public CommonResult<Boolean> rejectPlanGroupReview(@Valid @RequestBody PlanReviewAuditReqVO reqVO) {
        planReviewService.rejectPlanReview(PlanReviewServiceImpl.STAGE_GROUP_LEADER, reqVO);
        return success(true);
    }

}
