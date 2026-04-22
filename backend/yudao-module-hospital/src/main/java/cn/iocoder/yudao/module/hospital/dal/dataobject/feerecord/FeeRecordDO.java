package cn.iocoder.yudao.module.hospital.dal.dataobject.feerecord;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hospital_fee_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class FeeRecordDO extends TenantBaseDO {

    @TableId
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

}
