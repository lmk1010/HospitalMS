package cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeRecordRespVO {

    private Long id;
    private String bizNo;
    private Long patientId;
    private String patientName;
    private String sourceType;
    private Long sourceBizId;
    private String itemName;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private LocalDateTime chargeTime;
    private Long chargeUserId;
    private String chargeUserName;
    private String settlementStatus;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
