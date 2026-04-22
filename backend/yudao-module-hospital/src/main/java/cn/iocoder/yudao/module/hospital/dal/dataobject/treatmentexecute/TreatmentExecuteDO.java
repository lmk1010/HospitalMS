package cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("hospital_treatment_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class TreatmentExecuteDO extends TenantBaseDO {

    @TableId
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

}
