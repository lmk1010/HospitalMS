SET NAMES utf8mb4;

UPDATE `system_menu` SET `name` = '工作台' WHERE `id` = 20900;
UPDATE `system_menu` SET `name` = '工作总览' WHERE `id` = 20910;
UPDATE `system_menu` SET `name` = '用户管理' WHERE `id` = 1;

UPDATE `system_menu`
SET `visible` = b'0'
WHERE `id` IN (1224, 105, 2739, 108, 1261, 2447, 2083);
