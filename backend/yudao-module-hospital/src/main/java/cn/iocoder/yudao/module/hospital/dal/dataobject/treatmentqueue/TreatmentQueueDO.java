package cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("hospital_treatment_queue")
@Data
@EqualsAndHashCode(callSuper = true)
public class TreatmentQueueDO extends TenantBaseDO {

    @TableId
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

}
