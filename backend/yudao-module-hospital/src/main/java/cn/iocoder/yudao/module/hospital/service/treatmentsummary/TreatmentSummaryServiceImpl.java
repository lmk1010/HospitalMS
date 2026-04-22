package cn.iocoder.yudao.module.hospital.service.treatmentsummary;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummaryPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.treatmentsummary.vo.TreatmentSummarySaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentsummary.TreatmentSummaryDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentexecute.TreatmentExecuteMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentsummary.TreatmentSummaryMapper;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.hospital.service.patient.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_SUMMARY_BIZ_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.TREATMENT_SUMMARY_NOT_EXISTS;

@Service
@Validated
public class TreatmentSummaryServiceImpl implements TreatmentSummaryService {

    private static final String EXECUTE_RESULT_DONE = "DONE";

    @Resource
    private TreatmentSummaryMapper treatmentSummaryMapper;
    @Resource
    private TreatmentExecuteMapper treatmentExecuteMapper;
    @Resource
    private PatientService patientService;
    @Resource
    private DoctorService doctorService;

    @Override
    public Long createTreatmentSummary(TreatmentSummarySaveReqVO createReqVO) {
        String bizNo = prepareBizNo(null, createReqVO.getBizNo());
        TreatmentSummaryDO summary = buildSummary(null, bizNo, createReqVO);
        treatmentSummaryMapper.insert(summary);
        return summary.getId();
    }

    @Override
    public void updateTreatmentSummary(TreatmentSummarySaveReqVO updateReqVO) {
        TreatmentSummaryDO existing = validateTreatmentSummaryExists(updateReqVO.getId());
        String bizNo = prepareBizNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getBizNo(), existing.getBizNo()));
        TreatmentSummaryDO summary = buildSummary(existing, bizNo, updateReqVO);
        treatmentSummaryMapper.updateById(summary);
    }

    @Override
    public void syncFromTreatmentExecute(TreatmentExecuteDO execute) {
        if (execute == null || execute.getPatientId() == null) {
            return;
        }
        List<TreatmentExecuteDO> executeList = treatmentExecuteMapper.selectDoneListByPatientId(execute.getPatientId(), EXECUTE_RESULT_DONE);
        if (executeList.isEmpty()) {
            return;
        }
        TreatmentSummaryDO summary = treatmentSummaryMapper.selectLatestByPatientId(execute.getPatientId());
        boolean creating = summary == null;
        if (creating) {
            summary = new TreatmentSummaryDO();
            summary.setBizNo(prepareBizNo(null, null));
            summary.setPatientId(execute.getPatientId());
            summary.setEvaluationResult("");
            summary.setAbnormalDesc("");
            summary.setSummaryContent("");
            summary.setRemark("");
            summary.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        PatientDO patient = patientService.getPatient(execute.getPatientId());
        Long summaryDoctorId = resolveAutoSummaryDoctorId(summary, execute);
        DoctorDO doctor = summaryDoctorId != null && summaryDoctorId > 0 ? doctorService.getDoctor(summaryDoctorId) : null;
        summary.setPatientName(patient == null ? StrUtil.blankToDefault(execute.getPatientName(), "") : patient.getName());
        summary.setSummaryDoctorId(doctor == null ? 0L : doctor.getId());
        summary.setSummaryDoctorName(doctor == null ? StrUtil.blankToDefault(execute.getExecutorName(), "") : doctor.getName());
        summary.setSummaryTime(LocalDateTime.now());
        summary.setCompletedFractions(executeList.size());
        summary.setCourseStartDate(executeList.stream()
                .map(TreatmentExecuteDO::getTreatmentDate)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null));
        summary.setCourseEndDate(executeList.stream()
                .map(TreatmentExecuteDO::getTreatmentDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null));
        if (summary.getEvaluationResult() == null) {
            summary.setEvaluationResult("");
        }
        if (summary.getAbnormalDesc() == null) {
            summary.setAbnormalDesc("");
        }
        if (summary.getSummaryContent() == null) {
            summary.setSummaryContent("");
        }
        if (summary.getRemark() == null) {
            summary.setRemark("");
        }
        if (creating) {
            treatmentSummaryMapper.insert(summary);
            return;
        }
        treatmentSummaryMapper.updateById(summary);
    }

    @Override
    public TreatmentSummaryDO getTreatmentSummary(Long id) {
        return treatmentSummaryMapper.selectById(id);
    }

    @Override
    public PageResult<TreatmentSummaryDO> getTreatmentSummaryPage(TreatmentSummaryPageReqVO reqVO) {
        return treatmentSummaryMapper.selectPage(reqVO);
    }

    private TreatmentSummaryDO buildSummary(TreatmentSummaryDO existing, String bizNo, TreatmentSummarySaveReqVO reqVO) {
        patientService.validatePatientExists(reqVO.getPatientId());
        doctorService.validateDoctorExists(reqVO.getSummaryDoctorId());
        validateBizNoUnique(existing == null ? null : existing.getId(), bizNo);
        PatientDO patient = patientService.getPatient(reqVO.getPatientId());
        DoctorDO doctor = doctorService.getDoctor(reqVO.getSummaryDoctorId());
        List<TreatmentExecuteDO> executeList = treatmentExecuteMapper.selectDoneListByPatientId(reqVO.getPatientId(), EXECUTE_RESULT_DONE);

        TreatmentSummaryDO summary = BeanUtils.toBean(reqVO, TreatmentSummaryDO.class);
        if (existing != null) {
            summary.setId(existing.getId());
        }
        summary.setBizNo(bizNo);
        summary.setPatientName(patient.getName());
        summary.setSummaryDoctorName(doctor.getName());
        summary.setSummaryTime(LocalDateTime.now());
        summary.setCompletedFractions(executeList.size());
        summary.setCourseStartDate(executeList.stream()
                .map(TreatmentExecuteDO::getTreatmentDate)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null));
        summary.setCourseEndDate(executeList.stream()
                .map(TreatmentExecuteDO::getTreatmentDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null));
        if (summary.getStatus() == null) {
            summary.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (summary.getEvaluationResult() == null) {
            summary.setEvaluationResult("");
        }
        if (summary.getAbnormalDesc() == null) {
            summary.setAbnormalDesc("");
        }
        if (summary.getSummaryContent() == null) {
            summary.setSummaryContent("");
        }
        if (summary.getRemark() == null) {
            summary.setRemark("");
        }
        return summary;
    }

    private Long resolveAutoSummaryDoctorId(TreatmentSummaryDO summary, TreatmentExecuteDO execute) {
        if (summary.getSummaryDoctorId() != null && summary.getSummaryDoctorId() > 0) {
            return summary.getSummaryDoctorId();
        }
        if (execute.getExecutorId() != null && execute.getExecutorId() > 0) {
            return execute.getExecutorId();
        }
        List<DoctorDO> doctors = doctorService.getDoctorSimpleList();
        return doctors.isEmpty() ? 0L : doctors.get(0).getId();
    }

    private TreatmentSummaryDO validateTreatmentSummaryExists(Long id) {
        TreatmentSummaryDO summary = treatmentSummaryMapper.selectById(id);
        if (summary == null) {
            throw exception(TREATMENT_SUMMARY_NOT_EXISTS);
        }
        return summary;
    }

    private void validateBizNoUnique(Long id, String bizNo) {
        TreatmentSummaryDO summary = treatmentSummaryMapper.selectByBizNo(bizNo);
        if (summary == null) {
            return;
        }
        if (!Objects.equals(summary.getId(), id)) {
            throw exception(TREATMENT_SUMMARY_BIZ_NO_DUPLICATE);
        }
    }

    private String prepareBizNo(Long id, String bizNo) {
        if (StrUtil.isNotBlank(bizNo)) {
            return bizNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "ZJ" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(6);
            TreatmentSummaryDO summary = treatmentSummaryMapper.selectByBizNo(generated);
            if (summary == null || Objects.equals(summary.getId(), id)) {
                return generated;
            }
        }
        return "ZJ" + System.currentTimeMillis();
    }

}
