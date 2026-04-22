package cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 靶区勾画任务创建/修改 Request VO")
@Data
public class ContourTaskSaveReqVO {

    private Long id;
    private String bizNo;

    @NotNull(message = "患者不能为空")
    private Long patientId;

    private Long ctAppointmentId;

    @NotNull(message = "勾画医生不能为空")
    private Long contourDoctorId;

    private String treatmentSite;
    private String versionNo;
    private String attachmentUrl;
    private Integer status;
    private String remark;

}
