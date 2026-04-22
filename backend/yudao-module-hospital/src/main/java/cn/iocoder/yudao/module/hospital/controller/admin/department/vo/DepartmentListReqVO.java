package cn.iocoder.yudao.module.hospital.controller.admin.department.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 科室列表 Request VO")
@Data
public class DepartmentListReqVO {

    @Schema(description = "科室名称")
    private String name;

    @Schema(description = "科室编码")
    private String code;

    @Schema(description = "上级科室编号")
    private Long parentId;

    @Schema(description = "科室类型")
    private Integer type;

    @Schema(description = "状态")
    private Integer status;

}
