-- =====================================================
-- 租房申请流程调整：租客发起 → 房东审批 → 签约缴费 → 先到先得
-- 1) 移除管理端「申请审批」菜单及其按钮权限
-- 2) 审批人语义调整：approver_id 存 rental_owner_info.id
-- 执行：mysql -uroot -p ruoyi-vue-pro < rental-apply-owner.sql
-- 可重复执行（DELETE 与 MODIFY 都是幂等的）
-- =====================================================

-- 中文 Windows 控制台的 mysql 客户端默认 character_set_client=gbk，
-- 会把 UTF-8 的 SQL 文件读成乱码。强制按 utf8mb4 解析，否则注释会写坏
SET NAMES utf8mb4;

-- 先删角色-菜单关联，再删菜单（先子后父）
DELETE FROM `system_role_menu` WHERE `menu_id` IN (20041, 20042, 20043, 20040);
DELETE FROM `system_menu`      WHERE `id`      IN (20041, 20042, 20043);
DELETE FROM `system_menu`      WHERE `id`      =  20040;

-- 审批人由管理员改为房东：仅更新注释，不改类型
ALTER TABLE `rental_apply`
    MODIFY COLUMN `approver_id` BIGINT DEFAULT NULL COMMENT '审批人 ID：房东审批时为 rental_owner_info.id';
