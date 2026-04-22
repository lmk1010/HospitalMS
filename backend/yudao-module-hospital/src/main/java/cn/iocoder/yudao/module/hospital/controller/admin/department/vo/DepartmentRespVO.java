package cn.iocoder.yudao.module.hospital.controller.admin.department.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 科室 Response VO")
@Data
public class DepartmentRespVO {

    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private Integer type;
    private String directorName;
    private String phone;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
