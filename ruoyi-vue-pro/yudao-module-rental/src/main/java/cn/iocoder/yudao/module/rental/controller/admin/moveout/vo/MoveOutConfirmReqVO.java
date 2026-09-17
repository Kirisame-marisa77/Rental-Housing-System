package cn.iocoder.yudao.module.rental.controller.admin.moveout.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 退租处理（房屋验收 + 费用结算）Request VO")
@Data
public class MoveOutConfirmReqVO {

    @Schema(description = "退租申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "退租申请不能为空")
    private Long id;

    @Schema(description = "房屋验收结果：0-验收通过，1-轻微损坏，2-严重损坏", example = "0")
    private Integer inspectionResult;

    @Schema(description = "损坏维修费用（元）", example = "100.00")
    private BigDecimal repairFee;

    @Schema(description = "维修费用明细说明")
    private String repairFeeDesc;

    @Schema(description = "物业费欠费（元）", example = "0.00")
    private BigDecimal propertyFeeArrears;

    @Schema(description = "水电费欠费（元）", example = "0.00")
    private BigDecimal utilityFeeArrears;

    @Schema(description = "剩余租金（元，提前退租时已预缴未使用）", example = "0.00")
    private BigDecimal remainingRent;

    @Schema(description = "管理员备注")
    private String remark;

}
