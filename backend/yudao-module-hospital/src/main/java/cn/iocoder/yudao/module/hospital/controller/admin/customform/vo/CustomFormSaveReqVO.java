package cn.iocoder.yudao.module.hospital.controller.admin.customform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 医院自定义表单创建/修改 Request VO")
@Data
public class CustomFormSaveReqVO {

    @Schema(description = "表单编号", example = "1024")
    private Long id;

    @Schema(description = "表单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "表单名称不能为空")
    private String name;

    @Schema(description = "表单编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "表单编码不能为空")
    private String code;

    @Schema(description = "所属科室")
    private Long deptId;

    @Schema(description = "业务分类", example = "定位管理")
    private String bizCategory;

    @Schema(description = "流程定义标识", example = "hospital_radiotherapy_external_beam_px")
    private String processKey;

    @Schema(description = "流程名称", example = "外照射业务流程（PX）")
    private String processName;

    @Schema(description = "节点标识", example = "ct_queue")
    private String nodeKey;

    @Schema(description = "节点名称", example = "CT定位")
    private String nodeName;

    @Schema(description = "页面编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ct-queue")
    @NotBlank(message = "页面编码不能为空")
    private String pageCode;

    @Schema(description = "页面路由", example = "/hospital-flow/hospital-position/ct-queue")
    private String pagePath;

    @Schema(description = "表单配置-JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "表单配置不能为空")
    private String conf;

    @Schema(description = "表单项数组-JSON 字符串数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "表单项不能为空")
    private List<String> fields;

    @Schema(description = "表单状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "表单状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
