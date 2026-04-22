package cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummaryPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummaryRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummarySaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentsummary.TreatmentSummaryDO;
import cn.iocoder.yudao.module.hospital.service.treatmentsummary.TreatmentSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 治疗小结")
@RestController
@RequestMapping("/hospital/treatment-summary")
@Validated
public class TreatmentSummaryController {

    @Resource
    private TreatmentSummaryService treatmentSummaryService;

    @PostMapping("/create")
    @Operation(summary = "创建治疗小结")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-summary:create')")
    public CommonResult<Long> createTreatmentSummary(@Valid @RequestBody TreatmentSummarySaveReqVO createReqVO) {
        return success(treatmentSummaryService.createTreatmentSummary(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改治疗小结")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-summary:update')")
    public CommonResult<Boolean> updateTreatmentSummary(@Valid @RequestBody TreatmentSummarySaveReqVO updateReqVO) {
        treatmentSummaryService.updateTreatmentSummary(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得治疗小结详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-summary:query')")
    public CommonResult<TreatmentSummaryRespVO> getTreatmentSummary(@RequestParam("id") Long id) {
        TreatmentSummaryDO summary = treatmentSummaryService.getTreatmentSummary(id);
        return success(BeanUtils.toBean(summary, TreatmentSummaryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得治疗小结分页")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-summary:query')")
    public CommonResult<PageResult<TreatmentSummaryRespVO>> getTreatmentSummaryPage(@Validated TreatmentSummaryPageReqVO reqVO) {
        PageResult<TreatmentSummaryDO> pageResult = treatmentSummaryService.getTreatmentSummaryPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), TreatmentSummaryRespVO.class), pageResult.getTotal()));
    }

}
