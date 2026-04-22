package cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 治疗小结创建/修改 Request VO")
@Data
public class TreatmentSummarySaveReqVO {

    private Long id;
    private String bizNo;

    @NotNull(message = "患者不能为空")
    private Long patientId;

    @NotNull(message = "总结医生不能为空")
    private Long summaryDoctorId;

    private String evaluationResult;
    private String abnormalDesc;
    private String summaryContent;
    private Integer status;
    private String remark;

}
