package cn.iocoder.yudao.module.rental.dal.dataobject.house;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源信息表
 *
 * @author yudao
 */
@TableName("rental_house")
@Data
@EqualsAndHashCode(callSuper = true)
public class HouseDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 业主 ID，关联 rental_owner_info
     */
    private Long ownerId;
    /**
     * 房源编号
     */
    private String houseNo;
    /**
     * 小区名称
     */
    private String communityName;
    /**
     * 所在区域
     */
    private String area;
    /**
     * 楼栋号
     */
    private String buildingNo;
    /**
     * 房号
     */
    private String roomNo;
    /**
     * 户型
     */
    private String layout;
    /**
     * 建筑面积（㎡）
     */
    private BigDecimal squareArea;
    /**
     * 朝向
     */
    private String orientation;
    /**
     * 所在楼层
     */
    private Integer floor;
    /**
     * 楼栋总层数
     */
    private Integer totalFloor;
    /**
     * 装修情况：精装/简装/毛坯
     */
    private String decoration;
    /**
     * 月租金（元）
     */
    private BigDecimal monthlyRent;
    /**
     * 押金金额（元）
     */
    private BigDecimal deposit;
    /**
     * 付款方式：押N付M（N=押几个月，M=一次付几个月，各自 1~3）
     *
     * 押金 = N × 月租金；首期应缴 = 押金 + M × 月租金，账期 M 个月。
     * 属于房源属性，由房东设定，租客申请时不可更改。
     */
    private String paymentMethod;
    /**
     * 配套设施，JSON 格式
     */
    private String facilities;
    /**
     * 房源状态：0-下架，1-上架，2-已锁定，3-已出租
     */
    private Integer status;
    /**
     * 审核状态：0-待审核 1-已通过 2-已驳回
     */
    private Integer reviewStatus;
    /**
     * 审核驳回原因
     */
    private String reviewReason;
    /**
     * 水费计价方式：0-统一单价 1-三档梯度
     */
    private Integer waterBillType;
    /**
     * 水费统一单价（元/吨，统一价时用）
     */
    private BigDecimal waterUnitPrice;
    /**
     * 水费第一档上限（吨），0~limit1 按档一价
     */
    private BigDecimal waterTier1Limit;
    /**
     * 水费第一档单价（元/吨）
     */
    private BigDecimal waterTier1Price;
    /**
     * 水费第二档上限（吨），limit1~limit2 按档二价
     */
    private BigDecimal waterTier2Limit;
    /**
     * 水费第二档单价（元/吨）
     */
    private BigDecimal waterTier2Price;
    /**
     * 水费第三档单价（元/吨），超过 limit2 部分
     */
    private BigDecimal waterTier3Price;
    /**
     * 电费计价方式：0-统一单价 1-峰谷两价
     */
    private Integer electricityBillType;
    /**
     * 电费统一单价（元/度，统一价时用）
     */
    private BigDecimal electricityUnitPrice;
    /**
     * 电费峰段单价（元/度，峰 8:00-22:00）
     */
    private BigDecimal electricityPeakPrice;
    /**
     * 电费谷段单价（元/度，谷 22:00-8:00）
     */
    private BigDecimal electricityValleyPrice;
    /**
     * 最近一次上架时间
     */
    private LocalDateTime onlineTime;
    /**
     * 房源描述/周边环境介绍
     */
    private String description;

}
