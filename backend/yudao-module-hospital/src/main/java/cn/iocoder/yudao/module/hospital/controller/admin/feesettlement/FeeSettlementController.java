package cn.iocoder.yudao.module.hospital.controller.admin.feesettlement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementSettleReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feesettlement.FeeSettlementDO;
import cn.iocoder.yudao.module.hospital.service.feesettlement.FeeSettlementService;
import cn.iocoder.yudao.module.hospital.service.feesettlement.FeeSettlementServiceImpl;
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
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 费用结算")
@RestController
@RequestMapping("/hospital/fee-settlement")
@Validated
public class FeeSettlementController {

    @Resource
    private FeeSettlementService feeSettlementService;

    @PostMapping("/settle")
    @Operation(summary = "费用结算")
    @PreAuthorize("@ss.hasPermission('hospital:fee-settlement:settle')")
    public CommonResult<Long> settleFee(@Valid @RequestBody FeeSettlementSettleReqVO reqVO) {
        return success(feeSettlementService.settleFee(reqVO));
    }

    @PutMapping("/reverse")
    @Operation(summary = "费用冲正")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:fee-settlement:reverse')")
    public CommonResult<Boolean> reverseFeeSettlement(@RequestParam("id") Long id) {
        feeSettlementService.reverseFeeSettlement(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得费用结算分页")
    @PreAuthorize("@ss.hasPermission('hospital:fee-settlement:query')")
    public CommonResult<PageResult<FeeSettlementRespVO>> getFeeSettlementPage(@Validated FeeSettlementPageReqVO reqVO) {
        PageResult<FeeSettlementDO> pageResult = feeSettlementService.getFeeSettlementPage(reqVO);
        List<FeeSettlementRespVO> list = pageResult.getList().stream().map(item -> {
            FeeSettlementRespVO respVO = BeanUtils.toBean(item, FeeSettlementRespVO.class);
            respVO.setRemark(FeeSettlementServiceImpl.sanitizeRemark(item.getRemark()));
            return respVO;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

}
