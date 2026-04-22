package cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 医生 Response VO")
@Data
public class DoctorRespVO {

    private Long id;
    private Long deptId;
    private String deptName;
    private Long userId;
    private String doctorCode;
    private String name;
    private Integer gender;
    private String phone;
    private String title;
    private String practicingLicenseNo;
    private String specialty;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
