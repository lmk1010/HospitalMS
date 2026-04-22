SET NAMES utf8mb4;

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`updater`,`deleted`)
SELECT 22900,'流程管理','',1,28,0,'hospital-flow','ep:connection',NULL,NULL,0,b'1',b'1',b'1','admin','admin',b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 22900);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`updater`,`deleted`)
SELECT 22910,'数据管理','',1,29,0,'hospital-data','ep:data-analysis',NULL,NULL,0,b'1',b'1',b'1','admin','admin',b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 22910);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`creator`,`updater`,`deleted`)
SELECT 22920,'远程控制','',1,30,0,'hospital-remote','ep:monitor',NULL,NULL,0,b'1',b'1',b'1','admin','admin',b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `id` = 22920);

UPDATE `system_menu` SET `name` = '流程管理' WHERE `id` = 22900;
UPDATE `system_menu` SET `name` = '数据管理' WHERE `id` = 22910;
UPDATE `system_menu` SET `name` = '远程控制' WHERE `id` = 22920;

UPDATE `system_menu` SET `name` = '工作台', `parent_id` = 22900, `sort` = 1 WHERE `id` = 20900;
UPDATE `system_menu` SET `type` = 2, `path` = 'workbench', `component` = 'hospital/home/index', `component_name` = 'HospitalWorkbench', `icon` = 'ep:house', `always_show` = b'0' WHERE `id` = 20900;
UPDATE `system_menu` SET `status` = 1, `visible` = b'0' WHERE `id` = 20910;
UPDATE `system_menu` SET `parent_id` = 20900 WHERE `id` = 20911;
UPDATE `system_menu` SET `name` = 'CT排队叫号' WHERE `id` = 22020;
UPDATE `system_menu` SET `name` = '医师审核' WHERE `id` = 22240;
UPDATE `system_menu` SET `name` = '患者管理', `parent_id` = 22900, `sort` = 2 WHERE `id` = 21000;
UPDATE `system_menu` SET `name` = '定位管理', `parent_id` = 22900, `sort` = 3 WHERE `id` = 22000;
UPDATE `system_menu` SET `name` = '勾画管理', `parent_id` = 22900, `sort` = 4 WHERE `id` = 22100;
UPDATE `system_menu` SET `name` = '计划管理', `parent_id` = 22900, `sort` = 5 WHERE `id` = 22200;
UPDATE `system_menu` SET `name` = '治疗管理', `parent_id` = 22900, `sort` = 6 WHERE `id` = 22300;
UPDATE `system_menu` SET `name` = '费用管理', `parent_id` = 22900, `sort` = 7 WHERE `id` = 22400;

UPDATE `system_menu` SET `name` = '数据资源', `parent_id` = 22910, `sort` = 1 WHERE `id` = 21100;

UPDATE `system_menu` SET `name` = '流程管理', `parent_id` = 22900, `sort` = 10, `path` = 'manage-list', `component` = 'hospital/flow/process-manage/index', `component_name` = 'HospitalWorkflowManage' WHERE `id` = 22510;
UPDATE `system_menu` SET `name` = '数据监测', `parent_id` = 22900, `sort` = 8 WHERE `id` = 22520;
UPDATE `system_menu` SET `name` = '统计分析', `parent_id` = 22900, `sort` = 9 WHERE `id` = 22530;
UPDATE `system_menu` SET `name` = '远程控制中心', `parent_id` = 22920, `sort` = 1 WHERE `id` = 22540;

UPDATE `system_menu` SET `visible` = b'0' WHERE `id` = 22500;

INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`creator`,`updater`,`deleted`,`tenant_id`)
SELECT DISTINCT s.`role_id`, 22900, 'admin', 'admin', b'0', s.`tenant_id`
FROM `system_role_menu` s
WHERE s.`menu_id` IN (20900,21000,22000,22100,22200,22300,22400)
  AND s.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` x
    WHERE x.`role_id` = s.`role_id` AND x.`menu_id` = 22900 AND x.`tenant_id` = s.`tenant_id` AND x.`deleted` = b'0'
  );

INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`creator`,`updater`,`deleted`,`tenant_id`)
SELECT DISTINCT s.`role_id`, 22910, 'admin', 'admin', b'0', s.`tenant_id`
FROM `system_role_menu` s
WHERE s.`menu_id` IN (21100)
  AND s.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` x
    WHERE x.`role_id` = s.`role_id` AND x.`menu_id` = 22910 AND x.`tenant_id` = s.`tenant_id` AND x.`deleted` = b'0'
  );

INSERT INTO `system_role_menu` (`role_id`,`menu_id`,`creator`,`updater`,`deleted`,`tenant_id`)
SELECT DISTINCT s.`role_id`, 22920, 'admin', 'admin', b'0', s.`tenant_id`
FROM `system_role_menu` s
WHERE s.`menu_id` IN (22540)
  AND s.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` x
    WHERE x.`role_id` = s.`role_id` AND x.`menu_id` = 22920 AND x.`tenant_id` = s.`tenant_id` AND x.`deleted` = b'0'
  );
