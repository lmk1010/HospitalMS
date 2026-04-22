package cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 计划审核 Response VO")
@Data
public class PlanReviewRespVO {

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
    private LocalDateTime createTime;

}
