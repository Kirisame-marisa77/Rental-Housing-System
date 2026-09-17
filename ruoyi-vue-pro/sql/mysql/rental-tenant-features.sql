-- =====================================================
-- 租客端三功能：房源收藏 / 预约看房 / 公告阅读
-- 1) 管理端新增只读「看房预约」菜单
-- 2) 修正三张表里误导性的「关联 member_user」列注释
--    （member 模块未启用，实际存 rental_tenant_info.id）
-- 执行：mysql -uroot -p ruoyi-vue-pro < rental-tenant-features.sql
-- 可重复执行（菜单先 DELETE 再 INSERT，列注释用 MODIFY）
-- =====================================================

-- 中文 Windows 控制台的 mysql 客户端默认 character_set_client=gbk，
-- 会把 UTF-8 的 SQL 文件读成乱码（表现为 ERROR 1366 Incorrect string value）。
-- 强制按 utf8mb4 解析，否则菜单名和列注释会写坏
SET NAMES utf8mb4;

-- 1) 二级菜单：看房预约（挂在 20000 租赁管理 下；公告管理 20120/sort12、业主管理 20140/sort13）
DELETE FROM `system_role_menu` WHERE `menu_id` IN (20150, 20151);
DELETE FROM `system_menu`      WHERE `id`      IN (20150, 20151);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20150, '看房预约', 'rental:viewing-appointment:query', 2, 14, 20000, 'viewing-appointment', 'ep:calendar', 'rental/viewing/index', 'RentalViewing', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 菜单按钮权限（管理端只读，仅一个查询按钮）
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20151, '预约查询', 'rental:viewing-appointment:query', 3, 1, 20150, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 2) 修正列注释（MODIFY COLUMN 会整体替换定义，必须写全 BIGINT NOT NULL；不影响既有索引）
ALTER TABLE `rental_house_favorite`
    MODIFY COLUMN `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID：关联 rental_tenant_info.id（member 模块未启用）';

ALTER TABLE `rental_viewing_appointment`
    MODIFY COLUMN `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID：关联 rental_tenant_info.id（member 模块未启用）';

ALTER TABLE `rental_announcement_read`
    MODIFY COLUMN `user_id` BIGINT NOT NULL COMMENT '用户 ID：关联 rental_tenant_info.id（member 模块未启用，本期仅租客端写入）';
