package cn.iocoder.yudao.module.hospital.dal.dataobject.department;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("hospital_department")
@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String name;
    private String code;
    private Long parentId;
    private Integer type;
    private String directorName;
    private String phone;
    private Integer sort;
    private Integer status;
    private String remark;

}
