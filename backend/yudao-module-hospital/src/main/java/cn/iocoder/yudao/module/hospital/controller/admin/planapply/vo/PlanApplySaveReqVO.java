package cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 计划申请创建/修改 Request VO")
@Data
public class PlanApplySaveReqVO {

    private Long id;
    private String bizNo;

    @NotNull(message = "勾画任务不能为空")
    private Long contourTaskId;

    @NotNull(message = "申请医生不能为空")
    private Long applyDoctorId;

    private String treatmentSite;
    private String clinicalDiagnosis;

    @DecimalMin(value = "0", message = "处方总剂量不能小于 0")
    private BigDecimal prescriptionDose;

    private Integer totalFractions;
    private String priority;
    private Integer status;
    private String remark;

}
