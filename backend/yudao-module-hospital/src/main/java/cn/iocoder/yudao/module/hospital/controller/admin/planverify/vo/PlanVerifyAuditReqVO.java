package cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 计划验证动作 Request VO")
@Data
public class PlanVerifyAuditReqVO {

    @NotNull(message = "验证编号不能为空")
    private Long id;

    @NotNull(message = "验证人不能为空")
    private Long verifyUserId;

    private String verifyDeviceName;
    private String reportUrl;
    private String remark;

}
