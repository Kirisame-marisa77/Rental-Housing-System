package cn.iocoder.yudao.module.rental.controller.admin.repair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 维修工单信息 Response VO")
@Data
public class RepairOrderRespVO {

    @Schema(description = "工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工单编号", example = "WX-202606-0001")
    private String orderNo;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "报修类型", example = "水电维修")
    private String repairType;

    @Schema(description = "问题描述")
    private String description;

    @Schema(description = "问题图片，JSON 数组")
    private String images;

    @Schema(description = "期望上门时间")
    private LocalDateTime expectedTime;

    @Schema(description = "工单状态：0-待处理，1-处理中，2-待验收，3-已处理，4-已驳回", example = "0")
    private Integer status;

    @Schema(description = "优先级：0-普通，1-紧急，2-特急", example = "0")
    private Integer priority;

    @Schema(description = "维修人员 ID", example = "1")
    private Long repairerId;

    @Schema(description = "处理业主 ID", example = "1")
    private Long ownerId;

    @Schema(description = "分配时间")
    private LocalDateTime assignTime;

    @Schema(description = "维修完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "维修说明")
    private String repairDescription;

    @Schema(description = "处理证据图片，JSON 数组")
    private String handleEvidence;

    @Schema(description = "业主处理完成时间")
    private LocalDateTime handleTime;

    @Schema(description = "验收不合格原因")
    private String reviewReason;

    @Schema(description = "评价星级：1-5")
    private Integer rating;

    @Schema(description = "评价内容")
    private String evaluationContent;

    @Schema(description = "评价时间")
    private LocalDateTime evaluationTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
