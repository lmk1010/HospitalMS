-- 上海XX医院管理平台：业务全量骨架
-- 范围：字典、主数据、定位/勾画/计划/治疗/费用业务表、全量菜单与权限
-- 使用方式：导入 hospital_ms 库；适用于 Yudao 单体版初始化后的二次扩展

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `hospital_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室编号',
  `name` varchar(64) NOT NULL COMMENT '科室名称',
  `code` varchar(32) NOT NULL DEFAULT '' COMMENT '科室编码',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父科室编号',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '科室类型(1临床 2医技 3管理)',
  `director_name` varchar(32) NOT NULL DEFAULT '' COMMENT '负责人姓名',
  `phone` varchar(20) NOT NULL DEFAULT '' COMMENT '联系电话',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0正常 1停用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_name` (`name`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院科室表';

CREATE TABLE IF NOT EXISTS `hospital_doctor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生编号',
  `dept_id` bigint NOT NULL COMMENT '科室编号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '系统用户编号',
  `doctor_code` varchar(32) NOT NULL COMMENT '医生工号',
  `name` varchar(32) NOT NULL COMMENT '医生姓名',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别(0未知 1男 2女)',
  `phone` varchar(20) NOT NULL DEFAULT '' COMMENT '手机号',
  `title` varchar(32) NOT NULL DEFAULT '' COMMENT '职称',
  `practicing_license_no` varchar(64) NOT NULL DEFAULT '' COMMENT '执业证号',
  `specialty` varchar(255) DEFAULT NULL COMMENT '擅长方向',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0正常 1停用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_doctor_code` (`doctor_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院医生表';

CREATE TABLE IF NOT EXISTS `hospital_patient` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者编号',
  `patient_no` varchar(32) NOT NULL COMMENT '患者档案号',
  `name` varchar(32) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别(0未知 1男 2女)',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `age` int NOT NULL DEFAULT 0 COMMENT '年龄',
  `id_type` varchar(20) NOT NULL DEFAULT '身份证' COMMENT '证件类型',
  `id_no` varchar(64) NOT NULL DEFAULT '' COMMENT '证件号',
  `outpatient_no` varchar(32) NOT NULL DEFAULT '' COMMENT '门诊号',
  `hospitalization_no` varchar(32) NOT NULL DEFAULT '' COMMENT '住院号',
  `radiotherapy_no` varchar(32) NOT NULL DEFAULT '' COMMENT '放疗号',
  `medical_record_no` varchar(32) NOT NULL DEFAULT '' COMMENT '病案号',
  `marital_status` varchar(16) NOT NULL DEFAULT '' COMMENT '婚姻状态',
  `native_place` varchar(64) NOT NULL DEFAULT '' COMMENT '籍贯',
  `current_address` varchar(255) NOT NULL DEFAULT '' COMMENT '现住址',
  `patient_phone` varchar(20) NOT NULL DEFAULT '' COMMENT '患者电话',
  `contact_name` varchar(32) NOT NULL DEFAULT '' COMMENT '联系人姓名',
  `contact_relation` varchar(16) NOT NULL DEFAULT '' COMMENT '联系人关系',
  `contact_phone` varchar(20) NOT NULL DEFAULT '' COMMENT '联系人电话',
  `manager_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '主管医生编号',
  `attending_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '主诊医生编号',
  `patient_type` varchar(20) NOT NULL DEFAULT '' COMMENT '患者类型',
  `ward_name` varchar(32) NOT NULL DEFAULT '' COMMENT '病区',
  `payment_type` varchar(20) NOT NULL DEFAULT '' COMMENT '付费方式',
  `visit_no` varchar(32) NOT NULL DEFAULT '' COMMENT '就诊号',
  `campus` varchar(32) NOT NULL DEFAULT '' COMMENT '院区',
  `first_doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '首诊医生',
  `social_security_no` varchar(32) NOT NULL DEFAULT '' COMMENT '社保卡号',
  `photo_url` varchar(512) DEFAULT NULL COMMENT '患者照片',
  `allergy_history` text COMMENT '过敏史',
  `past_history` text COMMENT '既往史',
  `tags` varchar(255) NOT NULL DEFAULT '' COMMENT '标签',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0正常 1停用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_patient_no` (`patient_no`),
  KEY `idx_id_no` (`id_no`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院患者档案表';

CREATE TABLE IF NOT EXISTS `hospital_ct_appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'CT预约编号',
  `biz_no` varchar(32) NOT NULL COMMENT '预约单号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `appointment_slot` varchar(32) NOT NULL DEFAULT '' COMMENT '预约时段',
  `ct_room_name` varchar(32) NOT NULL DEFAULT '' COMMENT 'CT室',
  `ct_device_name` varchar(32) NOT NULL DEFAULT '' COMMENT 'CT设备',
  `apply_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '申请医生编号',
  `apply_doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '申请医生',
  `priority` varchar(16) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
  `position_note` varchar(500) DEFAULT NULL COMMENT '定位说明',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_appointment_date` (`appointment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CT预约表';

CREATE TABLE IF NOT EXISTS `hospital_ct_queue` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'CT排队编号',
  `queue_no` varchar(32) NOT NULL COMMENT '队列号',
  `appointment_id` bigint NOT NULL COMMENT 'CT预约编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `queue_date` date NOT NULL COMMENT '排队日期',
  `queue_seq` int NOT NULL DEFAULT 0 COMMENT '排队序号',
  `sign_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `call_time` datetime DEFAULT NULL COMMENT '叫号时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `queue_status` varchar(32) NOT NULL DEFAULT 'WAIT_SIGN' COMMENT '排队状态',
  `window_name` varchar(32) NOT NULL DEFAULT '' COMMENT '窗口/屏显名称',
  `ct_room_name` varchar(32) NOT NULL DEFAULT '' COMMENT 'CT室',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queue_no` (`queue_no`),
  KEY `idx_appointment_id` (`appointment_id`),
  KEY `idx_queue_date` (`queue_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CT排队叫号表';

CREATE TABLE IF NOT EXISTS `hospital_contour_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '勾画任务编号',
  `biz_no` varchar(32) NOT NULL COMMENT '任务单号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `ct_appointment_id` bigint NOT NULL DEFAULT 0 COMMENT 'CT预约编号',
  `contour_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '勾画医生编号',
  `contour_doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '勾画医生',
  `treatment_site` varchar(64) NOT NULL DEFAULT '' COMMENT '治疗部位',
  `version_no` varchar(32) NOT NULL DEFAULT 'V1' COMMENT '版本号',
  `attachment_url` varchar(255) NOT NULL DEFAULT '' COMMENT '附件地址',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'WAIT_CONTOUR' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='靶区勾画任务表';

CREATE TABLE IF NOT EXISTS `hospital_contour_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '勾画审核编号',
  `biz_no` varchar(32) NOT NULL COMMENT '审核单号',
  `contour_task_id` bigint NOT NULL COMMENT '勾画任务编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `review_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '审核医生编号',
  `review_doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '审核医生',
  `review_result` varchar(16) NOT NULL DEFAULT '' COMMENT '审核结果',
  `review_comment` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'REVIEWING' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_contour_task_id` (`contour_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='靶区勾画审核表';

CREATE TABLE IF NOT EXISTS `hospital_plan_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划申请编号',
  `biz_no` varchar(32) NOT NULL COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `contour_task_id` bigint NOT NULL DEFAULT 0 COMMENT '勾画任务编号',
  `apply_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '申请医生编号',
  `apply_doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '申请医生',
  `treatment_site` varchar(64) NOT NULL DEFAULT '' COMMENT '治疗部位',
  `clinical_diagnosis` varchar(255) NOT NULL DEFAULT '' COMMENT '临床诊断',
  `prescription_dose` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '处方总剂量',
  `total_fractions` int NOT NULL DEFAULT 0 COMMENT '总分次',
  `priority` varchar(16) NOT NULL DEFAULT 'NORMAL' COMMENT '紧急程度',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划申请表';

CREATE TABLE IF NOT EXISTS `hospital_plan_design` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划设计编号',
  `biz_no` varchar(32) NOT NULL COMMENT '设计单号',
  `plan_apply_id` bigint NOT NULL COMMENT '计划申请编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `design_user_id` bigint NOT NULL DEFAULT 0 COMMENT '设计人员编号',
  `design_user_name` varchar(32) NOT NULL DEFAULT '' COMMENT '设计人员',
  `plan_name` varchar(64) NOT NULL DEFAULT '' COMMENT '计划名称',
  `version_no` varchar(32) NOT NULL DEFAULT 'V1' COMMENT '版本号',
  `total_dose` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '总剂量',
  `single_dose` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单次剂量',
  `fractions` int NOT NULL DEFAULT 0 COMMENT '分次数',
  `design_time` datetime DEFAULT NULL COMMENT '设计时间',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_plan_apply_id` (`plan_apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划设计表';

CREATE TABLE IF NOT EXISTS `hospital_plan_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划审核编号',
  `biz_no` varchar(32) NOT NULL COMMENT '审核单号',
  `plan_design_id` bigint NOT NULL COMMENT '计划设计编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `review_stage` varchar(32) NOT NULL DEFAULT '' COMMENT '审核阶段',
  `review_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '审核人编号',
  `review_doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '审核人',
  `review_result` varchar(16) NOT NULL DEFAULT '' COMMENT '审核结果',
  `review_comment` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'REVIEWING' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_plan_design_id` (`plan_design_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划审核表';

CREATE TABLE IF NOT EXISTS `hospital_plan_verify` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计划验证编号',
  `biz_no` varchar(32) NOT NULL COMMENT '验证单号',
  `plan_design_id` bigint NOT NULL COMMENT '计划设计编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `verify_user_id` bigint NOT NULL DEFAULT 0 COMMENT '验证人编号',
  `verify_user_name` varchar(32) NOT NULL DEFAULT '' COMMENT '验证人',
  `verify_device_name` varchar(64) NOT NULL DEFAULT '' COMMENT '验证设备',
  `verify_result` varchar(16) NOT NULL DEFAULT '' COMMENT '验证结果',
  `report_url` varchar(255) NOT NULL DEFAULT '' COMMENT '验证报告地址',
  `verify_time` datetime DEFAULT NULL COMMENT '验证时间',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_plan_design_id` (`plan_design_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划验证表';

CREATE TABLE IF NOT EXISTS `hospital_treatment_appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '治疗预约编号',
  `biz_no` varchar(32) NOT NULL COMMENT '预约单号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `plan_verify_id` bigint NOT NULL DEFAULT 0 COMMENT '计划验证编号',
  `appointment_date` date NOT NULL COMMENT '治疗日期',
  `fraction_no` int NOT NULL DEFAULT 1 COMMENT '治疗分次',
  `treatment_room_name` varchar(32) NOT NULL DEFAULT '' COMMENT '治疗室',
  `device_name` varchar(64) NOT NULL DEFAULT '' COMMENT '设备名称',
  `doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '医生编号',
  `doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '医生姓名',
  `workflow_status` varchar(32) NOT NULL DEFAULT 'WAIT_APPOINTMENT' COMMENT '流程状态',
  `process_instance_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流程实例编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_appointment_date` (`appointment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗预约表';

CREATE TABLE IF NOT EXISTS `hospital_treatment_queue` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '治疗排队编号',
  `queue_no` varchar(32) NOT NULL COMMENT '队列号',
  `treatment_appointment_id` bigint NOT NULL COMMENT '治疗预约编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `queue_date` date NOT NULL COMMENT '排队日期',
  `queue_seq` int NOT NULL DEFAULT 0 COMMENT '排队序号',
  `sign_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `call_time` datetime DEFAULT NULL COMMENT '叫号时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '结束时间',
  `queue_status` varchar(32) NOT NULL DEFAULT 'WAIT_SIGN' COMMENT '排队状态',
  `treatment_room_name` varchar(32) NOT NULL DEFAULT '' COMMENT '治疗室',
  `device_name` varchar(64) NOT NULL DEFAULT '' COMMENT '设备名称',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queue_no` (`queue_no`),
  KEY `idx_treatment_appointment_id` (`treatment_appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗排队表';

CREATE TABLE IF NOT EXISTS `hospital_treatment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '治疗执行编号',
  `biz_no` varchar(32) NOT NULL COMMENT '执行单号',
  `treatment_appointment_id` bigint NOT NULL COMMENT '治疗预约编号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `treatment_date` date NOT NULL COMMENT '治疗日期',
  `fraction_no` int NOT NULL DEFAULT 1 COMMENT '治疗分次',
  `executor_id` bigint NOT NULL DEFAULT 0 COMMENT '执行人编号',
  `executor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '执行人姓名',
  `planned_dose` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '计划剂量',
  `actual_dose` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '实际剂量',
  `execute_result` varchar(16) NOT NULL DEFAULT '' COMMENT '执行结果',
  `abnormal_desc` varchar(500) DEFAULT NULL COMMENT '异常说明',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_treatment_appointment_id` (`treatment_appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗执行记录表';

CREATE TABLE IF NOT EXISTS `hospital_treatment_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '治疗小结编号',
  `biz_no` varchar(32) NOT NULL COMMENT '小结单号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `course_start_date` date DEFAULT NULL COMMENT '疗程开始日期',
  `course_end_date` date DEFAULT NULL COMMENT '疗程结束日期',
  `completed_fractions` int NOT NULL DEFAULT 0 COMMENT '完成分次',
  `summary_doctor_id` bigint NOT NULL DEFAULT 0 COMMENT '总结医生编号',
  `summary_doctor_name` varchar(32) NOT NULL DEFAULT '' COMMENT '总结医生',
  `evaluation_result` varchar(255) NOT NULL DEFAULT '' COMMENT '评估结果',
  `abnormal_desc` varchar(500) DEFAULT NULL COMMENT '异常说明',
  `summary_content` text COMMENT '小结内容',
  `summary_time` datetime DEFAULT NULL COMMENT '小结时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗小结表';

CREATE TABLE IF NOT EXISTS `hospital_fee_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '费用登记编号',
  `biz_no` varchar(32) NOT NULL COMMENT '费用单号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `source_type` varchar(32) NOT NULL DEFAULT '' COMMENT '来源类型',
  `source_biz_id` bigint NOT NULL DEFAULT 0 COMMENT '来源业务编号',
  `item_name` varchar(64) NOT NULL COMMENT '费用项目',
  `unit_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `quantity` decimal(10,2) NOT NULL DEFAULT 1.00 COMMENT '数量',
  `amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `charge_time` datetime DEFAULT NULL COMMENT '收费时间',
  `charge_user_id` bigint NOT NULL DEFAULT 0 COMMENT '收费人编号',
  `charge_user_name` varchar(32) NOT NULL DEFAULT '' COMMENT '收费人',
  `settlement_status` varchar(32) NOT NULL DEFAULT 'WAIT_SETTLEMENT' COMMENT '结算状态',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_charge_time` (`charge_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用登记表';

CREATE TABLE IF NOT EXISTS `hospital_fee_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '费用结算编号',
  `biz_no` varchar(32) NOT NULL COMMENT '结算单号',
  `patient_id` bigint NOT NULL COMMENT '患者编号',
  `patient_name` varchar(32) NOT NULL COMMENT '患者姓名',
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '应收总额',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `payable_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '应付金额',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '实收金额',
  `payment_type` varchar(32) NOT NULL DEFAULT '' COMMENT '支付方式',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `cashier_id` bigint NOT NULL DEFAULT 0 COMMENT '收费员编号',
  `cashier_name` varchar(32) NOT NULL DEFAULT '' COMMENT '收费员',
  `settlement_status` varchar(32) NOT NULL DEFAULT 'WAIT_SETTLEMENT' COMMENT '结算状态',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_no` (`biz_no`),
  KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费用结算表';

INSERT INTO `hospital_department` (`id`, `name`, `code`, `parent_id`, `type`, `director_name`, `phone`, `sort`, `status`, `remark`, `creator`, `updater`, `tenant_id`)
SELECT 1001, '放疗科', 'RT', 0, 1, '张主任', '021-66000001', 1, 0, '初始化数据', 'admin', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM `hospital_department` WHERE `id` = 1001);

INSERT INTO `hospital_department` (`id`, `name`, `code`, `parent_id`, `type`, `director_name`, `phone`, `sort`, `status`, `remark`, `creator`, `updater`, `tenant_id`)
SELECT 1002, '影像科', 'IMG', 0, 2, '李主任', '021-66000002', 2, 0, '初始化数据', 'admin', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM `hospital_department` WHERE `id` = 1002);

INSERT INTO `hospital_doctor` (`id`, `dept_id`, `user_id`, `doctor_code`, `name`, `gender`, `phone`, `title`, `practicing_license_no`, `specialty`, `sort`, `status`, `remark`, `creator`, `updater`, `tenant_id`)
SELECT 2001, 1001, 0, 'D2026001', '王医生', 1, '13800000001', '主任医师', 'LIC-RT-001', '放疗评估', 1, 0, '初始化数据', 'admin', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM `hospital_doctor` WHERE `id` = 2001);

-- 业务字典类型
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30001, '医院患者类型', 'hospital_patient_type', 0, '患者来源与就诊类型', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_patient_type');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30002, '医院付费方式', 'hospital_payment_type', 0, '患者付费方式', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_payment_type');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30003, 'CT预约状态', 'hospital_ct_appointment_status', 0, 'CT预约状态', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_ct_appointment_status');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30004, '排队状态', 'hospital_queue_status', 0, 'CT/治疗排队状态', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_queue_status');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30005, '勾画状态', 'hospital_contour_status', 0, '勾画任务与审核状态', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_contour_status');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30006, '计划状态', 'hospital_plan_status', 0, '计划申请/设计/验证状态', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_plan_status');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30007, '计划审核阶段', 'hospital_plan_review_stage', 0, '计划审核阶段', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_plan_review_stage');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30008, '治疗状态', 'hospital_treatment_status', 0, '治疗预约与执行状态', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_treatment_status');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`, `deleted_time`) SELECT 30009, '费用状态', 'hospital_fee_status', 0, '费用登记与结算状态', 'admin', 'admin', b'0', NULL WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'hospital_fee_status');

-- 业务字典数据
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31001, 1, '门诊', 'OUTPATIENT', 'hospital_patient_type', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31001);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31002, 2, '住院', 'INPATIENT', 'hospital_patient_type', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31002);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31003, 3, '复诊', 'REVISIT', 'hospital_patient_type', 0, 'warning', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31003);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31101, 4, '自费', 'SELF_PAY', 'hospital_payment_type', 0, 'default', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31101);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31102, 5, '医保', 'INSURANCE', 'hospital_payment_type', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31102);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31103, 6, '商保', 'COMMERCIAL', 'hospital_payment_type', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31103);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31201, 7, '待预约', 'PENDING', 'hospital_ct_appointment_status', 0, 'default', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31201);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31202, 8, '已预约', 'BOOKED', 'hospital_ct_appointment_status', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31202);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31203, 9, '已完成', 'DONE', 'hospital_ct_appointment_status', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31203);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31204, 10, '已取消', 'CANCELLED', 'hospital_ct_appointment_status', 0, 'danger', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31204);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31301, 11, '待签到', 'WAIT_SIGN', 'hospital_queue_status', 0, 'default', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31301);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31302, 12, '排队中', 'QUEUING', 'hospital_queue_status', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31302);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31303, 13, '已叫号', 'CALLED', 'hospital_queue_status', 0, 'warning', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31303);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31304, 14, '已完成', 'DONE', 'hospital_queue_status', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31304);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31305, 15, '已过号', 'SKIPPED', 'hospital_queue_status', 0, 'danger', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31305);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31401, 16, '待勾画', 'WAIT_CONTOUR', 'hospital_contour_status', 0, 'default', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31401);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31402, 17, '审核中', 'REVIEWING', 'hospital_contour_status', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31402);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31403, 18, '已通过', 'APPROVED', 'hospital_contour_status', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31403);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31404, 19, '已退回', 'REJECTED', 'hospital_contour_status', 0, 'danger', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31404);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31501, 20, '待提交', 'DRAFT', 'hospital_plan_status', 0, 'default', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31501);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31502, 21, '审核中', 'REVIEWING', 'hospital_plan_status', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31502);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31503, 22, '已通过', 'APPROVED', 'hospital_plan_status', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31503);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31504, 23, '已退回', 'REJECTED', 'hospital_plan_status', 0, 'danger', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31504);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31505, 24, '已验证', 'VERIFIED', 'hospital_plan_status', 0, 'warning', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31505);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31601, 25, '组长审核', 'GROUP_LEADER', 'hospital_plan_review_stage', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31601);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31602, 26, '医师核准', 'DOCTOR_APPROVAL', 'hospital_plan_review_stage', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31602);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31603, 27, '计划复核', 'RECHECK', 'hospital_plan_review_stage', 0, 'warning', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31603);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31701, 28, '待预约', 'WAIT_APPOINTMENT', 'hospital_treatment_status', 0, 'default', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31701);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31702, 29, '治疗中', 'TREATING', 'hospital_treatment_status', 0, 'primary', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31702);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31703, 30, '已完成', 'DONE', 'hospital_treatment_status', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31703);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31704, 31, '已终止', 'STOPPED', 'hospital_treatment_status', 0, 'danger', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31704);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31801, 32, '待结算', 'WAIT_SETTLEMENT', 'hospital_fee_status', 0, 'default', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31801);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31802, 33, '部分结算', 'PART_SETTLEMENT', 'hospital_fee_status', 0, 'warning', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31802);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31803, 34, '已结算', 'SETTLED', 'hospital_fee_status', 0, 'success', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31803);
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`) SELECT 31804, 35, '已作废', 'VOID', 'hospital_fee_status', 0, 'danger', '', '', 'admin', 'admin', b'0' WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `id` = 31804);

-- 清理既有医院菜单授权
DELETE FROM `system_role_menu` WHERE `menu_id` BETWEEN 20900 AND 22599 AND `tenant_id` = 1;
DELETE FROM `system_menu` WHERE `id` BETWEEN 20900 AND 22599;

-- 插入医院菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (20900, '医院工作台', '', 1, 29, 0, 'hospital-home', 'ep:monitor', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (20910, '工作台', 'hospital:home:query', 2, 1, 20900, 'workbench', 'ep:monitor', 'hospital/home/index', 'HospitalHome', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (20911, '工作台查询', 'hospital:home:query', 3, 1, 20910, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21000, '患者管理', '', 1, 30, 0, 'hospital-patient', 'ep:user-filled', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21010, '患者登记', 'hospital:patient:query', 2, 1, 21000, 'registration', 'ep:edit-pen', 'hospital/patient/registration/index', 'HospitalPatientRegistration', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21011, '患者登记查询', 'hospital:patient:query', 3, 1, 21010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21012, '患者登记创建', 'hospital:patient:create', 3, 2, 21010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21013, '患者登记更新', 'hospital:patient:update', 3, 3, 21010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21014, '患者登记删除', 'hospital:patient:delete', 3, 4, 21010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21015, '患者登记详情', 'hospital:patient:detail', 3, 5, 21010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21020, '患者列表', 'hospital:patient:query', 2, 2, 21000, 'list', 'ep:list', 'hospital/patient/list/index', 'HospitalPatientList', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21021, '患者列表查询', 'hospital:patient:query', 3, 1, 21020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21022, '患者列表导出', 'hospital:patient:export', 3, 2, 21020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21023, '患者列表删除', 'hospital:patient:delete', 3, 3, 21020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21024, '患者列表详情', 'hospital:patient:detail', 3, 4, 21020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21100, '医护资源', '', 1, 31, 0, 'hospital-resource', 'ep:avatar', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21110, '科室管理', 'hospital:department:query', 2, 1, 21100, 'department', 'ep:office-building', 'hospital/department/index', 'HospitalDepartment', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21111, '科室查询', 'hospital:department:query', 3, 1, 21110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21112, '科室创建', 'hospital:department:create', 3, 2, 21110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21113, '科室更新', 'hospital:department:update', 3, 3, 21110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21114, '科室删除', 'hospital:department:delete', 3, 4, 21110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21120, '医生管理', 'hospital:doctor:query', 2, 2, 21100, 'doctor', 'ep:first-aid-kit', 'hospital/doctor/index', 'HospitalDoctor', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21121, '医生查询', 'hospital:doctor:query', 3, 1, 21120, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21122, '医生创建', 'hospital:doctor:create', 3, 2, 21120, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21123, '医生更新', 'hospital:doctor:update', 3, 3, 21120, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (21124, '医生删除', 'hospital:doctor:delete', 3, 4, 21120, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22000, '定位管理', '', 1, 32, 0, 'hospital-position', 'ep:location', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22010, 'CT预约', 'hospital:ct-appointment:query', 2, 1, 22000, 'ct-appointment', 'ep:calendar', 'hospital/position/ct-appointment/index', 'HospitalCtAppointment', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22011, 'CT预约查询', 'hospital:ct-appointment:query', 3, 1, 22010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22012, 'CT预约创建', 'hospital:ct-appointment:create', 3, 2, 22010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22013, 'CT预约更新', 'hospital:ct-appointment:update', 3, 3, 22010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22014, 'CT预约提交', 'hospital:ct-appointment:submit', 3, 4, 22010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22015, 'CT预约删除', 'hospital:ct-appointment:delete', 3, 5, 22010, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22020, 'CT排队叫号', 'hospital:ct-queue:query', 2, 2, 22000, 'ct-queue', 'ep:bell', 'hospital/position/ct-queue/index', 'HospitalCtQueue', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22021, 'CT排队查询', 'hospital:ct-queue:query', 3, 1, 22020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22022, 'CT叫号', 'hospital:ct-queue:call', 3, 2, 22020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22023, 'CT过号', 'hospital:ct-queue:skip', 3, 3, 22020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22024, 'CT完成', 'hospital:ct-queue:finish', 3, 4, 22020, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22100, '勾画管理', '', 1, 33, 0, 'hospital-contour', 'ep:brush', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22110, '靶区勾画', 'hospital:contour-task:query', 2, 1, 22100, 'task', 'ep:crop', 'hospital/contour/task/index', 'HospitalContourTask', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22111, '勾画任务查询', 'hospital:contour-task:query', 3, 1, 22110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22112, '勾画任务创建', 'hospital:contour-task:create', 3, 2, 22110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22113, '勾画任务更新', 'hospital:contour-task:update', 3, 3, 22110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22114, '勾画任务提交', 'hospital:contour-task:submit', 3, 4, 22110, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22120, '勾画审核', 'hospital:contour-review:query', 2, 2, 22100, 'review', 'ep:circle-check', 'hospital/contour/review/index', 'HospitalContourReview', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22121, '勾画审核查询', 'hospital:contour-review:query', 3, 1, 22120, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22122, '勾画审核通过', 'hospital:contour-review:approve', 3, 2, 22120, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22123, '勾画审核退回', 'hospital:contour-review:reject', 3, 3, 22120, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22200, '计划管理', '', 1, 34, 0, 'hospital-plan', 'ep:document', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22210, '计划申请', 'hospital:plan-apply:query', 2, 1, 22200, 'apply', 'ep:document-add', 'hospital/plan/apply/index', 'HospitalPlanApply', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22211, '计划申请查询', 'hospital:plan-apply:query', 3, 1, 22210, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22212, '计划申请创建', 'hospital:plan-apply:create', 3, 2, 22210, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22213, '计划申请更新', 'hospital:plan-apply:update', 3, 3, 22210, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22214, '计划申请提交', 'hospital:plan-apply:submit', 3, 4, 22210, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22220, '计划设计', 'hospital:plan-design:query', 2, 2, 22200, 'design', 'ep:edit', 'hospital/plan/design/index', 'HospitalPlanDesign', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22221, '计划设计查询', 'hospital:plan-design:query', 3, 1, 22220, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22222, '计划设计创建', 'hospital:plan-design:create', 3, 2, 22220, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22223, '计划设计更新', 'hospital:plan-design:update', 3, 3, 22220, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22224, '计划设计提交', 'hospital:plan-design:submit', 3, 4, 22220, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22230, '组长审核', 'hospital:plan-group-review:query', 2, 3, 22200, 'group-review', 'ep:user', 'hospital/plan/group-review/index', 'HospitalPlanGroupReview', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22231, '组长审核查询', 'hospital:plan-group-review:query', 3, 1, 22230, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22232, '组长审核通过', 'hospital:plan-group-review:approve', 3, 2, 22230, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22233, '组长审核退回', 'hospital:plan-group-review:reject', 3, 3, 22230, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22240, '医师核准', 'hospital:plan-doctor-approval:query', 2, 4, 22200, 'doctor-approval', 'ep:stamp', 'hospital/plan/doctor-approval/index', 'HospitalPlanDoctorApproval', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22241, '医师核准查询', 'hospital:plan-doctor-approval:query', 3, 1, 22240, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22242, '医师核准通过', 'hospital:plan-doctor-approval:approve', 3, 2, 22240, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22243, '医师核准退回', 'hospital:plan-doctor-approval:reject', 3, 3, 22240, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22250, '计划复核', 'hospital:plan-recheck:query', 2, 5, 22200, 'recheck', 'ep:operation', 'hospital/plan/recheck/index', 'HospitalPlanRecheck', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22251, '计划复核查询', 'hospital:plan-recheck:query', 3, 1, 22250, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22252, '计划复核通过', 'hospital:plan-recheck:approve', 3, 2, 22250, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22253, '计划复核退回', 'hospital:plan-recheck:reject', 3, 3, 22250, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22260, '计划验证', 'hospital:plan-verify:query', 2, 6, 22200, 'verify', 'ep:finished', 'hospital/plan/verify/index', 'HospitalPlanVerify', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22261, '计划验证查询', 'hospital:plan-verify:query', 3, 1, 22260, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22262, '计划验证执行', 'hospital:plan-verify:verify', 3, 2, 22260, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22263, '计划验证不通过', 'hospital:plan-verify:reject', 3, 3, 22260, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22300, '治疗管理', '', 1, 35, 0, 'hospital-treatment', 'ep:first-aid-kit', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22310, '治疗预约', 'hospital:treatment-appointment:query', 2, 1, 22300, 'appointment', 'ep:calendar', 'hospital/treatment/appointment/index', 'HospitalTreatmentAppointment', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22311, '治疗预约查询', 'hospital:treatment-appointment:query', 3, 1, 22310, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22312, '治疗预约创建', 'hospital:treatment-appointment:create', 3, 2, 22310, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22313, '治疗预约更新', 'hospital:treatment-appointment:update', 3, 3, 22310, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22314, '治疗预约提交', 'hospital:treatment-appointment:submit', 3, 4, 22310, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22320, '排队叫号', 'hospital:treatment-queue:query', 2, 2, 22300, 'queue', 'ep:bell-filled', 'hospital/treatment/queue/index', 'HospitalTreatmentQueue', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22321, '治疗排队查询', 'hospital:treatment-queue:query', 3, 1, 22320, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22322, '治疗叫号', 'hospital:treatment-queue:call', 3, 2, 22320, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22323, '治疗过号', 'hospital:treatment-queue:skip', 3, 3, 22320, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22324, '治疗排队完成', 'hospital:treatment-queue:finish', 3, 4, 22320, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22330, '治疗执行', 'hospital:treatment-execute:query', 2, 3, 22300, 'execute', 'ep:video-play', 'hospital/treatment/execute/index', 'HospitalTreatmentExecute', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22331, '治疗执行查询', 'hospital:treatment-execute:query', 3, 1, 22330, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22332, '治疗执行开始', 'hospital:treatment-execute:execute', 3, 2, 22330, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22333, '治疗执行完成', 'hospital:treatment-execute:complete', 3, 3, 22330, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22340, '治疗小结', 'hospital:treatment-summary:query', 2, 4, 22300, 'summary', 'ep:document-checked', 'hospital/treatment/summary/index', 'HospitalTreatmentSummary', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22341, '治疗小结查询', 'hospital:treatment-summary:query', 3, 1, 22340, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22342, '治疗小结创建', 'hospital:treatment-summary:create', 3, 2, 22340, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22343, '治疗小结更新', 'hospital:treatment-summary:update', 3, 3, 22340, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22344, '治疗小结导出', 'hospital:treatment-summary:export', 3, 4, 22340, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22400, '费用管理', '', 1, 36, 0, 'hospital-fee', 'ep:money', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22410, '费用登记', 'hospital:fee-record:query', 2, 1, 22400, 'record', 'ep:coin', 'hospital/fee/record/index', 'HospitalFeeRecord', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22411, '费用登记查询', 'hospital:fee-record:query', 3, 1, 22410, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22412, '费用登记创建', 'hospital:fee-record:create', 3, 2, 22410, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22413, '费用登记更新', 'hospital:fee-record:update', 3, 3, 22410, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22414, '费用登记删除', 'hospital:fee-record:delete', 3, 4, 22410, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22420, '费用结算', 'hospital:fee-settlement:query', 2, 2, 22400, 'settlement', 'ep:wallet', 'hospital/fee/settlement/index', 'HospitalFeeSettlement', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22421, '费用结算查询', 'hospital:fee-settlement:query', 3, 1, 22420, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22422, '费用结算确认', 'hospital:fee-settlement:settle', 3, 2, 22420, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22423, '费用结算冲正', 'hospital:fee-settlement:reverse', 3, 3, 22420, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22430, '费用查询', 'hospital:fee-query:query', 2, 3, 22400, 'query', 'ep:search', 'hospital/fee/query/index', 'HospitalFeeQuery', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22431, '费用查询检索', 'hospital:fee-query:query', 3, 1, 22430, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22432, '费用查询导出', 'hospital:fee-query:export', 3, 2, 22430, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22500, '平台支撑', '', 1, 37, 0, 'hospital-support', 'ep:setting', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22510, '流程中心', 'hospital:support:workflow', 2, 1, 22500, 'workflow', 'ep:promotion', 'bpm/task/todo/index', 'HospitalSupportWorkflow', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22511, '流程中心查询', 'hospital:support:workflow', 3, 1, 22510, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22520, '消息通知', 'hospital:support:notify', 2, 2, 22500, 'notify', 'ep:bell', 'system/notify/my/index', 'HospitalSupportNotify', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22521, '消息通知查询', 'hospital:support:notify', 3, 1, 22520, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22530, '数据报表', 'hospital:support:report', 2, 3, 22500, 'report', 'ep:data-analysis', 'hospital/support/report/index', 'HospitalSupportReport', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22531, '数据报表查询', 'hospital:support:report', 3, 1, 22530, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22540, '远程控制', 'hospital:support:remote', 2, 4, 22500, 'remote', 'ep:monitor', 'hospital/support/remote/index', 'HospitalSupportRemote', 0, b'1', b'1', b'1', 'admin', 'admin', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`) VALUES (22541, '远程控制查询', 'hospital:support:remote', 3, 1, 22540, '', '', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

-- 授权超级管理员
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `updater`, `deleted`, `tenant_id`) SELECT 1, `id`, 'admin', 'admin', b'0', 1 FROM `system_menu` WHERE `id` BETWEEN 20900 AND 22599;

SET FOREIGN_KEY_CHECKS = 1;