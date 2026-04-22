package cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - 计划申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanApplyPageReqVO extends PageParam {

    private Long id;

    private String bizNo;
    private String patientName;
    private Long contourTaskId;
    private Long applyDoctorId;
    private String workflowStatus;
    private List<String> workflowStatusList;
    private Integer status;

}
