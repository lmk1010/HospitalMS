package cn.iocoder.yudao.module.hospital.service.feesettlement;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feesettlement.vo.FeeSettlementSettleReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feerecord.FeeRecordDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feesettlement.FeeSettlementDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.feerecord.FeeRecordMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.feesettlement.FeeSettlementMapper;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.FEE_SETTLEMENT_EMPTY_RECORDS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.FEE_SETTLEMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.FEE_SETTLEMENT_REVERSE_FAIL_STATUS;

@Service
@Validated
public class FeeSettlementServiceImpl implements FeeSettlementService {

    private static final String REMARK_LINK_PREFIX = "__LINK__";
    private static final String REMARK_NOTE_SPLIT = "__NOTE__";
    private static final String STATUS_WAIT = "WAIT_SETTLEMENT";
    private static final String STATUS_PART = "PART_SETTLEMENT";
    private static final String STATUS_SETTLED = "SETTLED";
    private static final String STATUS_VOID = "VOID";

    @Resource
    private FeeSettlementMapper feeSettlementMapper;
    @Resource
    private FeeRecordMapper feeRecordMapper;
    @Resource
    private PatientService patientService;
    @Resource
    private DoctorService doctorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long settleFee(FeeSettlementSettleReqVO reqVO) {
        PatientDO patient = patientService.getPatient(reqVO.getPatientId());
        DoctorDO cashier = doctorService.getDoctor(reqVO.getCashierId());
        List<Long> requestedRecordIds = reqVO.getRecordIds() == null ? java.util.Collections.emptyList() : reqVO.getRecordIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<FeeRecordDO> feeRecords = feeRecordMapper.selectListByPatientIdAndIds(reqVO.getPatientId(), STATUS_WAIT,
                requestedRecordIds.isEmpty() ? null : requestedRecordIds);
        if (feeRecords == null || feeRecords.isEmpty()) {
            throw exception(FEE_SETTLEMENT_EMPTY_RECORDS);
        }
        if (!requestedRecordIds.isEmpty() && feeRecords.size() != requestedRecordIds.size()) {
            throw exception(FEE_SETTLEMENT_EMPTY_RECORDS);
        }
        BigDecimal totalAmount = feeRecords.stream()
                .map(FeeRecordDO::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = defaultDecimal(reqVO.getDiscountAmount());
        BigDecimal payableAmount = totalAmount.subtract(discountAmount);
        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }
        BigDecimal paidAmount = reqVO.getPaidAmount() == null ? payableAmount : reqVO.getPaidAmount().setScale(2, RoundingMode.HALF_UP);
        String settlementStatus = paidAmount.compareTo(payableAmount) >= 0 ? STATUS_SETTLED : STATUS_PART;

        FeeSettlementDO settlement = new FeeSettlementDO();
        settlement.setBizNo(generateBizNo());
        settlement.setPatientId(patient.getId());
        settlement.setPatientName(patient.getName());
        settlement.setTotalAmount(totalAmount);
        settlement.setDiscountAmount(discountAmount);
        settlement.setPayableAmount(payableAmount);
        settlement.setPaidAmount(paidAmount);
        settlement.setPaymentType(StrUtil.blankToDefault(reqVO.getPaymentType(), "SELF_PAY"));
        settlement.setPayTime(LocalDateTime.now());
        settlement.setCashierId(cashier.getId());
        settlement.setCashierName(cashier.getName());
        settlement.setSettlementStatus(settlementStatus);
        settlement.setStatus(reqVO.getStatus() == null ? CommonStatusEnum.ENABLE.getStatus() : reqVO.getStatus());
        settlement.setRemark(buildRemark(feeRecords, reqVO.getRemark()));
        feeSettlementMapper.insert(settlement);

        for (FeeRecordDO feeRecord : feeRecords) {
            FeeRecordDO updateObj = new FeeRecordDO();
            updateObj.setId(feeRecord.getId());
            updateObj.setSettlementStatus(settlementStatus);
            feeRecordMapper.updateById(updateObj);
        }
        return settlement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverseFeeSettlement(Long id) {
        FeeSettlementDO settlement = feeSettlementMapper.selectById(id);
        if (settlement == null) {
            throw exception(FEE_SETTLEMENT_NOT_EXISTS);
        }
        if (Objects.equals(settlement.getSettlementStatus(), STATUS_VOID)) {
            throw exception(FEE_SETTLEMENT_REVERSE_FAIL_STATUS);
        }
        List<Long> recordIds = parseLinkedRecordIds(settlement.getRemark());
        FeeSettlementDO updateObj = new FeeSettlementDO();
        updateObj.setId(id);
        updateObj.setSettlementStatus(STATUS_VOID);
        feeSettlementMapper.updateById(updateObj);
        if (recordIds.isEmpty()) {
            return;
        }
        for (FeeRecordDO feeRecord : feeRecordMapper.selectBatchIds(recordIds)) {
            FeeRecordDO recordUpdate = new FeeRecordDO();
            recordUpdate.setId(feeRecord.getId());
            recordUpdate.setSettlementStatus(STATUS_WAIT);
            feeRecordMapper.updateById(recordUpdate);
        }
    }

    @Override
    public PageResult<FeeSettlementDO> getFeeSettlementPage(FeeSettlementPageReqVO reqVO) {
        return feeSettlementMapper.selectPage(reqVO);
    }

    public static String sanitizeRemark(String remark) {
        if (StrUtil.isBlank(remark) || !remark.startsWith(REMARK_LINK_PREFIX)) {
            return StrUtil.blankToDefault(remark, "");
        }
        int index = remark.indexOf(REMARK_NOTE_SPLIT);
        if (index < 0) {
            return "";
        }
        return remark.substring(index + REMARK_NOTE_SPLIT.length());
    }

    private String buildRemark(List<FeeRecordDO> feeRecords, String userRemark) {
        String ids = feeRecords.stream().map(item -> String.valueOf(item.getId())).collect(Collectors.joining(","));
        return REMARK_LINK_PREFIX + ids + REMARK_NOTE_SPLIT + StrUtil.blankToDefault(userRemark, "");
    }

    private List<Long> parseLinkedRecordIds(String remark) {
        if (StrUtil.isBlank(remark) || !remark.startsWith(REMARK_LINK_PREFIX)) {
            return java.util.Collections.emptyList();
        }
        int index = remark.indexOf(REMARK_NOTE_SPLIT);
        String ids = index < 0 ? remark.substring(REMARK_LINK_PREFIX.length()) : remark.substring(REMARK_LINK_PREFIX.length(), index);
        if (StrUtil.isBlank(ids)) {
            return java.util.Collections.emptyList();
        }
        return Arrays.stream(ids.split(","))
                .filter(StrUtil::isNotBlank)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    private String generateBizNo() {
        return "JS" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
    }

    private BigDecimal defaultDecimal(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

}
