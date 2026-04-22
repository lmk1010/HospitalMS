package cn.iocoder.yudao.module.hospital.controller.admin.treatmentqueue;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentqueue.vo.TreatmentQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentqueue.vo.TreatmentQueueRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;
import cn.iocoder.yudao.module.hospital.service.treatmentqueue.TreatmentQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 治疗排队叫号")
@RestController
@RequestMapping("/hospital/treatment-queue")
@Validated
public class TreatmentQueueController {

    @Resource
    private TreatmentQueueService treatmentQueueService;

    @GetMapping("/page")
    @Operation(summary = "获得治疗排队分页")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-queue:query')")
    public CommonResult<PageResult<TreatmentQueueRespVO>> getTreatmentQueuePage(@Validated TreatmentQueuePageReqVO reqVO) {
        PageResult<TreatmentQueueDO> pageResult = treatmentQueueService.getTreatmentQueuePage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), TreatmentQueueRespVO.class), pageResult.getTotal()));
    }

    @PutMapping("/call")
    @Operation(summary = "治疗叫号")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-queue:call')")
    public CommonResult<Boolean> callTreatmentQueue(@RequestParam("id") Long id) {
        treatmentQueueService.callTreatmentQueue(id);
        return success(true);
    }

    @PutMapping("/skip")
    @Operation(summary = "治疗过号")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-queue:skip')")
    public CommonResult<Boolean> skipTreatmentQueue(@RequestParam("id") Long id) {
        treatmentQueueService.skipTreatmentQueue(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "治疗完成")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-queue:finish')")
    public CommonResult<Boolean> finishTreatmentQueue(@RequestParam("id") Long id) {
        treatmentQueueService.finishTreatmentQueue(id);
        return success(true);
    }

}
