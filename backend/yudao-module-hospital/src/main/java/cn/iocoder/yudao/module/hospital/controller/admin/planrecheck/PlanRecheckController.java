package cn.iocoder.yudao.module.hospital.controller.admin.planrecheck;

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

@Tag(name = "管理后台 - 计划复核")
@RestController
@RequestMapping("/hospital/plan-recheck")
@Validated
public class PlanRecheckController {

    @Resource
    private PlanReviewService planReviewService;

    @GetMapping("/get")
    @Operation(summary = "获得计划复核详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-recheck:query')")
    public CommonResult<PlanReviewRespVO> getPlanRecheck(@RequestParam("id") Long id) {
        PlanReviewDO review = planReviewService.getPlanReview(id);
        return success(BeanUtils.toBean(review, PlanReviewRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得计划复核分页")
    @PreAuthorize("@ss.hasPermission('hospital:plan-recheck:query')")
    public CommonResult<PageResult<PlanReviewRespVO>> getPlanRecheckPage(@Validated PlanReviewPageReqVO reqVO) {
        PageResult<PlanReviewDO> pageResult = planReviewService.getPlanReviewPage(reqVO, PlanReviewServiceImpl.STAGE_RECHECK);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), PlanReviewRespVO.class), pageResult.getTotal()));
    }

    @PutMapping("/approve")
    @Operation(summary = "计划复核通过")
    @PreAuthorize("@ss.hasPermission('hospital:plan-recheck:approve')")
    public CommonResult<Boolean> approvePlanRecheck(@Valid @RequestBody PlanReviewAuditReqVO reqVO) {
        planReviewService.approvePlanReview(PlanReviewServiceImpl.STAGE_RECHECK, reqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "计划复核退回")
    @PreAuthorize("@ss.hasPermission('hospital:plan-recheck:reject')")
    public CommonResult<Boolean> rejectPlanRecheck(@Valid @RequestBody PlanReviewAuditReqVO reqVO) {
        planReviewService.rejectPlanReview(PlanReviewServiceImpl.STAGE_RECHECK, reqVO);
        return success(true);
    }

}
