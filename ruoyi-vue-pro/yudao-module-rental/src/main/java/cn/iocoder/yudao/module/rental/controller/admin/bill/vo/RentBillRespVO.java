package cn.iocoder.yudao.module.rental.controller.admin.bill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 租金账单信息 Response VO")
@Data
public class RentBillRespVO {

    @Schema(description = "账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "账单编号", example = "ZD-202606-0001")
    private String billNo;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "账单类型：0-首期账单，1-周期账单", example = "1")
    private Integer billType;

    @Schema(description = "费用周期开始日期")
    private LocalDate periodStart;

    @Schema(description = "费用周期结束日期")
    private LocalDate periodEnd;

    @Schema(description = "本期租金（元）", example = "3500.00")
    private BigDecimal rentAmount;

    @Schema(description = "押金金额（元），仅首期账单使用", example = "3500.00")
    private BigDecimal depositAmount;

    @Schema(description = "本期物业费（元）", example = "200.00")
    private BigDecimal propertyFeeAmount;

    @Schema(description = "应缴总金额（元）", example = "3700.00")
    private BigDecimal totalAmount;

    @Schema(description = "已缴金额（元）", example = "0.00")
    private BigDecimal paidAmount;

    @Schema(description = "缴费状态：0-待缴费，1-已缴费，2-已逾期", example = "0")
    private Integer payStatus;

    @Schema(description = "缴费截止日期")
    private LocalDate dueDate;

    @Schema(description = "最近一次缴费时间")
    private LocalDateTime payTime;

    @Schema(description = "缴费方式", example = "微信")
    private String payMethod;

    @Schema(description = "收款人 ID", example = "1")
    private Long payee;

    @Schema(description = "费用明细，JSON 格式")
    private String feeDetail;

    @Schema(description = "滞纳金（元）", example = "0.00")
    private BigDecimal lateFee;

    @Schema(description = "减免滞纳金（元）", example = "0.00")
    private BigDecimal lateFeeWaived;

    @Schema(description = "滞纳金减免原因")
    private String lateFeeWaiveReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 以下为房源地址展示字段，仅业主端「我的账单」接口回填 ==========

    @Schema(description = "小区名称（仅业主端回填）", example = "阳光小区")
    private String communityName;

    @Schema(description = "楼栋（仅业主端回填）", example = "1")
    private String buildingNo;

    @Schema(description = "房号（仅业主端回填）", example = "101")
    private String roomNo;

}
