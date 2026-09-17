package cn.iocoder.yudao.module.rental.controller.admin.repair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 维修进度 Response VO")
@Data
public class RepairProgressRespVO {

    @Schema(description = "进度编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工单 ID", example = "1024")
    private Long orderId;

    @Schema(description = "操作人 ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人类型：0-租客，1-管理员，2-维修人员", example = "1")
    private Integer operatorType;

    @Schema(description = "操作类型", example = "分配工单")
    private String actionType;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
