-- =====================================================
-- 缴费记录：收款人允许为空
-- 租客在租客端自助缴纳租金账单时，没有「收款人（sys_user）」可填，
-- 而原列是 NOT NULL 且无默认值，导致缴费直接 500（Field 'payee' doesn't have a default value）。
-- 该列语义本就只适用于管理端代收，故放开为可空，管理端代收时仍会写入真实收款人。
-- 执行：mysql -uroot -p ruoyi-vue-pro < rental-payment-record-payee.sql
-- 可重复执行（MODIFY 幂等）
-- =====================================================

-- 中文 Windows 控制台的 mysql 客户端默认 character_set_client=gbk，强制按 utf8mb4 解析
SET NAMES utf8mb4;

ALTER TABLE `rental_payment_record`
    MODIFY COLUMN `payee` BIGINT DEFAULT NULL COMMENT '收款人 ID，关联 sys_user；租客端自助缴费时为空';
