-- 租客账号独立登录：给租客表加密码字段
-- 注意：课程演示用明文存储，生产环境应使用 BCrypt 加密
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

ALTER TABLE `rental_tenant_info`
    ADD COLUMN `password` VARCHAR(100) DEFAULT NULL COMMENT '登录密码（演示明文，生产应加密）' AFTER `phone`;
