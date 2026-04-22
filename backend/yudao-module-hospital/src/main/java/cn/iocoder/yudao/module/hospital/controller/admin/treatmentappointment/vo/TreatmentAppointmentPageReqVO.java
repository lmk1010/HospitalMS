package cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Schema(description = "管理后台 - 治疗预约分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TreatmentAppointmentPageReqVO extends PageParam {

    private Long id;

    private String bizNo;
    private String patientName;
    private Long planVerifyId;
    private LocalDate appointmentDate;
    private Long doctorId;
    private String workflowStatus;
    private Integer status;

}
