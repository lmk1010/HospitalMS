package cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 计划设计创建/修改 Request VO")
@Data
public class PlanDesignSaveReqVO {

    private Long id;
    private String bizNo;

    @NotNull(message = "计划申请不能为空")
    private Long planApplyId;

    @NotNull(message = "设计人员不能为空")
    private Long designUserId;

    private String planName;
    private String versionNo;

    @DecimalMin(value = "0", message = "总剂量不能小于 0")
    private BigDecimal totalDose;

    @DecimalMin(value = "0", message = "单次剂量不能小于 0")
    private BigDecimal singleDose;

    private Integer fractions;
    private Integer status;
    private String remark;

}
