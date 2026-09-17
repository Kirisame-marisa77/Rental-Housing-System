-- =====================================================
-- 社区房屋租赁管理系统 - 业主管理菜单种子数据
-- 说明：超级管理员（角色 code=super_admin）自动拥有全部菜单与权限
-- 执行：本脚本单独执行即可
-- =====================================================

-- 二级菜单：业主管理（挂在 20000 租赁管理 下）
-- 注意：SET NAMES utf8mb4 必须保留 —— 中文 Windows 的 mysql 客户端默认
--       character_set_client=gbk，去掉这行会把下面所有中文读成乱码。
SET NAMES utf8mb4;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20140, '业主管理', 'rental:owner:query', 2, 13, 20000, 'owner', 'ep:avatar', 'rental/owner/index', 'RentalOwner', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');

-- 业主按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(20141, '业主新增', 'rental:owner:create', 3, 1, 20140, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20142, '业主修改', 'rental:owner:update', 3, 2, 20140, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0'),
(20143, '业主删除', 'rental:owner:delete', 3, 3, 20140, '', '', '', '', 0, b'1', b'1', b'1', 'admin', NOW(), 'admin', NOW(), b'0');
