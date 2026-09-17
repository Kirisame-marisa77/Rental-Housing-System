-- =====================================================
-- 社区房屋租赁管理系统 - 补充脚本
-- 用途：补齐 rental.sql 缺的 2 张表 + 按数据库设计文档 v2.1 校对字段
-- 执行顺序：先 rental.sql，再本脚本
-- =====================================================

-- -----------------------------------------------------
-- 1. 租客信息表（实名认证扩展，关联 member_user）
-- -----------------------------------------------------
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

CREATE TABLE `rental_tenant_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NULL COMMENT '关联会员用户ID（member_user.id）',
    `name` VARCHAR(50) DEFAULT NULL COMMENT '租客姓名（实名认证后填充）',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号（实名认证后填充，加密存储）',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话',
    `work_unit` VARCHAR(100) DEFAULT NULL COMMENT '工作单位',
    `auth_status` TINYINT NOT NULL DEFAULT 0 COMMENT '实名认证状态：0-未认证 1-认证中 2-已认证 3-认证失败 4-已锁定',
    `auth_fail_count` INT NOT NULL DEFAULT 0 COMMENT '连续认证失败次数',
    `auth_time` DATETIME DEFAULT NULL COMMENT '最近认证时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_phone` (`phone`),
    KEY `idx_auth_status` (`auth_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租客信息表';

-- -----------------------------------------------------
-- 2. 合同模板表
-- -----------------------------------------------------
CREATE TABLE `rental_contract_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` TINYINT NOT NULL DEFAULT 0 COMMENT '模板类型：0-标准租赁合同 1-短期租赁合同 2-商业租赁合同',
    `content` LONGTEXT NOT NULL COMMENT '合同模板内容（HTML 格式，支持变量占位符）',
    `variables` TEXT DEFAULT NULL COMMENT '模板变量定义，JSON 格式',
    `version` INT NOT NULL DEFAULT 1 COMMENT '模板版本号',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-启用 1-禁用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '合同模板表';

-- -----------------------------------------------------
-- 3. 字段校对（按数据库设计文档 v2.1）
-- -----------------------------------------------------

-- 3.1 租房申请表补申请编号
ALTER TABLE `rental_apply`
    ADD COLUMN `apply_no` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '申请编号，规则：SQ+时间戳+随机数' AFTER `id`,
    ADD UNIQUE KEY `uk_apply_no` (`apply_no`);

-- 3.2 退租申请表补申请编号
ALTER TABLE `rental_move_out_application`
    ADD COLUMN `apply_no` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '申请编号，规则：TZ+时间戳+随机数' AFTER `id`,
    ADD UNIQUE KEY `uk_apply_no` (`apply_no`);

-- 3.3 租金账单表补滞纳金字段
ALTER TABLE `rental_rent_bill`
    ADD COLUMN `late_fee` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '滞纳金（元）' AFTER `fee_detail`,
    ADD COLUMN `late_fee_waived` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '减免滞纳金（元）' AFTER `late_fee`,
    ADD COLUMN `late_fee_waive_reason` VARCHAR(500) DEFAULT NULL COMMENT '滞纳金减免原因' AFTER `late_fee_waived`;

-- 3.4 退租申请表验收结果改为 TINYINT（0-验收通过，1-轻微损坏，2-严重损坏）
ALTER TABLE `rental_move_out_application`
    MODIFY COLUMN `inspection_result` TINYINT DEFAULT NULL COMMENT '房屋验收结果：0-验收通过，1-轻微损坏，2-严重损坏';
