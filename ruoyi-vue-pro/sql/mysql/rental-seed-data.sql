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
-- 登录账号（业主端 / 租客端登录页分别选对应 tab，所有人密码统一 123456）：
--   业主：13800000001 沈志远 / 13800000002 何丽云 / 13800000003 周振华
--         13800000004 徐婉如 / 13603914844 吴宇杰
--   租客：13900000001 陈嘉禾 / 13900000002 林晓彤 / 13900000003 高鹏
--         13900000004 苏婉清 / 13900000005 郑昊 / 13900001111 白露
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
--
-- 关联关系（改数据时务必一起改，否则会出现指向空记录的孤儿行）：
--   rental_house.owner_id            → rental_owner_info.id
--   *.tenant_user_id / user_id       → rental_tenant_info.id（**不是** member_user，member 模块没启用）
--   rental_house_favorite.tenant_user_id、rental_announcement_read.user_id 同理
--   租金账单/水电账单**没有 owner_id**，业主归属靠 house_id 反查 rental_house.owner_id
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
-- 密码明文 123456（演示用，登录逻辑就是明文比对）
INSERT INTO `rental_owner_info` (`id`, `name`, `id_card`, `bank_card`, `phone`, `password`, `gender`, `emergency_contact`, `emergency_phone`) VALUES
(1, '沈志远', '310101197204183321', '6222021001113331', '13800000001', '123456', 1, '沈嘉宁', '13800000011'),
(2, '何丽云', '310101197809254432', '6222021001113332', '13800000002', '123456', 2, '何伟',   '13800000012'),
(3, '周振华', '310101197103126543', '6222021001113333', '13800000003', '123456', 1, '周小雅', '13800000013'),
(4, '徐婉如', '310101198306087654', '6222021001113334', '13800000004', '123456', 2, '徐建国', '13800000014'),
(5, '吴宇杰', '110101199501017890', '6222021001112227', '13603914844', '123456', 1, '吴建华', '13603914848');

-- ========== 3. 租客 ==========
-- auth_status：0-未认证 1-认证中 2-已认证 3-认证失败 4-已锁定
INSERT INTO `rental_tenant_info` (`id`, `name`, `id_card`, `phone`, `password`, `gender`, `emergency_contact`, `emergency_phone`, `work_unit`, `auth_status`, `auth_fail_count`, `auth_time`) VALUES
(1, '陈嘉禾', '310101199505122211', '13900000001', '123456', 1, '陈立群', '13900000011', '杭州云图软件有限公司', 2, 0, '2026-01-05 10:00:00'),
(2, '林晓彤', '310101199612073322', '13900000002', '123456', 2, '林国栋', '13900000012', '苏州明远贸易有限公司', 2, 0, '2026-05-20 14:30:00'),
(3, '高鹏',   '310101199203184433', '13900000003', '123456', 1, '高志强', '13900000013', '南京智联网络科技',     2, 0, '2026-09-14 09:15:00'),
(4, '苏婉清', '310101199710305544', '13900000004', '123456', 2, '苏文英', '13900000014', '自由职业',             0, 0, NULL),
(5, '郑昊',   '310101199001226655', '13900000005', '123456', 1, '郑美凤', '13900000015', '无锡精工机械有限公司', 1, 0, NULL),
(6, '白露',   '310101199408147766', '13900001111', '123456', 2, '白建华', '13900001112', '合肥市政设计院',       2, 0, '2025-08-15 11:00:00');

-- ========== 4. 房源 ==========
-- status：0-下架 1-上架 2-已锁定 3-已出租 ；review_status：0-待审核 1-已通过 2-已驳回
-- 「可被租客申请/预约」的标准组合是 review_status=1 且 status=1
-- water_bill_type：0-统一单价 1-三档梯度 ；electricity_bill_type：0-统一单价 1-峰谷两价
-- payment_method：押N付M，N=押几个月（押金 = N × 月租金），M=一次付几个月（首期 = 押金 + M × 月租金）
-- 已出租/已被申请的房源，付款方式必须与它的合同/申请一致，否则数据自相矛盾
INSERT INTO `rental_house` (`id`, `owner_id`, `house_no`, `community_name`, `area`, `building_no`, `room_no`, `layout`, `square_area`, `orientation`, `floor`, `total_floor`, `decoration`, `monthly_rent`, `deposit`, `payment_method`, `facilities`, `status`, `review_status`, `review_reason`, `water_bill_type`, `water_unit_price`, `electricity_bill_type`, `electricity_unit_price`, `electricity_peak_price`, `electricity_valley_price`, `online_time`, `description`) VALUES
(1,  1, 'FW20260105100001', '云栖花园', '云栖社区', '4', '1502', '两室一厅', 92.00,  '南',   15, 22, '精装修',   3600.00, 3600.00, '押一付三', '空调,热水器,洗衣机,冰箱,宽带,天然气', 1, 1, NULL, 0, 3.60, 1, NULL, 0.72, 0.38, '2026-01-06 09:00:00', '南北通透，客厅朝南采光充足，步行 6 分钟到地铁 2 号线云栖路站。'),
(2,  1, 'FW20260105100002', '云栖花园', '云栖社区', '4', '806',  '一室一厅', 58.00,  '东南',  8, 22, '简装修',   2800.00, 2800.00, '押一付一', '空调,热水器,洗衣机,宽带',           1, 1, NULL, 0, 3.60, 1, NULL, 0.72, 0.38, '2026-01-06 09:05:00', '户型方正无浪费面积，家电齐全，适合单身或情侣居住。'),
(3,  2, 'FW20260107100003', '望江台',   '望江社区', '2', '1203', '两室一厅', 88.00,  '南北', 12, 18, '精装修',   3100.00, 3100.00, '押一付三', '空调,热水器,洗衣机,冰箱,暖气',       1, 1, NULL, 0, 3.40, 0, 0.58, NULL, NULL, '2026-01-07 10:00:00', '小区改造后重新做了水电，楼下就是菜市场，生活便利。'),
(4,  2, 'FW20251202100004', '望江台',   '望江社区', '2', '1801', '三室两厅', 132.00, '南',   18, 26, '精装修',   4600.00, 4600.00, '押一付三', '空调,热水器,洗衣机,冰箱,宽带,天然气,车位', 3, 1, NULL, 0, 3.60, 1, NULL, 0.72, 0.38, '2025-12-02 10:00:00', '大三居，主卧带独立卫浴，小区绿化率高，适合一家人居住。'),
(5,  3, 'FW20260108100005', '梧桐里',   '梧桐社区', '7', '501',  '一室一厅', 54.00,  '东',    5, 11, '简装修',   2400.00, 4800.00, '押二付一', '空调,热水器,洗衣机',                 1, 1, NULL, 0, 3.20, 0, 0.56, NULL, NULL, '2026-01-08 09:30:00', '价格实惠，早上采光好，适合刚参加工作的年轻人。'),
(6,  3, 'FW20260108100006', '梧桐里',   '梧桐社区', '7', '1608', '两室两厅', 96.00,  '南',   16, 24, '精装修',   3700.00, 3700.00, '押一付三', '空调,热水器,洗衣机,冰箱,宽带,暖气', 1, 1, NULL, 0, 3.20, 1, NULL, 0.70, 0.36, '2026-01-08 09:35:00', '高层视野开阔，主卧带独立卫生间，全屋新换空调。'),
(7,  4, 'FW20250902100007', '鹿鸣苑',   '鹿鸣社区', '1', '302',  '一室一厅', 52.00,  '北',    3, 9,  '简装修',   2100.00, 2100.00, '押一付一', '空调,热水器,洗衣机',                 1, 1, NULL, 0, 2.90, 0, 0.56, NULL, NULL, '2025-09-02 09:00:00', '低楼层，小区安静，适合备考或居家办公。'),
(8,  4, 'FW20260502100008', '鹿鸣苑',   '鹿鸣社区', '1', '2201', '三室两厅', 138.00, '南北', 22, 28, '豪华装修', 4800.00, 4800.00, '押一付一', '空调,热水器,洗衣机,冰箱,宽带,天然气,车位,新风系统', 3, 1, NULL, 0, 2.90, 0, 0.56, NULL, NULL, '2026-05-02 09:00:00', '豪华装修三居，全套家电，带新风系统，拎包入住。'),
(9,  5, 'FW20260106100009', '云栖花园', '云栖社区', '9', '2603', '三室两厅', 145.00, '南',   26, 32, '豪华装修', 5200.00, 10400.00, '押二付一', '空调,热水器,洗衣机,冰箱,宽带,天然气,车位,新风系统,地暖', 3, 1, NULL, 0, 3.60, 1, NULL, 0.72, 0.38, '2026-01-06 09:10:00', '顶楼大平层，全屋地暖，家电全新，视野无遮挡。'),
(10, 5, 'FW20260915100010', '鹿鸣苑',   '鹿鸣社区', '5', '706',  '两室一厅', 85.00,  '西南',  7, 15, '精装修',   3050.00, 3050.00, '押一付一', '空调,热水器,洗衣机,冰箱',           0, 0, NULL, 0, 3.40, 0, 0.56, NULL, NULL, NULL, '刚装修完，配套齐全，待管理员审核后上架。'),
(11, 1, 'FW20250902100011', '望江台',   '望江社区', '3', '1101', '两室一厅', 80.00,  '东',   11, 18, '简装修',   3000.00, 3000.00, '押一付一', '空调,热水器,洗衣机,暖气',           0, 1, NULL, 0, 3.40, 0, 0.58, NULL, NULL, '2025-09-02 10:00:00', '房东自住中，暂不对外出租。'),
(12, 2, 'FW20260910100012', '梧桐里',   '梧桐社区', '2', '909',  '一室一厅', 56.00,  '北',    9, 16, '简装修',   2600.00, 2600.00, '押一付一', '空调,热水器,洗衣机',                 0, 2, '房屋照片模糊且与实际不符，请重新上传实拍照片后再提交审核', 0, 3.20, 0, 0.56, NULL, NULL, NULL, '朝北一居，价格可谈。');

-- ========== 4.1 房源图片 ==========
-- image_type：0-实景图 1-户型图；sort 数值越小越靠前（实景图与户型图各自从 0 开始）
-- 只给「上架」的房源配图（id 1/2/3/5/6/7），下架/待审核/已出租的不配。
--
-- 这里的 URL 是**相对路径**，指向 H5 前端的 public 目录（h5/public/demo/*.svg），
-- 由 Vite 开发服务器（8081）直接提供，因此不需要联网、也不依赖后端文件存储配置。
-- 前端 h5/src/utils/image.js 的 resolveImageUrl 会把「/ 开头」的地址原样返回。
-- 真实上传的图片走 infra 文件服务，存的是完整 URL（http://host/admin-api/infra/file/...），
-- 两种形态详情页都能渲染。
INSERT INTO `rental_house_image` (`id`, `house_id`, `image_url`, `sort`, `image_type`) VALUES
-- 1 号房：云栖花园 4栋 1502（两室一厅）
(1,  1, '/demo/living-1.svg',    0, 0),
(2,  1, '/demo/bedroom.svg',     1, 0),
(3,  1, '/demo/layout-2ldk.svg', 0, 1),
-- 2 号房：云栖花园 4栋 806（一室一厅）
(4,  2, '/demo/living-2.svg',    0, 0),
(5,  2, '/demo/layout-1ldk.svg', 0, 1),
-- 3 号房：望江台 2栋 1203（两室一厅）
(6,  3, '/demo/living-1.svg',    0, 0),
(7,  3, '/demo/bedroom.svg',     1, 0),
(8,  3, '/demo/layout-2ldk.svg', 0, 1),
-- 5 号房：梧桐里 7栋 501（一室一厅）
(9,  5, '/demo/living-2.svg',    0, 0),
(10, 5, '/demo/layout-1ldk.svg', 0, 1),
-- 6 号房：梧桐里 7栋 1608（两室两厅）
(11, 6, '/demo/living-1.svg',    0, 0),
(12, 6, '/demo/bedroom.svg',     1, 0),
(13, 6, '/demo/layout-2ldk.svg', 0, 1),
-- 7 号房：鹿鸣苑 1栋 302（一室一厅）
(14, 7, '/demo/living-2.svg',    0, 0),
(15, 7, '/demo/layout-1ldk.svg', 0, 1);

-- ========== 5. 租房申请 ==========
-- status：0-待审批 1-已通过(房东同意，待签约) 2-已驳回 3-已签约 4-已超时/已失效
-- approver_id 是**房东** rental_owner_info.id（申请审批权已从管理员收归房东）
INSERT INTO `rental_apply` (`id`, `apply_no`, `house_id`, `tenant_user_id`, `move_in_date`, `lease_term`, `payment_method`, `monthly_rent`, `deposit_amount`, `tenant_remark`, `status`, `approver_id`, `approve_time`, `reject_reason`, `contract_id`, `timeout_reason`) VALUES
(1, 'SQ202512201000011234', 4, 1, '2026-01-01', 12, '押一付三', 4600.00, 4600.00, '希望1月初入住，长租',        3, 2, '2025-12-21 09:30:00', NULL, 1, NULL),
(2, 'SQ202605201000025678', 8, 2, '2026-06-01', 12, '押一付一', 4800.00, 4800.00, '需要固定停车位',            3, 4, '2026-05-21 14:20:00', NULL, 2, NULL),
(3, 'SQ202609141000039012', 6, 3, '2026-10-01', 12, '押一付三', 3700.00, 3700.00, '公司在附近，希望尽快入住',  1, 3, '2026-09-15 10:05:00', NULL, 3, NULL),
(4, 'SQ202609161000043456', 1, 4, '2026-10-01', 12, '押一付三', 3600.00, 3600.00, '想先看房再决定',            0, NULL, NULL, NULL, NULL, NULL),
(5, 'SQ202609101000057890', 5, 5, '2026-09-20', 12, '押二付一', 2400.00, 4800.00, '养了一只猫',                2, 3, '2026-09-11 16:40:00', '该房源不接受饲养宠物，抱歉', NULL, NULL),
(6, 'SQ202609051000061234', 3, 2, '2026-09-15', 12, '押一付三', 3100.00, 3100.00, NULL,                        0, NULL, NULL, NULL, NULL, NULL),
(7, 'SQ202512221000075678', 4, 6, '2026-01-05', 12, '押一付三', 4600.00, 4600.00, NULL,                        4, NULL, NULL, NULL, NULL, '该房源已被其他租客承租，申请自动失效'),
(8, 'SQ202508151000089012', 7, 6, '2025-09-01', 12, '押一付一', 2100.00, 2100.00, '长租，至少一年',            3, 4, '2025-08-16 11:00:00', NULL, 4, NULL);

-- ========== 6. 合同 ==========
-- status：0-待签署 1-待缴费 2-生效中 3-即将到期 4-退租处理中 5-已到期 6-已退租 7-已取消
INSERT INTO `rental_contract` (`id`, `contract_no`, `house_id`, `tenant_user_id`, `rent_start_date`, `rent_end_date`, `monthly_rent`, `deposit_amount`, `payment_method`, `property_fee_unit`, `status`, `sign_time`, `owner_sign_time`, `source_apply_id`, `template_id`, `content`) VALUES
(1, 'HT-20251221100001-1', 4, 1, '2026-01-01', '2026-12-31', 4600.00, 4600.00, '押一付三', 2.60, 2, '2025-12-25 10:00:00', '2025-12-25 09:30:00', 1, 1, '房屋租赁合同：出租方何丽云，承租方陈嘉禾，租赁期限2026-01-01至2026-12-31，月租金4600元，押金4600元，付款方式押一付三。'),
(2, 'HT-20260521100002-2', 8, 2, '2026-06-01', '2027-05-31', 4800.00, 4800.00, '押一付一', 2.90, 2, '2026-05-25 15:00:00', '2026-05-25 14:30:00', 2, 1, '房屋租赁合同：出租方徐婉如，承租方林晓彤，租赁期限2026-06-01至2027-05-31，月租金4800元，押金4800元，付款方式押一付一。'),
(3, 'HT-20260915100003-3', 6, 3, '2026-10-01', '2027-09-30', 3700.00, 3700.00, '押一付三', 2.60, 0, NULL, NULL, 3, 1, '房屋租赁合同：出租方周振华，承租方高鹏，租赁期限2026-10-01至2027-09-30，月租金3700元，押金3700元，付款方式押一付三。尚未签署。'),
(4, 'HT-20250816100004-8', 7, 6, '2025-09-01', '2026-08-31', 2100.00, 2100.00, '押一付一', 2.10, 6, '2025-08-20 09:00:00', '2025-08-20 08:40:00', 8, 1, '房屋租赁合同：出租方徐婉如，承租方白露，租赁期限2025-09-01至2026-08-31，月租金2100元，押金2100元。合同已到期退租。'),
(5, 'HT-20251010100005', 9, 4, '2025-10-15', '2026-10-14', 5200.00, 10400.00, '押二付一', 3.10, 3, '2025-10-10 16:00:00', '2025-10-10 15:30:00', NULL, 1, '房屋租赁合同：出租方吴宇杰，承租方苏婉清，租赁期限2025-10-15至2026-10-14，月租金5200元，押金10400元，付款方式押二付一。即将到期。');

-- ========== 7. 租金/物业费账单 ==========
-- bill_type：0-首期账单 1-周期账单 ；pay_status：0-待缴费 1-已缴费 2-已逾期
-- 首期账单 = 付数 × 月租金 + 押金，账期也是付数个月（押一付三 → 3 个月）
-- 周期账单 = 当月租金 + 当月物业费
-- 注意：本表**没有 owner_id**，业主端「我的账单」靠 house_id 反查房源归属
INSERT INTO `rental_rent_bill` (`id`, `bill_no`, `contract_id`, `tenant_user_id`, `house_id`, `bill_type`, `period_start`, `period_end`, `rent_amount`, `deposit_amount`, `property_fee_amount`, `total_amount`, `paid_amount`, `pay_status`, `due_date`, `pay_time`, `pay_method`, `payee`, `fee_detail`, `late_fee`, `late_fee_waived`, `late_fee_waive_reason`) VALUES
(1, 'ZD-20251225100001-1', 1, 1, 4, 0, '2026-01-01', '2026-03-31',13800.00, 4600.00,   0.00, 18400.00,18400.00, 1, '2026-01-01', '2025-12-26 10:00:00', '银行转账', 2, '首期：3个月租金13800.00 + 押金4600.00（押一付三）', 0.00, 0.00, NULL),
(2, 'ZD-20260901100002-1', 1, 1, 4, 1, '2026-09-01', '2026-09-30', 4600.00,    0.00, 260.00,  4860.00, 4860.00, 1, '2026-09-05', '2026-09-02 09:12:00', '微信',     2, '租金4600.00 + 物业费260.00',        0.00, 0.00, NULL),
(3, 'ZD-20260901100003-1', 1, 1, 4, 1, '2026-10-01', '2026-10-31', 4600.00,    0.00, 260.00,  4860.00,    0.00, 0, '2026-10-05', NULL, NULL, NULL, '租金4600.00 + 物业费260.00', 0.00, 0.00, NULL),
(4, 'ZD-20260525100004-2', 2, 2, 8, 0, '2026-06-01', '2026-06-30', 4800.00, 4800.00,   0.00,  9600.00, 9600.00, 1, '2026-06-01', '2026-05-28 14:00:00', '支付宝',   4, '首期：首月租金4800.00 + 押金4800.00', 0.00, 0.00, NULL),
(5, 'ZD-20260801100005-2', 2, 2, 8, 1, '2026-08-01', '2026-08-31', 4800.00,    0.00, 290.00,  5090.00,    0.00, 2, '2026-08-05', NULL, NULL, NULL, '租金4800.00 + 物业费290.00',       50.90, 0.00, NULL),
(6, 'ZD-20260901100006-2', 2, 2, 8, 1, '2026-10-01', '2026-10-31', 4800.00,    0.00, 290.00,  5090.00,    0.00, 0, '2026-10-05', NULL, NULL, NULL, '租金4800.00 + 物业费290.00',        0.00, 0.00, NULL),
(7, 'ZD-20260915100007-3', 3, 3, 6, 0, '2026-10-01', '2026-12-31',11100.00, 3700.00,   0.00, 14800.00,    0.00, 0, '2026-09-24', NULL, NULL, NULL, '首期：3个月租金11100.00 + 押金3700.00（押一付三，合同待签署）', 0.00, 0.00, NULL),
(8, 'ZD-20250915100008-5', 5, 4, 9, 0, '2025-10-15', '2025-11-14', 5200.00,10400.00,   0.00, 15600.00,15600.00, 1, '2025-10-15', '2025-10-12 11:00:00', '银行转账', 5, '首期：首月租金5200.00 + 押金10400.00', 0.00, 0.00, NULL),
(9, 'ZD-20260915100009-5', 5, 4, 9, 1, '2026-09-15', '2026-10-14', 5200.00,    0.00, 310.00,  5510.00,    0.00, 2, '2026-09-15', NULL, NULL, NULL, '租金5200.00 + 物业费310.00',       55.10, 0.00, NULL);

-- ========== 8. 抄表记录 ==========
-- meter_type：0-水表 1-电表 ；status：0-正常 1-异常 2-已作废 ；review_status：0-待审核 1-已通过 2-已驳回
-- 注意：水电费账单必须挂在抄表记录上（utility_bill.meter_reading_id 为 NOT NULL），
--       所以下面 bill_id 非空的记录才有对应水电账单，第 3 条待审核、尚未出账
INSERT INTO `rental_meter_reading` (`id`, `owner_id`, `house_id`, `contract_id`, `meter_type`, `last_reading`, `current_reading`, `valley_reading`, `last_valley_reading`, `usage_amount`, `valley_usage`, `unit_price`, `peak_price`, `valley_price`, `fee_amount`, `reading_date`, `operator_id`, `bill_id`, `status`, `images`, `review_status`, `review_reason`, `reviewer_id`, `review_time`) VALUES
(1, 2, 4, 1, 0,  120.00,  138.50, NULL,   NULL,  18.50, NULL,   3.60, NULL, NULL,  66.60, '2026-09-01', 2, 1, 0, NULL, 1, NULL, 1, '2026-09-01 15:00:00'),
(2, 2, 4, 1, 1, 2200.00, 2456.00, 2600.00, 2500.00, 256.00, 100.00, 0.72, 0.72, 0.38, 153.92, '2026-09-01', 2, 2, 0, NULL, 1, NULL, 1, '2026-09-01 15:05:00'),
(3, 4, 8, 2, 1, 3100.00, 3390.00, NULL,   NULL, 290.00, NULL,   0.56, NULL, NULL, 162.40, '2026-09-10', 4, NULL, 0, NULL, 0, NULL, NULL, NULL),
(4, 4, 7, 4, 0,   88.00,   96.50, NULL,   NULL,   8.50, NULL,   2.90, NULL, NULL,  24.65, '2026-08-25', 4, 3, 0, NULL, 1, NULL, 1, '2026-08-25 16:00:00');

-- ========== 9. 水电费账单 ==========
-- fee_type：0-水费 1-电费 2-水电合并 ；pay_status：0-待缴费 1-已缴费 2-已逾期
INSERT INTO `rental_utility_bill` (`id`, `bill_no`, `contract_id`, `tenant_user_id`, `house_id`, `meter_reading_id`, `fee_type`, `water_amount`, `electricity_amount`, `total_amount`, `pay_status`, `due_date`, `pay_time`, `pay_method`, `payee`) VALUES
(1, 'SD-20260901100001', 1, 1, 4, 1, 0,  66.60,   0.00,  66.60, 1, '2026-09-10', '2026-09-08 11:00:00', '支付宝', 2),
(2, 'SD-20260901100002', 1, 1, 4, 2, 1,   0.00, 153.92, 153.92, 0, '2026-09-20', NULL, NULL, NULL),
(3, 'SD-20260825100003', 4, 6, 7, 4, 0,  24.65,   0.00,  24.65, 1, '2026-08-31', '2026-08-28 09:30:00', '现金',   4);

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
-- status：0-待处理 1-已处理 2-已驳回 ；move_out_type：0-到期退租 1-提前退租
-- inspection_result：0-验收通过 1-轻微损坏 2-严重损坏
-- handler_type：0-管理员 1-业主。退租处理权已收归业主，所以下面两行的 handler_id 是
--   rental_owner_info.id 且 handler_type=1 —— 漏填 handler_type 会让管理端把它当成管理员 id
INSERT INTO `rental_move_out_application` (`id`, `apply_no`, `contract_id`, `tenant_user_id`, `house_id`, `move_out_type`, `move_out_reason`, `expected_move_out_date`, `settlement_id`, `status`, `handler_id`, `handler_type`, `handle_time`, `inspection_result`, `repair_fee`, `repair_fee_desc`, `deposit_handle`, `remaining_rent`, `deduction_amount`, `refund_or_pay`) VALUES
(1, 'TZ20260720100001', 4, 6, 7, 0, '合同到期，不再续租', '2026-08-31', 1, 1, 4, 1, '2026-08-31 10:00:00', 0, 0.00, NULL, '扣除欠费后退还', 0.00, 24.65, 2075.35),
(2, 'TZ20260910100002', 5, 4, 9, 1, '工作调动到外地，需提前退租', '2026-10-14', NULL, 0, NULL, 0, NULL, NULL, 0.00, NULL, NULL, 0.00, 0.00, NULL);

-- ========== 13. 退租结算单 ==========
-- handler_type 同上：结算单的处理人也可能是业主
INSERT INTO `rental_settlement_bill` (`id`, `settlement_no`, `contract_id`, `house_id`, `tenant_user_id`, `move_out_application_id`, `move_out_type`, `deposit_amount`, `deposit_handle`, `remaining_rent`, `property_fee_arrears`, `utility_fee_arrears`, `repair_fee`, `deduction_amount`, `refund_or_pay`, `handler_id`, `handler_type`, `handle_time`) VALUES
(1, 'JS-20260831100000-4821', 4, 7, 6, 1, 0, 2100.00, '扣除欠费后退还', 0.00, 0.00, 24.65, 0.00, 24.65, 2075.35, 4, 1, '2026-08-31 10:00:00');

-- ========== 14. 缴费记录 ==========
-- bill_type：0-租金/物业费 1-水电费 2-退租结算 ；pay_method：现金/微信/支付宝/银行转账
-- payee 是收款业主的 rental_owner_info.id
INSERT INTO `rental_payment_record` (`id`, `bill_type`, `bill_id`, `pay_amount`, `pay_method`, `pay_time`, `payee`, `transaction_no`, `voucher_no`) VALUES
(1, 0, 1, 18400.00, '银行转账', '2025-12-26 10:00:00', 2, 'TXN2025122600001', 'SK20251226001'),
(2, 0, 8, 15600.00, '银行转账', '2025-10-12 11:00:00', 5, 'TXN2025101200001', 'SK20251012001'),
(3, 0, 4,  9600.00, '支付宝',   '2026-05-28 14:00:00', 4, 'TXN2026052800001', 'SK20260528001'),
(4, 1, 1,    66.60, '支付宝',   '2026-09-08 11:00:00', 2, 'TXN2026090800001', 'SK20260908001'),
(5, 0, 2,  4860.00, '微信',     '2026-09-02 09:12:00', 2, 'TXN2026090200001', 'SK20260902001'),
(6, 1, 3,    24.65, '现金',     '2026-08-28 09:30:00', 4, 'TXN2026082800001', 'SK20260828001');

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
(1, 'water_unit_price',       '3.60', '2026-01-01 00:00:00', 1),
(2, 'electricity_unit_price', '0.56', '2026-01-01 00:00:00', 1);

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
