package cn.iocoder.yudao.module.hospital.controller.admin.treatmentappointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 治疗预约排班查询 Request VO")
@Data
public class TreatmentAppointmentScheduleReqVO {

    private Long id;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

    @Schema(description = "关键字，支持预约单号/患者/医生/治疗室/设备")
    private String keyword;

    @Schema(description = "治疗室")
    private String treatmentRoomName;

    @Schema(description = "预约医生")
    private Long doctorId;

}
