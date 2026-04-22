package cn.iocoder.yudao.module.hospital.controller.admin.contourreview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourreview.ContourReviewDO;
import cn.iocoder.yudao.module.hospital.service.contourreview.ContourReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 勾画审核")
@RestController
@RequestMapping("/hospital/contour-review")
@Validated
public class ContourReviewController {

    @Resource
    private ContourReviewService contourReviewService;

    @GetMapping("/get")
    @Operation(summary = "获得勾画审核详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:contour-review:query')")
    public CommonResult<ContourReviewRespVO> getContourReview(@RequestParam("id") Long id) {
        ContourReviewDO review = contourReviewService.getContourReview(id);
        return success(BeanUtils.toBean(review, ContourReviewRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得勾画审核分页")
    @PreAuthorize("@ss.hasPermission('hospital:contour-review:query')")
    public CommonResult<PageResult<ContourReviewRespVO>> getContourReviewPage(@Validated ContourReviewPageReqVO reqVO) {
        PageResult<ContourReviewDO> pageResult = contourReviewService.getContourReviewPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), ContourReviewRespVO.class), pageResult.getTotal()));
    }

    @PutMapping("/approve")
    @Operation(summary = "勾画审核通过")
    @PreAuthorize("@ss.hasPermission('hospital:contour-review:approve')")
    public CommonResult<Boolean> approveContourReview(@Valid @RequestBody ContourReviewAuditReqVO reqVO) {
        contourReviewService.approveContourReview(reqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "勾画审核退回")
    @PreAuthorize("@ss.hasPermission('hospital:contour-review:reject')")
    public CommonResult<Boolean> rejectContourReview(@Valid @RequestBody ContourReviewAuditReqVO reqVO) {
        contourReviewService.rejectContourReview(reqVO);
        return success(true);
    }

}
