package cn.iocoder.yudao.module.rental.controller.admin.viewing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "租客端 - 预约看房创建 Request VO")
@Data
public class ViewingAppointmentSaveReqVO {

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "预约日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预约日期不能为空")
    private LocalDate appointmentDate;

    @Schema(description = "开始时间，零填充 24 小时制 HH:mm", requiredMode = Schema.RequiredMode.REQUIRED, example = "09:00")
    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @Schema(description = "结束时间，零填充 24 小时制 HH:mm", requiredMode = Schema.RequiredMode.REQUIRED, example = "10:00")
    @NotNull(message = "结束时间不能为空")
    private String endTime;

}
