package cn.iocoder.yudao.server.init;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.hospital.controller.admin.contourreview.vo.ContourReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.contourtask.vo.ContourTaskSaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.ctappointment.vo.CtAppointmentSaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planapply.vo.PlanApplySaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.plandesign.vo.PlanDesignSaveReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planreview.vo.PlanReviewAuditReqVO;
import cn.iocoder.yudao.module.hospital.controller.admin.planverify.vo.PlanVerifyAuditReqVO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourreview.ContourReviewDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.contourtask.ContourTaskDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctappointment.CtAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.ctqueue.CtQueueDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.doctor.DoctorDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planapply.PlanApplyDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.plandesign.PlanDesignDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planreview.PlanReviewDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.planverify.PlanVerifyDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentappointment.TreatmentAppointmentDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentexecute.TreatmentExecuteDO;
import cn.iocoder.yudao.module.hospital.dal.dataobject.treatmentqueue.TreatmentQueueDO;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourreview.ContourReviewMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.contourtask.ContourTaskMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.ctqueue.CtQueueMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.doctor.DoctorMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planapply.PlanApplyMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.plandesign.PlanDesignMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planreview.PlanReviewMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.planverify.PlanVerifyMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentappointment.TreatmentAppointmentMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentexecute.TreatmentExecuteMapper;
import cn.iocoder.yudao.module.hospital.dal.mysql.treatmentqueue.TreatmentQueueMapper;
import cn.iocoder.yudao.module.hospital.enums.HospitalWorkflowConstants;
import cn.iocoder.yudao.module.hospital.service.contourreview.ContourReviewService;
import cn.iocoder.yudao.module.hospital.service.contourtask.ContourTaskService;
import cn.iocoder.yudao.module.hospital.service.ctappointment.CtAppointmentService;
import cn.iocoder.yudao.module.hospital.service.ctqueue.CtQueueService;
import cn.iocoder.yudao.module.hospital.service.planapply.PlanApplyService;
import cn.iocoder.yudao.module.hospital.service.plandesign.PlanDesignService;
import cn.iocoder.yudao.module.hospital.service.planreview.PlanReviewService;
import cn.iocoder.yudao.module.hospital.service.planreview.PlanReviewServiceImpl;
import cn.iocoder.yudao.module.hospital.service.planverify.PlanVerifyService;
import cn.iocoder.yudao.module.hospital.service.treatmentappointment.TreatmentAppointmentService;
import cn.iocoder.yudao.module.hospital.service.treatmentexecute.TreatmentExecuteService;
import cn.iocoder.yudao.module.hospital.service.treatmentqueue.TreatmentQueueService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
@Profile("local")
@Order(1001)
@Slf4j
public class HospitalWorkflowDemoDataInitializer implements ApplicationRunner {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_CONTOUR = 39001L;
    private static final Long USER_REVIEW = 39002L;
    private static final Long USER_PHYSICS = 39003L;
    private static final Long USER_TREATMENT = 39004L;
    private static final String TREATMENT_APPOINTMENT_TASK_KEY =
            HospitalWorkflowConstants.RADIOTHERAPY_GZ_PROCESS_KEY + "_step_12";

    @Resource
    private ContourTaskService contourTaskService;
    @Resource
    private ContourReviewService contourReviewService;
    @Resource
    private CtAppointmentService ctAppointmentService;
    @Resource
    private CtQueueService ctQueueService;
    @Resource
    private PlanApplyService planApplyService;
    @Resource
    private PlanDesignService planDesignService;
    @Resource
    private PlanReviewService planReviewService;
    @Resource
    private PlanVerifyService planVerifyService;
    @Resource
    private TreatmentAppointmentService treatmentAppointmentService;
    @Resource
    private TreatmentQueueService treatmentQueueService;
    @Resource
    private TreatmentExecuteService treatmentExecuteService;
    @Resource
    private ContourTaskMapper contourTaskMapper;
    @Resource
    private ContourReviewMapper contourReviewMapper;
    @Resource
    private CtQueueMapper ctQueueMapper;
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
    private TreatmentQueueMapper treatmentQueueMapper;
    @Resource
    private TreatmentExecuteMapper treatmentExecuteMapper;
    @Resource
    private DoctorMapper doctorMapper;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private BpmTaskService bpmTaskService;
    @Resource
    private org.flowable.engine.TaskService flowableTaskService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        TenantUtils.execute(TENANT_ID, () -> {
            ensureDemoActor(USER_CONTOUR, "workflow_demo_zhangmin", "张敏", 1001L, 2,
                    "放疗医师", "头颈与胸部肿瘤勾画", "13800039001", "DEMO39001");
            ensureDemoActor(USER_REVIEW, "workflow_demo_lihua", "李华", 1001L, 1,
                    "主任医师", "放疗审核与医师核准", "13800039002", "DEMO39002");
            ensureDemoActor(USER_PHYSICS, "workflow_demo_zhouning", "周宁", 1002L, 1,
                    "医学物理师", "剂量设计与计划复核", "13800039003", "DEMO39003");
            ensureDemoActor(USER_TREATMENT, "workflow_demo_wuchen", "吴晨", 1002L, 1,
                    "治疗技师", "计划验证与治疗预约", "13800039004", "DEMO39004");
            repairDemoPatients();
            clearLegacyBusinessData();
            seedCases();
            log.info("[HospitalWorkflowDemoDataInitializer][本地示例数据重建完成 ctQueue={} contourTask={} contourReview={} planApply={} planDesign={} planReview={} planVerify={} treatmentAppointment={} treatmentQueue={} treatmentExecute={}]",
                    ctQueueMapper.selectCount(), contourTaskMapper.selectCount(), contourReviewMapper.selectCount(),
                    planApplyMapper.selectCount(), planDesignMapper.selectCount(), planReviewMapper.selectCount(),
                    planVerifyMapper.selectCount(), treatmentAppointmentMapper.selectCount(), treatmentQueueMapper.selectCount(),
                    treatmentExecuteMapper.selectCount());
        });
    }

    private boolean hasBusinessData() {
        return contourTaskMapper.selectCount() > 0 || contourReviewMapper.selectCount() > 0
                || planApplyMapper.selectCount() > 0 || planDesignMapper.selectCount() > 0
                || planReviewMapper.selectCount() > 0 || planVerifyMapper.selectCount() > 0;
    }

    private boolean isLegacyDemoData() {
        if (contourReviewMapper.selectCount() > 0 || planDesignMapper.selectCount() > 0
                || planReviewMapper.selectCount() > 0 || planVerifyMapper.selectCount() > 0) {
            return false;
        }
        List<ContourTaskDO> contourTasks = contourTaskMapper.selectList();
        List<PlanApplyDO> planApplies = planApplyMapper.selectList();
        if (contourTasks.size() != 3 || planApplies.size() != 4) {
            return false;
        }
        return contourTasks.stream().allMatch(task -> task.getBizNo() != null
                        && task.getBizNo().startsWith("CTASK2026")
                        && (task.getProcessInstanceId() == null || task.getProcessInstanceId().trim().isEmpty()))
                && planApplies.stream().allMatch(apply -> apply.getBizNo() != null
                        && apply.getBizNo().startsWith("PLAN2026")
                        && (apply.getProcessInstanceId() == null || apply.getProcessInstanceId().trim().isEmpty()));
    }

    private boolean isResidualDemoData() {
        List<ContourTaskDO> contourTasks = contourTaskMapper.selectList();
        List<PlanApplyDO> planApplies = planApplyMapper.selectList();
        List<PlanDesignDO> planDesigns = planDesignMapper.selectList();
        List<PlanVerifyDO> planVerifies = planVerifyMapper.selectList();
        if (contourTasks.isEmpty() && planApplies.isEmpty() && planDesigns.isEmpty() && planVerifies.isEmpty()) {
            return false;
        }
        boolean demoOnly = contourTasks.stream().allMatch(task -> task.getPatientId() != null
                        && task.getPatientId() >= 31001L && task.getPatientId() <= 31099L
                        && USER_CONTOUR.equals(task.getContourDoctorId()))
                && planApplies.stream().allMatch(apply -> apply.getPatientId() != null
                        && apply.getPatientId() >= 31001L && apply.getPatientId() <= 31099L
                        && USER_REVIEW.equals(apply.getApplyDoctorId()))
                && planDesigns.stream().allMatch(design -> design.getPatientId() != null
                        && design.getPatientId() >= 31001L && design.getPatientId() <= 31099L
                        && USER_PHYSICS.equals(design.getDesignUserId()))
                && planVerifies.stream().allMatch(verify -> verify.getPatientId() != null
                        && verify.getPatientId() >= 31001L && verify.getPatientId() <= 31099L
                        && USER_TREATMENT.equals(verify.getVerifyUserId()));
        if (!demoOnly) {
            return false;
        }
        return contourTasks.size() < 9 || planApplies.size() < 7 || planDesigns.size() < 6 || planVerifies.size() < 2;
    }

    private void clearLegacyBusinessData() {
        String patientScope = " patient_id BETWEEN 31001 AND 31015 AND tenant_id = " + TENANT_ID;
        jdbcTemplate.execute("DELETE FROM hospital_treatment_summary WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_treatment_record WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_treatment_queue WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_treatment_appointment WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_plan_verify WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_plan_review WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_plan_design WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_plan_apply WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_contour_review WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_contour_task WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_ct_queue WHERE" + patientScope);
        jdbcTemplate.execute("DELETE FROM hospital_ct_appointment WHERE" + patientScope);
        log.info("[HospitalWorkflowDemoDataInitializer][已清理演示流程单据，准备重建完整业务闭环]");
    }

    private void repairDemoPatients() {
        upsertDemoPatient(31001L, "P31001", "登记病例-张建国", 1, 48, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31002L, "P31002", "CT预约-李淑芬", 2, 55, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31003L, "P31003", "CT排队-王海峰", 1, 63, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31004L, "P31004", "勾画任务-陈丽", 2, 46, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31005L, "P31005", "勾画审核-赵国强", 1, 59, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31006L, "P31006", "计划申请-周敏", 2, 52, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31007L, "P31007", "计划设计-刘洋", 1, 57, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31008L, "P31008", "组长审核-吴洁", 2, 61, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31009L, "P31009", "医师核准-孙涛", 1, 67, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31010L, "P31010", "计划复核-何静", 2, 58, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31011L, "P31011", "计划验证-高峰", 1, 50, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31012L, "P31012", "治疗预约-林洁", 2, 45, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31013L, "P31013", "治疗排队-郭强", 1, 54, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31014L, "P31014", "治疗执行-赵敏", 2, 47, USER_CONTOUR, USER_REVIEW);
        upsertDemoPatient(31015L, "P31015", "治疗小结-钱涛", 1, 62, USER_CONTOUR, USER_REVIEW);
    }

    private void upsertDemoPatient(Long id, String patientNo, String name, Integer gender, Integer age,
                                   Long managerDoctorId, Long attendingDoctorId) {
        jdbcTemplate.update("INSERT INTO hospital_patient (id, patient_no, name, gender, age, manager_doctor_id, attending_doctor_id, " +
                        "patient_type, status, deleted, tenant_id) VALUES (?, ?, ?, ?, ?, ?, ?, '门诊', 0, b'0', ?) " +
                        "ON DUPLICATE KEY UPDATE patient_no = VALUES(patient_no), " +
                        "name = VALUES(name), gender = VALUES(gender), age = VALUES(age), " +
                        "manager_doctor_id = VALUES(manager_doctor_id), " +
                        "attending_doctor_id = VALUES(attending_doctor_id), " +
                        "patient_type = VALUES(patient_type), status = 0, deleted = b'0', tenant_id = VALUES(tenant_id)",
                id, patientNo, name, gender, age, managerDoctorId, attendingDoctorId, TENANT_ID);
    }

    private void seedCases() {
        createCtAppointmentPendingCase(31002L, "头颈部");
        createCtQueueCase(31003L, "肺部");
        createContourTaskPendingCase(31004L, "乳腺");
        createContourReviewingCase(31005L, "盆腔");
        createPlanApplyDraftCase(31006L, "纵隔");
        createPlanDesignDraftCase(31007L, "腹部");
        createGroupReviewingCase(31008L, "胸椎");
        createDoctorApprovalReviewingCase(31009L, "前列腺");
        createRecheckReviewingCase(31010L, "肝区");
        createPlanVerifyDraftCase(31011L, "鼻咽部");
        createTreatmentAppointmentCase(31012L, "乳腺");
        createTreatmentQueueCase(31013L, "肺部");
        createTreatmentExecuteCase(31014L, "盆腔");
        createTreatmentSummaryCase(31015L, "胸椎");
    }

    private void createCtAppointmentPendingCase(Long patientId, String treatmentSite) {
        createCtAppointment("CTAP02", patientId, treatmentSite, "CT预约待确认");
    }

    private void createCtQueueCase(Long patientId, String treatmentSite) {
        Long appointmentId = createCtAppointment("CTAP03", patientId, treatmentSite, "CT排队候诊");
        ctAppointmentService.submitCtAppointment(appointmentId);
    }

    private void createContourTaskPendingCase(Long patientId, String treatmentSite) {
        Long appointmentId = createCtAppointment("CTAP04", patientId, treatmentSite, "CT完成待勾画");
        ctAppointmentService.submitCtAppointment(appointmentId);
        CtQueueDO queue = ctQueueMapper.selectByAppointmentId(appointmentId);
        ctQueueService.callCtQueue(queue.getId());
        ctQueueService.finishCtQueue(queue.getId());
    }

    private void createContourReviewingCase(Long patientId, String treatmentSite) {
        Long contourTaskId = createSubmittedContourTask("GZTH001", patientId, treatmentSite, "勾画已提交，待审核");
        ContourReviewDO review = getContourReview(contourTaskId);
        assignCurrentTask(review.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_CONTOUR_REVIEW_KEY, USER_REVIEW);
    }

    private void createContourRejectedCase(Long patientId, String treatmentSite) {
        Long contourTaskId = createSubmittedContourTask("GZTH002", patientId, treatmentSite, "勾画退回重修案例");
        ContourReviewDO review = getContourReview(contourTaskId);
        assignCurrentTask(review.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_CONTOUR_REVIEW_KEY, USER_REVIEW);
        ContourReviewAuditReqVO reqVO = new ContourReviewAuditReqVO();
        reqVO.setId(review.getId());
        reqVO.setReviewDoctorId(USER_REVIEW);
        reqVO.setReviewComment("CTV 边界需补充锁骨上区，退回后重新提交");
        reqVO.setRemark("退回重勾");
        contourReviewService.rejectContourReview(reqVO);
    }
    private void createPlanApplyDraftCase(Long patientId, String treatmentSite) {
        Long contourTaskId = createSubmittedContourTask("GZTH003", patientId, treatmentSite, "待提交计划申请");
        approveContourReview(contourTaskId, "勾画审核通过，转入计划申请");
        Long planApplyId = createPlanApply("GZFA003", contourTaskId, treatmentSite, "局部晚期肿瘤", new BigDecimal("60.00"), 30,
                "HIGH", "计划申请待提交");
        PlanApplyDO apply = planApplyMapper.selectById(planApplyId);
        assignCurrentTask(apply.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_APPLY_KEY, USER_REVIEW);
    }

    private void createPlanDesignDraftCase(Long patientId, String treatmentSite) {
        Long contourTaskId = createSubmittedContourTask("GZTH004", patientId, treatmentSite, "待制定物理计划");
        approveContourReview(contourTaskId, "勾画审核通过");
        Long planApplyId = createPlanApply("GZFA004", contourTaskId, treatmentSite, "术后辅助放疗", new BigDecimal("50.00"), 25,
                "NORMAL", "计划申请已提交");
        planApplyService.submitPlanApply(planApplyId);
        Long planDesignId = createPlanDesign("GZSJ004", planApplyId, "乳腺 VMAT 计划", new BigDecimal("50.00"),
                new BigDecimal("2.00"), 25, "计划设计待提交");
        String processInstanceId = planApplyMapper.selectById(planApplyId).getProcessInstanceId();
        assignCurrentTask(processInstanceId, HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_DESIGN_KEY, USER_PHYSICS);
    }

    private void createGroupReviewingCase(Long patientId, String treatmentSite) {
        Long planDesignId = createReviewingPlanDesign(patientId, treatmentSite, "GZ005", "组长审核中");
        PlanReviewDO review = getPlanReview(planDesignId, PlanReviewServiceImpl.STAGE_GROUP_LEADER);
        assignCurrentTask(review.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_GROUP_REVIEW_KEY, USER_REVIEW);
    }

    private void createDoctorApprovalReviewingCase(Long patientId, String treatmentSite) {
        Long planDesignId = createReviewingPlanDesign(patientId, treatmentSite, "GZ006", "医师核准中");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_GROUP_LEADER, USER_REVIEW,
                "组长审核通过，可提交医师核准", "流转到医师核准");
        PlanReviewDO review = getPlanReview(planDesignId, PlanReviewServiceImpl.STAGE_DOCTOR_APPROVAL);
        assignCurrentTask(review.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_DOCTOR_APPROVAL_KEY, USER_REVIEW);
    }

    private void createRecheckReviewingCase(Long patientId, String treatmentSite) {
        Long planDesignId = createReviewingPlanDesign(patientId, treatmentSite, "GZ007", "计划复核中");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_GROUP_LEADER, USER_REVIEW,
                "组长审核通过", "转医师核准");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_DOCTOR_APPROVAL, USER_REVIEW,
                "医师核准通过", "转计划复核");
        PlanReviewDO review = getPlanReview(planDesignId, PlanReviewServiceImpl.STAGE_RECHECK);
        assignCurrentTask(review.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_RECHECK_KEY, USER_PHYSICS);
    }

    private void createPlanVerifyDraftCase(Long patientId, String treatmentSite) {
        Long planDesignId = createReviewingPlanDesign(patientId, treatmentSite, "GZ008", "计划验证待处理");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_GROUP_LEADER, USER_REVIEW,
                "组长审核通过", "转医师核准");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_DOCTOR_APPROVAL, USER_REVIEW,
                "医师核准通过", "转计划复核");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_RECHECK, USER_PHYSICS,
                "计划复核通过", "转计划验证");
        PlanVerifyDO verify = getPlanVerify(planDesignId);
        assignCurrentTask(verify.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_VERIFY_KEY, USER_TREATMENT);
    }

    private void createTreatmentAppointmentCase(Long patientId, String treatmentSite) {
        createTreatmentAppointmentWorkflowCase(patientId, treatmentSite, "GZ012", "治疗预约待安排");
    }

    private void createTreatmentQueueCase(Long patientId, String treatmentSite) {
        TreatmentAppointmentDO appointment = createTreatmentAppointmentWorkflowCase(patientId, treatmentSite, "GZ013", "治疗排队候诊");
        treatmentAppointmentService.submitTreatmentAppointment(appointment.getId());
    }

    private void createTreatmentExecuteCase(Long patientId, String treatmentSite) {
        TreatmentAppointmentDO appointment = createTreatmentAppointmentWorkflowCase(patientId, treatmentSite, "GZ014", "治疗执行待开始");
        treatmentAppointmentService.submitTreatmentAppointment(appointment.getId());
        TreatmentQueueDO queue = treatmentQueueMapper.selectByTreatmentAppointmentId(appointment.getId());
        treatmentQueueService.callTreatmentQueue(queue.getId());
        treatmentQueueService.finishTreatmentQueue(queue.getId());
    }

    private void createTreatmentSummaryCase(Long patientId, String treatmentSite) {
        TreatmentAppointmentDO appointment = createTreatmentAppointmentWorkflowCase(patientId, treatmentSite, "GZ015", "治疗完成待小结");
        treatmentAppointmentService.submitTreatmentAppointment(appointment.getId());
        TreatmentQueueDO queue = treatmentQueueMapper.selectByTreatmentAppointmentId(appointment.getId());
        treatmentQueueService.callTreatmentQueue(queue.getId());
        treatmentQueueService.finishTreatmentQueue(queue.getId());
        TreatmentExecuteDO execute = treatmentExecuteMapper.selectByTreatmentAppointmentId(appointment.getId());
        treatmentExecuteService.completeTreatment(execute.getId());
    }

    private TreatmentAppointmentDO createTreatmentAppointmentWorkflowCase(Long patientId, String treatmentSite, String caseNo, String remark) {
        Long planDesignId = createReviewingPlanDesign(patientId, treatmentSite, caseNo, remark);
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_GROUP_LEADER, USER_REVIEW,
                "组长审核通过", "转医师核准");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_DOCTOR_APPROVAL, USER_REVIEW,
                "医师核准通过", "转计划复核");
        approvePlanReview(planDesignId, PlanReviewServiceImpl.STAGE_RECHECK, USER_PHYSICS,
                "计划复核通过", "转计划验证");
        PlanVerifyDO verify = getPlanVerify(planDesignId);
        assignCurrentTask(verify.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_VERIFY_KEY, USER_TREATMENT);
        PlanVerifyAuditReqVO reqVO = new PlanVerifyAuditReqVO();
        reqVO.setId(verify.getId());
        reqVO.setVerifyUserId(USER_TREATMENT);
        reqVO.setVerifyDeviceName("Delta4 + VersaHD QA");
        reqVO.setReportUrl("/demo/report/" + caseNo + ".pdf");
        reqVO.setRemark(remark);
        planVerifyService.verifyPlan(reqVO);
        assignCurrentTask(verify.getProcessInstanceId(), TREATMENT_APPOINTMENT_TASK_KEY, USER_TREATMENT);
        return treatmentAppointmentMapper.selectByPlanVerifyId(verify.getId());
    }

    private Long createCtAppointment(String bizNo, Long patientId, String treatmentSite, String remark) {
        CtAppointmentSaveReqVO reqVO = new CtAppointmentSaveReqVO();
        reqVO.setBizNo(bizNo);
        reqVO.setPatientId(patientId);
        reqVO.setAppointmentDate(LocalDate.now());
        reqVO.setAppointmentSlot("上午");
        reqVO.setCtRoomName("CT模拟定位室");
        reqVO.setCtDeviceName("Somatom go.Sim");
        reqVO.setApplyDoctorId(USER_REVIEW);
        reqVO.setPriority("HIGH");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setPositionNote(treatmentSite);
        reqVO.setRemark(remark);
        return ctAppointmentService.createCtAppointment(reqVO);
    }

    private Long createReviewingPlanDesign(Long patientId, String treatmentSite, String caseNo, String remark) {
        Long contourTaskId = createSubmittedContourTask("TH" + caseNo, patientId, treatmentSite, remark);
        approveContourReview(contourTaskId, "勾画审核通过");
        Long planApplyId = createPlanApply("FA" + caseNo, contourTaskId, treatmentSite, "根治性放疗", new BigDecimal("60.00"), 30,
                "HIGH", remark);
        planApplyService.submitPlanApply(planApplyId);
        Long planDesignId = createPlanDesign("SJ" + caseNo, planApplyId, treatmentSite + " IMRT 计划", new BigDecimal("60.00"),
                new BigDecimal("2.00"), 30, remark);
        planDesignService.submitPlanDesign(planDesignId);
        return planDesignId;
    }

    private Long createSubmittedContourTask(String bizNo, Long patientId, String treatmentSite, String remark) {
        ContourTaskSaveReqVO reqVO = new ContourTaskSaveReqVO();
        reqVO.setBizNo(bizNo);
        reqVO.setPatientId(patientId);
        reqVO.setCtAppointmentId(0L);
        reqVO.setContourDoctorId(USER_CONTOUR);
        reqVO.setTreatmentSite(treatmentSite);
        reqVO.setVersionNo("V1");
        reqVO.setAttachmentUrl("");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark(remark);
        Long id = contourTaskService.createContourTask(reqVO);
        contourTaskService.submitContourTask(id);
        return id;
    }
    private Long createPlanApply(String bizNo, Long contourTaskId, String treatmentSite, String diagnosis,
                                 BigDecimal dose, Integer fractions, String priority, String remark) {
        PlanApplySaveReqVO reqVO = new PlanApplySaveReqVO();
        reqVO.setBizNo(bizNo);
        reqVO.setContourTaskId(contourTaskId);
        reqVO.setApplyDoctorId(USER_REVIEW);
        reqVO.setTreatmentSite(treatmentSite);
        reqVO.setClinicalDiagnosis(diagnosis);
        reqVO.setPrescriptionDose(dose);
        reqVO.setTotalFractions(fractions);
        reqVO.setPriority(priority);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark(remark);
        return planApplyService.createPlanApply(reqVO);
    }

    private Long createPlanDesign(String bizNo, Long planApplyId, String planName,
                                  BigDecimal totalDose, BigDecimal singleDose, Integer fractions, String remark) {
        PlanDesignSaveReqVO reqVO = new PlanDesignSaveReqVO();
        reqVO.setBizNo(bizNo);
        reqVO.setPlanApplyId(planApplyId);
        reqVO.setDesignUserId(USER_PHYSICS);
        reqVO.setPlanName(planName);
        reqVO.setVersionNo("V1");
        reqVO.setTotalDose(totalDose);
        reqVO.setSingleDose(singleDose);
        reqVO.setFractions(fractions);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark(remark);
        return planDesignService.createPlanDesign(reqVO);
    }

    private void approveContourReview(Long contourTaskId, String comment) {
        ContourReviewDO review = getContourReview(contourTaskId);
        assignCurrentTask(review.getProcessInstanceId(), HospitalWorkflowConstants.RADIOTHERAPY_GZ_CONTOUR_REVIEW_KEY, USER_REVIEW);
        ContourReviewAuditReqVO reqVO = new ContourReviewAuditReqVO();
        reqVO.setId(review.getId());
        reqVO.setReviewDoctorId(USER_REVIEW);
        reqVO.setReviewComment(comment);
        reqVO.setRemark("勾画审核通过");
        contourReviewService.approveContourReview(reqVO);
    }

    private void approvePlanReview(Long planDesignId, String stage, Long reviewDoctorId, String comment, String remark) {
        PlanReviewDO review = getPlanReview(planDesignId, stage);
        assignCurrentTask(review.getProcessInstanceId(), resolveTaskDefineKey(stage), reviewDoctorId);
        PlanReviewAuditReqVO reqVO = new PlanReviewAuditReqVO();
        reqVO.setId(review.getId());
        reqVO.setReviewDoctorId(reviewDoctorId);
        reqVO.setReviewComment(comment);
        reqVO.setRemark(remark);
        planReviewService.approvePlanReview(stage, reqVO);
    }

    private ContourReviewDO getContourReview(Long contourTaskId) {
        return contourReviewMapper.selectFirstOne(ContourReviewDO::getContourTaskId, contourTaskId);
    }

    private PlanReviewDO getPlanReview(Long planDesignId, String stage) {
        return planReviewMapper.selectFirstOne(PlanReviewDO::getPlanDesignId, planDesignId, PlanReviewDO::getReviewStage, stage);
    }

    private PlanVerifyDO getPlanVerify(Long planDesignId) {
        return planVerifyMapper.selectFirstOne(PlanVerifyDO::getPlanDesignId, planDesignId);
    }

    private String resolveTaskDefineKey(String stage) {
        if (PlanReviewServiceImpl.STAGE_GROUP_LEADER.equals(stage)) {
            return HospitalWorkflowConstants.RADIOTHERAPY_GZ_GROUP_REVIEW_KEY;
        }
        if (PlanReviewServiceImpl.STAGE_DOCTOR_APPROVAL.equals(stage)) {
            return HospitalWorkflowConstants.RADIOTHERAPY_GZ_DOCTOR_APPROVAL_KEY;
        }
        return HospitalWorkflowConstants.RADIOTHERAPY_GZ_PLAN_RECHECK_KEY;
    }

    private void assignCurrentTask(String processInstanceId, String taskDefineKey, Long userId) {
        List<Task> tasks = bpmTaskService.getRunningTaskListByProcessInstanceId(processInstanceId, null, taskDefineKey);
        if (tasks == null || tasks.isEmpty() || userId == null) {
            return;
        }
        flowableTaskService.setAssignee(tasks.get(0).getId(), String.valueOf(userId));
    }

    private void ensureDemoActor(Long id, String username, String nickname, Long deptId, Integer sex,
                                 String title, String specialty, String phone, String doctorCode) {
        AdminUserDO user = adminUserMapper.selectById(id);
        if (user == null) {
            AdminUserDO template = adminUserMapper.selectById(1L);
            user = new AdminUserDO();
            user.setId(id);
            user.setUsername(username);
            user.setPassword(template != null ? template.getPassword() : new BCryptPasswordEncoder().encode("123456"));
            user.setNickname(nickname);
            user.setRemark("本地 BPM 演示流程参与人");
            user.setDeptId(103L);
            user.setPostIds(Collections.emptySet());
            user.setEmail(username + "@demo.local");
            user.setMobile(phone);
            user.setSex(sex);
            user.setAvatar("");
            user.setStatus(CommonStatusEnum.ENABLE.getStatus());
            user.setTenantId(TENANT_ID);
            user.setDeleted(Boolean.FALSE);
            adminUserMapper.insert(user);
        }
        DoctorDO doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            doctor = new DoctorDO();
            doctor.setId(id);
            doctor.setDeptId(deptId);
            doctor.setUserId(id);
            doctor.setDoctorCode(doctorCode);
            doctor.setName(nickname);
            doctor.setGender(sex);
            doctor.setPhone(phone);
            doctor.setTitle(title);
            doctor.setPracticingLicenseNo("LIC-" + id);
            doctor.setSpecialty(specialty);
            doctor.setSort(id.intValue());
            doctor.setStatus(CommonStatusEnum.ENABLE.getStatus());
            doctor.setRemark("本地 BPM 演示流程参与人");
            doctor.setTenantId(TENANT_ID);
            doctor.setDeleted(Boolean.FALSE);
            doctorMapper.insert(doctor);
        }
    }
}
