package cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeeRecordPageReqVO extends PageParam {

    private String bizNo;
    private Long patientId;
    private String patientName;
    private String sourceType;
    private String itemName;
    private Long chargeUserId;
    private String settlementStatus;
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime chargeTime;

}
