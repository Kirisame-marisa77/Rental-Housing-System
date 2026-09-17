-- 业主注册：加银行卡号字段
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

ALTER TABLE `rental_owner_info`
    ADD COLUMN `bank_card` VARCHAR(30) DEFAULT NULL COMMENT '银行卡号' AFTER `id_card`;
