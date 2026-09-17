-- 电费支持统一单价（与峰谷两价二选一）
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

ALTER TABLE `rental_house`
    ADD COLUMN `electricity_bill_type` TINYINT NOT NULL DEFAULT 1 COMMENT '电费计价方式：0-统一单价 1-峰谷两价' AFTER `water_tier3_price`,
    ADD COLUMN `electricity_unit_price` DECIMAL(8,2) DEFAULT NULL COMMENT '电费统一单价（元/度，统一价时用）' AFTER `electricity_bill_type`;
