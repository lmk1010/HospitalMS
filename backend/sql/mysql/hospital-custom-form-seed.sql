DELETE FROM `hospital_custom_form` WHERE `code` IN ('CT_QUEUE_FORM','CONTOUR_REVIEW_FORM','PLAN_DESIGN_FORM') AND `tenant_id` = 1;

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-诊疗建档表单', 'hospital_radiotherapy_external_beam_gz_step_0_form', NULL, '综合业务', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_0', '诊疗建档', 'medical-record', '/hospital-flow/hospital-contour/task', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"diagnosis\\",\\"title\\":\\"临床诊断\\",\\"props\\":{\\"placeholder\\":\\"请输入临床诊断\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"archiveRemark\\",\\"title\\":\\"建档备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入建档备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-CT预约表单', 'hospital_radiotherapy_external_beam_gz_step_1_form', NULL, '定位管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_1', 'CT预约', 'ct-appointment', '/hospital-flow/hospital-position/ct-appointment', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"CT预约备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入CT预约备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-CT定位表单', 'hospital_radiotherapy_external_beam_gz_step_2_form', NULL, '定位管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_2', 'CT定位', 'ct-queue', '/hospital-flow/hospital-position/ct-queue', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"CT定位备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入CT定位备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-定位执行表单', 'hospital_radiotherapy_external_beam_gz_step_3_form', NULL, '定位管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_3', '定位执行', 'ct-queue', '/hospital-flow/hospital-position/ct-queue', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"定位执行备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入定位执行备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-靶区勾画表单', 'hospital_radiotherapy_external_beam_gz_step_4_form', NULL, '勾画管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_4', '靶区勾画', 'contour-task', '/hospital-flow/hospital-contour/task', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"contourDoctor\\",\\"title\\":\\"勾画医生\\",\\"props\\":{\\"placeholder\\":\\"请输入勾画医生\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"targetSite\\",\\"title\\":\\"靶区部位\\",\\"props\\":{\\"placeholder\\":\\"请输入靶区部位\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"contourRemark\\",\\"title\\":\\"靶区勾画说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入靶区勾画说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-勾画审核表单', 'hospital_radiotherapy_external_beam_gz_step_5_form', NULL, '勾画管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_5', '勾画审核', 'contour-review', '/hospital-flow/hospital-contour/review', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-计划申请表单', 'hospital_radiotherapy_external_beam_gz_step_6_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_6', '计划申请', 'plan-apply', '/hospital-flow/hospital-plan/apply', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"planName\\",\\"title\\":\\"计划名称\\",\\"props\\":{\\"placeholder\\":\\"请输入计划名称\\"}}","{\\"type\\":\\"inputNumber\\",\\"field\\":\\"totalDose\\",\\"title\\":\\"总剂量(Gy)\\",\\"props\\":{\\"min\\":0,\\"precision\\":2}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"planRemark\\",\\"title\\":\\"计划申请说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入计划申请说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-计划设计表单', 'hospital_radiotherapy_external_beam_gz_step_7_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_7', '计划设计', 'plan-design', '/hospital-flow/hospital-plan/design', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"planName\\",\\"title\\":\\"计划名称\\",\\"props\\":{\\"placeholder\\":\\"请输入计划名称\\"}}","{\\"type\\":\\"inputNumber\\",\\"field\\":\\"totalDose\\",\\"title\\":\\"总剂量(Gy)\\",\\"props\\":{\\"min\\":0,\\"precision\\":2}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"planRemark\\",\\"title\\":\\"计划设计说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入计划设计说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-组长审核表单', 'hospital_radiotherapy_external_beam_gz_step_8_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_8', '组长审核', 'plan-group-review', '/hospital-flow/hospital-plan/group-review', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-医师核准表单', 'hospital_radiotherapy_external_beam_gz_step_9_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_9', '医师核准', 'plan-doctor-approval', '/hospital-flow/hospital-plan/doctor-approval', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-计划复核表单', 'hospital_radiotherapy_external_beam_gz_step_10_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_10', '计划复核', 'plan-recheck', '/hospital-flow/hospital-plan/recheck', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-计划验证表单', 'hospital_radiotherapy_external_beam_gz_step_11_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_11', '计划验证', 'plan-verify', '/hospital-flow/hospital-plan/verify', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-治疗预约表单', 'hospital_radiotherapy_external_beam_gz_step_12_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_12', '治疗预约', 'treatment-appointment', '/hospital-flow/hospital-treatment/appointment', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗预约备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗预约备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-排队签到表单', 'hospital_radiotherapy_external_beam_gz_step_13_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_13', '排队签到', 'treatment-queue', '/hospital-flow/hospital-treatment/queue', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"排队签到备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入排队签到备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-叫号入室表单', 'hospital_radiotherapy_external_beam_gz_step_14_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_14', '叫号入室', 'treatment-queue', '/hospital-flow/hospital-treatment/queue', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"叫号入室备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入叫号入室备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-治疗执行表单', 'hospital_radiotherapy_external_beam_gz_step_15_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_15', '治疗执行', 'treatment-execute', '/hospital-flow/hospital-treatment/execute', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗执行备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗执行备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（GZ）-治疗小结表单', 'hospital_radiotherapy_external_beam_gz_step_16_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_gz', '外照射业务流程（GZ）', 'hospital_radiotherapy_external_beam_gz_step_16', '治疗小结', 'treatment-summary', '/hospital-flow/hospital-treatment/summary', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗小结备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗小结备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-诊疗建档表单', 'hospital_radiotherapy_external_beam_px_step_0_form', NULL, '综合业务', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_0', '诊疗建档', 'medical-record', '/hospital-workflow/external-beam-px', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"diagnosis\\",\\"title\\":\\"临床诊断\\",\\"props\\":{\\"placeholder\\":\\"请输入临床诊断\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"archiveRemark\\",\\"title\\":\\"建档备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入建档备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-影像采集表单', 'hospital_radiotherapy_external_beam_px_step_1_form', NULL, '定位管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_1', '影像采集', 'image-collect', '/hospital-workflow/external-beam-px', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"影像采集备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入影像采集备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-定位执行表单', 'hospital_radiotherapy_external_beam_px_step_2_form', NULL, '定位管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_2', '定位执行', 'ct-queue', '/hospital-flow/hospital-position/ct-queue', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"定位执行备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入定位执行备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-靶区勾画表单', 'hospital_radiotherapy_external_beam_px_step_3_form', NULL, '勾画管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_3', '靶区勾画', 'contour-task', '/hospital-flow/hospital-contour/task', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"contourDoctor\\",\\"title\\":\\"勾画医生\\",\\"props\\":{\\"placeholder\\":\\"请输入勾画医生\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"targetSite\\",\\"title\\":\\"靶区部位\\",\\"props\\":{\\"placeholder\\":\\"请输入靶区部位\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"contourRemark\\",\\"title\\":\\"靶区勾画说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入靶区勾画说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-勾画审核表单', 'hospital_radiotherapy_external_beam_px_step_4_form', NULL, '勾画管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_4', '勾画审核', 'contour-review', '/hospital-flow/hospital-contour/review', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-计划申请表单', 'hospital_radiotherapy_external_beam_px_step_5_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_5', '计划申请', 'plan-apply', '/hospital-flow/hospital-plan/apply', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"planName\\",\\"title\\":\\"计划名称\\",\\"props\\":{\\"placeholder\\":\\"请输入计划名称\\"}}","{\\"type\\":\\"inputNumber\\",\\"field\\":\\"totalDose\\",\\"title\\":\\"总剂量(Gy)\\",\\"props\\":{\\"min\\":0,\\"precision\\":2}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"planRemark\\",\\"title\\":\\"计划申请说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入计划申请说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-剂量设计表单', 'hospital_radiotherapy_external_beam_px_step_6_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_6', '剂量设计', 'plan-design', '/hospital-flow/hospital-plan/design', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"planName\\",\\"title\\":\\"计划名称\\",\\"props\\":{\\"placeholder\\":\\"请输入计划名称\\"}}","{\\"type\\":\\"inputNumber\\",\\"field\\":\\"totalDose\\",\\"title\\":\\"总剂量(Gy)\\",\\"props\\":{\\"min\\":0,\\"precision\\":2}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"planRemark\\",\\"title\\":\\"剂量设计说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入剂量设计说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-计划审核表单', 'hospital_radiotherapy_external_beam_px_step_7_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_7', '计划审核', 'plan-group-review', '/hospital-flow/hospital-plan/group-review', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-计划验证表单', 'hospital_radiotherapy_external_beam_px_step_8_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_8', '计划验证', 'plan-verify', '/hospital-flow/hospital-plan/verify', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-治疗执行表单', 'hospital_radiotherapy_external_beam_px_step_9_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_9', '治疗执行', 'treatment-execute', '/hospital-flow/hospital-treatment/execute', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗执行备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗执行备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（PX）-疗程评估表单', 'hospital_radiotherapy_external_beam_px_step_10_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_px', '外照射业务流程（PX）', 'hospital_radiotherapy_external_beam_px_step_10', '疗程评估', 'course-evaluate', '/hospital-flow/hospital-treatment/summary', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"evaluateDoctor\\",\\"title\\":\\"评估人\\",\\"props\\":{\\"placeholder\\":\\"请输入评估人\\"}}","{\\"type\\":\\"select\\",\\"field\\":\\"evaluateLevel\\",\\"title\\":\\"评估等级\\",\\"options\\":[{\\"label\\":\\"优\\",\\"value\\":\\"GOOD\\"},{\\"label\\":\\"良\\",\\"value\\":\\"NORMAL\\"},{\\"label\\":\\"待补充\\",\\"value\\":\\"PENDING\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"evaluateRemark\\",\\"title\\":\\"疗程评估备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入疗程评估备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-诊疗建档表单', 'hospital_radiotherapy_external_beam_sj_step_0_form', NULL, '综合业务', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_0', '诊疗建档', 'medical-record', '/hospital-workflow/external-beam-sj', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"diagnosis\\",\\"title\\":\\"临床诊断\\",\\"props\\":{\\"placeholder\\":\\"请输入临床诊断\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"archiveRemark\\",\\"title\\":\\"建档备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入建档备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-定位申请表单', 'hospital_radiotherapy_external_beam_sj_step_1_form', NULL, '定位管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_1', '定位申请', 'position-apply', '/hospital-workflow/external-beam-sj', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"定位申请备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入定位申请备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-CT定位表单', 'hospital_radiotherapy_external_beam_sj_step_2_form', NULL, '定位管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_2', 'CT定位', 'ct-queue', '/hospital-flow/hospital-position/ct-queue', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"CT定位备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入CT定位备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-影像融合表单', 'hospital_radiotherapy_external_beam_sj_step_3_form', NULL, '勾画管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_3', '影像融合', 'image-fusion', '/hospital-workflow/external-beam-sj', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"影像融合备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入影像融合备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-靶区勾画表单', 'hospital_radiotherapy_external_beam_sj_step_4_form', NULL, '勾画管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_4', '靶区勾画', 'contour-task', '/hospital-flow/hospital-contour/task', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"contourDoctor\\",\\"title\\":\\"勾画医生\\",\\"props\\":{\\"placeholder\\":\\"请输入勾画医生\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"targetSite\\",\\"title\\":\\"靶区部位\\",\\"props\\":{\\"placeholder\\":\\"请输入靶区部位\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"contourRemark\\",\\"title\\":\\"靶区勾画说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入靶区勾画说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-勾画审核表单', 'hospital_radiotherapy_external_beam_sj_step_5_form', NULL, '勾画管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_5', '勾画审核', 'contour-review', '/hospital-flow/hospital-contour/review', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-计划设计表单', 'hospital_radiotherapy_external_beam_sj_step_6_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_6', '计划设计', 'plan-design', '/hospital-flow/hospital-plan/design', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"planName\\",\\"title\\":\\"计划名称\\",\\"props\\":{\\"placeholder\\":\\"请输入计划名称\\"}}","{\\"type\\":\\"inputNumber\\",\\"field\\":\\"totalDose\\",\\"title\\":\\"总剂量(Gy)\\",\\"props\\":{\\"min\\":0,\\"precision\\":2}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"planRemark\\",\\"title\\":\\"计划设计说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入计划设计说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-医师核准表单', 'hospital_radiotherapy_external_beam_sj_step_7_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_7', '医师核准', 'plan-doctor-approval', '/hospital-flow/hospital-plan/doctor-approval', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-计划验证表单', 'hospital_radiotherapy_external_beam_sj_step_8_form', NULL, '计划管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_8', '计划验证', 'plan-verify', '/hospital-flow/hospital-plan/verify', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-治疗执行表单', 'hospital_radiotherapy_external_beam_sj_step_9_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_9', '治疗执行', 'treatment-execute', '/hospital-flow/hospital-treatment/execute', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗执行备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗执行备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '外照射业务流程（SJ）-治疗小结表单', 'hospital_radiotherapy_external_beam_sj_step_10_form', NULL, '治疗管理', 'hospital_radiotherapy_external_beam_sj', '外照射业务流程（SJ）', 'hospital_radiotherapy_external_beam_sj_step_10', '治疗小结', 'treatment-summary', '/hospital-flow/hospital-treatment/summary', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗小结备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗小结备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-诊疗建档表单', 'hospital_radiotherapy_brachytherapy_step_0_form', NULL, '综合业务', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_0', '诊疗建档', 'medical-record', '/hospital-workflow/brachytherapy', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"diagnosis\\",\\"title\\":\\"临床诊断\\",\\"props\\":{\\"placeholder\\":\\"请输入临床诊断\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"archiveRemark\\",\\"title\\":\\"建档备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入建档备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-术前评估表单', 'hospital_radiotherapy_brachytherapy_step_1_form', NULL, '综合业务', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_1', '术前评估', 'preoperative-evaluate', '/hospital-workflow/brachytherapy', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"evaluateDoctor\\",\\"title\\":\\"评估人\\",\\"props\\":{\\"placeholder\\":\\"请输入评估人\\"}}","{\\"type\\":\\"select\\",\\"field\\":\\"evaluateLevel\\",\\"title\\":\\"评估等级\\",\\"options\\":[{\\"label\\":\\"优\\",\\"value\\":\\"GOOD\\"},{\\"label\\":\\"良\\",\\"value\\":\\"NORMAL\\"},{\\"label\\":\\"待补充\\",\\"value\\":\\"PENDING\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"evaluateRemark\\",\\"title\\":\\"术前评估备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入术前评估备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-后装申请表单', 'hospital_radiotherapy_brachytherapy_step_2_form', NULL, '综合业务', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_2', '后装申请', 'brachy-apply', '/hospital-workflow/brachytherapy', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"责任人\\",\\"props\\":{\\"placeholder\\":\\"请输入责任人\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"taskContent\\",\\"title\\":\\"后装申请内容\\",\\"props\\":{\\"placeholder\\":\\"请输入后装申请内容\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"taskRemark\\",\\"title\\":\\"后装申请备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入后装申请备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-后装定位表单', 'hospital_radiotherapy_brachytherapy_step_3_form', NULL, '定位管理', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_3', '后装定位', 'brachy-position', '/hospital-workflow/brachytherapy', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"appointTime\\",\\"title\\":\\"安排时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"resourceName\\",\\"title\\":\\"机房/设备\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或设备\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"positionRemark\\",\\"title\\":\\"后装定位备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入后装定位备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-通道勾画表单', 'hospital_radiotherapy_brachytherapy_step_4_form', NULL, '勾画管理', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_4', '通道勾画', 'contour-task', '/hospital-flow/hospital-contour/task', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"contourDoctor\\",\\"title\\":\\"勾画医生\\",\\"props\\":{\\"placeholder\\":\\"请输入勾画医生\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"targetSite\\",\\"title\\":\\"靶区部位\\",\\"props\\":{\\"placeholder\\":\\"请输入靶区部位\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"contourRemark\\",\\"title\\":\\"通道勾画说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入通道勾画说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-计划设计表单', 'hospital_radiotherapy_brachytherapy_step_5_form', NULL, '计划管理', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_5', '计划设计', 'plan-design', '/hospital-flow/hospital-plan/design', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"planName\\",\\"title\\":\\"计划名称\\",\\"props\\":{\\"placeholder\\":\\"请输入计划名称\\"}}","{\\"type\\":\\"inputNumber\\",\\"field\\":\\"totalDose\\",\\"title\\":\\"总剂量(Gy)\\",\\"props\\":{\\"min\\":0,\\"precision\\":2}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"planRemark\\",\\"title\\":\\"计划设计说明\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入计划设计说明\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-医师核准表单', 'hospital_radiotherapy_brachytherapy_step_6_form', NULL, '计划管理', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_6', '医师核准', 'plan-doctor-approval', '/hospital-flow/hospital-plan/doctor-approval', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"handlerName\\",\\"title\\":\\"处理人\\",\\"props\\":{\\"placeholder\\":\\"请输入处理人\\"}}","{\\"type\\":\\"radio\\",\\"field\\":\\"handleResult\\",\\"title\\":\\"处理结果\\",\\"options\\":[{\\"label\\":\\"通过\\",\\"value\\":\\"PASS\\"},{\\"label\\":\\"退回\\",\\"value\\":\\"REJECT\\"}]}","{\\"type\\":\\"textarea\\",\\"field\\":\\"handleOpinion\\",\\"title\\":\\"处理意见\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入处理意见\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-治疗执行表单', 'hospital_radiotherapy_brachytherapy_step_7_form', NULL, '治疗管理', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_7', '治疗执行', 'treatment-execute', '/hospital-flow/hospital-treatment/execute', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗执行备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗执行备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';

INSERT INTO `hospital_custom_form` (
  `name`,`code`,`dept_id`,`biz_category`,`process_key`,`process_name`,`node_key`,`node_name`,`page_code`,`page_path`,`status`,`conf`,`fields`,`remark`,`creator`,`updater`,`deleted`,`tenant_id`
) VALUES (
  '后装治疗业务流程-治疗小结表单', 'hospital_radiotherapy_brachytherapy_step_8_form', NULL, '治疗管理', 'hospital_radiotherapy_brachytherapy', '后装治疗业务流程', 'hospital_radiotherapy_brachytherapy_step_8', '治疗小结', 'treatment-summary', '/hospital-flow/hospital-treatment/summary', 0, '{"form":{"labelWidth":"110px","labelPosition":"right","size":"default"}}', '["{\\"type\\":\\"input\\",\\"field\\":\\"patientName\\",\\"title\\":\\"患者姓名\\",\\"props\\":{\\"placeholder\\":\\"请输入患者姓名\\"}}","{\\"type\\":\\"input\\",\\"field\\":\\"patientNo\\",\\"title\\":\\"患者编号\\",\\"props\\":{\\"placeholder\\":\\"请输入患者编号\\"}}","{\\"type\\":\\"datePicker\\",\\"field\\":\\"scheduleTime\\",\\"title\\":\\"计划时间\\"}","{\\"type\\":\\"input\\",\\"field\\":\\"executePosition\\",\\"title\\":\\"机房/队列\\",\\"props\\":{\\"placeholder\\":\\"请输入机房或队列信息\\"}}","{\\"type\\":\\"textarea\\",\\"field\\":\\"treatmentRemark\\",\\"title\\":\\"治疗小结备注\\",\\"props\\":{\\"rows\\":4,\\"placeholder\\":\\"请输入治疗小结备注\\"}}"]', '系统预置节点表单', '1', '1', b'0', 1
)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `dept_id` = VALUES(`dept_id`),
  `biz_category` = VALUES(`biz_category`),
  `process_key` = VALUES(`process_key`),
  `process_name` = VALUES(`process_name`),
  `node_key` = VALUES(`node_key`),
  `node_name` = VALUES(`node_name`),
  `page_code` = VALUES(`page_code`),
  `page_path` = VALUES(`page_path`),
  `status` = VALUES(`status`),
  `conf` = VALUES(`conf`),
  `fields` = VALUES(`fields`),
  `remark` = VALUES(`remark`),
  `updater` = '1',
  `update_time` = NOW(),
  `deleted` = b'0';
