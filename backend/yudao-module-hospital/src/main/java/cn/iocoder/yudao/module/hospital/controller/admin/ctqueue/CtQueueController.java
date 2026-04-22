package cn.iocoder.yudao.module.hospital.controller.admin.ctqueue;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.ctqueue.vo.CtQueuePageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctqueue.vo.CtQueueRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctqueue.CtQueueDO;
import cn.iocoder.yudao.module.hospital.service.ctqueue.CtQueueService;
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

@Tag(name = "管理后台 - CT排队叫号")
@RestController
@RequestMapping("/hospital/ct-queue")
@Validated
public class CtQueueController {

    @Resource
    private CtQueueService ctQueueService;

    @GetMapping("/page")
    @Operation(summary = "获得CT排队分页")
    @PreAuthorize("@ss.hasPermission('hospital:ct-queue:query')")
    public CommonResult<PageResult<CtQueueRespVO>> getCtQueuePage(@Validated CtQueuePageReqVO reqVO) {
        PageResult<CtQueueDO> pageResult = ctQueueService.getCtQueuePage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), CtQueueRespVO.class), pageResult.getTotal()));
    }

    @PutMapping("/call")
    @Operation(summary = "CT叫号")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:ct-queue:call')")
    public CommonResult<Boolean> callCtQueue(@RequestParam("id") Long id) {
        ctQueueService.callCtQueue(id);
        return success(true);
    }

    @PutMapping("/skip")
    @Operation(summary = "CT过号")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:ct-queue:skip')")
    public CommonResult<Boolean> skipCtQueue(@RequestParam("id") Long id) {
        ctQueueService.skipCtQueue(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "CT完成")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:ct-queue:finish')")
    public CommonResult<Boolean> finishCtQueue(@RequestParam("id") Long id) {
        ctQueueService.finishCtQueue(id);
        return success(true);
    }

}
