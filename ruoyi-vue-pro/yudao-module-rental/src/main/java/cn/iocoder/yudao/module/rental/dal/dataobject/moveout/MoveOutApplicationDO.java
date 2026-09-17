package cn.iocoder.yudao.module.rental.dal.dataobject.moveout;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 退租申请表
 *
 * @author yudao
 */
@TableName("rental_move_out_application")
@Data
@EqualsAndHashCode(callSuper = true)
public class MoveOutApplicationDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 申请编号，规则：TZ + 时间戳 + 随机数
     *
     * 数据库上是 NOT NULL + 唯一键 uk_apply_no，所以每条都必须生成：
     * 不生成的话所有行都会落到默认值 ''，第二条起直接撞唯一键报 500
     */
    private String applyNo;
    /**
     * 合同 ID，关联 rental_contract
     */
    private Long contractId;
    /**
     * 租客 ID，关联 rental_tenant_info
     */
    private Long tenantUserId;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 退租类型：0-到期退租，1-提前退租
     */
    private Integer moveOutType;
    /**
     * 退租原因说明
     */
    private String moveOutReason;
    /**
     * 预计退租日期
     */
    private LocalDate expectedMoveOutDate;
    /**
     * 关联退租结算单 ID，关联 rental_settlement_bill
     */
    private Long settlementId;
    /**
     * 状态：0-待处理，1-已处理
     */
    private Integer status;
    /**
     * 处理人 ID，关联 sys_user
     */
    private Long handlerId;
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    /**
     * 房屋验收结果：0-验收通过，1-轻微损坏，2-严重损坏
     *
     * 库里是 TINYINT。曾经这里写成 String、管理端传中文「通过」，
     * 在严格模式下插入会直接报 1366 Incorrect integer value
     */
    private Integer inspectionResult;
    /**
     * 损坏维修费用（管理员手动录入）
     */
    private BigDecimal repairFee;
    /**
     * 维修费用明细说明
     */
    private String repairFeeDesc;
    /**
     * 管理员备注
     */
    private String remark;
    /**
     * 押金处理方式：不退（违约金）/扣除欠费后退还
     */
    private String depositHandle;
    /**
     * 剩余租金（提前退租时已预缴未使用的租金）
     */
    private BigDecimal remainingRent;
    /**
     * 欠费抵扣金额（物业费+水电费+维修费）
     */
    private BigDecimal deductionAmount;
    /**
     * 退还/补缴金额，正数=退还，负数=补缴
     */
    private BigDecimal refundOrPay;

}
