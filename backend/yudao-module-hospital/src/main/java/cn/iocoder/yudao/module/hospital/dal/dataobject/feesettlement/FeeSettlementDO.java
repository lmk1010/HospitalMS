package cn.iocoder.yudao.module.hospital.dal.dataobject.feesettlement;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hospital_fee_settlement")
@Data
@EqualsAndHashCode(callSuper = true)
public class FeeSettlementDO extends TenantBaseDO {

    @TableId
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

}
