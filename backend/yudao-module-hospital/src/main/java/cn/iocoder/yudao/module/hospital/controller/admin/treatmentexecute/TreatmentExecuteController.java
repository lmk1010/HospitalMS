package cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute.vo.TreatmentExecutePageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute.vo.TreatmentExecuteRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import cn.iocoder.yudao.module.hospital.service.treatmentexecute.TreatmentExecuteService;
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

@Tag(name = "管理后台 - 治疗执行")
@RestController
@RequestMapping("/hospital/treatment-execute")
@Validated
public class TreatmentExecuteController {

    @Resource
    private TreatmentExecuteService treatmentExecuteService;

    @GetMapping("/page")
    @Operation(summary = "获得治疗执行分页")
    @PreAuthorize("@ss.hasPermission('hospital:treatment-execute:query')")
    public CommonResult<PageResult<TreatmentExecuteRespVO>> getTreatmentExecutePage(@Validated TreatmentExecutePageReqVO reqVO) {
        PageResult<TreatmentExecuteDO> pageResult = treatmentExecuteService.getTreatmentExecutePage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), TreatmentExecuteRespVO.class), pageResult.getTotal()));
    }

    @PutMapping("/execute")
    @Operation(summary = "开始治疗执行")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-execute:execute')")
    public CommonResult<Boolean> executeTreatment(@RequestParam("id") Long id) {
        treatmentExecuteService.executeTreatment(id);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成治疗执行")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:treatment-execute:complete')")
    public CommonResult<Boolean> completeTreatment(@RequestParam("id") Long id) {
        treatmentExecuteService.completeTreatment(id);
        return success(true);
    }

}
