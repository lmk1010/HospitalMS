package cn.iocoder.yudao.module.hospital.controller.admin.treatmentqueue.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TreatmentQueueRespVO {

    private Long id;
    private String queueNo;
    private Long treatmentAppointmentId;
    private Long patientId;
    private String patientName;
    private LocalDate queueDate;
    private Integer queueSeq;
    private LocalDateTime signInTime;
    private LocalDateTime callTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private String queueStatus;
    private String treatmentRoomName;
    private String deviceName;
    private String remark;
    private LocalDateTime createTime;

}
