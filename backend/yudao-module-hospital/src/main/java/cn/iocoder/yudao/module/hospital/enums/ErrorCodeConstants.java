package cn.iocoder.yudao.module.hospital.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode DEPARTMENT_NOT_EXISTS = new ErrorCode(1_010_001_000, "科室不存在");
    ErrorCode DEPARTMENT_NAME_DUPLICATE = new ErrorCode(1_010_001_001, "已存在同名科室");
    ErrorCode DEPARTMENT_CODE_DUPLICATE = new ErrorCode(1_010_001_002, "已存在相同科室编码");
    ErrorCode DEPARTMENT_PARENT_NOT_EXISTS = new ErrorCode(1_010_001_003, "上级科室不存在");
    ErrorCode DEPARTMENT_PARENT_ERROR = new ErrorCode(1_010_001_004, "不能设置自己为上级科室");
    ErrorCode DEPARTMENT_EXISTS_CHILDREN = new ErrorCode(1_010_001_005, "存在下级科室，无法删除");

    ErrorCode DOCTOR_NOT_EXISTS = new ErrorCode(1_010_002_000, "医生不存在");
    ErrorCode DOCTOR_CODE_DUPLICATE = new ErrorCode(1_010_002_001, "已存在相同医生工号");
    ErrorCode DOCTOR_USER_ID_DUPLICATE = new ErrorCode(1_010_002_002, "该系统用户已绑定其他医生");

    ErrorCode PATIENT_NOT_EXISTS = new ErrorCode(1_010_003_000, "患者不存在");
    ErrorCode PATIENT_NO_DUPLICATE = new ErrorCode(1_010_003_001, "已存在相同患者档案号");
    ErrorCode PATIENT_ID_NO_DUPLICATE = new ErrorCode(1_010_003_002, "已存在相同证件号患者");

    ErrorCode CT_APPOINTMENT_NOT_EXISTS = new ErrorCode(1_010_004_000, "CT预约不存在");
    ErrorCode CT_APPOINTMENT_BIZ_NO_DUPLICATE = new ErrorCode(1_010_004_001, "已存在相同CT预约单号");
    ErrorCode CT_APPOINTMENT_SUBMIT_FAIL_STATUS = new ErrorCode(1_010_004_002, "当前CT预约状态不允许提交");

    ErrorCode CT_QUEUE_NOT_EXISTS = new ErrorCode(1_010_005_000, "CT排队记录不存在");
    ErrorCode CT_QUEUE_CALL_FAIL_STATUS = new ErrorCode(1_010_005_001, "当前CT排队状态不允许叫号");
    ErrorCode CT_QUEUE_SKIP_FAIL_STATUS = new ErrorCode(1_010_005_002, "当前CT排队状态不允许过号");
    ErrorCode CT_QUEUE_FINISH_FAIL_STATUS = new ErrorCode(1_010_005_003, "当前CT排队状态不允许完成");

    ErrorCode CONTOUR_TASK_NOT_EXISTS = new ErrorCode(1_010_006_000, "靶区勾画任务不存在");
    ErrorCode CONTOUR_TASK_BIZ_NO_DUPLICATE = new ErrorCode(1_010_006_001, "已存在相同勾画任务单号");
    ErrorCode CONTOUR_TASK_SUBMIT_FAIL_STATUS = new ErrorCode(1_010_006_002, "当前勾画任务状态不允许提交");

    ErrorCode CONTOUR_REVIEW_NOT_EXISTS = new ErrorCode(1_010_007_000, "勾画审核记录不存在");
    ErrorCode CONTOUR_REVIEW_BIZ_NO_DUPLICATE = new ErrorCode(1_010_007_001, "已存在相同勾画审核单号");
    ErrorCode CONTOUR_REVIEW_AUDIT_FAIL_STATUS = new ErrorCode(1_010_007_002, "当前勾画审核状态不允许操作");

    ErrorCode PLAN_APPLY_NOT_EXISTS = new ErrorCode(1_010_008_000, "计划申请不存在");
    ErrorCode PLAN_APPLY_BIZ_NO_DUPLICATE = new ErrorCode(1_010_008_001, "已存在相同计划申请单号");
    ErrorCode PLAN_APPLY_SUBMIT_FAIL_STATUS = new ErrorCode(1_010_008_002, "当前计划申请状态不允许提交");
    ErrorCode PLAN_APPLY_CONTOUR_STATUS_INVALID = new ErrorCode(1_010_008_003, "仅已通过的勾画任务允许发起计划申请");

    ErrorCode PLAN_DESIGN_NOT_EXISTS = new ErrorCode(1_010_009_000, "计划设计不存在");
    ErrorCode PLAN_DESIGN_BIZ_NO_DUPLICATE = new ErrorCode(1_010_009_001, "已存在相同计划设计单号");
    ErrorCode PLAN_DESIGN_SUBMIT_FAIL_STATUS = new ErrorCode(1_010_009_002, "当前计划设计状态不允许提交");
    ErrorCode PLAN_DESIGN_APPLY_STATUS_INVALID = new ErrorCode(1_010_009_003, "仅已提交或已通过的计划申请允许设计");

    ErrorCode PLAN_REVIEW_NOT_EXISTS = new ErrorCode(1_010_010_000, "计划审核记录不存在");
    ErrorCode PLAN_REVIEW_BIZ_NO_DUPLICATE = new ErrorCode(1_010_010_001, "已存在相同计划审核单号");
    ErrorCode PLAN_REVIEW_AUDIT_FAIL_STATUS = new ErrorCode(1_010_010_002, "当前计划审核状态不允许操作");
    ErrorCode PLAN_REVIEW_STAGE_INVALID = new ErrorCode(1_010_010_003, "当前计划审核阶段不匹配");

    ErrorCode PLAN_VERIFY_NOT_EXISTS = new ErrorCode(1_010_011_000, "计划验证记录不存在");
    ErrorCode PLAN_VERIFY_BIZ_NO_DUPLICATE = new ErrorCode(1_010_011_001, "已存在相同计划验证单号");
    ErrorCode PLAN_VERIFY_VERIFY_FAIL_STATUS = new ErrorCode(1_010_011_002, "当前计划验证状态不允许操作");

    ErrorCode TREATMENT_APPOINTMENT_NOT_EXISTS = new ErrorCode(1_010_012_000, "治疗预约不存在");
    ErrorCode TREATMENT_APPOINTMENT_BIZ_NO_DUPLICATE = new ErrorCode(1_010_012_001, "已存在相同治疗预约单号");
    ErrorCode TREATMENT_APPOINTMENT_SUBMIT_FAIL_STATUS = new ErrorCode(1_010_012_002, "当前治疗预约状态不允许提交");
    ErrorCode TREATMENT_APPOINTMENT_PLAN_VERIFY_STATUS_INVALID = new ErrorCode(1_010_012_003, "仅已验证计划允许发起治疗预约");

    ErrorCode TREATMENT_QUEUE_NOT_EXISTS = new ErrorCode(1_010_013_000, "治疗排队记录不存在");
    ErrorCode TREATMENT_QUEUE_CALL_FAIL_STATUS = new ErrorCode(1_010_013_001, "当前治疗排队状态不允许叫号");
    ErrorCode TREATMENT_QUEUE_SKIP_FAIL_STATUS = new ErrorCode(1_010_013_002, "当前治疗排队状态不允许过号");
    ErrorCode TREATMENT_QUEUE_FINISH_FAIL_STATUS = new ErrorCode(1_010_013_003, "当前治疗排队状态不允许完成");

    ErrorCode TREATMENT_EXECUTE_NOT_EXISTS = new ErrorCode(1_010_014_000, "治疗执行记录不存在");
    ErrorCode TREATMENT_EXECUTE_BIZ_NO_DUPLICATE = new ErrorCode(1_010_014_001, "已存在相同治疗执行单号");
    ErrorCode TREATMENT_EXECUTE_EXECUTE_FAIL_STATUS = new ErrorCode(1_010_014_002, "当前治疗执行状态不允许开始");
    ErrorCode TREATMENT_EXECUTE_COMPLETE_FAIL_STATUS = new ErrorCode(1_010_014_003, "当前治疗执行状态不允许完成");

    ErrorCode TREATMENT_SUMMARY_NOT_EXISTS = new ErrorCode(1_010_015_000, "治疗小结不存在");
    ErrorCode TREATMENT_SUMMARY_BIZ_NO_DUPLICATE = new ErrorCode(1_010_015_001, "已存在相同治疗小结单号");

    ErrorCode FEE_RECORD_NOT_EXISTS = new ErrorCode(1_010_016_000, "费用登记不存在");
    ErrorCode FEE_RECORD_BIZ_NO_DUPLICATE = new ErrorCode(1_010_016_001, "已存在相同费用登记单号");
    ErrorCode FEE_RECORD_UPDATE_FAIL_STATUS = new ErrorCode(1_010_016_002, "当前费用登记状态不允许修改");
    ErrorCode FEE_RECORD_DELETE_FAIL_STATUS = new ErrorCode(1_010_016_003, "当前费用登记状态不允许删除");

    ErrorCode FEE_SETTLEMENT_NOT_EXISTS = new ErrorCode(1_010_017_000, "费用结算不存在");
    ErrorCode FEE_SETTLEMENT_EMPTY_RECORDS = new ErrorCode(1_010_017_001, "当前患者暂无待结算费用");
    ErrorCode FEE_SETTLEMENT_REVERSE_FAIL_STATUS = new ErrorCode(1_010_017_002, "当前费用结算状态不允许冲正");

    ErrorCode CUSTOM_FORM_NOT_EXISTS = new ErrorCode(1_010_018_000, "医院自定义表单不存在");
    ErrorCode CUSTOM_FORM_CODE_DUPLICATE = new ErrorCode(1_010_018_001, "已存在相同表单编码");

}
