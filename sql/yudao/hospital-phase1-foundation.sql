-- 上海XX医院管理平台：一期基础骨架
-- 范围：科室、医生、患者、一期菜单（患者管理 / 医护资源）
-- 执行顺序：在 hospital_ms 基础库初始化后执行

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 基础主数据表
-- ----------------------------
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
  KEY `idx_doctor_code` (`doctor_code`),
  KEY `idx_name` (`name`)
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
  KEY `idx_name` (`name`),
  KEY `idx_id_no` (`id_no`),
  KEY `idx_manager_doctor_id` (`manager_doctor_id`),
  KEY `idx_attending_doctor_id` (`attending_doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院患者档案表';

-- ----------------------------
-- 2. 一期示例数据
-- ----------------------------
INSERT INTO `hospital_department` (`id`, `name`, `code`, `parent_id`, `type`, `director_name`, `phone`, `sort`, `status`, `remark`, `creator`, `updater`, `tenant_id`)
SELECT 1001, '放疗科', 'RT', 0, 1, '张主任', '021-66000001', 1, 0, '一期初始化', 'admin', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM `hospital_department` WHERE `id` = 1001);

INSERT INTO `hospital_department` (`id`, `name`, `code`, `parent_id`, `type`, `director_name`, `phone`, `sort`, `status`, `remark`, `creator`, `updater`, `tenant_id`)
SELECT 1002, '影像科', 'IMG', 0, 2, '李主任', '021-66000002', 2, 0, '一期初始化', 'admin', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM `hospital_department` WHERE `id` = 1002);

INSERT INTO `hospital_doctor` (`id`, `dept_id`, `user_id`, `doctor_code`, `name`, `gender`, `phone`, `title`, `practicing_license_no`, `specialty`, `sort`, `status`, `remark`, `creator`, `updater`, `tenant_id`)
SELECT 2001, 1001, 0, 'D2026001', '王医生', 1, '13800000001', '主任医师', 'LIC-RT-001', '放疗评估', 1, 0, '一期初始化', 'admin', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM `hospital_doctor` WHERE `id` = 2001);

-- ----------------------------
-- 3. 一期菜单（患者管理 / 医护资源）
-- ----------------------------
DELETE FROM `system_role_menu` WHERE `menu_id` IN (
  21000,21010,21011,21012,21013,21020,21021,21022,21023,21024,
  21100,21110,21111,21112,21113,21114,21120,21121,21122,21123,21124
) AND `role_id` = 1 AND `tenant_id` = 1;

DELETE FROM `system_menu` WHERE `id` IN (
  21000,21010,21011,21012,21013,21020,21021,21022,21023,21024,
  21100,21110,21111,21112,21113,21114,21120,21121,21122,21123,21124
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `updater`, `deleted`)
VALUES
(21000, '患者管理', '', 1, 30, 0, 'hospital-patient', 'ep:user-filled', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21010, '患者登记', '', 2, 1, 21000, 'registration', 'ep:edit-pen', 'hospital/patient/registration/index', 'HospitalPatientRegistration', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21011, '患者登记创建', 'hospital:patient:create', 3, 1, 21010, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21012, '患者登记更新', 'hospital:patient:update', 3, 2, 21010, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21013, '患者登记查询', 'hospital:patient:query', 3, 3, 21010, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21020, '患者列表', '', 2, 2, 21000, 'list', 'ep:list', 'hospital/patient/list/index', 'HospitalPatientList', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21021, '患者列表查询', 'hospital:patient:query', 3, 1, 21020, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21022, '患者列表导出', 'hospital:patient:export', 3, 2, 21020, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21023, '患者列表删除', 'hospital:patient:delete', 3, 3, 21020, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21024, '患者列表详情', 'hospital:patient:detail', 3, 4, 21020, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21100, '医护资源', '', 1, 31, 0, 'hospital-resource', 'ep:avatar', NULL, NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21110, '科室管理', '', 2, 1, 21100, 'department', 'ep:office-building', 'hospital/department/index', 'HospitalDepartment', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21111, '科室查询', 'hospital:department:query', 3, 1, 21110, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21112, '科室创建', 'hospital:department:create', 3, 2, 21110, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21113, '科室更新', 'hospital:department:update', 3, 3, 21110, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21114, '科室删除', 'hospital:department:delete', 3, 4, 21110, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21120, '医生管理', '', 2, 2, 21100, 'doctor', 'ep:first-aid-kit', 'hospital/doctor/index', 'HospitalDoctor', 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21121, '医生查询', 'hospital:doctor:query', 3, 1, 21120, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21122, '医生创建', 'hospital:doctor:create', 3, 2, 21120, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21123, '医生更新', 'hospital:doctor:update', 3, 3, 21120, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0'),
(21124, '医生删除', 'hospital:doctor:delete', 3, 4, 21120, '', '', '', NULL, 0, b'1', b'1', b'1', 'admin', 'admin', b'0');

INSERT INTO `system_role_menu` (`id`, `role_id`, `menu_id`, `creator`, `updater`, `deleted`, `tenant_id`)
VALUES
(70001, 1, 21000, 'admin', 'admin', b'0', 1),
(70002, 1, 21010, 'admin', 'admin', b'0', 1),
(70003, 1, 21011, 'admin', 'admin', b'0', 1),
(70004, 1, 21012, 'admin', 'admin', b'0', 1),
(70005, 1, 21013, 'admin', 'admin', b'0', 1),
(70006, 1, 21020, 'admin', 'admin', b'0', 1),
(70007, 1, 21021, 'admin', 'admin', b'0', 1),
(70008, 1, 21022, 'admin', 'admin', b'0', 1),
(70009, 1, 21023, 'admin', 'admin', b'0', 1),
(70010, 1, 21024, 'admin', 'admin', b'0', 1),
(70011, 1, 21100, 'admin', 'admin', b'0', 1),
(70012, 1, 21110, 'admin', 'admin', b'0', 1),
(70013, 1, 21111, 'admin', 'admin', b'0', 1),
(70014, 1, 21112, 'admin', 'admin', b'0', 1),
(70015, 1, 21113, 'admin', 'admin', b'0', 1),
(70016, 1, 21114, 'admin', 'admin', b'0', 1),
(70017, 1, 21120, 'admin', 'admin', b'0', 1),
(70018, 1, 21121, 'admin', 'admin', b'0', 1),
(70019, 1, 21122, 'admin', 'admin', b'0', 1),
(70020, 1, 21123, 'admin', 'admin', b'0', 1),
(70021, 1, 21124, 'admin', 'admin', b'0', 1);

SET FOREIGN_KEY_CHECKS = 1;
