package cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 计划验证 Response VO")
@Data
public class PlanVerifyRespVO {

    private Long id;
    private String bizNo;
    private Long planDesignId;
    private Long patientId;
    private String patientName;
    private Long verifyUserId;
    private String verifyUserName;
    private String verifyDeviceName;
    private String verifyResult;
    private String reportUrl;
    private LocalDateTime verifyTime;
    private String workflowStatus;
    private String processInstanceId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
