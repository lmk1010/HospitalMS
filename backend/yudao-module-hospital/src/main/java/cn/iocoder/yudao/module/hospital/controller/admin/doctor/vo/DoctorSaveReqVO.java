package cn.iocoder.yudao.module.hospital.controller.admin.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 医生创建/修改 Request VO")
@Data
public class DoctorSaveReqVO {

    private Long id;

    @NotNull(message = "所属科室不能为空")
    private Long deptId;

    private Long userId;

    @NotBlank(message = "医生工号不能为空")
    private String doctorCode;

    @NotBlank(message = "医生姓名不能为空")
    private String name;

    private Integer gender;
    private String phone;
    private String title;
    private String practicingLicenseNo;
    private String specialty;
    private Integer sort;
    private Integer status;
    private String remark;

}
