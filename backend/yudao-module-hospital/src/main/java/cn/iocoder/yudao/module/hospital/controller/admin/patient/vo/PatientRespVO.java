package cn.iocoder.yudao.module.hospital.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 患者 Response VO")
@Data
public class PatientRespVO {

    private Long id;
    private String patientNo;
    private String name;
    private Integer gender;
    private LocalDate birthday;
    private Integer age;
    private String idType;
    private String idNo;
    private String outpatientNo;
    private String hospitalizationNo;
    private String radiotherapyNo;
    private String medicalRecordNo;
    private String maritalStatus;
    private String nativePlace;
    private String currentAddress;
    private String patientPhone;
    private String contactName;
    private String contactRelation;
    private String contactPhone;
    private Long managerDoctorId;
    private String managerDoctorName;
    private Long attendingDoctorId;
    private String attendingDoctorName;
    private String patientType;
    private String wardName;
    private String paymentType;
    private String visitNo;
    private String campus;
    private String firstDoctorName;
    private String socialSecurityNo;
    private String photoUrl;
    private String allergyHistory;
    private String pastHistory;
    private String tags;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
