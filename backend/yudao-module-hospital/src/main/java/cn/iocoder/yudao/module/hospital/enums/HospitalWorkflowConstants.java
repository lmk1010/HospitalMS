package cn.iocoder.yudao.module.hospital.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class HospitalWorkflowConstants {

    public static final String RADIOTHERAPY_GZ_PROCESS_KEY = "hospital_radiotherapy_external_beam_gz";
    public static final String RADIOTHERAPY_GZ_CONTOUR_TASK_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_4";
    public static final String RADIOTHERAPY_GZ_CONTOUR_REVIEW_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_5";
    public static final String RADIOTHERAPY_GZ_PLAN_APPLY_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_6";
    public static final String RADIOTHERAPY_GZ_PLAN_DESIGN_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_7";
    public static final String RADIOTHERAPY_GZ_GROUP_REVIEW_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_8";
    public static final String RADIOTHERAPY_GZ_DOCTOR_APPROVAL_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_9";
    public static final String RADIOTHERAPY_GZ_PLAN_RECHECK_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_10";
    public static final String RADIOTHERAPY_GZ_PLAN_VERIFY_KEY = RADIOTHERAPY_GZ_PROCESS_KEY + "_step_11";

    public static final List<String> RADIOTHERAPY_GZ_TASKS = Collections.unmodifiableList(Arrays.asList(
            "诊疗建档", "CT预约", "CT定位", "定位执行", "靶区勾画", "勾画审核", "计划申请", "计划设计",
            "组长审核", "医师核准", "计划复核", "计划验证", "治疗预约", "排队签到", "叫号入室", "治疗执行", "治疗小结"
    ));

    private HospitalWorkflowConstants() {
    }
}
