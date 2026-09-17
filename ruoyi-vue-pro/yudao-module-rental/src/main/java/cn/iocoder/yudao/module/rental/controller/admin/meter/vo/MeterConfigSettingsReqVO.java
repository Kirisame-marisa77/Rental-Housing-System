package cn.iocoder.yudao.module.rental.controller.admin.meter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 抄表配置更新 Request VO")
@Data
public class MeterConfigSettingsReqVO {

    @Schema(description = "水费单价（元/吨）", example = "3.50")
    private BigDecimal waterPrice;

    @Schema(description = "电费单价（元/度）", example = "0.60")
    private BigDecimal electricityPrice;

    @Schema(description = "抄表周期：monthly-按月，bimonthly-按双月", example = "monthly")
    private String meterPeriod;

}
