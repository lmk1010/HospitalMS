package cn.iocoder.yudao.module.hospital.controller.admin.department.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 科室精简 Response VO")
@Data
public class DepartmentSimpleRespVO {

    private Long id;
    private String name;
    private String code;

}
