package cn.iocoder.yudao.module.hospital.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 患者列表大屏 Response VO")
@Data
public class PatientDashboardRespVO {

    private Long id;
    private String patientNo;
    private String name;
    private Integer gender;
    private Integer age;
    private String idNo;
    private String photoUrl;
    private String patientPhone;
    private String medicalRecordNo;
    private String radiotherapyNo;
    private String hospitalizationNo;
    private String diagnosis;
    private Long managerDoctorId;
    private String managerDoctorName;
    private String planPhysicistName;
    private String currentStageCode;
    private String currentStage;
    private String currentNodeCode;
    private String currentNodeName;
    private String currentRoutePath;
    private Long currentBizId;
    private Integer totalFractions;
    private Integer completedFractions;
    private String patientType;
    private String wardName;
    private String registrationUserName;
    private LocalDateTime registrationTime;

}
