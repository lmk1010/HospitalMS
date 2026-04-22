package cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@TableName("hospital_treatment_appointment")
@Data
@EqualsAndHashCode(callSuper = true)
public class TreatmentAppointmentDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long patientId;
    private String patientName;
    private Long planVerifyId;
    private LocalDate appointmentDate;
    private Integer fractionNo;
    private String treatmentRoomName;
    private String deviceName;
    private Long doctorId;
    private String doctorName;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
