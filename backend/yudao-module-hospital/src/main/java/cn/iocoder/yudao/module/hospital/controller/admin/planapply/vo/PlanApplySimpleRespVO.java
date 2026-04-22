package cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 计划申请精简 Response VO")
@Data
public class PlanApplySimpleRespVO {

    private Long id;
    private String bizNo;
    private String patientName;
    private String treatmentSite;
    private String displayName;

}
