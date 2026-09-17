package cn.iocoder.yudao.module.rental.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * rental 模块错误码枚举类
 *
 * rental 系统，使用 1-120-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 房源 1-120-001-000 ==========
    ErrorCode HOUSE_NOT_EXISTS = new ErrorCode(1_120_001_001, "房源不存在");

    // ========== 租客 1-120-002-000 ==========
    ErrorCode TENANT_INFO_NOT_EXISTS = new ErrorCode(1_120_002_001, "租客不存在");
    ErrorCode TENANT_INFO_ID_CARD_DUPLICATE = new ErrorCode(1_120_002_002, "身份证号已存在");
    ErrorCode TENANT_PHONE_DUPLICATE = new ErrorCode(1_120_002_003, "手机号已存在");
    ErrorCode TENANT_LOGIN_FAILED = new ErrorCode(1_120_002_004, "手机号或密码错误");
    ErrorCode TENANT_NOT_LOGIN = new ErrorCode(1_120_002_005, "未登录或登录已过期");
    ErrorCode TENANT_OLD_PASSWORD_ERROR = new ErrorCode(1_120_002_006, "旧密码错误");

    // ========== 租房申请 1-120-003-000 ==========
    ErrorCode APPLY_NOT_EXISTS = new ErrorCode(1_120_003_001, "租房申请不存在");
    ErrorCode APPLY_ALREADY_PROCESSED = new ErrorCode(1_120_003_002, "申请已审批，不能重复操作");
    ErrorCode APPLY_HOUSE_NOT_AVAILABLE = new ErrorCode(1_120_003_003, "房源当前不可申请");
    ErrorCode APPLY_DUPLICATE_PENDING = new ErrorCode(1_120_003_004, "您已提交过该房源的申请，请等待房东处理");
    ErrorCode APPLY_NOT_OWNER = new ErrorCode(1_120_003_005, "无权操作该申请（非本房源的房东）");
    ErrorCode APPLY_REJECT_REASON_REQUIRED = new ErrorCode(1_120_003_006, "请填写驳回原因");

    // ========== 合同 1-120-004-000 ==========
    ErrorCode CONTRACT_NOT_EXISTS = new ErrorCode(1_120_004_001, "合同不存在");
    ErrorCode CONTRACT_HOUSE_TAKEN = new ErrorCode(1_120_004_002, "该房源已被其他租客承租，本合同已自动取消");
    ErrorCode CONTRACT_FIRST_BILL_UNPAID = new ErrorCode(1_120_004_003, "请先缴纳首期账单后再确认签约");
    ErrorCode CONTRACT_CANCELLED = new ErrorCode(1_120_004_004, "该合同已取消，无法签署");

    // ========== 账单 1-120-005-000 ==========
    ErrorCode RENT_BILL_NOT_EXISTS = new ErrorCode(1_120_005_001, "租金账单不存在");
    ErrorCode RENT_BILL_ALREADY_PAID = new ErrorCode(1_120_005_002, "该账单已缴费");
    ErrorCode RENT_BILL_NOT_OWNER = new ErrorCode(1_120_005_003, "无权操作该账单");

    // ========== 缴费记录 1-120-006-000 ==========
    ErrorCode PAYMENT_RECORD_NOT_EXISTS = new ErrorCode(1_120_006_001, "缴费记录不存在");

    // ========== 退租 1-120-007-000 ==========
    ErrorCode MOVE_OUT_APPLICATION_NOT_EXISTS = new ErrorCode(1_120_007_001, "退租申请不存在");
    ErrorCode SETTLEMENT_BILL_NOT_EXISTS = new ErrorCode(1_120_007_002, "退租结算单不存在");
    ErrorCode MOVE_OUT_APPLICATION_HANDLED = new ErrorCode(1_120_007_003, "退租申请已处理");
    ErrorCode MOVE_OUT_CONTRACT_NOT_TENANT = new ErrorCode(1_120_007_004, "该合同不属于当前租客");
    ErrorCode MOVE_OUT_CONTRACT_STATUS_INVALID = new ErrorCode(1_120_007_005, "该合同当前状态不可申请退租（仅生效中/即将到期的合同可退租）");
    ErrorCode MOVE_OUT_APPLICATION_DUPLICATE = new ErrorCode(1_120_007_006, "该合同已有进行中的退租申请，不能重复提交");

    // ========== 抄表 1-120-008-000 ==========
    ErrorCode METER_READING_NOT_EXISTS = new ErrorCode(1_120_008_001, "抄表记录不存在");
    ErrorCode METER_CONFIG_NOT_EXISTS = new ErrorCode(1_120_008_002, "抄表配置不存在");
    ErrorCode METER_READING_INVALID = new ErrorCode(1_120_008_003, "本期读数不能小于上期读数");
    ErrorCode METER_READING_BILL_GENERATED = new ErrorCode(1_120_008_004, "该抄表记录已生成账单");

    // ========== 水电费账单 1-120-009-000 ==========
    ErrorCode UTILITY_BILL_NOT_EXISTS = new ErrorCode(1_120_009_001, "水电费账单不存在");

    // ========== 维修工单 1-120-010-000 ==========
    ErrorCode REPAIR_ORDER_NOT_EXISTS = new ErrorCode(1_120_010_001, "维修工单不存在");

    // ========== 公告 1-120-011-000 ==========
    ErrorCode ANNOUNCEMENT_NOT_EXISTS = new ErrorCode(1_120_011_001, "公告不存在");

    // ========== 预约看房 1-120-012-000 ==========
    ErrorCode VIEWING_APPOINTMENT_NOT_EXISTS = new ErrorCode(1_120_012_001, "预约看房记录不存在");
    ErrorCode VIEWING_APPOINTMENT_ALREADY_PROCESSED = new ErrorCode(1_120_012_002, "该预约已处理，不能重复操作");
    ErrorCode VIEWING_APPOINTMENT_HOUSE_NOT_AVAILABLE = new ErrorCode(1_120_012_003, "房源当前不可预约看房");
    ErrorCode VIEWING_APPOINTMENT_DATE_INVALID = new ErrorCode(1_120_012_004, "预约日期不能早于今天");
    ErrorCode VIEWING_APPOINTMENT_TIME_INVALID = new ErrorCode(1_120_012_005, "预约时间段格式不正确（HH:mm，且开始时间须早于结束时间）");
    ErrorCode VIEWING_APPOINTMENT_NOT_OWNER = new ErrorCode(1_120_012_006, "无权操作该预约（非本房源的房东）");
    ErrorCode VIEWING_APPOINTMENT_CANNOT_CANCEL = new ErrorCode(1_120_012_007, "该预约当前状态不可取消");
    ErrorCode VIEWING_APPOINTMENT_SLOT_TAKEN = new ErrorCode(1_120_012_008, "该看房时段已被预约，请另选时间");
    ErrorCode VIEWING_APPOINTMENT_DUPLICATE = new ErrorCode(1_120_012_009, "您已预约过该房源的同一时段");

    // ========== 房源收藏 1-120-013-000 ==========
    ErrorCode HOUSE_FAVORITE_NOT_EXISTS = new ErrorCode(1_120_013_001, "房源收藏不存在");

    // ========== 合同模板 1-120-014-000 ==========
    ErrorCode CONTRACT_TEMPLATE_NOT_EXISTS = new ErrorCode(1_120_014_001, "合同模板不存在");

    // ========== 业主 1-120-015-000 ==========
    ErrorCode OWNER_INFO_NOT_EXISTS = new ErrorCode(1_120_015_001, "业主不存在");
    ErrorCode OWNER_INFO_ID_CARD_DUPLICATE = new ErrorCode(1_120_015_002, "身份证号已存在");
    ErrorCode OWNER_INFO_PHONE_DUPLICATE = new ErrorCode(1_120_015_003, "手机号已存在");
    ErrorCode OWNER_LOGIN_FAILED = new ErrorCode(1_120_015_004, "手机号或密码错误");
    ErrorCode OWNER_NOT_LOGIN = new ErrorCode(1_120_015_005, "未登录或登录已过期");
    ErrorCode OWNER_OLD_PASSWORD_ERROR = new ErrorCode(1_120_015_006, "旧密码错误");

    // ========== 审核/判定相关 ==========
    ErrorCode HOUSE_ALREADY_REVIEWED = new ErrorCode(1_120_001_002, "房源已审核，不能重复审核");
    ErrorCode METER_READING_ALREADY_REVIEWED = new ErrorCode(1_120_008_005, "该抄表记录已审核");
    ErrorCode REPAIR_ORDER_ALREADY_HANDLED = new ErrorCode(1_120_010_002, "维修工单已处理，不能重复操作");

}
