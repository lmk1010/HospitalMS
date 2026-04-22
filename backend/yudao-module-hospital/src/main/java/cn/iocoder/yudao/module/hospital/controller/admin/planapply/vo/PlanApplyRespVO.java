package cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 计划申请 Response VO")
@Data
public class PlanApplyRespVO {

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
    private LocalDateTime createTime;

}
