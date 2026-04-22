package cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 勾画审核 Response VO")
@Data
public class ContourReviewRespVO {

    private Long id;
    private String bizNo;
    private Long contourTaskId;
    private Long patientId;
    private String patientName;
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
