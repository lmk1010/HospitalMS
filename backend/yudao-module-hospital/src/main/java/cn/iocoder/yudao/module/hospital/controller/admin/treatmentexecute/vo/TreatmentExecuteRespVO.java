package cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TreatmentExecuteRespVO {

    private Long id;
    private String bizNo;
    private Long treatmentAppointmentId;
    private Long patientId;
    private String patientName;
    private LocalDate treatmentDate;
    private Integer fractionNo;
    private Long executorId;
    private String executorName;
    private BigDecimal plannedDose;
    private BigDecimal actualDose;
    private String executeResult;
    private String abnormalDesc;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
