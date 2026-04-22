CREATE TABLE IF NOT EXISTS `hospital_custom_form` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '表单编号',
  `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '表单名称',
  `code` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '表单编码',
  `dept_id` bigint DEFAULT NULL COMMENT '所属科室编号',
  `biz_category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务分类',
  `process_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程定义标识',
  `process_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程名称',
  `node_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节点标识',
  `node_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节点名称',
  `page_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '页面编码',
  `page_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '页面路由',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `conf` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '表单配置',
  `fields` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '表单字段数组',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_code_tenant_deleted` (`code`, `tenant_id`, `deleted`),
  KEY `idx_page_code` (`page_code`),
  KEY `idx_process_node` (`process_key`, `node_key`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院自定义表单表';

UPDATE `system_menu`
SET `name` = '流程管理', `path` = 'manage-list', `component` = 'hospital/flow/process-manage/index', `component_name` = 'HospitalWorkflowManage', `update_time` = NOW(), `updater` = '1'
WHERE `id` = 22510;

INSERT INTO `system_menu` (
  `id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
  `status`, `visible`, `keep_alive`, `always_show`, `create_time`, `update_time`, `creator`, `updater`, `deleted`
) VALUES
  (22550, '表单管理', 'hospital:custom-form:query', 2, 11, 22900, 'form-manage', 'ant-design:form-outlined', 'hospital/flow/form-manage/index', 'HospitalCustomForm', 0, 1, 1, 0, NOW(), NOW(), '1', '1', b'0'),
  (22551, '表单查询', 'hospital:custom-form:query', 3, 1, 22550, NULL, NULL, NULL, NULL, 0, 1, 0, 0, NOW(), NOW(), '1', '1', b'0'),
  (22552, '表单新增', 'hospital:custom-form:create', 3, 2, 22550, NULL, NULL, NULL, NULL, 0, 1, 0, 0, NOW(), NOW(), '1', '1', b'0'),
  (22553, '表单修改', 'hospital:custom-form:update', 3, 3, 22550, NULL, NULL, NULL, NULL, 0, 1, 0, 0, NOW(), NOW(), '1', '1', b'0'),
  (22554, '表单删除', 'hospital:custom-form:delete', 3, 4, 22550, NULL, NULL, NULL, NULL, 0, 1, 0, 0, NOW(), NOW(), '1', '1', b'0')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `permission` = VALUES(`permission`),
  `type` = VALUES(`type`),
  `sort` = VALUES(`sort`),
  `parent_id` = VALUES(`parent_id`),
  `path` = VALUES(`path`),
  `icon` = VALUES(`icon`),
  `component` = VALUES(`component`),
  `component_name` = VALUES(`component_name`),
  `status` = VALUES(`status`),
  `visible` = VALUES(`visible`),
  `keep_alive` = VALUES(`keep_alive`),
  `always_show` = VALUES(`always_show`),
  `update_time` = NOW(),
  `updater` = '1',
  `deleted` = b'0';

INSERT INTO `system_role_menu` (
  `role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`
)
SELECT base.role_id, target.menu_id, '1', NOW(), '1', NOW(), b'0', base.tenant_id
FROM (
  SELECT DISTINCT `role_id`, `tenant_id`
  FROM `system_role_menu`
  WHERE `menu_id` = 22510 AND `deleted` = b'0'
) base
JOIN (
  SELECT 22550 AS menu_id UNION ALL
  SELECT 22551 UNION ALL
  SELECT 22552 UNION ALL
  SELECT 22553 UNION ALL
  SELECT 22554
) target
LEFT JOIN `system_role_menu` existing
  ON existing.role_id = base.role_id
  AND existing.menu_id = target.menu_id
  AND existing.tenant_id = base.tenant_id
  AND existing.deleted = b'0'
WHERE existing.id IS NULL;
