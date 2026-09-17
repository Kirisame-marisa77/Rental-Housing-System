-- ============================================================================
-- 社区房屋租赁管理系统 —— 演示种子数据
--
-- 用法：先执行本文件（会清空全部 rental 业务表），再启动后端。
--       mysql -uroot -p --default-character-set=utf8mb4 ruoyi-vue-pro < rental-seed-data.sql
--
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
--
-- 保留不动的表：rental_contract_template（合同模板属配置数据，不在演示数据范围内）
--
-- 登录账号（业主端 / 租客端登录页分别选对应 tab，密码统一 123456）：
--   业主：13800000001 张建国 / 13800000002 李秀兰 / 13800000003 王海涛
--         13800000004 陈美玲 / 13603914844 吴宇杰
--   租客：13900000001 刘洋 / 13900000002 赵敏 / 13900000003 孙浩
--         13900000004 周雅琴 / 13900000005 吴强 / 13900001111 李四
--   管理端：admin / admin123
--
-- 覆盖的业务状态（便于逐个演示）：
--   房源：上架可租 / 已出租 / 待审核 / 已驳回 / 已下架
--   申请：待审批 / 已通过(待签约) / 已驳回 / 已签约 / 已失效(被他人抢租)
--   合同：待签署 / 生效中 / 即将到期 / 已退租
--   租金账单：首期 / 周期，待缴 / 已缴 / 已逾期(含滞纳金)
--   抄表：已审核已出账 / 待审核未出账；水电账单 水费 / 电费
--   维修：待处理 / 处理中 / 待验收 / 已处理(含评价) 及对应进度流水
--   退租：待处理 / 已处理(含结算单)
--   公告：已发布(含置顶) / 草稿；另有阅读记录、收藏、看房预约
-- ============================================================================

SET NAMES utf8mb4;

-- ========== 1. 清空业务表 ==========
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `rental_announcement`;
TRUNCATE TABLE `rental_announcement_read`;
TRUNCATE TABLE `rental_apply`;
TRUNCATE TABLE `rental_contract`;
TRUNCATE TABLE `rental_house`;
TRUNCATE TABLE `rental_house_favorite`;
TRUNCATE TABLE `rental_house_image`;
TRUNCATE TABLE `rental_meter_config`;
TRUNCATE TABLE `rental_meter_reading`;
TRUNCATE TABLE `rental_move_out_application`;
TRUNCATE TABLE `rental_owner_info`;
TRUNCATE TABLE `rental_payment_record`;
TRUNCATE TABLE `rental_rent_bill`;
TRUNCATE TABLE `rental_repair_order`;
TRUNCATE TABLE `rental_repair_progress`;
TRUNCATE TABLE `rental_settlement_bill`;
TRUNCATE TABLE `rental_tenant_info`;
TRUNCATE TABLE `rental_utility_bill`;
TRUNCATE TABLE `rental_viewing_appointment`;
SET FOREIGN_KEY_CHECKS = 1;

-- ========== 2. 业主 ==========
INSERT INTO `rental_owner_info` (`id`, `name`, `id_card`, `bank_card`, `phone`, `password`, `gender`, `emergency_contact`, `emergency_phone`) VALUES
(1, '张建国', '110101196805121234', '6222021001112223', '13800000001', '123456', 1, '张小明', '13800000011'),
(2, '李秀兰', '110101197203084567', '6222021001112224', '13800000002', '123456', 2, '李强',   '13800000012'),
(3, '王海涛', '110101197511239876', '6222021001112225', '13800000003', '123456', 1, '王丽',   '13800000013'),
(4, '陈美玲', '110101198002145432', '6222021001112226', '13800000004', '123456', 2, '陈刚',   '13800000014'),
(5, '吴宇杰', '110101199501017890', '6222021001112227', '13603914844', '123456', 1, '吴建华', '13603914848');

-- ========== 3. 租客 ==========
-- auth_status：0-未认证 1-认证中 2-已认证 3-认证失败 4-已锁定
INSERT INTO `rental_tenant_info` (`id`, `name`, `id_card`, `phone`, `password`, `gender`, `emergency_contact`, `emergency_phone`, `work_unit`, `auth_status`, `auth_fail_count`, `auth_time`) VALUES
(1, '刘洋',   '110101199203152345', '13900000001', '123456', 1, '刘建军', '13900000011', '北京星辰科技有限公司', 2, 0, '2026-01-05 10:00:00'),
(2, '赵敏',   '110101199407223456', '13900000002', '123456', 2, '赵国强', '13900000012', '上海远洋贸易有限公司', 2, 0, '2026-05-20 14:30:00'),
(3, '孙浩',   '110101199108093567', '13900000003', '123456', 1, '孙立',   '13900000013', '深圳智联网络科技',     2, 0, '2026-09-14 09:15:00'),
(4, '周雅琴', '110101199611304578', '13900000004', '123456', 2, '周美华', '13900000014', '自由职业',             0, 0, NULL),
(5, '吴强',   '110101198905176789', '13900000005', '123456', 1, '吴桂芳', '13900000015', '广州精工制造有限公司', 1, 0, NULL),
(6, '李四',   '110101199312128901', '13900001111', '123456', 1, '李国平', '13900001112', '成都建筑设计院',       2, 0, '2025-08-15 11:00:00');

-- ========== 4. 房源 ==========
-- status：0-下架 1-上架 2-已锁定 3-已出租 ；review_status：0-待审核 1-已通过 2-已驳回
-- 「可被租客申请/预约」的标准组合是 review_status=1 且 status=1
-- water_bill_type：0-统一单价 1-三档梯度 ；electricity_bill_type：0-统一单价 1-峰谷两价
INSERT INTO `rental_house` (`id`, `owner_id`, `house_no`, `community_name`, `area`, `building_no`, `room_no`, `layout`, `square_area`, `orientation`, `floor`, `total_floor`, `decoration`, `monthly_rent`, `deposit`, `facilities`, `status`, `review_status`, `review_reason`, `water_bill_type`, `water_unit_price`, `electricity_bill_type`, `electricity_unit_price`, `electricity_peak_price`, `electricity_valley_price`, `online_time`, `description`) VALUES
(1,  1, 'FW20260105100001', '碧湖家园', '禾盛社区', '3', '1105', '两室一厅', 89.50,  '南',   11, 18, '精装修',   3500.00, 3500.00, '空调,热水器,洗衣机,冰箱,宽带,天然气', 1, 1, NULL, 0, 3.50, 1, NULL, 0.68, 0.35, '2026-01-06 09:00:00', '南北通透，采光好，紧邻地铁4号线禾盛站，周边配套成熟。'),
(2,  1, 'FW20260105100002', '碧湖家园', '禾盛社区', '3', '1206', '一室一厅', 62.00,  '东南', 12, 18, '简装修',   3200.00, 3200.00, '空调,热水器,洗衣机,宽带',           1, 1, NULL, 0, 3.50, 1, NULL, 0.68, 0.35, '2026-01-06 09:05:00', '一室一厅户型方正，适合单身或情侣居住。'),
(3,  2, 'FW20260105100003', '禾盛社区', '禾盛社区', '1', '802',  '两室一厅', 85.00,  '南北',  8, 11, '精装修',   2800.00, 2800.00, '空调,热水器,洗衣机,冰箱,暖气',       1, 1, NULL, 0, 3.20, 0, 0.55, NULL, NULL, '2026-01-07 10:00:00', '老小区改造房源，生活气息浓，菜市场就在楼下。'),
(4,  2, 'FW20251201100004', '禾盛社区', '禾盛社区', '2', '1503', '三室两厅', 128.00, '南',   15, 22, '精装修',   4200.00, 4200.00, '空调,热水器,洗衣机,冰箱,宽带,天然气,车位', 3, 1, NULL, 0, 3.50, 1, NULL, 0.68, 0.35, '2025-12-02 10:00:00', '大三居，适合一家人居住，小区绿化率高。'),
(5,  3, 'FW20260105100005', '阳光新城', '阳光社区', '5', '601',  '一室一厅', 58.00,  '东',    6, 16, '简装修',   2600.00, 2600.00, '空调,热水器,洗衣机',                 1, 1, NULL, 0, 3.30, 0, 0.55, NULL, NULL, '2026-01-08 09:30:00', '价格实惠，适合刚参加工作的年轻人。'),
(6,  3, 'FW20260105100006', '阳光新城', '阳光社区', '5', '1902', '两室两厅', 98.00,  '南',   19, 25, '精装修',   3800.00, 3800.00, '空调,热水器,洗衣机,冰箱,宽带,暖气', 1, 1, NULL, 0, 3.30, 1, NULL, 0.70, 0.36, '2026-01-08 09:35:00', '高层视野开阔，主卧带独立卫生间。'),
(7,  4, 'FW20250901100007', '翠竹苑',   '翠竹社区', '8', '303',  '一室一厅', 55.00,  '北',    3, 12, '简装修',   2200.00, 2200.00, '空调,热水器,洗衣机',                 1, 1, NULL, 0, 2.80, 0, 0.55, NULL, NULL, '2025-09-02 09:00:00', '小区安静，适合备考或居家办公。'),
(8,  4, 'FW20260501100008', '翠竹苑',   '翠竹社区', '8', '1701', '三室两厅', 135.00, '南北', 17, 24, '豪华装修', 4500.00, 4500.00, '空调,热水器,洗衣机,冰箱,宽带,天然气,车位,新风系统', 3, 1, NULL, 0, 2.80, 0, 0.55, NULL, NULL, '2026-05-02 09:00:00', '豪华装修三居，全套家电，拎包入住。'),
(9,  5, 'FW20260105100009', '碧湖家园', '禾盛社区', '6', '2201', '三室两厅', 142.00, '南',   22, 28, '豪华装修', 5000.00, 10000.00, '空调,热水器,洗衣机,冰箱,宽带,天然气,车位,新风系统,地暖', 3, 1, NULL, 0, 3.50, 1, NULL, 0.68, 0.35, '2026-01-06 09:10:00', '顶楼大平层，全屋地暖，家电全新。'),
(10, 5, 'FW20260915100010', '翰林书院', '翰林社区', '2', '604',  '两室一厅', 88.00,  '西南',  6, 14, '精装修',   3000.00, 3000.00, '空调,热水器,洗衣机,冰箱',           0, 0, NULL, 0, 3.40, 0, 0.55, NULL, NULL, NULL, '学区房，步行5分钟到实验小学，刚装修完待上架。'),
(11, 1, 'FW20250901100011', '禾盛社区', '禾盛社区', '3', '901',  '两室一厅', 82.00,  '东',    9, 11, '简装修',   3300.00, 3300.00, '空调,热水器,洗衣机,暖气',           0, 1, NULL, 0, 3.20, 0, 0.55, NULL, NULL, '2025-09-02 10:00:00', '业主自住中，暂不对外出租。'),
(12, 2, 'FW20260910100012', '阳光新城', '阳光社区', '7', '1104', '一室一厅', 60.00,  '北',   11, 20, '简装修',   2900.00, 2900.00, '空调,热水器,洗衣机',                 0, 2, '房屋照片模糊且与实际不符，请重新上传实拍照片后再提交审核', 0, 3.30, 0, 0.55, NULL, NULL, NULL, '朝北一居，价格可谈。');

-- ========== 5. 租房申请 ==========
-- status：0-待审批 1-已通过(房东同意，待签约) 2-已驳回 3-已签约 4-已超时/已失效
INSERT INTO `rental_apply` (`id`, `apply_no`, `house_id`, `tenant_user_id`, `move_in_date`, `lease_term`, `payment_method`, `monthly_rent`, `deposit_amount`, `tenant_remark`, `status`, `approver_id`, `approve_time`, `reject_reason`, `contract_id`, `timeout_reason`) VALUES
(1, 'SQ202512201000011234', 4, 1, '2026-01-01', 12, '押一付三', 4200.00, 4200.00, '希望1月初入住，长租',        3, 2, '2025-12-21 09:30:00', NULL, 1, NULL),
(2, 'SQ202605201000025678', 8, 2, '2026-06-01', 12, '押一付一', 4500.00, 4500.00, '需要固定停车位',            3, 4, '2026-05-21 14:20:00', NULL, 2, NULL),
(3, 'SQ202609141000039012', 6, 3, '2026-10-01', 12, '押一付三', 3800.00, 3800.00, '公司在附近，希望尽快入住',  1, 3, '2026-09-15 10:05:00', NULL, 3, NULL),
(4, 'SQ202609161000043456', 1, 4, '2026-10-01', 12, '押一付一', 3500.00, 3500.00, '想先看房再决定',            0, NULL, NULL, NULL, NULL, NULL),
(5, 'SQ202609101000057890', 5, 5, '2026-09-20', 12, '押二付一', 2600.00, 5200.00, '养了一只猫',                2, 3, '2026-09-11 16:40:00', '该房源不接受饲养宠物，抱歉', NULL, NULL),
(6, 'SQ202609051000061234', 3, 2, '2026-09-15', 12, '押一付三', 2800.00, 2800.00, NULL,                        0, NULL, NULL, NULL, NULL, NULL),
(7, 'SQ202512221000075678', 4, 6, '2026-01-05', 12, '押一付三', 4200.00, 4200.00, NULL,                        4, NULL, NULL, NULL, NULL, '该房源已被其他租客承租，申请自动失效'),
(8, 'SQ202508151000089012', 7, 6, '2025-09-01', 12, '押一付一', 2200.00, 2200.00, '长租，至少一年',            3, 4, '2025-08-16 11:00:00', NULL, 4, NULL);

-- ========== 6. 合同 ==========
-- status：0-待签署 1-待缴费 2-生效中 3-即将到期 4-退租处理中 5-已到期 6-已退租 7-已取消
INSERT INTO `rental_contract` (`id`, `contract_no`, `house_id`, `tenant_user_id`, `rent_start_date`, `rent_end_date`, `monthly_rent`, `deposit_amount`, `payment_method`, `property_fee_unit`, `status`, `sign_time`, `owner_sign_time`, `source_apply_id`, `template_id`, `content`) VALUES
(1, 'HT-20251221100001-1', 4, 1, '2026-01-01', '2026-12-31', 4200.00, 4200.00, '押一付三', 2.50, 2, '2025-12-25 10:00:00', '2025-12-25 09:30:00', 1, 1, '房屋租赁合同：出租方李秀兰，承租方刘洋，租赁期限2026-01-01至2026-12-31，月租金4200元，押金4200元，付款方式押一付三。'),
(2, 'HT-20260521100002-2', 8, 2, '2026-06-01', '2027-05-31', 4500.00, 4500.00, '押一付一', 2.80, 2, '2026-05-25 15:00:00', '2026-05-25 14:30:00', 2, 1, '房屋租赁合同：出租方陈美玲，承租方赵敏，租赁期限2026-06-01至2027-05-31，月租金4500元，押金4500元，付款方式押一付一。'),
(3, 'HT-20260915100003-3', 6, 3, '2026-10-01', '2027-09-30', 3800.00, 3800.00, '押一付三', 2.50, 0, NULL, NULL, 3, 1, '房屋租赁合同：出租方王海涛，承租方孙浩，租赁期限2026-10-01至2027-09-30，月租金3800元，押金3800元，付款方式押一付三。尚未签署。'),
(4, 'HT-20250816100004-8', 7, 6, '2025-09-01', '2026-08-31', 2200.00, 2200.00, '押一付一', 2.00, 6, '2025-08-20 09:00:00', '2025-08-20 08:40:00', 8, 1, '房屋租赁合同：出租方陈美玲，承租方李四，租赁期限2025-09-01至2026-08-31，月租金2200元，押金2200元。合同已到期退租。'),
(5, 'HT-20251010100005', 9, 4, '2025-10-15', '2026-10-14', 5000.00, 10000.00, '押二付一', 3.00, 3, '2025-10-10 16:00:00', '2025-10-10 15:30:00', NULL, 1, '房屋租赁合同：出租方吴宇杰，承租方周雅琴，租赁期限2025-10-15至2026-10-14，月租金5000元，押金10000元，付款方式押二付一。即将到期。');

-- ========== 7. 租金/物业费账单 ==========
-- bill_type：0-首期账单 1-周期账单 ；pay_status：0-待缴费 1-已缴费 2-已逾期
-- 首期账单 = 首月租金 + 押金；周期账单 = 当月租金 + 当月物业费
INSERT INTO `rental_rent_bill` (`id`, `bill_no`, `contract_id`, `tenant_user_id`, `house_id`, `bill_type`, `period_start`, `period_end`, `rent_amount`, `deposit_amount`, `property_fee_amount`, `total_amount`, `paid_amount`, `pay_status`, `due_date`, `pay_time`, `pay_method`, `payee`, `fee_detail`, `late_fee`, `late_fee_waived`, `late_fee_waive_reason`) VALUES
(1, 'ZD-20251225100001-1', 1, 1, 4, 0, '2026-01-01', '2026-01-31', 4200.00, 4200.00,   0.00,  8400.00, 8400.00, 1, '2026-01-01', '2025-12-26 10:00:00', '银行转账', 2, '首期：首月租金4200.00 + 押金4200.00', 0.00, 0.00, NULL),
(2, 'ZD-20260901100002-1', 1, 1, 4, 1, '2026-09-01', '2026-09-30', 4200.00,    0.00, 250.00,  4450.00, 4450.00, 1, '2026-09-05', '2026-09-02 09:12:00', '微信',     2, '租金4200.00 + 物业费250.00',        0.00, 0.00, NULL),
(3, 'ZD-20260901100003-1', 1, 1, 4, 1, '2026-10-01', '2026-10-31', 4200.00,    0.00, 250.00,  4450.00,    0.00, 0, '2026-10-05', NULL, NULL, NULL, '租金4200.00 + 物业费250.00', 0.00, 0.00, NULL),
(4, 'ZD-20260525100004-2', 2, 2, 8, 0, '2026-06-01', '2026-06-30', 4500.00, 4500.00,   0.00,  9000.00, 9000.00, 1, '2026-06-01', '2026-05-28 14:00:00', '支付宝',   4, '首期：首月租金4500.00 + 押金4500.00', 0.00, 0.00, NULL),
(5, 'ZD-20260801100005-2', 2, 2, 8, 1, '2026-08-01', '2026-08-31', 4500.00,    0.00, 280.00,  4780.00,    0.00, 2, '2026-08-05', NULL, NULL, NULL, '租金4500.00 + 物业费280.00',       47.80, 0.00, NULL),
(6, 'ZD-20260901100006-2', 2, 2, 8, 1, '2026-10-01', '2026-10-31', 4500.00,    0.00, 280.00,  4780.00,    0.00, 0, '2026-10-05', NULL, NULL, NULL, '租金4500.00 + 物业费280.00',        0.00, 0.00, NULL),
(7, 'ZD-20260915100007-3', 3, 3, 6, 0, '2026-10-01', '2026-10-31', 3800.00, 3800.00,   0.00,  7600.00,    0.00, 0, '2026-09-24', NULL, NULL, NULL, '首期：首月租金3800.00 + 押金3800.00（合同待签署）', 0.00, 0.00, NULL),
(8, 'ZD-20250915100008-5', 5, 4, 9, 0, '2025-10-15', '2025-11-14', 5000.00,10000.00,   0.00, 15000.00,15000.00, 1, '2025-10-15', '2025-10-12 11:00:00', '银行转账', 5, '首期：首月租金5000.00 + 押金10000.00', 0.00, 0.00, NULL),
(9, 'ZD-20260915100009-5', 5, 4, 9, 1, '2026-09-15', '2026-10-14', 5000.00,    0.00, 300.00,  5300.00,    0.00, 2, '2026-09-15', NULL, NULL, NULL, '租金5000.00 + 物业费300.00',       53.00, 0.00, NULL);

-- ========== 8. 抄表记录 ==========
-- meter_type：0-水表 1-电表 ；status：0-正常 1-异常 2-已作废 ；review_status：0-待审核 1-已通过 2-已驳回
-- 注意：水电费账单必须挂在抄表记录上（utility_bill.meter_reading_id 为 NOT NULL），
--       所以下面 bill_id 非空的记录才有对应水电账单，第 3 条待审核、尚未出账
INSERT INTO `rental_meter_reading` (`id`, `owner_id`, `house_id`, `contract_id`, `meter_type`, `last_reading`, `current_reading`, `valley_reading`, `last_valley_reading`, `usage_amount`, `valley_usage`, `unit_price`, `peak_price`, `valley_price`, `fee_amount`, `reading_date`, `operator_id`, `bill_id`, `status`, `images`, `review_status`, `review_reason`, `reviewer_id`, `review_time`) VALUES
(1, 2, 4, 1, 0,  120.00,  138.50, NULL,   NULL,  18.50, NULL,   3.50, NULL, NULL,  64.75, '2026-09-01', 2, 1, 0, NULL, 1, NULL, 1, '2026-09-01 15:00:00'),
(2, 2, 4, 1, 1, 2200.00, 2456.00, 2600.00, 2500.00, 256.00, 100.00, 0.68, 0.68, 0.35, 141.08, '2026-09-01', 2, 2, 0, NULL, 1, NULL, 1, '2026-09-01 15:05:00'),
(3, 4, 8, 2, 1, 3100.00, 3390.00, NULL,   NULL, 290.00, NULL,   0.55, NULL, NULL, 159.50, '2026-09-10', 4, NULL, 0, NULL, 0, NULL, NULL, NULL),
(4, 4, 7, 4, 0,   88.00,   96.50, NULL,   NULL,   8.50, NULL,   2.80, NULL, NULL,  23.80, '2026-08-25', 4, 3, 0, NULL, 1, NULL, 1, '2026-08-25 16:00:00');

-- ========== 9. 水电费账单 ==========
-- fee_type：0-水费 1-电费 2-水电合并 ；pay_status：0-待缴费 1-已缴费 2-已逾期
INSERT INTO `rental_utility_bill` (`id`, `bill_no`, `contract_id`, `tenant_user_id`, `house_id`, `meter_reading_id`, `fee_type`, `water_amount`, `electricity_amount`, `total_amount`, `pay_status`, `due_date`, `pay_time`, `pay_method`, `payee`) VALUES
(1, 'SD-20260901100001', 1, 1, 4, 1, 0,  64.75,   0.00,  64.75, 1, '2026-09-10', '2026-09-08 11:00:00', '支付宝', 2),
(2, 'SD-20260901100002', 1, 1, 4, 2, 1,   0.00, 141.08, 141.08, 0, '2026-09-20', NULL, NULL, NULL),
(3, 'SD-20260825100003', 4, 6, 7, 4, 0,  23.80,   0.00,  23.80, 1, '2026-08-31', '2026-08-28 09:30:00', '现金',   4);

-- ========== 10. 维修工单 ==========
-- status：0-待处理 1-处理中 2-待验收 3-已处理 4-已驳回(退回重做) ；priority：0-普通 1-紧急 2-特急
INSERT INTO `rental_repair_order` (`id`, `order_no`, `tenant_user_id`, `house_id`, `repair_type`, `description`, `images`, `expected_time`, `status`, `priority`, `repairer_id`, `owner_id`, `assign_time`, `complete_time`, `repair_description`, `handle_evidence`, `handle_time`, `review_reason`, `rating`, `evaluation_content`, `evaluation_time`) VALUES
(1, 'WX-20260915100001', 1, 4, '水龙头漏水', '厨房水龙头一直滴水，关不紧，水费在涨', NULL, '2026-09-18 10:00:00', 0, 1, NULL, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 'WX-20260810100002', 1, 4, '空调不制冷', '卧室空调开机后不制冷，怀疑缺制冷剂',   NULL, '2026-08-12 14:00:00', 3, 0, NULL, 2, '2026-08-11 09:00:00', '2026-08-12 17:00:00', '补充制冷剂并清洗滤网，已恢复正常制冷', NULL, '2026-08-12 17:00:00', NULL, 5, '师傅很专业，半小时就修好了，态度也好', '2026-08-13 09:00:00'),
(3, 'WX-20260912100003', 2, 8, '门锁故障',   '入户门智能锁无法识别指纹，只能用密码开', NULL, '2026-09-13 09:00:00', 1, 1, NULL, 4, '2026-09-12 15:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'WX-20260905100004', 2, 8, '墙面渗水',   '卫生间外侧墙面有渗水痕迹，墙皮开始脱落', NULL, '2026-09-08 10:00:00', 2, 2, NULL, 4, '2026-09-06 09:30:00', NULL, '已重做卫生间防水层，等待租客验收', NULL, '2026-09-10 16:00:00', NULL, NULL, NULL, NULL);

-- ========== 11. 维修进度 ==========
-- operator_type：0-租客 1-管理员 2-维修人员
INSERT INTO `rental_repair_progress` (`id`, `order_id`, `operator_id`, `operator_type`, `action_type`, `description`) VALUES
(1, 1, 1, 0, '提交报修', '厨房水龙头一直滴水，关不紧'),
(2, 2, 1, 0, '提交报修', '卧室空调开机后不制冷'),
(3, 2, 2, 1, '分配工单', '已指派维修师傅上门'),
(4, 2, 2, 2, '维修完成', '补充制冷剂并清洗滤网'),
(5, 2, 1, 0, '确认完成', '已确认，问题解决'),
(6, 3, 2, 0, '提交报修', '智能锁指纹失效'),
(7, 3, 4, 1, '分配工单', '已联系维修人员，等待接单'),
(8, 4, 2, 0, '提交报修', '卫生间外侧墙面渗水'),
(9, 4, 4, 2, '维修完成', '防水层已重做，等待验收');

-- ========== 12. 退租申请 ==========
-- status：0-待处理 1-已处理 ；move_out_type：0-到期退租 1-提前退租
-- inspection_result：0-验收通过 1-轻微损坏 2-严重损坏
INSERT INTO `rental_move_out_application` (`id`, `apply_no`, `contract_id`, `tenant_user_id`, `house_id`, `move_out_type`, `move_out_reason`, `expected_move_out_date`, `settlement_id`, `status`, `handler_id`, `handle_time`, `inspection_result`, `repair_fee`, `repair_fee_desc`, `deposit_handle`, `remaining_rent`, `deduction_amount`, `refund_or_pay`) VALUES
(1, 'TZ20260720100001', 4, 6, 7, 0, '合同到期，不再续租', '2026-08-31', 1, 1, 4, '2026-08-31 10:00:00', 0, 0.00, NULL, '扣除欠费后退还', 0.00, 23.80, 2176.20),
(2, 'TZ20260910100002', 5, 4, 9, 1, '工作调动到外地，需提前退租', '2026-10-14', NULL, 0, NULL, NULL, NULL, 0.00, NULL, NULL, 0.00, 0.00, NULL);

-- ========== 13. 退租结算单 ==========
INSERT INTO `rental_settlement_bill` (`id`, `settlement_no`, `contract_id`, `house_id`, `tenant_user_id`, `move_out_application_id`, `move_out_type`, `deposit_amount`, `deposit_handle`, `remaining_rent`, `property_fee_arrears`, `utility_fee_arrears`, `repair_fee`, `deduction_amount`, `refund_or_pay`, `handler_id`, `handle_time`) VALUES
(1, 'JS20260831100001', 4, 7, 6, 1, 0, 2200.00, '扣除欠费后退还', 0.00, 0.00, 23.80, 0.00, 23.80, 2176.20, 4, '2026-08-31 10:00:00');

-- ========== 14. 缴费记录 ==========
-- bill_type：0-租金/物业费 1-水电费 2-退租结算 ；pay_method：现金/微信/支付宝/银行转账
INSERT INTO `rental_payment_record` (`id`, `bill_type`, `bill_id`, `pay_amount`, `pay_method`, `pay_time`, `payee`, `transaction_no`, `voucher_no`) VALUES
(1, 0, 1,  8400.00, '银行转账', '2025-12-26 10:00:00', 2, 'TXN2025122600001', 'SK20251226001'),
(2, 0, 8, 15000.00, '银行转账', '2025-10-12 11:00:00', 5, 'TXN2025101200001', 'SK20251012001'),
(3, 0, 4,  9000.00, '支付宝',   '2026-05-28 14:00:00', 4, 'TXN2026052800001', 'SK20260528001'),
(4, 1, 1,    64.75, '支付宝',   '2026-09-08 11:00:00', 2, 'TXN2026090800001', 'SK20260908001'),
(5, 0, 2,  4450.00, '微信',     '2026-09-02 09:12:00', 2, 'TXN2026090200001', 'SK20260902001'),
(6, 1, 3,    23.80, '现金',     '2026-08-28 09:30:00', 4, 'TXN2026082800001', 'SK20260828001');

-- ========== 15. 公告 ==========
-- status：0-草稿 1-已发布 2-已删除 ；category：缴费通知/维修通知/社区公告/紧急通知
INSERT INTO `rental_announcement` (`id`, `notice_no`, `title`, `content`, `category`, `publisher_id`, `publish_time`, `status`, `is_top`, `target_scope`) VALUES
(1, 'GG20260901100001', '关于2026年9月租金缴纳的提醒',
 '各位租客：\n本月租金账单已生成，请于账单到期日前完成缴纳。可通过租客端「我的账单」查看明细并缴费。\n如有疑问请联系物业管理处。',
 '缴费通知', 1, '2026-09-01 09:00:00', 1, 1, NULL),
(2, 'GG20260905100002', '紧急停水通知（9月10日 08:00-18:00）',
 '因市政管网检修，本小区将于9月10日08:00至18:00暂停供水，请各位业主与租客提前储水，给您带来不便敬请谅解。',
 '紧急通知', 1, '2026-09-05 16:00:00', 1, 1, NULL),
(3, 'GG20260910100003', '电梯年度检修安排',
 '小区各楼栋电梯将于9月11日至9月15日分批进行年度检修，检修期间单梯运行，请合理安排出行时间。',
 '社区公告', 1, '2026-09-10 10:00:00', 1, 0, NULL),
(4, 'GG20260912100004', '维修服务响应时效说明',
 '为提升服务质量，自9月起维修工单响应时效调整为：特急工单2小时内响应，紧急工单24小时内响应，普通工单48小时内响应。',
 '维修通知', 1, '2026-09-12 11:00:00', 1, 0, NULL),
(5, 'GG20260916100005', '国庆假期物业值班安排（草稿）',
 '国庆假期期间物业服务中心值班时间为每日9:00-17:00，紧急报修请拨打24小时值班电话。',
 '社区公告', 1, '2026-09-16 15:00:00', 0, 0, NULL);

-- ========== 16. 公告阅读记录 ==========
-- user_id 存的是 rental_tenant_info.id（member 模块未启用）
INSERT INTO `rental_announcement_read` (`id`, `announcement_id`, `user_id`, `read_time`) VALUES
(1, 1, 1, '2026-09-02 08:00:00'),
(2, 1, 2, '2026-09-03 09:30:00'),
(3, 2, 1, '2026-09-05 17:00:00'),
(4, 3, 3, '2026-09-11 10:00:00'),
(5, 4, 2, '2026-09-13 12:00:00');

-- ========== 17. 房源收藏 ==========
-- tenant_user_id 存的是 rental_tenant_info.id；取消收藏走物理删除
INSERT INTO `rental_house_favorite` (`id`, `house_id`, `tenant_user_id`) VALUES
(1, 1, 4),
(2, 3, 4),
(3, 9, 2),
(4, 5, 5),
(5, 6, 3);

-- ========== 18. 预约看房 ==========
-- status：0-待确认 1-已确认 2-已完成 3-已取消
INSERT INTO `rental_viewing_appointment` (`id`, `house_id`, `tenant_user_id`, `appointment_date`, `start_time`, `end_time`, `status`, `feedback`) VALUES
(1, 1, 4, '2026-09-18', '10:00', '11:00', 0, NULL),
(2, 3, 2, '2026-09-19', '14:00', '15:00', 1, NULL),
(3, 5, 5, '2026-09-12', '09:00', '10:00', 2, '房子采光不错，但离地铁站有点远，再考虑一下'),
(4, 6, 3, '2026-09-15', '16:00', '17:00', 3, NULL),
(5, 9, 4, '2026-09-20', '10:00', '11:00', 0, NULL);

-- ========== 19. 抄表配置 ==========
INSERT INTO `rental_meter_config` (`id`, `config_key`, `config_value`, `effective_time`, `config_user`) VALUES
(1, 'water_unit_price',       '3.50', '2026-01-01 00:00:00', 1),
(2, 'electricity_unit_price', '0.55', '2026-01-01 00:00:00', 1);

-- ========== 20. 重置自增（让后续新建数据的 id 接在种子数据之后）==========
ALTER TABLE `rental_owner_info`            AUTO_INCREMENT = 100;
ALTER TABLE `rental_tenant_info`           AUTO_INCREMENT = 100;
ALTER TABLE `rental_house`                 AUTO_INCREMENT = 100;
ALTER TABLE `rental_apply`                 AUTO_INCREMENT = 100;
ALTER TABLE `rental_contract`              AUTO_INCREMENT = 100;
ALTER TABLE `rental_rent_bill`             AUTO_INCREMENT = 100;
ALTER TABLE `rental_utility_bill`          AUTO_INCREMENT = 100;
ALTER TABLE `rental_meter_reading`         AUTO_INCREMENT = 100;
ALTER TABLE `rental_repair_order`          AUTO_INCREMENT = 100;
ALTER TABLE `rental_repair_progress`       AUTO_INCREMENT = 100;
ALTER TABLE `rental_move_out_application`  AUTO_INCREMENT = 100;
ALTER TABLE `rental_settlement_bill`       AUTO_INCREMENT = 100;
ALTER TABLE `rental_payment_record`        AUTO_INCREMENT = 100;
ALTER TABLE `rental_announcement`          AUTO_INCREMENT = 100;
ALTER TABLE `rental_announcement_read`     AUTO_INCREMENT = 100;
ALTER TABLE `rental_house_favorite`        AUTO_INCREMENT = 100;
ALTER TABLE `rental_viewing_appointment`   AUTO_INCREMENT = 100;
ALTER TABLE `rental_house_image`           AUTO_INCREMENT = 100;
ALTER TABLE `rental_meter_config`          AUTO_INCREMENT = 100;
