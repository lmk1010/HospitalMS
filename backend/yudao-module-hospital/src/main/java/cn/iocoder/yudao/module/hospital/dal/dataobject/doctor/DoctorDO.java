package cn.iocoder.yudao.module.hospital.dal.dataobject.doctor;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("hospital_doctor")
@Data
@EqualsAndHashCode(callSuper = true)
public class DoctorDO extends TenantBaseDO {

    @TableId
    private Long id;

    private Long deptId;
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

}
