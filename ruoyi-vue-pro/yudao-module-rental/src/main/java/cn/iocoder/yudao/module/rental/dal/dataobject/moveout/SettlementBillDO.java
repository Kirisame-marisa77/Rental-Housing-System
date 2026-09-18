package cn.iocoder.yudao.module.rental.dal.dataobject.moveout;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退租结算单表
 *
 * @author yudao
 */
@TableName("rental_settlement_bill")
@Data
@EqualsAndHashCode(callSuper = true)
public class SettlementBillDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 结算单号，规则：JS-年月-流水号
     */
    private String settlementNo;
    /**
     * 合同 ID，关联 rental_contract
     */
    private Long contractId;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 租客 ID，关联 rental_tenant_info
     */
    private Long tenantUserId;
    /**
     * 退租申请 ID，关联 rental_move_out_application
     */
    private Long moveOutApplicationId;
    /**
     * 退租类型：0-到期退租，1-提前退租
     */
    private Integer moveOutType;
    /**
     * 押金金额（元）
     */
    private BigDecimal depositAmount;
    /**
     * 押金处理方式：不退（违约金）/扣除欠费后退还
     */
    private String depositHandle;
    /**
     * 剩余租金（元）
     */
    private BigDecimal remainingRent;
    /**
     * 物业费欠费（元）
     */
    private BigDecimal propertyFeeArrears;
    /**
     * 水电费欠费（元）
     */
    private BigDecimal utilityFeeArrears;
    /**
     * 维修费金额（元）
     */
    private BigDecimal repairFee;
    /**
     * 欠费抵扣金额（元）
     */
    private BigDecimal deductionAmount;
    /**
     * 退还/补缴金额，正数=退还，负数=补缴
     */
    private BigDecimal refundOrPay;
    /**
     * 处理人 ID
     *
     * handlerType=0 时关联 sys_user（管理员），=1 时关联 rental_owner_info（业主）
     */
    private Long handlerId;
    /**
     * 处理人类型：0-管理员，1-业主
     */
    private Integer handlerType;
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;
    /**
     * 备注
     */
    private String remark;

}
