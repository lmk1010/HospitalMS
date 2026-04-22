package cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 医生精简 Response VO")
@Data
public class DoctorSimpleRespVO {

    private Long id;
    private String doctorCode;
    private String name;

}
