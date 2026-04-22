package cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 计划设计 Response VO")
@Data
public class PlanDesignRespVO {

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
    private LocalDateTime createTime;

}
