package cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 费用登记创建/修改 Request VO")
@Data
public class FeeRecordSaveReqVO {

    private Long id;
    private String bizNo;

    @NotNull(message = "患者不能为空")
    private Long patientId;

    private String sourceType;
    private Long sourceBizId;

    @NotBlank(message = "费用项目不能为空")
    private String itemName;

    @DecimalMin(value = "0", message = "单价不能小于 0")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0", message = "数量不能小于 0")
    private BigDecimal quantity;

    @NotNull(message = "收费人不能为空")
    private Long chargeUserId;

    private Integer status;
    private String remark;

}
