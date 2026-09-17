package cn.iocoder.yudao.module.rental.dal.dataobject.bill;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 水电费账单表
 *
 * @author yudao
 */
@TableName("rental_utility_bill")
@Data
@EqualsAndHashCode(callSuper = true)
public class UtilityBillDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 账单编号，规则：SD-年月-流水号
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
     * 抄表记录 ID，关联 rental_meter_reading
     */
    private Long meterReadingId;
    /**
     * 费用类型：0-水费，1-电费，2-水+电
     */
    private Integer feeType;
    /**
     * 水费金额（元）
     */
    private BigDecimal waterAmount;
    /**
     * 电费金额（元）
     */
    private BigDecimal electricityAmount;
    /**
     * 应缴总金额（元）
     */
    private BigDecimal totalAmount;
    /**
     * 缴费状态：0-待缴费，1-已缴费，2-已逾期
     */
    private Integer payStatus;
    /**
     * 缴费截止日期
     */
    private LocalDate dueDate;
    /**
     * 缴费时间
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

}
