INSERT INTO `hospital_doctor` (`id`,`dept_id`,`user_id`,`doctor_code`,`name`,`gender`,`phone`,`title`,`practicing_license_no`,`specialty`,`sort`,`status`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`) VALUES
(2101,1001,1,'DOC2026002','张敏',2,'13800000021','主任医师','LIC-2101','鼻咽癌、宫颈癌放疗',1,0,'工作台演示医生','admin','admin',b'0',1),
(2102,1001,1,'DOC2026003','李华',1,'13800000022','副主任医师','LIC-2102','肺癌、乳腺癌放疗',2,0,'工作台演示医生','admin','admin',b'0',1),
(2103,1002,1,'DOC2026004','周宁',1,'13800000023','主治医师','LIC-2103','CT定位与计划验证',3,0,'工作台演示医生','admin','admin',b'0',1)
ON DUPLICATE KEY UPDATE `update_time` = `update_time`;

INSERT INTO `hospital_patient` (`id`,`patient_no`,`name`,`gender`,`birthday`,`age`,`id_type`,`id_no`,`outpatient_no`,`hospitalization_no`,`radiotherapy_no`,`medical_record_no`,`marital_status`,`native_place`,`current_address`,`patient_phone`,`contact_name`,`contact_relation`,`contact_phone`,`manager_doctor_id`,`attending_doctor_id`,`patient_type`,`ward_name`,`payment_type`,`visit_no`,`campus`,`first_doctor_name`,`social_security_no`,`allergy_history`,`past_history`,`tags`,`status`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`) VALUES
(31001,'P20260419001','王海燕',2,'1978-04-12',48,'ID_CARD','340101197804120021','MZ20260419001','ZY20260419001','FS20260419001','MR20260419001','已婚','安徽合肥','合肥市蜀山区望江西路','13900000001','王峰','配偶','13900000011',2101,2101,'住院','放疗一病区','INSURANCE','VST20260419001','本部院区','张敏','340101197804120021','青霉素轻敏','乳腺癌术后','乳腺癌,医保,复诊',0,'工作台演示患者','admin','admin',b'0',1),
(31002,'P20260419002','刘建国',1,'1969-07-08',57,'ID_CARD','340101196907080031','MZ20260419002','ZY20260419002','FS20260419002','MR20260419002','已婚','安徽六安','合肥市包河区屯溪路','13900000002','刘静','女儿','13900000012',2102,2102,'住院','放疗二病区','INSURANCE','VST20260419002','南区院区','李华','340101196907080031','','肺癌术后','肺癌,高风险',0,'工作台演示患者','admin','admin',b'0',1),
(31003,'P20260419003','陈晓梅',2,'1985-11-16',41,'ID_CARD','340101198511160042','MZ20260419003','ZY20260419003','FS20260419003','MR20260419003','已婚','安徽芜湖','合肥市政务区怀宁路','13900000003','陈伟','丈夫','13900000013',2101,2101,'门诊','日间病房','COMMERCIAL','VST20260419003','本部院区','张敏','340101198511160042','','宫颈癌同步放化疗','宫颈癌,加急',0,'工作台演示患者','admin','admin',b'0',1),
(31004,'P20260419004','张国梁',1,'1958-02-20',68,'ID_CARD','340101195802200053','MZ20260419004','ZY20260419004','FS20260419004','MR20260419004','已婚','安徽安庆','合肥市庐阳区阜阳路','13900000004','张帆','儿子','13900000014',2102,2102,'住院','放疗三病区','SELF_PAY','VST20260419004','北区院区','李华','340101195802200053','造影剂轻敏','前列腺癌','前列腺癌,自费',0,'工作台演示患者','admin','admin',b'0',1),
(31005,'P20260419005','赵雪',2,'1990-09-10',36,'ID_CARD','340101199009100064','MZ20260419005','ZY20260419005','FS20260419005','MR20260419005','未婚','安徽蚌埠','合肥市高新区创新大道','13900000005','赵丽','母亲','13900000015',2103,2103,'门诊','门诊治疗区','INSURANCE','VST20260419005','本部院区','周宁','340101199009100064','','鼻咽癌','鼻咽癌,验证中',0,'工作台演示患者','admin','admin',b'0',1),
(31006,'P20260419006','孙宏',1,'1974-03-01',52,'ID_CARD','340101197403010075','MZ20260419006','ZY20260419006','FS20260419006','MR20260419006','已婚','安徽阜阳','合肥市经开区丹霞路','13900000006','孙悦','妻子','13900000016',2103,2103,'住院','放疗监护区','INSURANCE','VST20260419006','南区院区','周宁','340101197403010075','','食管癌术后','食管癌,欠费',0,'工作台演示患者','admin','admin',b'0',1)
ON DUPLICATE KEY UPDATE `update_time` = `update_time`;

INSERT INTO `hospital_contour_task` (`id`,`biz_no`,`patient_id`,`patient_name`,`ct_appointment_id`,`contour_doctor_id`,`contour_doctor_name`,`treatment_site`,`version_no`,`attachment_url`,`submit_time`,`workflow_status`,`process_instance_id`,`status`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`) VALUES
(33001,'CTASK20260419001',31001,'王海燕',0,2101,'张敏','左乳腺','V2','',DATE_SUB(NOW(), INTERVAL 5 HOUR),'REVIEWING','',0,'等待审核意见','admin','admin',b'0',1),
(33002,'CTASK20260419002',31002,'刘建国',0,2102,'李华','右肺门','V1','',DATE_SUB(NOW(), INTERVAL 9 HOUR),'WAIT_CONTOUR','',0,'待医生提交勾画','admin','admin',b'0',1),
(33003,'CTASK20260419003',31003,'陈晓梅',0,2101,'张敏','宫颈盆腔','V3','',DATE_SUB(NOW(), INTERVAL 13 HOUR),'REJECTED','',0,'需要补充分层信息','admin','admin',b'0',1)
ON DUPLICATE KEY UPDATE `update_time` = `update_time`;

INSERT INTO `hospital_plan_apply` (`id`,`biz_no`,`patient_id`,`patient_name`,`contour_task_id`,`apply_doctor_id`,`apply_doctor_name`,`treatment_site`,`clinical_diagnosis`,`prescription_dose`,`total_fractions`,`priority`,`submit_time`,`workflow_status`,`process_instance_id`,`status`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`) VALUES
(34001,'PLAN20260419001',31001,'王海燕',33001,2101,'张敏','左乳腺','乳腺癌术后辅助放疗',50.00,25,'URGENT',DATE_SUB(NOW(), INTERVAL 3 HOUR),'REVIEWING','',0,'等待组长审核','admin','admin',b'0',1),
(34002,'PLAN20260419002',31002,'刘建国',33002,2102,'李华','右肺门','肺癌术后巩固放疗',60.00,30,'EMERGENCY',DATE_SUB(NOW(), INTERVAL 11 HOUR),'DRAFT','',0,'医生待提交','admin','admin',b'0',1),
(34003,'PLAN20260419003',31003,'陈晓梅',33003,2101,'张敏','宫颈盆腔','宫颈癌同步放化疗',45.00,25,'URGENT',DATE_SUB(NOW(), INTERVAL 18 HOUR),'REJECTED','',0,'需补充临床诊断','admin','admin',b'0',1),
(34004,'PLAN20260419004',31005,'赵雪',33001,2103,'周宁','鼻咽部','鼻咽癌根治放疗',70.00,33,'NORMAL',DATE_SUB(NOW(), INTERVAL 20 HOUR),'APPROVED','',0,'演示已通过单据','admin','admin',b'0',1)
ON DUPLICATE KEY UPDATE `update_time` = `update_time`;

INSERT INTO `hospital_treatment_appointment` (`id`,`biz_no`,`patient_id`,`patient_name`,`plan_verify_id`,`appointment_date`,`fraction_no`,`treatment_room_name`,`device_name`,`doctor_id`,`doctor_name`,`workflow_status`,`process_instance_id`,`status`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`) VALUES
(35001,'TREAT20260419001',31004,'张国梁',0,CURDATE(),1,'一号治疗室','瓦里安 TrueBeam',2102,'李华','WAIT_APPOINTMENT','',0,'待排今日首程治疗','admin','admin',b'0',1),
(35002,'TREAT20260419002',31005,'赵雪',0,DATE_ADD(CURDATE(), INTERVAL 1 DAY),8,'二号治疗室','医科达 Versa HD',2103,'周宁','TREATING','',0,'继续分次治疗','admin','admin',b'0',1),
(35003,'TREAT20260419003',31006,'孙宏',0,DATE_SUB(CURDATE(), INTERVAL 1 DAY),3,'三号治疗室','TomoTherapy',2103,'周宁','STOPPED','',0,'医保审批待补充','admin','admin',b'0',1)
ON DUPLICATE KEY UPDATE `update_time` = `update_time`;

INSERT INTO `hospital_ct_queue` (`id`,`queue_no`,`appointment_id`,`patient_id`,`patient_name`,`queue_date`,`queue_seq`,`sign_in_time`,`call_time`,`finish_time`,`queue_status`,`window_name`,`ct_room_name`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`) VALUES
(32001,'CTQ20260419001',0,31001,'王海燕',CURDATE(),1,DATE_SUB(NOW(), INTERVAL 50 MINUTE),DATE_SUB(NOW(), INTERVAL 30 MINUTE),NULL,'CALLED','一号签到窗','CT-01','已叫号等待进入机房','admin','admin',b'0',1),
(32002,'CTQ20260419002',0,31002,'刘建国',CURDATE(),2,DATE_SUB(NOW(), INTERVAL 35 MINUTE),NULL,NULL,'WAIT_SIGN','二号签到窗','CT-02','患者未签到','admin','admin',b'0',1),
(32003,'CTQ20260419003',0,31003,'陈晓梅',CURDATE(),3,DATE_SUB(NOW(), INTERVAL 90 MINUTE),DATE_SUB(NOW(), INTERVAL 70 MINUTE),NULL,'SKIPPED','一号签到窗','CT-01','已过号待人工处理','admin','admin',b'0',1),
(32004,'CTQ20260419004',0,31005,'赵雪',CURDATE(),4,DATE_SUB(NOW(), INTERVAL 15 MINUTE),NULL,NULL,'QUEUING','三号签到窗','CT-03','排队中','admin','admin',b'0',1)
ON DUPLICATE KEY UPDATE `update_time` = `update_time`;

INSERT INTO `hospital_fee_settlement` (`id`,`biz_no`,`patient_id`,`patient_name`,`total_amount`,`discount_amount`,`payable_amount`,`paid_amount`,`payment_type`,`pay_time`,`cashier_id`,`cashier_name`,`settlement_status`,`status`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`) VALUES
(36001,'FEE20260419001',31001,'王海燕',18600.00,600.00,18000.00,9000.00,'INSURANCE',DATE_SUB(NOW(), INTERVAL 2 HOUR),1,'收费岗A','PART_SETTLEMENT',0,'医保部分到账','admin','admin',b'0',1),
(36002,'FEE20260419002',31004,'张国梁',12800.00,0.00,12800.00,0.00,'SELF_PAY',NULL,1,'收费岗B','WAIT_SETTLEMENT',0,'待患者补缴','admin','admin',b'0',1),
(36003,'FEE20260419003',31006,'孙宏',9800.00,300.00,9500.00,9500.00,'INSURANCE',DATE_SUB(NOW(), INTERVAL 6 HOUR),1,'收费岗A','VOID',0,'原结算单冲正','admin','admin',b'0',1)
ON DUPLICATE KEY UPDATE `update_time` = `update_time`;
