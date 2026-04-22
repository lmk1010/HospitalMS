package cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 靶区勾画任务 Response VO")
@Data
public class ContourTaskRespVO {

    private Long id;
    private String bizNo;
    private Long patientId;
    private String patientName;
    private Long ctAppointmentId;
    private Long contourDoctorId;
    private String contourDoctorName;
    private String treatmentSite;
    private String versionNo;
    private String attachmentUrl;
    private LocalDateTime submitTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
