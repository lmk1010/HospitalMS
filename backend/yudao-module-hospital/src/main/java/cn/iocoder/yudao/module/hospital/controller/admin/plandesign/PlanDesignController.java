package cn.iocoder.yudao.module.hospital.controller.admin.plandesign;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.service.plandesign.PlanDesignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 计划设计")
@RestController
@RequestMapping("/hospital/plan-design")
@Validated
public class PlanDesignController {

    @Resource
    private PlanDesignService planDesignService;

    @PostMapping("/create")
    @Operation(summary = "创建计划设计")
    @PreAuthorize("@ss.hasPermission('hospital:plan-design:create')")
    public CommonResult<Long> createPlanDesign(@Valid @RequestBody PlanDesignSaveReqVO createReqVO) {
        return success(planDesignService.createPlanDesign(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改计划设计")
    @PreAuthorize("@ss.hasPermission('hospital:plan-design:update')")
    public CommonResult<Boolean> updatePlanDesign(@Valid @RequestBody PlanDesignSaveReqVO updateReqVO) {
        planDesignService.updatePlanDesign(updateReqVO);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交计划设计")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-design:submit')")
    public CommonResult<Boolean> submitPlanDesign(@RequestParam("id") Long id) {
        planDesignService.submitPlanDesign(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除计划设计")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-design:delete')")
    public CommonResult<Boolean> deletePlanDesign(@RequestParam("id") Long id) {
        planDesignService.deletePlanDesign(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得计划设计详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-design:query')")
    public CommonResult<PlanDesignRespVO> getPlanDesign(@RequestParam("id") Long id) {
        PlanDesignDO design = planDesignService.getPlanDesign(id);
        return success(BeanUtils.toBean(design, PlanDesignRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得计划设计分页")
    @PreAuthorize("@ss.hasPermission('hospital:plan-design:query')")
    public CommonResult<PageResult<PlanDesignRespVO>> getPlanDesignPage(@Validated PlanDesignPageReqVO reqVO) {
        PageResult<PlanDesignDO> pageResult = planDesignService.getPlanDesignPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), PlanDesignRespVO.class), pageResult.getTotal()));
    }

}
