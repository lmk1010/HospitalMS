package cn.iocoder.yudao.module.hospital.controller.admin.patient.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 患者分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PatientPageReqVO extends PageParam {

    private String patientNo;
    private String name;
    private String idNo;
    private String patientPhone;
    private String hospitalizationNo;
    private String socialSecurityNo;
    private String patientType;
    private Long managerDoctorId;
    private Long attendingDoctorId;
    private Integer status;

}
