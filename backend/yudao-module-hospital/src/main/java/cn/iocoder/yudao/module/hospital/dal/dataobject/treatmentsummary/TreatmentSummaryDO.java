package cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentsummary;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("hospital_treatment_summary")
@Data
@EqualsAndHashCode(callSuper = true)
public class TreatmentSummaryDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long patientId;
    private String patientName;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    private Integer completedFractions;
    private Long summaryDoctorId;
    private String summaryDoctorName;
    private String evaluationResult;
    private String abnormalDesc;
    private String summaryContent;
    private LocalDateTime summaryTime;
    private Integer status;
    private String remark;

}
