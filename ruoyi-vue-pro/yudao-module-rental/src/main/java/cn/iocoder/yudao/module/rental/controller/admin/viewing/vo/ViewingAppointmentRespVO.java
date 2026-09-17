package cn.iocoder.yudao.module.rental.controller.admin.viewing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "租客端/业主端/管理端 - 预约看房 Response VO")
@Data
public class ViewingAppointmentRespVO {

    @Schema(description = "预约编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "预约日期")
    private LocalDate appointmentDate;

    @Schema(description = "开始时间", example = "09:00")
    private String startTime;

    @Schema(description = "结束时间", example = "10:00")
    private String endTime;

    @Schema(description = "状态：0-待确认，1-已确认，2-已完成，3-已取消", example = "0")
    private Integer status;

    @Schema(description = "看房反馈")
    private String feedback;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 以下为补全信息，由 Service 批量填充，非数据库字段 ==========

    @Schema(description = "房源编号")
    private String houseNo;

    @Schema(description = "小区名称")
    private String communityName;

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

    @Schema(description = "房源状态：0-下架，1-上架，2-已锁定，3-已出租")
    private Integer houseStatus;

    @Schema(description = "房东 ID", example = "1")
    private Long ownerId;

    @Schema(description = "房东姓名")
    private String ownerName;

    @Schema(description = "房东联系电话")
    private String ownerPhone;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客联系电话")
    private String tenantPhone;

}
