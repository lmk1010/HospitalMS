package cn.iocoder.yudao.module.hospital.controller.admin.customform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 医院自定义表单精简 Response VO")
@Data
public class CustomFormSimpleRespVO {

    private Long id;
    private String name;
    private String code;
    private String processKey;
    private String nodeKey;
    private String pageCode;

}
