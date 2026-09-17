package cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 片区房源分布 Response VO")
@Data
public class DashboardDistributionRespVO {

    @Schema(description = "所在区域", example = "海淀区")
    private String area;

    @Schema(description = "房源数量", example = "20")
    private Long count;

}
