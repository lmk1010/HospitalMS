package cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeSettlementRespVO {

    private Long id;
    private String bizNo;
    private Long patientId;
    private String patientName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    private String paymentType;
    private LocalDateTime payTime;
    private Long cashierId;
    private String cashierName;
    private String settlementStatus;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
