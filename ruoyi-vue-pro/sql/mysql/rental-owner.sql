-- =====================================================
-- 社区房屋租赁管理系统 - 业主端扩展脚本
-- 用途：新增业主信息表 + 房源水电计价/审核 + 抄表峰谷/截图/审核 + 维修三方流程字段
-- 执行顺序：先 rental.sql、rental-extra.sql，再本脚本
-- =====================================================

-- -----------------------------------------------------
-- 1. 业主信息表（类比 rental_tenant_info，关联 member_user 可空）
-- -----------------------------------------------------
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

CREATE TABLE `rental_owner_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联会员用户ID（member_user.id），未接入会员模块时为 null',
    `name` VARCHAR(50) DEFAULT NULL COMMENT '业主姓名',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '业主信息表';

-- -----------------------------------------------------
-- 2. 房源表：业主归属 + 水电计价 + 审核
-- -----------------------------------------------------
ALTER TABLE `rental_house`
    ADD COLUMN `owner_id` BIGINT DEFAULT NULL COMMENT '业主 ID，关联 rental_owner_info' AFTER `id`,
    ADD COLUMN `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已驳回' AFTER `status`,
    ADD COLUMN `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核驳回原因' AFTER `review_status`,
    ADD COLUMN `water_bill_type` TINYINT NOT NULL DEFAULT 0 COMMENT '水费计价方式：0-统一单价 1-三档梯度' AFTER `review_reason`,
    ADD COLUMN `water_unit_price` DECIMAL(8,2) DEFAULT NULL COMMENT '水费统一单价（元/吨，统一价时用）' AFTER `water_bill_type`,
    ADD COLUMN `water_tier1_limit` DECIMAL(10,2) DEFAULT NULL COMMENT '水费第一档上限（吨），0~limit1 按档一价' AFTER `water_unit_price`,
    ADD COLUMN `water_tier1_price` DECIMAL(8,2) DEFAULT NULL COMMENT '水费第一档单价（元/吨）' AFTER `water_tier1_limit`,
    ADD COLUMN `water_tier2_limit` DECIMAL(10,2) DEFAULT NULL COMMENT '水费第二档上限（吨），limit1~limit2 按档二价' AFTER `water_tier1_price`,
    ADD COLUMN `water_tier2_price` DECIMAL(8,2) DEFAULT NULL COMMENT '水费第二档单价（元/吨）' AFTER `water_tier2_limit`,
    ADD COLUMN `water_tier3_price` DECIMAL(8,2) DEFAULT NULL COMMENT '水费第三档单价（元/吨），超过 limit2 部分' AFTER `water_tier2_price`,
    ADD COLUMN `electricity_peak_price` DECIMAL(8,2) DEFAULT NULL COMMENT '电费峰段单价（元/度，峰 8:00-22:00）' AFTER `water_tier3_price`,
    ADD COLUMN `electricity_valley_price` DECIMAL(8,2) DEFAULT NULL COMMENT '电费谷段单价（元/度，谷 22:00-8:00）' AFTER `electricity_peak_price`,
    ADD KEY `idx_owner_id` (`owner_id`),
    ADD KEY `idx_review_status` (`review_status`);

-- 房源状态对齐代码语义：0-下架 1-上架 2-已锁定(签约中) 3-已出租
-- 注意：旧数据中 status=2 曾表示「已出租」，如有存量数据请先 UPDATE rental_house SET status=3 WHERE status=2 AND <已出租>;
ALTER TABLE `rental_house`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '房源状态：0-下架 1-上架 2-已锁定(签约中) 3-已出租';

-- -----------------------------------------------------
-- 3. 抄表记录表：峰谷双读数 + 截图 + 审核
-- -----------------------------------------------------
ALTER TABLE `rental_meter_reading`
    ADD COLUMN `owner_id` BIGINT DEFAULT NULL COMMENT '上传业主 ID，关联 rental_owner_info' AFTER `id`,
    ADD COLUMN `valley_reading` DECIMAL(10,2) DEFAULT NULL COMMENT '电表谷段本期读数' AFTER `current_reading`,
    ADD COLUMN `last_valley_reading` DECIMAL(10,2) DEFAULT NULL COMMENT '电表谷段上期读数' AFTER `valley_reading`,
    ADD COLUMN `valley_usage` DECIMAL(10,2) DEFAULT NULL COMMENT '电表谷段用量' AFTER `usage_amount`,
    ADD COLUMN `peak_price` DECIMAL(8,2) DEFAULT NULL COMMENT '电费峰段单价' AFTER `unit_price`,
    ADD COLUMN `valley_price` DECIMAL(8,2) DEFAULT NULL COMMENT '电费谷段单价' AFTER `peak_price`,
    ADD COLUMN `images` VARCHAR(1000) DEFAULT NULL COMMENT '抄表截图，JSON 数组（水 1 张 / 电峰谷各 1 张）' AFTER `remark`,
    ADD COLUMN `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已驳回' AFTER `images`,
    ADD COLUMN `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核驳回原因' AFTER `review_status`,
    ADD COLUMN `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人 ID，关联 sys_user' AFTER `review_reason`,
    ADD COLUMN `review_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `reviewer_id`,
    ADD KEY `idx_owner_id` (`owner_id`),
    ADD KEY `idx_review_status` (`review_status`);

-- 录入人可空（业主上传时无 sys_user）
ALTER TABLE `rental_meter_reading`
    MODIFY COLUMN `operator_id` BIGINT DEFAULT NULL COMMENT '录入人 ID，关联 sys_user（业主上传时为 null）';

-- -----------------------------------------------------
-- 4. 维修工单表：业主处理 + 证据 + 三方状态机
-- -----------------------------------------------------
ALTER TABLE `rental_repair_order`
    ADD COLUMN `owner_id` BIGINT DEFAULT NULL COMMENT '处理业主 ID，关联 rental_owner_info' AFTER `repairer_id`,
    ADD COLUMN `handle_evidence` VARCHAR(1000) DEFAULT NULL COMMENT '处理证据图片，JSON 数组' AFTER `repair_description`,
    ADD COLUMN `handle_time` DATETIME DEFAULT NULL COMMENT '业主处理完成（上传证据）时间' AFTER `handle_evidence`,
    ADD COLUMN `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '验收不合格原因' AFTER `handle_time`,
    ADD KEY `idx_owner_id` (`owner_id`);

-- 维修工单状态机调整为三方流程：
-- 0-待处理（租客报修后等业主处理）1-处理中（业主接单处理）2-待验收（业主已传证据，等管理员判定/租客确认）3-已处理（合格或租客确认）4-已驳回（退回重做）
ALTER TABLE `rental_repair_order`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '工单状态：0-待处理 1-处理中 2-待验收 3-已处理 4-已驳回(退回重做)';

-- =====================================================
-- 脚本完成
-- =====================================================
