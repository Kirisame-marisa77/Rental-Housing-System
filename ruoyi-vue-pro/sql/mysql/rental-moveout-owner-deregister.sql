-- ============================================================================
-- 社区房屋租赁管理系统 —— 业主端退租处理 + 账号注销
--
-- 执行顺序：rental.sql → rental-extra.sql → rental-owner.sql → rental-menu.sql → 本脚本
-- 用法：mysql -uroot -p --default-character-set=utf8mb4 ruoyi-vue-pro < rental-moveout-owner-deregister.sql
--
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文注释读成乱码。
--
-- 本脚本只改结构，演示数据在 rental-seed-data.sql 里。
-- ============================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1. 退租申请按房源过滤需要一个索引
--
--    业主端「退租处理」的查询链路是：先查自己名下的 house_id 集合，
--    再用 IN (house_id) 过滤退租申请。没有这个索引就是全表扫描。
--    原表只有 idx_contract_id / idx_tenant_user_id / idx_status。
-- ---------------------------------------------------------------------------
ALTER TABLE `rental_move_out_application`
    ADD KEY `idx_house_id` (`house_id`);

-- ---------------------------------------------------------------------------
-- 2. 处理人类型
--
--    handler_id 原本注释写死「关联 sys_user」，因为只有管理员会处理。
--    现在业主也能处理，同一个字段要表达两种身份，必须加一个类型列区分，
--    否则管理端列表会把业主 id 当成管理员 id 显示，排查问题时严重误导。
-- ---------------------------------------------------------------------------
ALTER TABLE `rental_move_out_application`
    ADD COLUMN `handler_type` TINYINT NOT NULL DEFAULT 0
        COMMENT '处理人类型：0-管理员(sys_user)，1-业主(rental_owner_info)' AFTER `handler_id`;

ALTER TABLE `rental_move_out_application`
    MODIFY COLUMN `handler_id` BIGINT DEFAULT NULL
        COMMENT '处理人 ID：handler_type=0 时关联 sys_user，=1 时关联 rental_owner_info';

--     remark / repair_fee 的原注释也写死了「管理员」，一并修正
ALTER TABLE `rental_move_out_application`
    MODIFY COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注';

ALTER TABLE `rental_move_out_application`
    MODIFY COLUMN `repair_fee` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '损坏维修费用（处理人手动录入）';

ALTER TABLE `rental_settlement_bill`
    ADD COLUMN `handler_type` TINYINT NOT NULL DEFAULT 0
        COMMENT '处理人类型：0-管理员(sys_user)，1-业主(rental_owner_info)' AFTER `handler_id`;

ALTER TABLE `rental_settlement_bill`
    MODIFY COLUMN `handler_id` BIGINT DEFAULT NULL
        COMMENT '处理人 ID：handler_type=0 时关联 sys_user，=1 时关联 rental_owner_info';

-- ---------------------------------------------------------------------------
-- 3. 退租申请状态扩展：新增 2-已驳回
--
--    业主驳回退租后，申请置「已驳回」，同时把合同从 4-退租处理中 回退到 2-生效中。
--    TINYINT 本身没有取值约束，这里只是把注释改准。
-- ---------------------------------------------------------------------------
ALTER TABLE `rental_move_out_application`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0
        COMMENT '状态：0-待处理，1-已处理，2-已驳回';

-- ---------------------------------------------------------------------------
-- 4. 合同状态注释补全
--
--    4-退租处理中 / 6-已退租 这两个值建表时就有定义，但从来没有任何代码写入过
--    （「处理退租」原本只生成结算单，不改合同、不改房源 —— 房源会永远停在已出租）。
--    本次补全状态机后它们才第一次被真正使用。
-- ---------------------------------------------------------------------------
ALTER TABLE `rental_contract`
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 0
        COMMENT '合同状态：0-待签署，1-待缴费，2-生效中，3-即将到期，4-退租处理中，5-已到期，6-已退租，7-已取消';

-- ---------------------------------------------------------------------------
-- 5. 注销账号重新注册的前提（只做确认，不要改）
--
--    逻辑删除（deleted=1）能让 Service 层的唯一性校验放行
--    （selectByPhone 走 MyBatis-Plus，会自动追加 deleted = 0），
--    但**拦不住数据库的唯一索引**。所以注销时代码会把 phone / id_card 改写释放：
--      phone    → 'D' + id
--      id_card  → NULL（InnoDB 唯一索引允许多个 NULL）
--
--    期望的索引形态（执行下面两条 SHOW 确认）：
--      rental_tenant_info：uk_user_id(唯一) uk_id_card(唯一) idx_phone(**非唯一**)
--      rental_owner_info ：uk_phone(唯一) uk_id_card(唯一)
--
--    租客手机号能重新注册纯属侥幸（建表时写成了 KEY 而非 UNIQUE KEY）。
--    若某些环境上 rental_tenant_info.phone 被改成了唯一键，必须改回来：
--      ALTER TABLE `rental_tenant_info` DROP INDEX `uk_phone`, ADD KEY `idx_phone` (`phone`);
-- ---------------------------------------------------------------------------
-- SHOW INDEX FROM `rental_tenant_info`;
-- SHOW INDEX FROM `rental_owner_info`;

-- ============================================================================
-- 脚本完成
-- ============================================================================
