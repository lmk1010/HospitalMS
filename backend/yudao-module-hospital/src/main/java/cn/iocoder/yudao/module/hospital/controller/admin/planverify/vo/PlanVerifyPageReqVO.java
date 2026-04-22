package cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 计划验证分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanVerifyPageReqVO extends PageParam {

    private String bizNo;
    private Long planDesignId;
    private String patientName;
    private Long verifyUserId;
    private String verifyResult;
    private String workflowStatus;
    private Integer status;

}
