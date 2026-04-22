package cn.iocoder.yudao.module.hospital.dal.dataobject.planverify;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@TableName("hospital_plan_verify")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanVerifyDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long planDesignId;
    private Long patientId;
    private String patientName;
    private Long verifyUserId;
    private String verifyUserName;
    private String verifyDeviceName;
    private String verifyResult;
    private String reportUrl;
    private LocalDateTime verifyTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
