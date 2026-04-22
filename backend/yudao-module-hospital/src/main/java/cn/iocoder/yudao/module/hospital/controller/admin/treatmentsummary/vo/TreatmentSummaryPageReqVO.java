package cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TreatmentSummaryPageReqVO extends PageParam {

    private Long id;

    private String bizNo;
    private String patientName;
    private Long summaryDoctorId;
    private Integer status;

}
