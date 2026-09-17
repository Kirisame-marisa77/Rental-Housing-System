package cn.iocoder.yudao.module.rental.dal.dataobject.bill;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租金/物业费账单表
 *
 * @author yudao
 */
@TableName("rental_rent_bill")
@Data
@EqualsAndHashCode(callSuper = true)
public class RentBillDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 账单编号，规则：ZD-年月-流水号
     */
    private String billNo;
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
     * 账单类型：0-首期账单，1-周期账单
     */
    private Integer billType;
    /**
     * 费用周期开始日期
     */
    private LocalDate periodStart;
    /**
     * 费用周期结束日期
     */
    private LocalDate periodEnd;
    /**
     * 本期租金（元）
     */
    private BigDecimal rentAmount;
    /**
     * 押金金额（元），仅首期账单使用
     */
    private BigDecimal depositAmount;
    /**
     * 本期物业费（元）
     */
    private BigDecimal propertyFeeAmount;
    /**
     * 应缴总金额（元）
     */
    private BigDecimal totalAmount;
    /**
     * 已缴金额（元）
     */
    private BigDecimal paidAmount;
    /**
     * 缴费状态：0-待缴费，1-已缴费，2-已逾期
     */
    private Integer payStatus;
    /**
     * 缴费截止日期
     */
    private LocalDate dueDate;
    /**
     * 最近一次缴费时间
     */
    private LocalDateTime payTime;
    /**
     * 缴费方式：现金/微信/支付宝/银行转账
     */
    private String payMethod;
    /**
     * 收款人 ID，关联 sys_user
     */
    private Long payee;
    /**
     * 费用明细，JSON 格式
     */
    private String feeDetail;
    /**
     * 滞纳金（元）
     */
    private BigDecimal lateFee;
    /**
     * 减免滞纳金（元）
     */
    private BigDecimal lateFeeWaived;
    /**
     * 滞纳金减免原因
     */
    private String lateFeeWaiveReason;

}
