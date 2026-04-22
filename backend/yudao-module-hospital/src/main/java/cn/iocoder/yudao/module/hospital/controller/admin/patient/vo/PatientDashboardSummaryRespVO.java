package cn.iocoder.yudao.module.hospital.controller.admin.patient.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 患者列表大屏汇总 Response VO")
@Data
public class PatientDashboardSummaryRespVO {

    private Long totalCount;
    private Long todayCount;
    private Long monthCount;

    private Long registerCount;
    private Long positioningCount;
    private Long contourCount;
    private Long planningCount;
    private Long reviewCount;
    private Long treatmentCount;

}
