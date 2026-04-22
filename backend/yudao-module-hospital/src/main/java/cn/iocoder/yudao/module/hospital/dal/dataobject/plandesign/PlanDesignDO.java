package cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hospital_plan_design")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanDesignDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long planApplyId;
    private Long patientId;
    private String patientName;
    private Long designUserId;
    private String designUserName;
    private String planName;
    private String versionNo;
    private BigDecimal totalDose;
    private BigDecimal singleDose;
    private Integer fractions;
    private LocalDateTime designTime;
    private LocalDateTime submitTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
