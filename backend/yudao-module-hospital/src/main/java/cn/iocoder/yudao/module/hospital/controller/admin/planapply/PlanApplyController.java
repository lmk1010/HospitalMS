package cn.iocoder.yudao.module.hospital.controller.admin.planapply;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplyPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplyRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplySaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplySimpleRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;
import cn.iocoder.yudao.module.hospital.service.planapply.PlanApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 计划申请")
@RestController
@RequestMapping("/hospital/plan-apply")
@Validated
public class PlanApplyController {

    @Resource
    private PlanApplyService planApplyService;

    @PostMapping("/create")
    @Operation(summary = "创建计划申请")
    @PreAuthorize("@ss.hasPermission('hospital:plan-apply:create')")
    public CommonResult<Long> createPlanApply(@Valid @RequestBody PlanApplySaveReqVO createReqVO) {
        return success(planApplyService.createPlanApply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改计划申请")
    @PreAuthorize("@ss.hasPermission('hospital:plan-apply:update')")
    public CommonResult<Boolean> updatePlanApply(@Valid @RequestBody PlanApplySaveReqVO updateReqVO) {
        planApplyService.updatePlanApply(updateReqVO);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交计划申请")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-apply:submit')")
    public CommonResult<Boolean> submitPlanApply(@RequestParam("id") Long id) {
        planApplyService.submitPlanApply(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除计划申请")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-apply:delete')")
    public CommonResult<Boolean> deletePlanApply(@RequestParam("id") Long id) {
        planApplyService.deletePlanApply(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得计划申请详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:plan-apply:query')")
    public CommonResult<PlanApplyRespVO> getPlanApply(@RequestParam("id") Long id) {
        PlanApplyDO apply = planApplyService.getPlanApply(id);
        return success(BeanUtils.toBean(apply, PlanApplyRespVO.class));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获得计划申请精简列表")
    public CommonResult<List<PlanApplySimpleRespVO>> getPlanApplySimpleList() {
        return success(planApplyService.getPlanApplySimpleList().stream().map(this::buildSimpleRespVO).collect(Collectors.toList()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得计划申请分页")
    @PreAuthorize("@ss.hasPermission('hospital:plan-apply:query')")
    public CommonResult<PageResult<PlanApplyRespVO>> getPlanApplyPage(@Validated PlanApplyPageReqVO reqVO) {
        PageResult<PlanApplyDO> pageResult = planApplyService.getPlanApplyPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), PlanApplyRespVO.class), pageResult.getTotal()));
    }

    private PlanApplySimpleRespVO buildSimpleRespVO(PlanApplyDO apply) {
        PlanApplySimpleRespVO respVO = BeanUtils.toBean(apply, PlanApplySimpleRespVO.class);
        respVO.setDisplayName(apply.getBizNo() + " / " + apply.getPatientName() + (apply.getTreatmentSite() == null || apply.getTreatmentSite().isEmpty() ? "" : " / " + apply.getTreatmentSite()));
        return respVO;
    }

}
