package cn.iocoder.yudao.module.hospital.dal.dataobject.customform;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@TableName(value = "hospital_custom_form", autoResultMap = true)
@KeySequence("hospital_custom_form_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class CustomFormDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String name;
    private String code;
    private Long deptId;
    private String bizCategory;
    private String processKey;
    private String processName;
    private String nodeKey;
    private String nodeName;
    private String pageCode;
    private String pagePath;
    private Integer status;
    private String conf;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fields;

    private String remark;

}
