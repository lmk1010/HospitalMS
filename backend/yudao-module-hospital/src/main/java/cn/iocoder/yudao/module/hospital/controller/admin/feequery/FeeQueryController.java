package cn.iocoder.yudao.module.hospital.controller.admin.feequery;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordRespVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feerecord.FeeRecordDO;
import cn.iocoder.yudao.module.hospital.service.feerecord.FeeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 费用查询")
@RestController
@RequestMapping("/hospital/fee-query")
@Validated
public class FeeQueryController {

    @Resource
    private FeeRecordService feeRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得费用查询分页")
    @PreAuthorize("@ss.hasPermission('hospital:fee-query:query')")
    public CommonResult<PageResult<FeeRecordRespVO>> getFeeQueryPage(@Validated FeeRecordPageReqVO reqVO) {
        PageResult<FeeRecordDO> pageResult = feeRecordService.getFeeRecordPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), FeeRecordRespVO.class), pageResult.getTotal()));
    }

}
