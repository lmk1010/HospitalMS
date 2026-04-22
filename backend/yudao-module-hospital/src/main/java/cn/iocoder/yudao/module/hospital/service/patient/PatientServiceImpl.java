package cn.iocoder.yudao.module.hospital.service.patient;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientDashboardSummaryRespVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientPageReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.patient.vo.PatientSaveReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourreview.ContourReviewDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctqueue.CtQueueDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.patient.PatientDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planreview.PlanReviewDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentsummary.TreatmentSummaryDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourreview.ContourReviewMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourtask.ContourTaskMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.ctappointment.CtAppointmentMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.ctqueue.CtQueueMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.patient.PatientMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planapply.PlanApplyMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.plandesign.PlanDesignMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planreview.PlanReviewMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planverify.PlanVerifyMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentappointment.TreatmentAppointmentMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentexecute.TreatmentExecuteMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentqueue.TreatmentQueueMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentsummary.TreatmentSummaryMapper;
import cn.iocoder.yudao.module.hospital.service.doctor.DoctorService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PATIENT_ID_NO_DUPLICATE;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PATIENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hospital.enums.ErrorCodeConstants.PATIENT_NO_DUPLICATE;

@Service
@Validated
public class PatientServiceImpl implements PatientService {

    private static final String STAGE_REGISTER = "REGISTER";
    private static final String STAGE_POSITIONING = "POSITIONING";
    private static final String STAGE_CONTOUR = "CONTOUR";
    private static final String STAGE_PLANNING = "PLANNING";
    private static final String STAGE_REVIEW = "REVIEW";
    private static final String STAGE_TREATMENT = "TREATMENT";

    private static final String NODE_REGISTER = "REGISTER";
    private static final String NODE_CT_APPOINTMENT = "CT_APPOINTMENT";
    private static final String NODE_CT_QUEUE = "CT_QUEUE";
    private static final String NODE_CONTOUR_TASK = "CONTOUR_TASK";
    private static final String NODE_CONTOUR_REVIEW = "CONTOUR_REVIEW";
    private static final String NODE_PLAN_APPLY = "PLAN_APPLY";
    private static final String NODE_PLAN_DESIGN = "PLAN_DESIGN";
    private static final String NODE_PLAN_GROUP_REVIEW = "PLAN_GROUP_REVIEW";
    private static final String NODE_PLAN_DOCTOR_APPROVAL = "PLAN_DOCTOR_APPROVAL";
    private static final String NODE_PLAN_RECHECK = "PLAN_RECHECK";
    private static final String NODE_PLAN_VERIFY = "PLAN_VERIFY";
    private static final String NODE_TREATMENT_APPOINTMENT = "TREATMENT_APPOINTMENT";
    private static final String NODE_TREATMENT_QUEUE = "TREATMENT_QUEUE";
    private static final String NODE_TREATMENT_EXECUTE = "TREATMENT_EXECUTE";
    private static final String NODE_TREATMENT_SUMMARY = "TREATMENT_SUMMARY";

    private static final String ROUTE_REGISTER = "/hospital-flow/hospital-patient/registration";
    private static final String ROUTE_CT_APPOINTMENT = "/hospital-flow/hospital-position/ct-appointment";
    private static final String ROUTE_CT_QUEUE = "/hospital-flow/hospital-position/ct-queue";
    private static final String ROUTE_CONTOUR_TASK = "/hospital-flow/hospital-contour/task";
    private static final String ROUTE_CONTOUR_REVIEW = "/hospital-flow/hospital-contour/review";
    private static final String ROUTE_PLAN_APPLY = "/hospital-flow/hospital-plan/apply";
    private static final String ROUTE_PLAN_DESIGN = "/hospital-flow/hospital-plan/design";
    private static final String ROUTE_PLAN_GROUP_REVIEW = "/hospital-flow/hospital-plan/group-review";
    private static final String ROUTE_PLAN_DOCTOR_APPROVAL = "/hospital-flow/hospital-plan/doctor-approval";
    private static final String ROUTE_PLAN_RECHECK = "/hospital-flow/hospital-plan/recheck";
    private static final String ROUTE_PLAN_VERIFY = "/hospital-flow/hospital-plan/verify";
    private static final String ROUTE_TREATMENT_APPOINTMENT = "/hospital-flow/hospital-treatment/appointment";
    private static final String ROUTE_TREATMENT_QUEUE = "/hospital-flow/hospital-treatment/queue";
    private static final String ROUTE_TREATMENT_EXECUTE = "/hospital-flow/hospital-treatment/execute";
    private static final String ROUTE_TREATMENT_SUMMARY = "/hospital-flow/hospital-treatment/summary";

    @Resource
    private PatientMapper patientMapper;
    @Resource
    private DoctorService doctorService;
    @Resource
    private CtAppointmentMapper ctAppointmentMapper;
    @Resource
    private CtQueueMapper ctQueueMapper;
    @Resource
    private ContourReviewMapper contourReviewMapper;
    @Resource
    private ContourTaskMapper contourTaskMapper;
    @Resource
    private PlanApplyMapper planApplyMapper;
    @Resource
    private PlanDesignMapper planDesignMapper;
    @Resource
    private PlanReviewMapper planReviewMapper;
    @Resource
    private PlanVerifyMapper planVerifyMapper;
    @Resource
    private TreatmentAppointmentMapper treatmentAppointmentMapper;
    @Resource
    private TreatmentExecuteMapper treatmentExecuteMapper;
    @Resource
    private TreatmentQueueMapper treatmentQueueMapper;
    @Resource
    private TreatmentSummaryMapper treatmentSummaryMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createPatient(PatientSaveReqVO createReqVO) {
        String patientNo = preparePatientNo(null, createReqVO.getPatientNo());
        validatePatientForCreateOrUpdate(null, patientNo, createReqVO);
        PatientDO patient = BeanUtils.toBean(createReqVO, PatientDO.class);
        patient.setPatientNo(patientNo);
        normalizePatient(patient);
        patientMapper.insert(patient);
        return patient.getId();
    }

    @Override
    public void updatePatient(PatientSaveReqVO updateReqVO) {
        PatientDO existing = patientMapper.selectById(updateReqVO.getId());
        if (existing == null) {
            throw exception(PATIENT_NOT_EXISTS);
        }
        String patientNo = preparePatientNo(existing.getId(), StrUtil.blankToDefault(updateReqVO.getPatientNo(), existing.getPatientNo()));
        validatePatientForCreateOrUpdate(updateReqVO.getId(), patientNo, updateReqVO);
        PatientDO patient = BeanUtils.toBean(updateReqVO, PatientDO.class);
        patient.setPatientNo(patientNo);
        if (patient.getPhotoUrl() == null) {
            patient.setPhotoUrl(existing.getPhotoUrl());
        }
        normalizePatient(patient);
        patientMapper.updateById(patient);
    }

    @Override
    public void deletePatient(Long id) {
        if (patientMapper.selectById(id) == null) {
            throw exception(PATIENT_NOT_EXISTS);
        }
        patientMapper.deleteById(id);
    }

    @Override
    public PatientDO getPatient(Long id) {
        return patientMapper.selectById(id);
    }

    @Override
    public List<PatientDO> getPatientSimpleList() {
        return patientMapper.selectSimpleList();
    }

    @Override
    public PageResult<PatientDO> getPatientPage(PatientPageReqVO reqVO) {
        return patientMapper.selectPage(reqVO);
    }

    @Override
    public PageResult<PatientDashboardRespVO> getPatientDashboardPage(PatientDashboardPageReqVO reqVO) {
        List<PatientDO> patients = getDashboardBasePatients(reqVO);
        if (patients.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        DashboardContext context = buildDashboardContext(patients);
        List<PatientDashboardRespVO> rows = patients.stream()
                .map(patient -> buildDashboardResp(patient, context))
                .filter(row -> matchesDashboardFilter(row, reqVO))
                .collect(Collectors.toList());
        return buildPageResult(rows, reqVO);
    }

    @Override
    public PatientDashboardSummaryRespVO getPatientDashboardSummary(PatientDashboardPageReqVO reqVO) {
        List<PatientDO> patients = getDashboardBasePatients(reqVO);
        PatientDashboardSummaryRespVO summary = new PatientDashboardSummaryRespVO();
        summary.setTotalCount(0L);
        summary.setTodayCount(0L);
        summary.setMonthCount(0L);
        summary.setRegisterCount(0L);
        summary.setPositioningCount(0L);
        summary.setContourCount(0L);
        summary.setPlanningCount(0L);
        summary.setReviewCount(0L);
        summary.setTreatmentCount(0L);
        if (patients.isEmpty()) {
            return summary;
        }
        DashboardContext context = buildDashboardContext(patients);
        List<PatientDashboardRespVO> rows = patients.stream()
                .map(patient -> buildDashboardResp(patient, context))
                .filter(row -> matchesDashboardFilter(row, reqVO, false))
                .collect(Collectors.toList());
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        summary.setTotalCount((long) rows.size());
        summary.setTodayCount(rows.stream()
                .filter(row -> row.getRegistrationTime() != null && today.equals(row.getRegistrationTime().toLocalDate()))
                .count());
        summary.setMonthCount(rows.stream()
                .filter(row -> row.getRegistrationTime() != null && !row.getRegistrationTime().toLocalDate().isBefore(monthStart))
                .count());
        summary.setRegisterCount(countByStage(rows, STAGE_REGISTER));
        summary.setPositioningCount(countByStage(rows, STAGE_POSITIONING));
        summary.setContourCount(countByStage(rows, STAGE_CONTOUR));
        summary.setPlanningCount(countByStage(rows, STAGE_PLANNING));
        summary.setReviewCount(countByStage(rows, STAGE_REVIEW));
        summary.setTreatmentCount(countByStage(rows, STAGE_TREATMENT));
        return summary;
    }

    @Override
    public Map<Long, String> getCurrentNodeCodeMap(Collection<Long> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> idSet = patientIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }
        List<PatientDO> patients = patientMapper.selectBatchIds(idSet);
        if (patients == null || patients.isEmpty()) {
            return Collections.emptyMap();
        }
        DashboardContext context = buildDashboardContext(patients);
        return patients.stream().collect(Collectors.toMap(
                PatientDO::getId,
                patient -> buildDashboardResp(patient, context).getCurrentNodeCode(),
                (left, right) -> left,
                LinkedHashMap::new
        ));
    }

    @Override
    public void validatePatientExists(Long id) {
        if (id == null || id <= 0 || patientMapper.selectById(id) == null) {
            throw exception(PATIENT_NOT_EXISTS);
        }
    }

    private List<PatientDO> getDashboardBasePatients(PatientDashboardPageReqVO reqVO) {
        return patientMapper.selectList(new LambdaQueryWrapperX<PatientDO>()
                .eqIfPresent(PatientDO::getManagerDoctorId, reqVO.getManagerDoctorId())
                .eqIfPresent(PatientDO::getPatientType, reqVO.getPatientType())
                .orderByDesc(PatientDO::getId));
    }

    private DashboardContext buildDashboardContext(List<PatientDO> patients) {
        Set<Long> patientIds = patients.stream().map(PatientDO::getId).filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        DashboardContext context = new DashboardContext();
        context.creatorNameMap = buildCreatorNameMap(patients);
        context.doctorNameMap = buildDoctorNameMap(patients);
        if (patientIds.isEmpty()) {
            context.ctAppointmentMap = Collections.emptyMap();
            context.ctQueueMap = Collections.emptyMap();
            context.doctorNameMap = Collections.emptyMap();
            context.contourReviewMap = Collections.emptyMap();
            context.contourTaskMap = Collections.emptyMap();
            context.planApplyMap = Collections.emptyMap();
            context.planDesignMap = Collections.emptyMap();
            context.planReviewMap = Collections.emptyMap();
            context.planVerifyMap = Collections.emptyMap();
            context.treatmentAppointmentMap = Collections.emptyMap();
            context.treatmentExecuteMap = Collections.emptyMap();
            context.treatmentQueueMap = Collections.emptyMap();
            context.treatmentSummaryMap = Collections.emptyMap();
            return context;
        }
        context.ctAppointmentMap = buildLatestMap(ctAppointmentMapper.selectList(new LambdaQueryWrapperX<CtAppointmentDO>()
                .in(CtAppointmentDO::getPatientId, patientIds)
                .orderByDesc(CtAppointmentDO::getId)), CtAppointmentDO::getPatientId);
        context.ctQueueMap = buildLatestMap(ctQueueMapper.selectList(new LambdaQueryWrapperX<CtQueueDO>()
                .in(CtQueueDO::getPatientId, patientIds)
                .orderByDesc(CtQueueDO::getId)), CtQueueDO::getPatientId);
        context.contourReviewMap = buildLatestMap(contourReviewMapper.selectList(new LambdaQueryWrapperX<ContourReviewDO>()
                .in(ContourReviewDO::getPatientId, patientIds)
                .orderByDesc(ContourReviewDO::getId)), ContourReviewDO::getPatientId);
        context.contourTaskMap = buildLatestMap(contourTaskMapper.selectList(new LambdaQueryWrapperX<ContourTaskDO>()
                .in(ContourTaskDO::getPatientId, patientIds)
                .orderByDesc(ContourTaskDO::getId)), ContourTaskDO::getPatientId);
        context.planApplyMap = buildLatestMap(planApplyMapper.selectList(new LambdaQueryWrapperX<PlanApplyDO>()
                .in(PlanApplyDO::getPatientId, patientIds)
                .orderByDesc(PlanApplyDO::getId)), PlanApplyDO::getPatientId);
        context.planDesignMap = buildLatestMap(planDesignMapper.selectList(new LambdaQueryWrapperX<PlanDesignDO>()
                .in(PlanDesignDO::getPatientId, patientIds)
                .orderByDesc(PlanDesignDO::getId)), PlanDesignDO::getPatientId);
        context.planReviewMap = buildLatestMap(planReviewMapper.selectList(new LambdaQueryWrapperX<PlanReviewDO>()
                .in(PlanReviewDO::getPatientId, patientIds)
                .orderByDesc(PlanReviewDO::getId)), PlanReviewDO::getPatientId);
        context.planVerifyMap = buildLatestMap(planVerifyMapper.selectList(new LambdaQueryWrapperX<PlanVerifyDO>()
                .in(PlanVerifyDO::getPatientId, patientIds)
                .orderByDesc(PlanVerifyDO::getId)), PlanVerifyDO::getPatientId);
        context.treatmentAppointmentMap = buildListMap(treatmentAppointmentMapper.selectList(new LambdaQueryWrapperX<TreatmentAppointmentDO>()
                .in(TreatmentAppointmentDO::getPatientId, patientIds)
                .orderByDesc(TreatmentAppointmentDO::getId)), TreatmentAppointmentDO::getPatientId);
        context.treatmentExecuteMap = buildListMap(treatmentExecuteMapper.selectList(new LambdaQueryWrapperX<TreatmentExecuteDO>()
                .in(TreatmentExecuteDO::getPatientId, patientIds)
                .orderByDesc(TreatmentExecuteDO::getId)), TreatmentExecuteDO::getPatientId);
        context.treatmentQueueMap = buildLatestMap(treatmentQueueMapper.selectList(new LambdaQueryWrapperX<TreatmentQueueDO>()
                .in(TreatmentQueueDO::getPatientId, patientIds)
                .orderByDesc(TreatmentQueueDO::getId)), TreatmentQueueDO::getPatientId);
        context.treatmentSummaryMap = buildLatestMap(treatmentSummaryMapper.selectList(new LambdaQueryWrapperX<TreatmentSummaryDO>()
                .in(TreatmentSummaryDO::getPatientId, patientIds)
                .orderByDesc(TreatmentSummaryDO::getId)), TreatmentSummaryDO::getPatientId);
        return context;
    }

    private Map<String, String> buildCreatorNameMap(List<PatientDO> patients) {
        Set<Long> userIds = new HashSet<>();
        Map<String, String> creatorNameMap = new HashMap<>();
        for (PatientDO patient : patients) {
            if (StrUtil.isBlank(patient.getCreator())) {
                continue;
            }
            Long userId = parseLong(patient.getCreator());
            if (userId == null) {
                creatorNameMap.put(patient.getCreator(), patient.getCreator());
            } else {
                userIds.add(userId);
            }
        }
        if (!userIds.isEmpty()) {
            Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
            userMap.forEach((id, user) -> creatorNameMap.put(String.valueOf(id), StrUtil.blankToDefault(user.getNickname(), String.valueOf(id))));
        }
        return creatorNameMap;
    }

    private Map<Long, String> buildDoctorNameMap(List<PatientDO> patients) {
        Set<Long> doctorIds = patients.stream()
                .flatMap(patient -> java.util.stream.Stream.of(patient.getManagerDoctorId(), patient.getAttendingDoctorId()))
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.toCollection(HashSet::new));
        if (doctorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return doctorService.getDoctorList(doctorIds).stream()
                .collect(Collectors.toMap(DoctorDO::getId, DoctorDO::getName, (a, b) -> a));
    }

    private PatientDashboardRespVO buildDashboardResp(PatientDO patient, DashboardContext context) {
        PatientDashboardRespVO respVO = new PatientDashboardRespVO();
        respVO.setId(patient.getId());
        respVO.setPatientNo(patient.getPatientNo());
        respVO.setName(patient.getName());
        respVO.setGender(patient.getGender());
        respVO.setAge(patient.getAge());
        respVO.setIdNo(patient.getIdNo());
        respVO.setPhotoUrl(patient.getPhotoUrl());
        respVO.setPatientPhone(patient.getPatientPhone());
        respVO.setMedicalRecordNo(patient.getMedicalRecordNo());
        respVO.setRadiotherapyNo(patient.getRadiotherapyNo());
        respVO.setHospitalizationNo(patient.getHospitalizationNo());
        respVO.setManagerDoctorId(patient.getManagerDoctorId());
        String managerDoctorName = context.doctorNameMap.get(patient.getManagerDoctorId());
        if (StrUtil.isBlank(managerDoctorName)) {
            managerDoctorName = patient.getFirstDoctorName();
        }
        respVO.setManagerDoctorName(StrUtil.blankToDefault(managerDoctorName, "--"));
        respVO.setPatientType(patient.getPatientType());
        respVO.setWardName(patient.getWardName());
        respVO.setRegistrationTime(patient.getCreateTime());
        respVO.setRegistrationUserName(StrUtil.blankToDefault(context.creatorNameMap.get(patient.getCreator()), StrUtil.blankToDefault(patient.getCreator(), "系统")));

        PlanApplyDO planApply = context.planApplyMap.get(patient.getId());
        PlanDesignDO planDesign = context.planDesignMap.get(patient.getId());
        PlanReviewDO planReview = context.planReviewMap.get(patient.getId());
        PlanVerifyDO planVerify = context.planVerifyMap.get(patient.getId());
        ContourReviewDO contourReview = context.contourReviewMap.get(patient.getId());
        ContourTaskDO contourTask = context.contourTaskMap.get(patient.getId());
        CtAppointmentDO ctAppointment = context.ctAppointmentMap.get(patient.getId());
        CtQueueDO ctQueue = context.ctQueueMap.get(patient.getId());
        TreatmentQueueDO treatmentQueue = context.treatmentQueueMap.get(patient.getId());
        TreatmentSummaryDO treatmentSummary = context.treatmentSummaryMap.get(patient.getId());
        List<TreatmentAppointmentDO> treatmentAppointments = context.treatmentAppointmentMap.getOrDefault(patient.getId(), Collections.emptyList());
        List<TreatmentExecuteDO> treatmentExecutes = context.treatmentExecuteMap.getOrDefault(patient.getId(), Collections.emptyList());

        String diagnosis = planApply == null ? null : planApply.getClinicalDiagnosis();
        if (StrUtil.isBlank(diagnosis)) {
            diagnosis = patient.getPastHistory();
        }
        respVO.setDiagnosis(StrUtil.blankToDefault(diagnosis, "--"));
        respVO.setPlanPhysicistName(planDesign == null ? "--" : StrUtil.blankToDefault(planDesign.getDesignUserName(), "--"));

        int totalFractions = 0;
        if (planDesign != null && planDesign.getFractions() != null) {
            totalFractions = planDesign.getFractions();
        } else if (planApply != null && planApply.getTotalFractions() != null) {
            totalFractions = planApply.getTotalFractions();
        }
        int appointmentMaxFraction = treatmentAppointments.stream()
                .map(TreatmentAppointmentDO::getFractionNo)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        if (totalFractions <= 0) {
            totalFractions = appointmentMaxFraction;
        }
        int executeMaxFraction = treatmentExecutes.stream()
                .map(TreatmentExecuteDO::getFractionNo)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        int completedFractions = treatmentSummary != null && treatmentSummary.getCompletedFractions() != null
                ? treatmentSummary.getCompletedFractions()
                : Math.max(executeMaxFraction, appointmentMaxFraction);
        respVO.setTotalFractions(Math.max(totalFractions, 0));
        respVO.setCompletedFractions(Math.max(completedFractions, 0));

        String stageCode = resolveStageCode(ctAppointment, contourTask, planApply, planDesign, planVerify, treatmentAppointments, treatmentExecutes, treatmentSummary);
        respVO.setCurrentStageCode(stageCode);
        respVO.setCurrentStage(resolveStageLabel(stageCode));

        TreatmentAppointmentDO latestTreatmentAppointment = treatmentAppointments.isEmpty() ? null : treatmentAppointments.get(0);
        TreatmentExecuteDO latestTreatmentExecute = treatmentExecutes.isEmpty() ? null : treatmentExecutes.get(0);
        CurrentNodeInfo currentNodeInfo = resolveCurrentNodeInfo(patient, ctAppointment, ctQueue, contourTask,
                contourReview, planApply, planDesign, planReview, planVerify, latestTreatmentAppointment,
                treatmentQueue, latestTreatmentExecute, treatmentSummary);
        respVO.setCurrentNodeCode(currentNodeInfo.code);
        respVO.setCurrentNodeName(currentNodeInfo.name);
        respVO.setCurrentRoutePath(currentNodeInfo.routePath);
        respVO.setCurrentBizId(currentNodeInfo.bizId);
        return respVO;
    }

    private boolean matchesDashboardFilter(PatientDashboardRespVO row, PatientDashboardPageReqVO reqVO) {
        return matchesDashboardFilter(row, reqVO, true);
    }

    private boolean matchesDashboardFilter(PatientDashboardRespVO row, PatientDashboardPageReqVO reqVO, boolean includeCurrentStage) {
        if (includeCurrentStage && StrUtil.isNotBlank(reqVO.getCurrentStage())
                && !StrUtil.equalsIgnoreCase(StrUtil.trim(reqVO.getCurrentStage()), row.getCurrentStageCode())) {
            return false;
        }
        if (StrUtil.isNotBlank(reqVO.getPlanPhysicistName())
                && !containsIgnoreCase(row.getPlanPhysicistName(), reqVO.getPlanPhysicistName())) {
            return false;
        }
        if (StrUtil.isBlank(reqVO.getKeyword())) {
            return true;
        }
        String keyword = StrUtil.trim(reqVO.getKeyword());
        return containsIgnoreCase(String.valueOf(row.getId()), keyword)
                || containsIgnoreCase(row.getName(), keyword)
                || containsIgnoreCase(row.getPatientNo(), keyword)
                || containsIgnoreCase(row.getMedicalRecordNo(), keyword)
                || containsIgnoreCase(row.getRadiotherapyNo(), keyword)
                || containsIgnoreCase(row.getHospitalizationNo(), keyword)
                || containsIgnoreCase(row.getIdNo(), keyword)
                || containsIgnoreCase(row.getPatientPhone(), keyword)
                || containsIgnoreCase(row.getDiagnosis(), keyword);
    }

    private PageResult<PatientDashboardRespVO> buildPageResult(List<PatientDashboardRespVO> rows, PatientDashboardPageReqVO reqVO) {
        long total = rows.size();
        long pageNo = Math.max(reqVO.getPageNo(), 1);
        long pageSize = Math.max(reqVO.getPageSize(), 1);
        int fromIndex = (int) Math.min((pageNo - 1) * pageSize, total);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);
        if (fromIndex >= toIndex) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        return new PageResult<>(new ArrayList<>(rows.subList(fromIndex, toIndex)), total);
    }

    private long countByStage(List<PatientDashboardRespVO> rows, String stageCode) {
        return rows.stream().filter(row -> StrUtil.equals(stageCode, row.getCurrentStageCode())).count();
    }

    private CurrentNodeInfo resolveCurrentNodeInfo(PatientDO patient,
                                                   CtAppointmentDO ctAppointment,
                                                   CtQueueDO ctQueue,
                                                   ContourTaskDO contourTask,
                                                   ContourReviewDO contourReview,
                                                   PlanApplyDO planApply,
                                                   PlanDesignDO planDesign,
                                                   PlanReviewDO planReview,
                                                   PlanVerifyDO planVerify,
                                                   TreatmentAppointmentDO treatmentAppointment,
                                                   TreatmentQueueDO treatmentQueue,
                                                   TreatmentExecuteDO treatmentExecute,
                                                   TreatmentSummaryDO treatmentSummary) {
        if (treatmentSummary != null && treatmentSummary.getId() != null) {
            return new CurrentNodeInfo(NODE_TREATMENT_SUMMARY, "治疗小结", ROUTE_TREATMENT_SUMMARY, treatmentSummary.getId());
        }
        if (treatmentExecute != null && treatmentExecute.getId() != null
                && !StrUtil.equals(treatmentExecute.getExecuteResult(), "DONE")) {
            return new CurrentNodeInfo(NODE_TREATMENT_EXECUTE, "治疗执行", ROUTE_TREATMENT_EXECUTE, treatmentExecute.getId());
        }
        if (treatmentQueue != null && treatmentQueue.getId() != null
                && !StrUtil.equals(treatmentQueue.getQueueStatus(), "DONE")) {
            return new CurrentNodeInfo(NODE_TREATMENT_QUEUE, "治疗排队", ROUTE_TREATMENT_QUEUE, treatmentQueue.getId());
        }
        if (treatmentAppointment != null && treatmentAppointment.getId() != null
                && !StrUtil.equals(treatmentAppointment.getWorkflowStatus(), "DONE")) {
            return new CurrentNodeInfo(NODE_TREATMENT_APPOINTMENT, "治疗预约", ROUTE_TREATMENT_APPOINTMENT, treatmentAppointment.getId());
        }
        if (planVerify != null && planVerify.getId() != null
                && !StrUtil.equals(planVerify.getWorkflowStatus(), "VERIFIED")) {
            return new CurrentNodeInfo(NODE_PLAN_VERIFY, "计划验证", ROUTE_PLAN_VERIFY, planVerify.getId());
        }
        if (planReview != null && planReview.getId() != null) {
            if (StrUtil.equals(planReview.getWorkflowStatus(), "REVIEWING")) {
                if (StrUtil.equals(planReview.getReviewStage(), "GROUP_LEADER")) {
                    return new CurrentNodeInfo(NODE_PLAN_GROUP_REVIEW, "组长审核", ROUTE_PLAN_GROUP_REVIEW, planReview.getId());
                }
                if (StrUtil.equals(planReview.getReviewStage(), "DOCTOR_APPROVAL")) {
                    return new CurrentNodeInfo(NODE_PLAN_DOCTOR_APPROVAL, "医师核准", ROUTE_PLAN_DOCTOR_APPROVAL, planReview.getId());
                }
                if (StrUtil.equals(planReview.getReviewStage(), "RECHECK")) {
                    return new CurrentNodeInfo(NODE_PLAN_RECHECK, "计划复核", ROUTE_PLAN_RECHECK, planReview.getId());
                }
            }
            if (StrUtil.equals(planReview.getWorkflowStatus(), "REJECTED") && planDesign != null && planDesign.getId() != null) {
                return new CurrentNodeInfo(NODE_PLAN_DESIGN, "计划设计", ROUTE_PLAN_DESIGN, planDesign.getId());
            }
        }
        if (planDesign != null && planDesign.getId() != null
                && (StrUtil.equals(planDesign.getWorkflowStatus(), "DRAFT")
                || StrUtil.equals(planDesign.getWorkflowStatus(), "REJECTED"))) {
            return new CurrentNodeInfo(NODE_PLAN_DESIGN, "计划设计", ROUTE_PLAN_DESIGN, planDesign.getId());
        }
        if (planApply != null && planApply.getId() != null
                && (StrUtil.equals(planApply.getWorkflowStatus(), "DRAFT")
                || StrUtil.equals(planApply.getWorkflowStatus(), "REJECTED"))) {
            return new CurrentNodeInfo(NODE_PLAN_APPLY, "计划申请", ROUTE_PLAN_APPLY, planApply.getId());
        }
        if (contourReview != null && contourReview.getId() != null) {
            if (StrUtil.equals(contourReview.getWorkflowStatus(), "REVIEWING")) {
                return new CurrentNodeInfo(NODE_CONTOUR_REVIEW, "勾画审核", ROUTE_CONTOUR_REVIEW, contourReview.getId());
            }
            if (StrUtil.equals(contourReview.getWorkflowStatus(), "REJECTED") && contourTask != null && contourTask.getId() != null) {
                return new CurrentNodeInfo(NODE_CONTOUR_TASK, "勾画任务", ROUTE_CONTOUR_TASK, contourTask.getId());
            }
        }
        if (contourTask != null && contourTask.getId() != null
                && (StrUtil.equals(contourTask.getWorkflowStatus(), "WAIT_CONTOUR")
                || StrUtil.equals(contourTask.getWorkflowStatus(), "REJECTED"))) {
            return new CurrentNodeInfo(NODE_CONTOUR_TASK, "勾画任务", ROUTE_CONTOUR_TASK, contourTask.getId());
        }
        if (ctQueue != null && ctQueue.getId() != null
                && !StrUtil.equals(ctQueue.getQueueStatus(), "DONE")) {
            return new CurrentNodeInfo(NODE_CT_QUEUE, "CT排队", ROUTE_CT_QUEUE, ctQueue.getId());
        }
        if (ctAppointment != null && ctAppointment.getId() != null
                && !StrUtil.equals(ctAppointment.getWorkflowStatus(), "DONE")) {
            return new CurrentNodeInfo(NODE_CT_APPOINTMENT, "CT预约", ROUTE_CT_APPOINTMENT, ctAppointment.getId());
        }
        return new CurrentNodeInfo(NODE_REGISTER, "患者登记", ROUTE_REGISTER, patient.getId());
    }

    private String resolveStageCode(CtAppointmentDO ctAppointment,
                                    ContourTaskDO contourTask,
                                    PlanApplyDO planApply,
                                    PlanDesignDO planDesign,
                                    PlanVerifyDO planVerify,
                                    List<TreatmentAppointmentDO> treatmentAppointments,
                                    List<TreatmentExecuteDO> treatmentExecutes,
                                    TreatmentSummaryDO treatmentSummary) {
        if (treatmentSummary != null || !treatmentExecutes.isEmpty() || !treatmentAppointments.isEmpty()) {
            return STAGE_TREATMENT;
        }
        if (planVerify != null) {
            return STAGE_REVIEW;
        }
        if (planDesign != null || planApply != null) {
            return STAGE_PLANNING;
        }
        if (contourTask != null) {
            return STAGE_CONTOUR;
        }
        if (ctAppointment != null) {
            return STAGE_POSITIONING;
        }
        return STAGE_REGISTER;
    }

    private String resolveStageLabel(String stageCode) {
        if (StrUtil.equals(stageCode, STAGE_POSITIONING)) {
            return "定位";
        }
        if (StrUtil.equals(stageCode, STAGE_CONTOUR)) {
            return "勾画";
        }
        if (StrUtil.equals(stageCode, STAGE_PLANNING)) {
            return "计划";
        }
        if (StrUtil.equals(stageCode, STAGE_REVIEW)) {
            return "复核";
        }
        if (StrUtil.equals(stageCode, STAGE_TREATMENT)) {
            return "治疗";
        }
        return "登记";
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return true;
        }
        if (StrUtil.isBlank(source)) {
            return false;
        }
        return source.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private Long parseLong(String value) {
        if (StrUtil.isBlank(value) || !StrUtil.isNumeric(value)) {
            return null;
        }
        return Long.parseLong(value);
    }

    private <T> Map<Long, T> buildLatestMap(List<T> list, Function<T, Long> patientIdGetter) {
        Map<Long, T> result = new LinkedHashMap<>();
        for (T item : list) {
            Long patientId = patientIdGetter.apply(item);
            if (patientId == null || result.containsKey(patientId)) {
                continue;
            }
            result.put(patientId, item);
        }
        return result;
    }

    private <T> Map<Long, List<T>> buildListMap(List<T> list, Function<T, Long> patientIdGetter) {
        Map<Long, List<T>> result = new LinkedHashMap<>();
        for (T item : list) {
            Long patientId = patientIdGetter.apply(item);
            if (patientId == null) {
                continue;
            }
            result.computeIfAbsent(patientId, key -> new ArrayList<>()).add(item);
        }
        return result;
    }

    private void validatePatientForCreateOrUpdate(Long id, String patientNo, PatientSaveReqVO reqVO) {
        doctorService.validateDoctorExists(reqVO.getManagerDoctorId());
        doctorService.validateDoctorExists(reqVO.getAttendingDoctorId());
        validatePatientNoUnique(id, patientNo);
        validatePatientIdNoUnique(id, reqVO.getIdNo());
    }

    private void validatePatientNoUnique(Long id, String patientNo) {
        PatientDO patient = patientMapper.selectByPatientNo(patientNo);
        if (patient == null) {
            return;
        }
        if (!Objects.equals(patient.getId(), id)) {
            throw exception(PATIENT_NO_DUPLICATE);
        }
    }

    private void validatePatientIdNoUnique(Long id, String idNo) {
        if (StrUtil.isBlank(idNo)) {
            return;
        }
        PatientDO patient = patientMapper.selectByIdNo(idNo);
        if (patient == null) {
            return;
        }
        if (!Objects.equals(patient.getId(), id)) {
            throw exception(PATIENT_ID_NO_DUPLICATE);
        }
    }

    private String preparePatientNo(Long id, String patientNo) {
        if (StrUtil.isNotBlank(patientNo)) {
            return patientNo.trim();
        }
        for (int i = 0; i < 10; i++) {
            String generated = "PT" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + RandomUtil.randomNumbers(8);
            PatientDO patient = patientMapper.selectByPatientNo(generated);
            if (patient == null || Objects.equals(patient.getId(), id)) {
                return generated;
            }
        }
        return "PT" + System.currentTimeMillis();
    }

    private void normalizePatient(PatientDO patient) {
        if (patient.getGender() == null) {
            patient.setGender(0);
        }
        if (StrUtil.isBlank(patient.getIdType())) {
            patient.setIdType("身份证");
        }
        if (patient.getManagerDoctorId() == null) {
            patient.setManagerDoctorId(0L);
        }
        if (patient.getAttendingDoctorId() == null) {
            patient.setAttendingDoctorId(0L);
        }
        if (patient.getAge() == null) {
            patient.setAge(0);
        }
        if (patient.getBirthday() != null) {
            patient.setAge(Math.max(0, Period.between(patient.getBirthday(), LocalDate.now()).getYears()));
        }
        if (patient.getStatus() == null) {
            patient.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
    }

    private static class DashboardContext {
        private Map<String, String> creatorNameMap;
        private Map<Long, String> doctorNameMap;
        private Map<Long, CtAppointmentDO> ctAppointmentMap;
        private Map<Long, CtQueueDO> ctQueueMap;
        private Map<Long, ContourReviewDO> contourReviewMap;
        private Map<Long, ContourTaskDO> contourTaskMap;
        private Map<Long, PlanApplyDO> planApplyMap;
        private Map<Long, PlanDesignDO> planDesignMap;
        private Map<Long, PlanReviewDO> planReviewMap;
        private Map<Long, PlanVerifyDO> planVerifyMap;
        private Map<Long, List<TreatmentAppointmentDO>> treatmentAppointmentMap;
        private Map<Long, List<TreatmentExecuteDO>> treatmentExecuteMap;
        private Map<Long, TreatmentQueueDO> treatmentQueueMap;
        private Map<Long, TreatmentSummaryDO> treatmentSummaryMap;
    }

    private static class CurrentNodeInfo {
        private final String code;
        private final String name;
        private final String routePath;
        private final Long bizId;

        private CurrentNodeInfo(String code, String name, String routePath, Long bizId) {
            this.code = code;
            this.name = name;
            this.routePath = routePath;
            this.bizId = bizId;
        }
    }

}
