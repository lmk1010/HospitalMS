package cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 治疗预约 Response VO")
@Data
public class TreatmentAppointmentRespVO {

    private Long id;
    private String bizNo;
    private Long patientId;
    private String patientName;
    private Long planVerifyId;
    private LocalDate appointmentDate;
    private Integer fractionNo;
    private String treatmentRoomName;
    private String deviceName;
    private Long doctorId;
    private String doctorName;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
