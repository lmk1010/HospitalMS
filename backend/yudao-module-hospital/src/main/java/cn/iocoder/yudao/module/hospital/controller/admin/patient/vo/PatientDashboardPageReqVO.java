package cn.iocoder.yudao.module.hospital.controller.admin.patient.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 患者列表大屏分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PatientDashboardPageReqVO extends PageParam {

    @Schema(description = "关键字，支持患者姓名/档案号/病案号/放疗号/住院号/身份证/手机号")
    private String keyword;

    @Schema(description = "主管医生编号")
    private Long managerDoctorId;

    @Schema(description = "患者类型")
    private String patientType;

    @Schema(description = "当前阶段")
    private String currentStage;

    @Schema(description = "物理师")
    private String planPhysicistName;

}
