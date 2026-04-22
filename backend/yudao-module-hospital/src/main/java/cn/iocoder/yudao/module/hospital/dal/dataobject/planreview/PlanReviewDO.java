package cn.iocoder.yudao.module.hospital.dal.dataobject.planreview;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@TableName("hospital_plan_review")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanReviewDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long planDesignId;
    private Long patientId;
    private String patientName;
    private String reviewStage;
    private Long reviewDoctorId;
    private String reviewDoctorName;
    private String reviewResult;
    private String reviewComment;
    private LocalDateTime reviewTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
