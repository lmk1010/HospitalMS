package cn.iocoder.yudao.module.hospital.controller.admin.feerecord;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feerecord.FeeRecordDO;
import cn.iocoder.yudao.module.hospital.service.feerecord.FeeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@Tag(name = "管理后台 - 费用登记")
@RestController
@RequestMapping("/hospital/fee-record")
@Validated
public class FeeRecordController {

    @Resource
    private FeeRecordService feeRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建费用登记")
    @PreAuthorize("@ss.hasPermission('hospital:fee-record:create')")
    public CommonResult<Long> createFeeRecord(@Valid @RequestBody FeeRecordSaveReqVO createReqVO) {
        return success(feeRecordService.createFeeRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改费用登记")
    @PreAuthorize("@ss.hasPermission('hospital:fee-record:update')")
    public CommonResult<Boolean> updateFeeRecord(@Valid @RequestBody FeeRecordSaveReqVO updateReqVO) {
        feeRecordService.updateFeeRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除费用登记")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:fee-record:delete')")
    public CommonResult<Boolean> deleteFeeRecord(@RequestParam("id") Long id) {
        feeRecordService.deleteFeeRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得费用登记详情")
    @Parameter(name = "id", required = true)
    @PreAuthorize("@ss.hasPermission('hospital:fee-record:query')")
    public CommonResult<FeeRecordRespVO> getFeeRecord(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(feeRecordService.getFeeRecord(id), FeeRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得费用登记分页")
    @PreAuthorize("@ss.hasPermission('hospital:fee-record:query')")
    public CommonResult<PageResult<FeeRecordRespVO>> getFeeRecordPage(@Validated FeeRecordPageReqVO reqVO) {
        PageResult<FeeRecordDO> pageResult = feeRecordService.getFeeRecordPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), FeeRecordRespVO.class), pageResult.getTotal()));
    }

}
