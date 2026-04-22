package cn.iocoder.yudao.module.hospital.controller.admin.treatmentexecute.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Data
@EqualsAndHashCode(callSuper = true)
public class TreatmentExecutePageReqVO extends PageParam {

    private Long id;

    private String bizNo;
    private String patientName;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate treatmentDate;

    private Long executorId;
    private String executeResult;
    private Integer status;

}
