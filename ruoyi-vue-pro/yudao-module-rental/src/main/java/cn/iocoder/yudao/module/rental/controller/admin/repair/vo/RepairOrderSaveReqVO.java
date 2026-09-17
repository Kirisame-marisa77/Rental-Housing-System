package cn.iocoder.yudao.module.rental.controller.admin.repair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 维修工单创建/修改 Request VO")
@Data
public class RepairOrderSaveReqVO {

    @Schema(description = "工单编号", example = "1")
    private Long id;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "报修类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "水电维修")
    @NotNull(message = "报修类型不能为空")
    private String repairType;

    @Schema(description = "问题描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问题描述不能为空")
    private String description;

    @Schema(description = "问题图片，JSON 数组")
    private String images;

    @Schema(description = "期望上门时间")
    private LocalDateTime expectedTime;

    @Schema(description = "优先级：0-普通，1-紧急，2-特急", example = "0")
    private Integer priority;

}
