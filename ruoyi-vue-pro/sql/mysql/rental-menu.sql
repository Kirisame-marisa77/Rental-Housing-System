-- =====================================================
-- 社区房屋租赁管理系统 - 菜单种子数据
-- 说明：超级管理员（角色 code=super_admin）自动拥有全部菜单与权限，
--       本脚本只需插入 system_menu 即可，无需配置 system_role_menu。
-- =====================================================

-- 一级目录：租赁管理
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20000, '租赁管理', '', 1, 30, 0, '/rental', 'ep:house', NULL, NULL, 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 二级菜单：房源管理
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20010, '房源管理', 'rental:house:query', 2, 1, 20000, 'house', 'ep:office-building', 'rental/house/index', 'RentalHouse', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20020, '租客管理', 'rental:tenant:query', 2, 2, 20000, 'tenant', 'ep:user', 'rental/tenant/index', 'RentalTenant', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20030, '合同管理', 'rental:contract:query', 2, 3, 20000, 'contract', 'ep:document', 'rental/contract/index', 'RentalContract', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
-- 注：原 (20040, '申请审批', ...) 已移除 —— 租房申请改由业主端「租房申请」审批，
--     管理端不再提供该菜单。已有环境请执行 rental-apply-owner.sql 清理
(20050, '账单管理', 'rental:bill:query', 2, 5, 20000, 'bill', 'ep:money', 'rental/bill/index', 'RentalBill', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20011, '房源新增', 'rental:house:create', 3, 1, 20010, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20012, '房源修改', 'rental:house:update', 3, 2, 20010, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20013, '房源删除', 'rental:house:delete', 3, 3, 20010, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20021, '租客新增', 'rental:tenant:create', 3, 1, 20020, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20022, '租客修改', 'rental:tenant:update', 3, 2, 20020, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20023, '租客删除', 'rental:tenant:delete', 3, 3, 20020, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20031, '合同新增', 'rental:contract:create', 3, 1, 20030, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20032, '合同修改', 'rental:contract:update', 3, 2, 20030, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20033, '合同删除', 'rental:contract:delete', 3, 3, 20030, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20051, '账单新增', 'rental:bill:create', 3, 1, 20050, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20052, '账单修改', 'rental:bill:update', 3, 2, 20050, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20053, '账单删除', 'rental:bill:delete', 3, 3, 20050, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 二级菜单：抄表管理 / 水电费账单 / 缴费记录
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20060, '抄表管理', 'rental:meter:query', 2, 6, 20000, 'meter', 'ep:odometer', 'rental/meter/index', 'RentalMeter', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20070, '水电费账单', 'rental:bill:query', 2, 7, 20000, 'utility-bill', 'ep:data-line', 'rental/utilityBill/index', 'RentalUtilityBill', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20080, '缴费记录', 'rental:bill:query', 2, 8, 20000, 'payment-record', 'ep:list', 'rental/paymentRecord/index', 'RentalPaymentRecord', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 抄表按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20061, '抄表新增', 'rental:meter:create', 3, 1, 20060, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20062, '抄表修改', 'rental:meter:update', 3, 2, 20060, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20063, '抄表删除', 'rental:meter:delete', 3, 3, 20060, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 二级菜单：退租管理 / 退租结算
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20090, '退租管理', 'rental:moveout:query', 2, 9, 20000, 'move-out', 'ep:switch-button', 'rental/moveOut/index', 'RentalMoveOut', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20100, '退租结算', 'rental:moveout:query', 2, 10, 20000, 'settlement-bill', 'ep:finished', 'rental/settlementBill/index', 'RentalSettlementBill', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 退租按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20091, '退租新增', 'rental:moveout:create', 3, 1, 20090, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20092, '退租修改', 'rental:moveout:update', 3, 2, 20090, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20093, '退租删除', 'rental:moveout:delete', 3, 3, 20090, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 二级菜单：维修工单 / 公告管理
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20110, '维修工单', 'rental:repair:query', 2, 11, 20000, 'repair-order', 'ep:tools', 'rental/repairOrder/index', 'RentalRepairOrder', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20120, '公告管理', 'rental:announcement:query', 2, 12, 20000, 'announcement', 'ep:bell', 'rental/announcement/index', 'RentalAnnouncement', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 维修工单按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20111, '工单新增', 'rental:repair:create', 3, 1, 20110, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20112, '工单修改', 'rental:repair:update', 3, 2, 20110, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20113, '工单删除', 'rental:repair:delete', 3, 3, 20110, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 公告按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20121, '公告新增', 'rental:announcement:create', 3, 1, 20120, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20122, '公告修改', 'rental:announcement:update', 3, 2, 20120, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20123, '公告删除', 'rental:announcement:delete', 3, 3, 20120, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 二级菜单：首页看板
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20130, '首页看板', 'rental:dashboard:query', 2, 0, 20000, 'dashboard', 'ep:data-analysis', 'rental/dashboard/index', 'RentalDashboard', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 二级菜单：看房预约（管理端只读监管；预约由租客发起、房东在业主端确认完成）
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20150, '看房预约', 'rental:viewing-appointment:query', 2, 14, 20000, 'viewing-appointment', 'ep:calendar', 'rental/viewing/index', 'RentalViewing', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 看房预约按钮权限（只读，仅一个查询按钮）
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20151, '预约查询', 'rental:viewing-appointment:query', 3, 1, 20150, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');
