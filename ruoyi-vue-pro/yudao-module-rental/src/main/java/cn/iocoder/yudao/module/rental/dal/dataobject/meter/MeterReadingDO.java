package cn.iocoder.yudao.module.rental.dal.dataobject.meter;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 抄表记录表
 *
 * @author yudao
 */
@TableName("rental_meter_reading")
@Data
@EqualsAndHashCode(callSuper = true)
public class MeterReadingDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 上传业主 ID，关联 rental_owner_info
     */
    private Long ownerId;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 合同 ID，关联 rental_contract
     */
    private Long contractId;
    /**
     * 抄表类型：0-水表，1-电表
     */
    private Integer meterType;
    /**
     * 上期读数
     */
    private BigDecimal lastReading;
    /**
     * 本期读数
     */
    private BigDecimal currentReading;
    /**
     * 电表谷段本期读数
     */
    private BigDecimal valleyReading;
    /**
     * 电表谷段上期读数
     */
    private BigDecimal lastValleyReading;
    /**
     * 用量（本期读数 - 上期读数）
     */
    private BigDecimal usageAmount;
    /**
     * 电表谷段用量
     */
    private BigDecimal valleyUsage;
    /**
     * 单价（元/吨 或 元/度）
     */
    private BigDecimal unitPrice;
    /**
     * 电费峰段单价
     */
    private BigDecimal peakPrice;
    /**
     * 电费谷段单价
     */
    private BigDecimal valleyPrice;
    /**
     * 费用金额（用量 × 单价）
     */
    private BigDecimal feeAmount;
    /**
     * 抄表日期
     */
    private LocalDate readingDate;
    /**
     * 录入人 ID，关联 sys_user
     */
    private Long operatorId;
    /**
     * 关联生成的水电费账单 ID，关联 rental_utility_bill
     */
    private Long billId;
    /**
     * 状态：0-正常，1-异常，2-已作废
     */
    private Integer status;
    /**
     * 备注（异常说明等）
     */
    private String remark;
    /**
     * 抄表截图，JSON 数组（水 1 张 / 电峰谷各 1 张）
     */
    private String images;
    /**
     * 审核状态：0-待审核 1-已通过 2-已驳回
     */
    private Integer reviewStatus;
    /**
     * 审核驳回原因
     */
    private String reviewReason;
    /**
     * 审核人 ID，关联 sys_user
     */
    private Long reviewerId;
    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

}
