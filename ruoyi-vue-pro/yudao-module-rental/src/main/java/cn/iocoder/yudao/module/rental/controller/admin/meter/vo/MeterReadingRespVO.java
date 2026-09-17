package cn.iocoder.yudao.module.rental.controller.admin.meter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 抄表记录信息 Response VO")
@Data
public class MeterReadingRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "上传业主 ID", example = "1")
    private Long ownerId;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "抄表类型：0-水表，1-电表", example = "0")
    private Integer meterType;

    @Schema(description = "上期读数", example = "90.00")
    private BigDecimal lastReading;

    @Schema(description = "本期读数", example = "100.00")
    private BigDecimal currentReading;

    @Schema(description = "电表谷段本期读数", example = "80.00")
    private BigDecimal valleyReading;

    @Schema(description = "电表谷段上期读数", example = "70.00")
    private BigDecimal lastValleyReading;

    @Schema(description = "用量", example = "10.00")
    private BigDecimal usageAmount;

    @Schema(description = "电表谷段用量", example = "10.00")
    private BigDecimal valleyUsage;

    @Schema(description = "单价", example = "3.50")
    private BigDecimal unitPrice;

    @Schema(description = "电费峰段单价", example = "0.60")
    private BigDecimal peakPrice;

    @Schema(description = "电费谷段单价", example = "0.30")
    private BigDecimal valleyPrice;

    @Schema(description = "费用金额", example = "35.00")
    private BigDecimal feeAmount;

    @Schema(description = "抄表日期")
    private LocalDate readingDate;

    @Schema(description = "录入人 ID", example = "1")
    private Long operatorId;

    @Schema(description = "关联水电费账单 ID", example = "1024")
    private Long billId;

    @Schema(description = "状态：0-正常，1-异常，2-已作废", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "抄表截图，JSON 数组")
    private String images;

    @Schema(description = "审核状态：0-待审核 1-已通过 2-已驳回", example = "0")
    private Integer reviewStatus;

    @Schema(description = "审核驳回原因")
    private String reviewReason;

    @Schema(description = "审核人 ID")
    private Long reviewerId;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
