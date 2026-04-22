package cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@TableName("hospital_contour_task")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContourTaskDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long patientId;
    private String patientName;
    private Long ctAppointmentId;
    private Long contourDoctorId;
    private String contourDoctorName;
    private String treatmentSite;
    private String versionNo;
    private String attachmentUrl;
    private LocalDateTime submitTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
