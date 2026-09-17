package cn.iocoder.yudao.module.rental.dal.dataobject.bill;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缴费记录表
 *
 * @author yudao
 */
@TableName("rental_payment_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentRecordDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 账单类型：0-租金/物业费，1-水电费，2-退租结算
     */
    private Integer billType;
    /**
     * 关联账单 ID
     */
    private Long billId;
    /**
     * 缴费金额（元）
     */
    private BigDecimal payAmount;
    /**
     * 缴费方式：现金/微信/支付宝/银行转账
     */
    private String payMethod;
    /**
     * 缴费日期
     */
    private LocalDateTime payTime;
    /**
     * 收款人 ID，关联 sys_user
     */
    private Long payee;
    /**
     * 交易流水号（在线支付）
     */
    private String transactionNo;
    /**
     * 缴费凭证编号
     */
    private String voucherNo;
    /**
     * 备注
     */
    private String remark;

}
