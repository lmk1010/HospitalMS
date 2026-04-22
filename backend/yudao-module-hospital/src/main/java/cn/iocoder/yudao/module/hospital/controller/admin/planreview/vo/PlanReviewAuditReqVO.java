package cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 计划审核动作 Request VO")
@Data
public class PlanReviewAuditReqVO {

    @NotNull(message = "审核编号不能为空")
    private Long id;

    @NotNull(message = "审核人不能为空")
    private Long reviewDoctorId;

    private String reviewComment;
    private String remark;

}
