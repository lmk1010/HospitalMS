package cn.iocoder.yudao.module.hospital.service.feerecord;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.feerecord.vo.FeeRecordSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.feerecord.FeeRecordDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.feerecord.FeeRecordMapper;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.FEE_RECORD_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.FEE_RECORD_DELETE_FAIL_STATUS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.FEE_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.FEE_RECORD_UPDATE_FAIL_STATUS;

@Service
@Validated
public class FeeRecordServiceImpl implements FeeRecordService {

    private static final String SETTLEMENT_STATUS_WAIT = "WAIT_SETTLEMENT";

    @Resource
    private FeeRecordMapper feeRecordMapper;
    @Resource
    private PatientService patientService;
    @Resource
    private DoctorService doctorService;

    @Override
    public Long createFeeRecord(FeeRecordSaveReqVO createReqVO) {
        String bizNo = prepareBizNo(null, createReqVO.getBizNo());
        PatientDO patient = patientService.getPatient(createReqVO.getPatientId());
        DoctorDO doctor = doctorService.getDoctor(createReqVO.getChargeUserId());
        validateForCreateOrUpdate(null, bizNo, patient, doctor);
        FeeRecordDO feeRecord = BeanUtils.toBean(createReqVO, FeeRecordDO.class);
        feeRecord.setBizNo(bizNo);
        feeRecord.setPatientName(patient.getName());
        feeRecord.setChargeUserName(doctor.getName());
        normalizeFeeRecord(feeRecord);
        feeRecordMapper.insert(feeRecord);
        return feeRecord.getId();
    }

    @Override
    public void updateFeeRecord(FeeRecordSaveReqVO updateReqVO) {
        FeeRecordDO existing = validateFeeRecordExists(updateReqVO.getId());
        if (!Objects.equals(existing.getSettlementStatus(), SETTLEMENT_STATUS_WAIT)) {
            throw exception(FEE_RECORD_UPDATE_FAIL_STATUS);
        }
        String bizNo = prepareBizNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getBizNo(), existing.getBizNo()));
        PatientDO patient = patientService.getPatient(updateReqVO.getPatientId());
        DoctorDO doctor = doctorService.getDoctor(updateReqVO.getChargeUserId());
        validateForCreateOrUpdate(updateReqVO.getId(), bizNo, patient, doctor);
        FeeRecordDO feeRecord = BeanUtils.toBean(updateReqVO, FeeRecordDO.class);
        feeRecord.setBizNo(bizNo);
        feeRecord.setPatientName(patient.getName());
        feeRecord.setChargeUserName(doctor.getName());
        feeRecord.setSettlementStatus(existing.getSettlementStatus());
        feeRecord.setChargeTime(existing.getChargeTime());
        normalizeFeeRecord(feeRecord);
        feeRecordMapper.updateById(feeRecord);
    }

    @Override
    public void deleteFeeRecord(Long id) {
        FeeRecordDO existing = validateFeeRecordExists(id);
        if (!Objects.equals(existing.getSettlementStatus(), SETTLEMENT_STATUS_WAIT)) {
            throw exception(FEE_RECORD_DELETE_FAIL_STATUS);
        }
        feeRecordMapper.deleteById(id);
    }

    @Override
    public FeeRecordDO getFeeRecord(Long id) {
        return feeRecordMapper.selectById(id);
    }

    @Override
    public PageResult<FeeRecordDO> getFeeRecordPage(FeeRecordPageReqVO reqVO) {
        return feeRecordMapper.selectPage(reqVO);
    }

    private void validateForCreateOrUpdate(Long id, String bizNo, PatientDO patient, DoctorDO doctor) {
        if (patient == null) {
            throw exception(cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PATIENT_NOT_EXISTS);
        }
        if (doctor == null) {
            throw exception(cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.DOCTOR_NOT_EXISTS);
        }
        FeeRecordDO record = feeRecordMapper.selectByBizNo(bizNo);
        if (record == null) {
            return;
        }
        if (!Objects.equals(record.getId(), id)) {
            throw exception(FEE_RECORD_BIZ_NO_DUPLICATE);
        }
    }

    private FeeRecordDO validateFeeRecordExists(Long id) {
        FeeRecordDO feeRecord = feeRecordMapper.selectById(id);
        if (feeRecord == null) {
            throw exception(FEE_RECORD_NOT_EXISTS);
        }
        return feeRecord;
    }

    private void normalizeFeeRecord(FeeRecordDO feeRecord) {
        if (feeRecord.getSourceType() == null) {
            feeRecord.setSourceType("OTHER");
        }
        if (feeRecord.getSourceBizId() == null) {
            feeRecord.setSourceBizId(0L);
        }
        if (feeRecord.getUnitPrice() == null) {
            feeRecord.setUnitPrice(BigDecimal.ZERO);
        }
        if (feeRecord.getQuantity() == null) {
            feeRecord.setQuantity(BigDecimal.ONE);
        }
        feeRecord.setAmount(feeRecord.getUnitPrice().multiply(feeRecord.getQuantity()).setScale(2, RoundingMode.HALF_UP));
        if (feeRecord.getChargeTime() == null) {
            feeRecord.setChargeTime(LocalDateTime.now());
        }
        if (feeRecord.getSettlementStatus() == null) {
            feeRecord.setSettlementStatus(SETTLEMENT_STATUS_WAIT);
        }
        if (feeRecord.getStatus() == null) {
            feeRecord.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (feeRecord.getRemark() == null) {
            feeRecord.setRemark("");
        }
    }

    private String prepareBizNo(Long id, String bizNo) {
        if (StrUtil.isNotBlank(bizNo)) {
            return bizNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "FY" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            FeeRecordDO feeRecord = feeRecordMapper.selectByBizNo(generated);
            if (feeRecord == null || Objects.equals(feeRecord.getId(), id)) {
                return generated;
            }
        }
        return "FY" + System.currentTimeMillis();
    }

}
