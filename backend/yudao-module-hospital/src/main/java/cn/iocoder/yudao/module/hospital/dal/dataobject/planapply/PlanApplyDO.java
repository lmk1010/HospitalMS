package cn.iocoder.yudao.module.hospital.dal.dataobject.planapply;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hospital_plan_apply")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanApplyDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long patientId;
    private String patientName;
    private Long contourTaskId;
    private Long applyDoctorId;
    private String applyDoctorName;
    private String treatmentSite;
    private String clinicalDiagnosis;
    private BigDecimal prescriptionDose;
    private Integer totalFractions;
    private String priority;
    private LocalDateTime submitTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
