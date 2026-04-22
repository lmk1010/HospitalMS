package cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 靶区勾画任务精简 Response VO")
@Data
public class ContourTaskSimpleRespVO {

    private Long id;
    private String bizNo;
    private String patientName;
    private String treatmentSite;
    private String displayName;

}
