-- =====================================================
-- 合同部分完善
-- 1) rental_contract 增加「合同模板关联」与「合同正文快照」两列
-- 2) 管理端新增「合同模板」菜单（挂在 20000 租赁管理 下）
-- 执行：mysql -uroot -p ruoyi-vue-pro < rental-contract.sql
-- 可重复执行（列用 information_schema 判断后动态 ALTER，菜单先 DELETE 再 INSERT）
-- =====================================================

-- 中文 Windows 控制台的 mysql 客户端默认 character_set_client=gbk，
-- 会把 UTF-8 的 SQL 文件读成乱码（表现为 ERROR 1366 Incorrect string value）。
-- 强制按 utf8mb4 解析，否则菜单名和列注释会写坏
SET NAMES utf8mb4;

-- 1) 合同表加列
--    注意：MySQL 不支持 ALTER TABLE ... ADD COLUMN IF NOT EXISTS（那是 MariaDB 的语法），
--    所以用 information_schema 判断 + 动态 SQL 来做到可重复执行
SET @has_template_id = (SELECT COUNT(*) FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'rental_contract'
                          AND COLUMN_NAME = 'template_id');
SET @sql = IF(@has_template_id = 0,
    'ALTER TABLE `rental_contract` ADD COLUMN `template_id` BIGINT NULL COMMENT ''合同模板 ID：关联 rental_contract_template'' AFTER `source_apply_id`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_content = (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'rental_contract'
                      AND COLUMN_NAME = 'content');
SET @sql = IF(@has_content = 0,
    'ALTER TABLE `rental_contract` ADD COLUMN `content` LONGTEXT NULL COMMENT ''合同正文快照（HTML，由模板渲染后固化；模板改版不影响已签合同）'' AFTER `template_id`',
    'DO 0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 二级菜单：合同模板
--    sort 15：公告管理 12 / 业主管理 13 / 看房预约 14 之后
--    component 必须与 src/views/ 下的相对路径对得上，否则前端 routerHelper 找不到组件会白屏
DELETE FROM `system_role_menu` WHERE `menu_id` IN (20160, 20161, 20162, 20163);
DELETE FROM `system_menu`      WHERE `id`      IN (20160, 20161, 20162, 20163);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20160, '合同模板', 'rental:contract-template:query', 2, 15, 20000, 'contract-template', 'ep:document', 'rental/contractTemplate/index', 'RentalContractTemplate', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 菜单按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20161, '模板新增', 'rental:contract-template:create', 3, 1, 20160, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20162, '模板修改', 'rental:contract-template:update', 3, 2, 20160, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20163, '模板删除', 'rental:contract-template:delete', 3, 3, 20160, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');
