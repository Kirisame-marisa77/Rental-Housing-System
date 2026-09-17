-- 合同签约：加业主签署时间字段（租客签署用 sign_time，业主签署用 owner_sign_time）
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

ALTER TABLE `rental_contract`
    ADD COLUMN `owner_sign_time` DATETIME DEFAULT NULL COMMENT '业主签署时间' AFTER `sign_time`;
