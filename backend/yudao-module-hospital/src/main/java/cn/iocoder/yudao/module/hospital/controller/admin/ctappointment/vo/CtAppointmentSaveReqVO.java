package cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "管理后台 - CT预约创建/修改 Request VO")
@Data
public class CtAppointmentSaveReqVO {

    private Long id;
    private String bizNo;

    @NotNull(message = "患者不能为空")
    private Long patientId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate appointmentDate;

    private String appointmentSlot;
    private String ctRoomName;
    private String ctDeviceName;

    @NotNull(message = "申请医生不能为空")
    private Long applyDoctorId;

    private String priority;
    private Integer status;
    private String positionNote;
    private String remark;

}
