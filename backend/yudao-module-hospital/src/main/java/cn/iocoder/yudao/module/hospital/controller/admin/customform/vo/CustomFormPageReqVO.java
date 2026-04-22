package cn.iocoder.yudao.module.hospital.controller.admin.customform.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 医院自定义表单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomFormPageReqVO extends PageParam {

    private String name;
    private String code;
    private Long deptId;
    private String bizCategory;
    private String processKey;
    private String nodeKey;
    private String pageCode;
    private Integer status;

}
