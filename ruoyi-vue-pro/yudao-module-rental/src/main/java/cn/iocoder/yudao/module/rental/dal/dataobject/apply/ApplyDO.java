package cn.iocoder.yudao.module.rental.dal.dataobject.apply;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租房申请记录表
 *
 * @author yudao
 */
@TableName("rental_apply")
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 申请编号，规则：SQ+时间戳+随机数
     */
    private String applyNo;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 租客 ID，关联 rental_tenant_info
     */
    private Long tenantUserId;
    /**
     * 期望入住日期
     */
    private LocalDate moveInDate;
    /**
     * 租期（月）
     */
    private Integer leaseTerm;
    /**
     * 付款方式：押一付一/押一付三/押二付一/押二付三/自定义
     */
    private String paymentMethod;
    /**
     * 签约月租金（元）
     */
    private BigDecimal monthlyRent;
    /**
     * 押金金额（元）
     */
    private BigDecimal depositAmount;
    /**
     * 租客备注/留言
     */
    private String tenantRemark;
    /**
     * 状态：0-待审批，1-已通过，2-已驳回，3-已签约，4-已超时
     */
    private Integer status;
    /**
     * 审批人 ID：房东审批时为 rental_owner_info.id
     */
    private Long approverId;
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    /**
     * 驳回原因
     */
    private String rejectReason;
    /**
     * 通过后自动生成的合同 ID，关联 rental_contract
     */
    private Long contractId;
    /**
     * 失效原因：超时未签署/未缴费，或房源已被其他租客承租（此时状态为 4-已超时，前端显示「已失效」）
     */
    private String timeoutReason;

}
