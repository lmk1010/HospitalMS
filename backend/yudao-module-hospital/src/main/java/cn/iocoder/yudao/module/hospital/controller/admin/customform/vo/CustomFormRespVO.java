package cn.iocoder.yudao.module.hospital.controller.admin.customform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 医院自定义表单 Response VO")
@Data
public class CustomFormRespVO {

    private Long id;
    private String name;
    private String code;
    private Long deptId;
    private String deptName;
    private String bizCategory;
    private String processKey;
    private String processName;
    private String nodeKey;
    private String nodeName;
    private String pageCode;
    private String pagePath;
    private Integer status;
    private String conf;
    private List<String> fields;
    private String remark;
    private String creator;
    private String updater;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
