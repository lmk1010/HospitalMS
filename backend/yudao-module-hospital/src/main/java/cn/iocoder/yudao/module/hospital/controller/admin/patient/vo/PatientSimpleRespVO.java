package cn.iocoder.yudao.module.hospital.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 患者精简 Response VO")
@Data
public class PatientSimpleRespVO {

    private Long id;
    private String patientNo;
    private String name;

}
