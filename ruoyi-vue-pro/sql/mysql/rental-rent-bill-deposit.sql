-- =====================================================
-- 租金账单新增押金字段
-- 房东同意租房申请后会自动生成首期账单（押金 + 首月租金），需要存放押金金额
-- 执行：mysql -uroot -p ruoyi-vue-pro < rental-rent-bill-deposit.sql
-- 可重复执行（列已存在时改为 MODIFY 修注释，不会报 Duplicate column name）
-- =====================================================

-- 中文 Windows 控制台的 mysql 客户端默认 character_set_client=gbk，
-- 会把 UTF-8 的 SQL 文件读成乱码。强制按 utf8mb4 解析，否则注释会写坏
SET NAMES utf8mb4;

SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'rental_rent_bill'
                  AND COLUMN_NAME = 'deposit_amount');

SET @sql := IF(@exists = 0,
    'ALTER TABLE `rental_rent_bill` ADD COLUMN `deposit_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT ''押金金额（元），仅首期账单使用'' AFTER `rent_amount`',
    'ALTER TABLE `rental_rent_bill` MODIFY COLUMN `deposit_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT ''押金金额（元），仅首期账单使用''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
