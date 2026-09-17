package cn.iocoder.yudao.module.rental.controller.admin.meter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 抄表记录创建/修改 Request VO")
@Data
public class MeterReadingSaveReqVO {

    @Schema(description = "记录编号", example = "1")
    private Long id;

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "合同 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "合同不能为空")
    private Long contractId;

    @Schema(description = "抄表类型：0-水表，1-电表", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "抄表类型不能为空")
    private Integer meterType;

    @Schema(description = "本期读数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "本期读数不能为空")
    private BigDecimal currentReading;

    @Schema(description = "单价（元/吨 或 元/度），为空时取抄表配置", example = "3.50")
    private BigDecimal unitPrice;

    @Schema(description = "抄表日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "抄表日期不能为空")
    private LocalDate readingDate;

    @Schema(description = "备注（异常说明等）")
    private String remark;

    @Schema(description = "上传业主 ID", example = "1")
    private Long ownerId;

    @Schema(description = "电表谷段本期读数", example = "80.00")
    private BigDecimal valleyReading;

    @Schema(description = "抄表截图，JSON 数组（水 1 张 / 电峰谷各 1 张）", example = "[\"https://...\"]")
    private String images;

}
