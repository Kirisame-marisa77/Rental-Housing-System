package cn.iocoder.yudao.module.rental.controller.admin.bill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 水电费账单信息 Response VO")
@Data
public class UtilityBillRespVO {

    @Schema(description = "账单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "账单编号", example = "SD-202606-0001")
    private String billNo;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "抄表记录 ID", example = "1024")
    private Long meterReadingId;

    @Schema(description = "费用类型：0-水费，1-电费，2-水+电", example = "0")
    private Integer feeType;

    @Schema(description = "水费金额（元）", example = "30.00")
    private BigDecimal waterAmount;

    @Schema(description = "电费金额（元）", example = "80.00")
    private BigDecimal electricityAmount;

    @Schema(description = "应缴总金额（元）", example = "110.00")
    private BigDecimal totalAmount;

    @Schema(description = "缴费状态：0-待缴费，1-已缴费，2-已逾期", example = "0")
    private Integer payStatus;

    @Schema(description = "缴费截止日期")
    private LocalDate dueDate;

    @Schema(description = "缴费时间")
    private LocalDateTime payTime;

    @Schema(description = "缴费方式", example = "微信")
    private String payMethod;

    @Schema(description = "收款人 ID", example = "1")
    private Long payee;

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
