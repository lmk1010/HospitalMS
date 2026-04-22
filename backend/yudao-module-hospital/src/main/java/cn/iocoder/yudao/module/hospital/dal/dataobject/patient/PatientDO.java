package cn.iocoder.yudao.module.hospital.dal.dataobject.patient;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@TableName("hospital_patient")
@Data
@EqualsAndHashCode(callSuper = true)
public class PatientDO extends TenantBaseDO {

    @TableId
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
    private Long attendingDoctorId;
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

}
