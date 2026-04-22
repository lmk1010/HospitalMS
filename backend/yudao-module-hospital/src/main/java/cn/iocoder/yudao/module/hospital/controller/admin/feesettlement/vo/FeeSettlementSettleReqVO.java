package cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 费用结算 Request VO")
@Data
public class FeeSettlementSettleReqVO {

    @NotNull(message = "患者不能为空")
    private Long patientId;

    @DecimalMin(value = "0", message = "优惠金额不能小于 0")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0", message = "实收金额不能小于 0")
    private BigDecimal paidAmount;

    private String paymentType;

    @NotNull(message = "收费员不能为空")
    private Long cashierId;

    private Integer status;
    private String remark;

    @Schema(description = "结算的费用记录编号列表，为空时默认结算患者全部待结算费用")
    private List<Long> recordIds;

}
