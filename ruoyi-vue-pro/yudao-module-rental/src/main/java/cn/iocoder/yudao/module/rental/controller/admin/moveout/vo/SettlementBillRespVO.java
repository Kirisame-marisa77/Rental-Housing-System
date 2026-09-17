package cn.iocoder.yudao.module.rental.controller.admin.moveout.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 退租结算单信息 Response VO")
@Data
public class SettlementBillRespVO {

    @Schema(description = "结算单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "结算单号", example = "JS-202606-0001")
    private String settlementNo;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "退租申请 ID", example = "1024")
    private Long moveOutApplicationId;

    @Schema(description = "退租类型：0-到期退租，1-提前退租", example = "0")
    private Integer moveOutType;

    @Schema(description = "押金金额（元）", example = "2000.00")
    private BigDecimal depositAmount;

    @Schema(description = "押金处理方式")
    private String depositHandle;

    @Schema(description = "剩余租金（元）", example = "0.00")
    private BigDecimal remainingRent;

    @Schema(description = "物业费欠费（元）", example = "0.00")
    private BigDecimal propertyFeeArrears;

    @Schema(description = "水电费欠费（元）", example = "0.00")
    private BigDecimal utilityFeeArrears;

    @Schema(description = "维修费金额（元）", example = "100.00")
    private BigDecimal repairFee;

    @Schema(description = "欠费抵扣金额（元）", example = "100.00")
    private BigDecimal deductionAmount;

    @Schema(description = "退还/补缴金额（元），正数=退还，负数=补缴", example = "1900.00")
    private BigDecimal refundOrPay;

    @Schema(description = "处理人 ID", example = "1")
    private Long handlerId;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
