package cn.iocoder.yudao.module.hospital.controller.admin.ctqueue.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CtQueueRespVO {

    private Long id;
    private String queueNo;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private LocalDate queueDate;
    private Integer queueSeq;
    private LocalDateTime signInTime;
    private LocalDateTime callTime;
    private LocalDateTime finishTime;
    private String queueStatus;
    private String windowName;
    private String ctRoomName;
    private String remark;
    private LocalDateTime createTime;

}
