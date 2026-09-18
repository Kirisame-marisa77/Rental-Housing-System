package cn.iocoder.yudao.module.rental.controller.admin.house.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 房源信息 Response VO")
@Data
public class HouseRespVO {

    @Schema(description = "房源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "业主 ID", example = "1")
    private Long ownerId;

    @Schema(description = "房源编号", example = "FW202606240001")
    private String houseNo;

    @Schema(description = "小区名称", example = "万科城市花园")
    private String communityName;

    @Schema(description = "所在区域", example = "浦东新区")
    private String area;

    @Schema(description = "楼栋号", example = "3栋")
    private String buildingNo;

    @Schema(description = "房号", example = "502")
    private String roomNo;

    @Schema(description = "户型", example = "两室一厅一卫")
    private String layout;

    @Schema(description = "建筑面积（㎡）", example = "89.50")
    private BigDecimal squareArea;

    @Schema(description = "朝向", example = "南北通透")
    private String orientation;

    @Schema(description = "所在楼层", example = "5")
    private Integer floor;

    @Schema(description = "楼栋总层数", example = "18")
    private Integer totalFloor;

    @Schema(description = "装修情况：精装/简装/毛坯", example = "精装")
    private String decoration;

    @Schema(description = "月租金（元）", example = "3500.00")
    private BigDecimal monthlyRent;

    @Schema(description = "押金金额（元）", example = "3500.00")
    private BigDecimal deposit;

    @Schema(description = "付款方式：押N付M，N/M 各取 1~3", example = "押一付三")
    private String paymentMethod;

    @Schema(description = "配套设施，JSON 格式", example = "[\"空调\",\"洗衣机\",\"冰箱\"]")
    private String facilities;

    @Schema(description = "房源状态：0-下架，1-上架，2-已锁定，3-已出租", example = "1")
    private Integer status;

    @Schema(description = "审核状态：0-待审核 1-已通过 2-已驳回", example = "0")
    private Integer reviewStatus;

    @Schema(description = "审核驳回原因")
    private String reviewReason;

    @Schema(description = "水费计价方式：0-统一单价 1-三档梯度", example = "0")
    private Integer waterBillType;

    @Schema(description = "水费统一单价（元/吨）", example = "3.50")
    private BigDecimal waterUnitPrice;

    @Schema(description = "水费第一档上限（吨）", example = "10.00")
    private BigDecimal waterTier1Limit;

    @Schema(description = "水费第一档单价（元/吨）", example = "3.00")
    private BigDecimal waterTier1Price;

    @Schema(description = "水费第二档上限（吨）", example = "20.00")
    private BigDecimal waterTier2Limit;

    @Schema(description = "水费第二档单价（元/吨）", example = "4.00")
    private BigDecimal waterTier2Price;

    @Schema(description = "水费第三档单价（元/吨）", example = "5.00")
    private BigDecimal waterTier3Price;

    @Schema(description = "电费计价方式：0-统一单价 1-峰谷两价", example = "1")
    private Integer electricityBillType;

    @Schema(description = "电费统一单价（元/度）", example = "0.50")
    private BigDecimal electricityUnitPrice;

    @Schema(description = "电费峰段单价（元/度）", example = "0.60")
    private BigDecimal electricityPeakPrice;

    @Schema(description = "电费谷段单价（元/度）", example = "0.30")
    private BigDecimal electricityValleyPrice;

    @Schema(description = "最近一次上架时间")
    private LocalDateTime onlineTime;

    @Schema(description = "房源描述/周边环境介绍", example = "交通便利，近地铁")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
