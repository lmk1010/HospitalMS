package cn.iocoder.yudao.module.hospital.dal.dataobject.contourreview;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@TableName("hospital_contour_review")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContourReviewDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String bizNo;
    private Long contourTaskId;
    private Long patientId;
    private String patientName;
    private Long reviewDoctorId;
    private String reviewDoctorName;
    private String reviewResult;
    private String reviewComment;
    private LocalDateTime reviewTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;

}
