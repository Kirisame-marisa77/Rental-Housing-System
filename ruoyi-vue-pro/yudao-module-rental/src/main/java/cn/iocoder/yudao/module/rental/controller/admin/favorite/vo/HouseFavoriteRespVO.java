package cn.iocoder.yudao.module.rental.controller.admin.favorite.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "租客端 - 我的收藏 Response VO")
@Data
public class HouseFavoriteRespVO {

    @Schema(description = "收藏记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "收藏时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 以下为补全的房源信息，由 Service 批量填充，非数据库字段 ==========

    @Schema(description = "房源编号")
    private String houseNo;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "所在区域")
    private String area;

    @Schema(description = "楼栋号")
    private String buildingNo;

    @Schema(description = "房号")
    private String roomNo;

    @Schema(description = "户型")
    private String layout;

    @Schema(description = "建筑面积（㎡）")
    private BigDecimal squareArea;

    @Schema(description = "月租金（元）")
    private BigDecimal monthlyRent;

    @Schema(description = "押金（元）")
    private BigDecimal deposit;

    @Schema(description = "装修情况")
    private String decoration;

    @Schema(description = "房源状态：0-下架，1-上架，2-已锁定，3-已出租")
    private Integer houseStatus;

    @Schema(description = "房东 ID", example = "1")
    private Long ownerId;

    @Schema(description = "房东姓名")
    private String ownerName;

    @Schema(description = "房东联系电话")
    private String ownerPhone;

}
