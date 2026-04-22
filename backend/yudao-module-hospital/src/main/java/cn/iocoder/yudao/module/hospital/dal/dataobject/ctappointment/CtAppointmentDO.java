package cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@TableName("hospital_ct_appointment")
@Data
@EqualsAndHashCode(callSuper = true)
public class CtAppointmentDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long patientId;
    private String patientName;
    private LocalDate appointmentDate;
    private String appointmentSlot;
    private String ctRoomName;
    private String ctDeviceName;
    private Long applyDoctorId;
    private String applyDoctorName;
    private String priority;
    private String positionNote;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
