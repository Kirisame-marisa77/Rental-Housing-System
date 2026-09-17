package cn.iocoder.yudao.module.rental.controller.admin.bill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 水电费账单创建/修改 Request VO")
@Data
public class UtilityBillSaveReqVO {

    @Schema(description = "账单编号", example = "1")
    private Long id;

    @Schema(description = "账单编号", example = "SD-202606-0001")
    private String billNo;

    @Schema(description = "合同 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "合同不能为空")
    private Long contractId;

    @Schema(description = "租客 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "租客不能为空")
    private Long tenantUserId;

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "抄表记录 ID", example = "1024")
    private Long meterReadingId;

    @Schema(description = "费用类型：0-水费，1-电费，2-水+电", example = "0")
    private Integer feeType;

    @Schema(description = "水费金额（元）", example = "30.00")
    private BigDecimal waterAmount;

    @Schema(description = "电费金额（元）", example = "80.00")
    private BigDecimal electricityAmount;

    @Schema(description = "应缴总金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "110.00")
    @NotNull(message = "应缴总金额不能为空")
    private BigDecimal totalAmount;

    @Schema(description = "缴费状态：0-待缴费，1-已缴费，2-已逾期", example = "0")
    private Integer payStatus;

    @Schema(description = "缴费截止日期")
    private LocalDate dueDate;

}
