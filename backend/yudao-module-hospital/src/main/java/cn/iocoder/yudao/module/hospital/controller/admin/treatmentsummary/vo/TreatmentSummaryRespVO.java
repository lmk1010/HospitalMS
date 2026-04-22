package cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TreatmentSummaryRespVO {

    private Long id;
    private String bizNo;
    private Long patientId;
    private String patientName;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    private Integer completedFractions;
    private Long summaryDoctorId;
    private String summaryDoctorName;
    private String evaluationResult;
    private String abnormalDesc;
    private String summaryContent;
    private LocalDateTime summaryTime;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
