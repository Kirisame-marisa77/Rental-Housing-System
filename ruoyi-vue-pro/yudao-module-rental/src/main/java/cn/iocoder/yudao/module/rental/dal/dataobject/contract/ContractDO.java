package cn.iocoder.yudao.module.rental.dal.dataobject.contract;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租房合同表
 *
 * @author yudao
 */
@TableName("rental_contract")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 合同编号，规则：HT-年月-流水号
     */
    private String contractNo;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 租客 ID，关联 rental_tenant_info
     */
    private Long tenantUserId;
    /**
     * 租期开始日期
     */
    private LocalDate rentStartDate;
    /**
     * 租期结束日期
     */
    private LocalDate rentEndDate;
    /**
     * 签约月租金（元）
     */
    private BigDecimal monthlyRent;
    /**
     * 押金金额（元）
     */
    private BigDecimal depositAmount;
    /**
     * 付款方式：押一付一/押一付三/押二付一/押二付三/自定义
     */
    private String paymentMethod;
    /**
     * 物业费单价（元/㎡/月）
     */
    private BigDecimal propertyFeeUnit;
    /**
     * 合同状态：0-待签署，1-待缴费，2-生效中，3-即将到期，4-退租处理中，5-已到期，6-已退租，7-已取消
     */
    private Integer status;
    /**
     * 租客签署时间
     */
    private LocalDateTime signTime;
    /**
     * 业主签署时间
     */
    private LocalDateTime ownerSignTime;
    /**
     * 来源申请 ID，关联 rental_apply
     */
    private Long sourceApplyId;
    /**
     * 合同模板 ID，关联 rental_contract_template
     */
    private Long templateId;
    /**
     * 合同正文快照（HTML）
     *
     * 存渲染后的快照而不是每次按模板实时渲染：模板改版不应该改写已签合同的内容，
     * 合同是凭据。同时它也是「新建时套模板、之后手工微调」的前提。
     */
    private String content;

}
