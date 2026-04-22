package cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - 靶区勾画任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContourTaskPageReqVO extends PageParam {

    private Long id;

    private String bizNo;
    private String patientName;
    private Long contourDoctorId;
    private String treatmentSite;
    private String workflowStatus;
    private List<String> workflowStatusList;
    private Integer status;

}
