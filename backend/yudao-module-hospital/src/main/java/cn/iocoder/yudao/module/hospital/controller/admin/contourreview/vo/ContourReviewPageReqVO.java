package cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - 勾画审核分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContourReviewPageReqVO extends PageParam {

    private Long id;

    private String bizNo;
    private Long contourTaskId;
    private String patientName;
    private Long reviewDoctorId;
    private String reviewResult;
    private String workflowStatus;
    private List<String> workflowStatusList;
    private Integer status;

}
