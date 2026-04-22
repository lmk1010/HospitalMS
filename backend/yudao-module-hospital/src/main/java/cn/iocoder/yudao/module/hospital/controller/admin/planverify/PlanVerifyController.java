package cn.iocoder.yudao.module.hospital.controller.admin.planverify;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;
import cn.iocoder.yudao.module.hospital.service.planverify.PlanVerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 计划验证")
@RestController
@RequestMapping("/hospital/plan-verify")
@Validated
public class PlanVerifyController {

    @Resource
    private PlanVerifyService planVerifyService;

    @GetMapping("/get")
    @Operation(summary = "获得计划验证详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-verify:query')")
    public CommonResult<PlanVerifyRespVO> getPlanVerify(@RequestParam("id") Long id) {
        PlanVerifyDO verify = planVerifyService.getPlanVerify(id);
        return success(BeanUtils.toBean(verify, PlanVerifyRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得计划验证分页")
    @PreAuthorize("@ss.hasPermission('hospital:plan-verify:query')")
    public CommonResult<PageResult<PlanVerifyRespVO>> getPlanVerifyPage(@Validated PlanVerifyPageReqVO reqVO) {
        PageResult<PlanVerifyDO> pageResult = planVerifyService.getPlanVerifyPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), PlanVerifyRespVO.class), pageResult.getTotal()));
    }

    @PutMapping("/verify")
    @Operation(summary = "计划验证通过")
    @PreAuthorize("@ss.hasPermission('hospital:plan-verify:verify')")
    public CommonResult<Boolean> verifyPlan(@Valid @RequestBody PlanVerifyAuditReqVO reqVO) {
        planVerifyService.verifyPlan(reqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "计划验证不通过")
    @PreAuthorize("@ss.hasPermission('hospital:plan-verify:reject')")
    public CommonResult<Boolean> rejectPlanVerify(@Valid @RequestBody PlanVerifyAuditReqVO reqVO) {
        planVerifyService.rejectPlanVerify(reqVO);
        return success(true);
    }

}
