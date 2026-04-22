package cn.iocoder.yudao.module.hospital.controller.admin.department.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 科室创建/修改 Request VO")
@Data
public class DepartmentSaveReqVO {

    @Schema(description = "科室编号")
    private Long id;

    @Schema(description = "科室名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "科室名称不能为空")
    private String name;

    @Schema(description = "科室编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "科室编码不能为空")
    private String code;

    @Schema(description = "上级科室编号")
    private Long parentId;

    @Schema(description = "科室类型")
    private Integer type;

    @Schema(description = "负责人")
    private String directorName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
