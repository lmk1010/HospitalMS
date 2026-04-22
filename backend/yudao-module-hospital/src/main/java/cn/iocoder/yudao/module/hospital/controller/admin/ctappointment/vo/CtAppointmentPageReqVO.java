package cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - CT预约分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CtAppointmentPageReqVO extends PageParam {

    private String bizNo;
    private String patientName;
    private Long applyDoctorId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate appointmentDate;

    private String workflowStatus;
    private Integer status;

}
