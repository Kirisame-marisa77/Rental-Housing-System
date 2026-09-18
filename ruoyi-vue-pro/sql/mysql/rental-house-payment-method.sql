-- ============================================================================
-- 社区房屋租赁管理系统 —— 房源付款方式
--
-- 执行顺序：rental.sql → rental-extra.sql → rental-owner.sql → rental-menu.sql
--          → rental-moveout-owner-deregister.sql → 本脚本
-- 用法：mysql -uroot -p --default-character-set=utf8mb4 ruoyi-vue-pro < rental-house-payment-method.sql
--
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文注释读成乱码。
-- ============================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1. 房源增加「付款方式」
--
--    格式「押N付M」，N/M 各自取 1~3，共 9 种组合。
--      N = 押几个月租金（押金 = N × 月租金）
--      M = 一次付几个月租金（首期应缴 = 押金 + M × 月租金，账期 M 个月）
--
--    为什么必须存到房源上（而不是像之前那样让租客在申请时自选）：
--      ① 付款方式是房东出租房时定下的条件，属于房源属性，不该由租客更改；
--      ② 押二付一 与 押二付二 的押金完全相同，只看押金反推不出是哪种，
--         所以「只存押金不存付款方式」的偷懒做法在 9 种组合下不成立。
-- ---------------------------------------------------------------------------
ALTER TABLE `rental_house`
    ADD COLUMN `payment_method` VARCHAR(20) NOT NULL DEFAULT '押一付一'
        COMMENT '付款方式：押N付M（N=押几个月，M=一次付几个月，各自 1~3）' AFTER `deposit`;

-- ---------------------------------------------------------------------------
-- 2. 把已有房源的付款方式对齐到它当前的租约
--
--    历史上付款方式只记在申请单和合同上，房源本身没有。下面按
--    「该房源已有的未结束合同 / 待审批申请」回填，没有的按押一付一兜底。
--    （rental-seed-data.sql 里已经写好了正确的值，重跑种子的话这段是冗余的，
--      但对着已有的库升级时它能把数据接上。）
-- ---------------------------------------------------------------------------
UPDATE `rental_house` h
SET h.`payment_method` = COALESCE(
        (SELECT c.`payment_method` FROM `rental_contract` c
          WHERE c.`house_id` = h.`id` AND c.`deleted` = 0
            AND c.`payment_method` IS NOT NULL
          ORDER BY c.`id` DESC LIMIT 1),
        (SELECT a.`payment_method` FROM `rental_apply` a
          WHERE a.`house_id` = h.`id` AND a.`deleted` = 0
            AND a.`payment_method` IS NOT NULL
          ORDER BY a.`id` DESC LIMIT 1),
        '押一付一');

-- ============================================================================
-- 脚本完成
-- ============================================================================
