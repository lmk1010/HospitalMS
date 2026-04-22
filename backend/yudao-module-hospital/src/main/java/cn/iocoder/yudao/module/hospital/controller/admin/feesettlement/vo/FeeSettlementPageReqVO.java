package cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeeSettlementPageReqVO extends PageParam {

    private String bizNo;
    private Long patientId;
    private String patientName;
    private String paymentType;
    private Long cashierId;
    private String settlementStatus;
    private Integer status;

}
