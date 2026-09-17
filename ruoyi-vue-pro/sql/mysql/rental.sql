-- =====================================================
-- 社区房屋租赁管理系统 - 数据库脚本
-- =====================================================
-- 数据库版本：MySQL 8.0+
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- 创建时间：2026-06-24
-- =====================================================

-- 1. 房源模块

-- 1.1 房源信息表
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

CREATE TABLE `rental_house` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `house_no` VARCHAR(20) NOT NULL COMMENT '房源编号',
    `community_name` VARCHAR(100) NOT NULL COMMENT '小区名称',
    `area` VARCHAR(50) NOT NULL COMMENT '所在区域',
    `building_no` VARCHAR(20) NOT NULL COMMENT '楼栋号',
    `room_no` VARCHAR(20) NOT NULL COMMENT '房号',
    `layout` VARCHAR(20) NOT NULL COMMENT '户型',
    `square_area` DECIMAL(8, 2) NOT NULL COMMENT '建筑面积（㎡）',
    `orientation` VARCHAR(20) DEFAULT NULL COMMENT '朝向',
    `floor` INT DEFAULT NULL COMMENT '所在楼层',
    `total_floor` INT DEFAULT NULL COMMENT '楼栋总层数',
    `decoration` VARCHAR(20) DEFAULT NULL COMMENT '装修情况：精装/简装/毛坯',
    `monthly_rent` DECIMAL(10, 2) NOT NULL COMMENT '月租金（元）',
    `deposit` DECIMAL(10, 2) DEFAULT NULL COMMENT '押金金额（元）',
    `facilities` TEXT DEFAULT NULL COMMENT '配套设施，JSON格式',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '房源状态：0-下架，1-上架，2-已出租',
    `online_time` DATETIME DEFAULT NULL COMMENT '最近一次上架时间',
    `description` TEXT DEFAULT NULL COMMENT '房源描述/周边环境介绍',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_house_no` (`house_no`),
    KEY `idx_community_area` (`community_name`, `area`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源信息表';

-- 1.2 房源图片表
CREATE TABLE `rental_house_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片 URL',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序序号，数值越小越靠前',
    `image_type` TINYINT NOT NULL DEFAULT 0 COMMENT '图片类型：0-实景图，1-户型图',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源图片表';

-- =====================================================
-- 2. 合同与租约模块
-- =====================================================

-- 2.1 租房合同表
CREATE TABLE `rental_contract` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `contract_no` VARCHAR(30) NOT NULL COMMENT '合同编号，规则：HT-年月-流水号',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `rent_start_date` DATE NOT NULL COMMENT '租期开始日期',
    `rent_end_date` DATE NOT NULL COMMENT '租期结束日期',
    `monthly_rent` DECIMAL(10, 2) NOT NULL COMMENT '签约月租金（元）',
    `deposit_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '押金金额（元）',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '付款方式：押一付一/押一付三/押二付一/押二付三/自定义',
    `property_fee_unit` DECIMAL(8, 2) DEFAULT NULL COMMENT '物业费单价（元/㎡/月）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '合同状态：0-待签署，1-待缴费，2-生效中，3-即将到期，4-退租处理中，5-已到期，6-已退租，7-已取消',
    `sign_time` DATETIME DEFAULT NULL COMMENT '租客签署时间',
    `source_apply_id` BIGINT DEFAULT NULL COMMENT '来源申请 ID，关联 rental_apply',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_contract_no` (`contract_no`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_tenant_user_id` (`tenant_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_rent_end_date` (`rent_end_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租房合同表';

-- =====================================================
-- 3. 账单与缴费模块
-- =====================================================

-- 3.1 租金/物业费账单表
CREATE TABLE `rental_rent_bill` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `bill_no` VARCHAR(30) NOT NULL COMMENT '账单编号，规则：ZD-年月-流水号',
    `contract_id` BIGINT NOT NULL COMMENT '合同 ID，关联 rental_contract',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `bill_type` TINYINT NOT NULL DEFAULT 1 COMMENT '账单类型：0-首期账单，1-周期账单',
    `period_start` DATE NOT NULL COMMENT '费用周期开始日期',
    `period_end` DATE NOT NULL COMMENT '费用周期结束日期',
    `rent_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '本期租金（元）',
    `property_fee_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '本期物业费（元）',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '应缴总金额（元）',
    `paid_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '已缴金额（元）',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '缴费状态：0-待缴费，1-已缴费，2-已逾期',
    `due_date` DATE NOT NULL COMMENT '缴费截止日期',
    `pay_time` DATETIME DEFAULT NULL COMMENT '最近一次缴费时间',
    `pay_method` VARCHAR(20) DEFAULT NULL COMMENT '缴费方式：现金/微信/支付宝/银行转账',
    `payee` BIGINT DEFAULT NULL COMMENT '收款人 ID，关联 sys_user',
    `fee_detail` TEXT DEFAULT NULL COMMENT '费用明细，JSON格式',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bill_no` (`bill_no`),
    KEY `idx_contract_id` (`contract_id`),
    KEY `idx_tenant_user_id` (`tenant_user_id`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_due_date` (`due_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租金/物业费账单表';

-- 3.2 水电费账单表
CREATE TABLE `rental_utility_bill` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `bill_no` VARCHAR(30) NOT NULL COMMENT '账单编号，规则：SD-年月-流水号',
    `contract_id` BIGINT NOT NULL COMMENT '合同 ID，关联 rental_contract',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `meter_reading_id` BIGINT NOT NULL COMMENT '抄表记录 ID，关联 rental_meter_reading',
    `fee_type` TINYINT NOT NULL DEFAULT 0 COMMENT '费用类型：0-水费，1-电费，2-水+电',
    `water_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '水费金额（元）',
    `electricity_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '电费金额（元）',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '应缴总金额（元）',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '缴费状态：0-待缴费，1-已缴费，2-已逾期',
    `due_date` DATE NOT NULL COMMENT '缴费截止日期',
    `pay_time` DATETIME DEFAULT NULL COMMENT '缴费时间',
    `pay_method` VARCHAR(20) DEFAULT NULL COMMENT '缴费方式',
    `payee` BIGINT DEFAULT NULL COMMENT '收款人 ID，关联 sys_user',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bill_no` (`bill_no`),
    KEY `idx_contract_id` (`contract_id`),
    KEY `idx_meter_reading_id` (`meter_reading_id`),
    KEY `idx_pay_status` (`pay_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '水电费账单表';

-- 3.3 缴费记录表
CREATE TABLE `rental_payment_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `bill_type` TINYINT NOT NULL COMMENT '账单类型：0-租金/物业费，1-水电费，2-退租结算',
    `bill_id` BIGINT NOT NULL COMMENT '关联账单 ID（根据 bill_type 关联不同账单表）',
    `pay_amount` DECIMAL(10, 2) NOT NULL COMMENT '缴费金额（元）',
    `pay_method` VARCHAR(20) NOT NULL COMMENT '缴费方式：现金/微信/支付宝/银行转账',
    `pay_time` DATETIME NOT NULL COMMENT '缴费日期',
    `payee` BIGINT NOT NULL COMMENT '收款人 ID，关联 sys_user',
    `transaction_no` VARCHAR(100) DEFAULT NULL COMMENT '交易流水号（在线支付）',
    `voucher_no` VARCHAR(30) DEFAULT NULL COMMENT '缴费凭证编号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_bill_type_bill_id` (`bill_type`, `bill_id`),
    KEY `idx_pay_time` (`pay_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '缴费记录表';

-- =====================================================
-- 4. 抄表模块
-- =====================================================

-- 4.1 抄表记录表
CREATE TABLE `rental_meter_reading` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `contract_id` BIGINT NOT NULL COMMENT '合同 ID，关联 rental_contract',
    `meter_type` TINYINT NOT NULL DEFAULT 0 COMMENT '抄表类型：0-水表，1-电表',
    `last_reading` DECIMAL(10, 2) NOT NULL COMMENT '上期读数',
    `current_reading` DECIMAL(10, 2) NOT NULL COMMENT '本期读数',
    `usage_amount` DECIMAL(10, 2) NOT NULL COMMENT '用量（本期读数 - 上期读数）',
    `unit_price` DECIMAL(8, 2) NOT NULL COMMENT '单价（元/吨 或 元/度）',
    `fee_amount` DECIMAL(10, 2) NOT NULL COMMENT '费用金额（用量 × 单价）',
    `reading_date` DATE NOT NULL COMMENT '抄表日期',
    `operator_id` BIGINT NOT NULL COMMENT '录入人 ID，关联 sys_user',
    `bill_id` BIGINT DEFAULT NULL COMMENT '关联生成的水电费账单 ID，关联 rental_utility_bill',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-异常，2-已作废',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注（异常说明等）',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_contract_id` (`contract_id`),
    KEY `idx_reading_date` (`reading_date`),
    KEY `idx_meter_type_house` (`meter_type`, `house_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '抄表记录表';

-- 4.2 抄表配置表
CREATE TABLE `rental_meter_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置项：meter_period / water_price / electricity_price',
    `config_value` VARCHAR(200) NOT NULL COMMENT '配置值',
    `effective_time` DATETIME NOT NULL COMMENT '生效时间',
    `config_user` BIGINT NOT NULL COMMENT '配置人 ID，关联 sys_user',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '抄表配置表';

-- =====================================================
-- 5. 申请审批模块
-- =====================================================

-- 5.1 租房申请记录表
CREATE TABLE `rental_apply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `move_in_date` DATE NOT NULL COMMENT '期望入住日期',
    `lease_term` INT NOT NULL COMMENT '租期（月）',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '付款方式：押一付一/押一付三/押二付一/押二付三/自定义',
    `monthly_rent` DECIMAL(10, 2) NOT NULL COMMENT '签约月租金（元）',
    `deposit_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '押金金额（元）',
    `tenant_remark` VARCHAR(500) DEFAULT NULL COMMENT '租客备注/留言',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已驳回，3-已签约，4-已超时',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人 ID，关联 sys_user',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    `contract_id` BIGINT DEFAULT NULL COMMENT '通过后自动生成的合同 ID，关联 rental_contract',
    `timeout_reason` VARCHAR(200) DEFAULT NULL COMMENT '超时取消原因（未签署/未缴费）',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_tenant_user_id` (`tenant_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_contract_id` (`contract_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租房申请记录表';

-- =====================================================
-- 6. 退租结算模块
-- =====================================================

-- 6.1 退租申请表
CREATE TABLE `rental_move_out_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `contract_id` BIGINT NOT NULL COMMENT '合同 ID，关联 rental_contract',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `move_out_type` TINYINT NOT NULL DEFAULT 0 COMMENT '退租类型：0-到期退租，1-提前退租',
    `move_out_reason` VARCHAR(500) DEFAULT NULL COMMENT '退租原因说明',
    `expected_move_out_date` DATE NOT NULL COMMENT '预计退租日期',
    `settlement_id` BIGINT DEFAULT NULL COMMENT '关联退租结算单 ID，关联 rental_settlement_bill',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已处理',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人 ID，关联 sys_user',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `inspection_result` VARCHAR(50) DEFAULT NULL COMMENT '房屋验收情况：通过/轻微损坏/严重损坏',
    `repair_fee` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '损坏维修费用（管理员手动录入）',
    `repair_fee_desc` VARCHAR(500) DEFAULT NULL COMMENT '维修费用明细说明',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
    `deposit_handle` VARCHAR(50) DEFAULT NULL COMMENT '押金处理方式：不退（违约金）/扣除欠费后退还',
    `remaining_rent` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '剩余租金（提前退租时已预缴未使用的租金）',
    `deduction_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '欠费抵扣金额（物业费+水电费+维修费）',
    `refund_or_pay` DECIMAL(10, 2) DEFAULT NULL COMMENT '退还/补缴金额，正数=退还，负数=补缴',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请提交时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_contract_id` (`contract_id`),
    KEY `idx_tenant_user_id` (`tenant_user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退租申请表';

-- 6.2 退租结算单表
CREATE TABLE `rental_settlement_bill` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `settlement_no` VARCHAR(30) NOT NULL COMMENT '结算单号，规则：JS-年月-流水号',
    `contract_id` BIGINT NOT NULL COMMENT '合同 ID，关联 rental_contract',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `move_out_application_id` BIGINT NOT NULL COMMENT '退租申请 ID，关联 rental_move_out_application',
    `move_out_type` TINYINT NOT NULL DEFAULT 0 COMMENT '退租类型：0-到期退租，1-提前退租',
    `deposit_amount` DECIMAL(10, 2) NOT NULL COMMENT '押金金额（元）',
    `deposit_handle` VARCHAR(50) NOT NULL COMMENT '押金处理方式：不退（违约金）/扣除欠费后退还',
    `remaining_rent` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '剩余租金（元）',
    `property_fee_arrears` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '物业费欠费（元）',
    `utility_fee_arrears` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '水电费欠费（元）',
    `repair_fee` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '维修费金额（元）',
    `deduction_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '欠费抵扣金额（元）',
    `refund_or_pay` DECIMAL(10, 2) NOT NULL COMMENT '退还/补缴金额，正数=退还，负数=补缴',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人 ID，关联 sys_user',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settlement_no` (`settlement_no`),
    KEY `idx_contract_id` (`contract_id`),
    KEY `idx_move_out_application_id` (`move_out_application_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退租结算单表';

-- =====================================================
-- 7. 维修工单模块
-- =====================================================

-- 7.1 维修工单表
CREATE TABLE `rental_repair_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_no` VARCHAR(30) NOT NULL COMMENT '工单编号，规则：WX-年月-流水号',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `repair_type` VARCHAR(50) NOT NULL COMMENT '报修类型：水电维修/家电维修/管道疏通/门窗维修/墙面地面/其他',
    `description` TEXT NOT NULL COMMENT '问题描述，最多 500 字',
    `images` TEXT DEFAULT NULL COMMENT '问题图片，JSON 数组，最多 5 张',
    `expected_time` DATETIME DEFAULT NULL COMMENT '期望上门时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '工单状态：0-待分配，1-待处理，2-处理中，3-待确认，4-已归档',
    `priority` TINYINT NOT NULL DEFAULT 0 COMMENT '优先级：0-普通，1-紧急，2-特急',
    `repairer_id` BIGINT DEFAULT NULL COMMENT '维修人员 ID，关联 sys_user',
    `assign_time` DATETIME DEFAULT NULL COMMENT '分配时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '维修完成时间',
    `repair_description` TEXT DEFAULT NULL COMMENT '维修说明（处理过程和结果）',
    `rating` TINYINT DEFAULT NULL COMMENT '评价星级：1-5',
    `evaluation_content` TEXT DEFAULT NULL COMMENT '评价内容',
    `evaluation_time` DATETIME DEFAULT NULL COMMENT '评价时间',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报修提交时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_tenant_user_id` (`tenant_user_id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_status` (`status`),
    KEY `idx_repairer_id` (`repairer_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '维修工单表';

-- 7.2 维修进度表
CREATE TABLE `rental_repair_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_id` BIGINT NOT NULL COMMENT '工单 ID，关联 rental_repair_order',
    `operator_id` BIGINT NOT NULL COMMENT '操作人 ID，关联 sys_user 或 member_user',
    `operator_type` TINYINT NOT NULL DEFAULT 0 COMMENT '操作人类型：0-租客，1-管理员，2-维修人员',
    `action_type` VARCHAR(50) NOT NULL COMMENT '操作类型：提交报修/分配工单/接单/开始维修/维修完成/确认完成/驳回返工/评价',
    `description` TEXT DEFAULT NULL COMMENT '操作描述',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '维修进度表';

-- =====================================================
-- 8. 公告通知模块
-- =====================================================

-- 8.1 公告信息表
CREATE TABLE `rental_announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `notice_no` VARCHAR(30) NOT NULL COMMENT '公告编号，规则：GG-年月-流水号',
    `title` VARCHAR(100) NOT NULL COMMENT '公告标题，最多 50 字',
    `content` TEXT NOT NULL COMMENT '富文本内容（支持文字、图片、链接）',
    `category` VARCHAR(20) NOT NULL COMMENT '公告分类：缴费通知/维修通知/社区公告/紧急通知',
    `publisher_id` BIGINT NOT NULL COMMENT '发布人 ID，关联 sys_user',
    `publish_time` DATETIME NOT NULL COMMENT '发布时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已删除',
    `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    `target_scope` TEXT DEFAULT NULL COMMENT '定向推送范围，JSON 格式：房源 ID 数组/租客 ID 数组/全量',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notice_no` (`notice_no`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_is_top` (`is_top`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告信息表';

-- 8.2 公告阅读记录表
CREATE TABLE `rental_announcement_read` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `announcement_id` BIGINT NOT NULL COMMENT '公告 ID，关联 rental_announcement',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID，关联 member_user',
    `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次阅读时间',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告阅读记录表';

-- =====================================================
-- 9. 预约看房模块
-- =====================================================

-- 9.1 预约看房记录表
CREATE TABLE `rental_viewing_appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `appointment_date` DATE NOT NULL COMMENT '预约日期',
    `start_time` VARCHAR(10) NOT NULL COMMENT '开始时间，如"09:00"',
    `end_time` VARCHAR(10) NOT NULL COMMENT '结束时间，如"10:00"',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-已完成，3-已取消',
    `feedback` VARCHAR(500) DEFAULT NULL COMMENT '看房反馈',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_tenant_user_id` (`tenant_user_id`),
    KEY `idx_appointment_date` (`appointment_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '预约看房记录表';

-- =====================================================
-- 10. 房源收藏模块
-- =====================================================

-- 10.1 房源收藏表
CREATE TABLE `rental_house_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `house_id` BIGINT NOT NULL COMMENT '房源 ID，关联 rental_house',
    `tenant_user_id` BIGINT NOT NULL COMMENT '租客 ID，关联 member_user',
    `creator` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `updater` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_house_tenant` (`house_id`, `tenant_user_id`),
    KEY `idx_tenant_user_id` (`tenant_user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房源收藏表';

-- =====================================================
-- 脚本完成
-- =====================================================
-- 总计：19 张业务表
-- 复用框架表：sys_user、sys_role、sys_menu、sys_role_menu、sys_user_role、sys_dept、sys_operate_log、sys_login_log（yudao-module-system）
--           member_user（yudao-module-member）
--
-- 注意事项：
-- 1. 所有 BIGINT 类型字段均为自增主键或外键关联 ID
-- 2. 所有表均包含 created（创建时间）、updated（更新时间）、deleted（逻辑删除）、tenant_id（多租户） 四个公共字段
-- 3. 数据库字符集为 utf8mb4，排序规则为 utf8mb4_unicode_ci
-- 4. 不使用物理外键约束，通过应用层维护关联关系
-- 5. 建议定期检查表结构和索引效率
-- =====================================================