package cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 计划审核分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanReviewPageReqVO extends PageParam {

    private Long id;

    private String bizNo;
    private Long planDesignId;
    private String patientName;
    private Long reviewDoctorId;
    private String reviewResult;
    private String workflowStatus;
    private Integer status;

}
