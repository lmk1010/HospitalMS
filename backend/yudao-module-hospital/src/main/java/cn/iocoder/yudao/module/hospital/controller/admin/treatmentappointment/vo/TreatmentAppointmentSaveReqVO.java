package cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "管理后台 - 治疗预约创建/修改 Request VO")
@Data
public class TreatmentAppointmentSaveReqVO {

    private Long id;
    private String bizNo;

    @NotNull(message = "计划验证不能为空")
    private Long planVerifyId;

    private LocalDate appointmentDate;
    private Integer fractionNo;
    private String treatmentRoomName;
    private String deviceName;

    @NotNull(message = "预约医生不能为空")
    private Long doctorId;

    private Integer status;
    private String remark;

}
