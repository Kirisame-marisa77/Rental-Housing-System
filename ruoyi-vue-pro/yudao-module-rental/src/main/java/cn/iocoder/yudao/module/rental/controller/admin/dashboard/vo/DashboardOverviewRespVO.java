package cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 首页看板综合数据 Response VO")
@Data
public class DashboardOverviewRespVO {

    @Schema(description = "总房源数", example = "100")
    private Long houseCount;

    @Schema(description = "已出租房源数", example = "60")
    private Long rentedCount;

    @Schema(description = "上架房源数", example = "30")
    private Long onSaleCount;

    @Schema(description = "下架房源数", example = "10")
    private Long offSaleCount;

    @Schema(description = "待审批申请数", example = "5")
    private Long pendingApplyCount;

    @Schema(description = "待处理工单数", example = "3")
    private Long pendingRepairCount;

    @Schema(description = "待缴费账单数", example = "8")
    private Long pendingBillCount;

    @Schema(description = "即将到期合同数", example = "4")
    private Long expiringContractCount;

}
