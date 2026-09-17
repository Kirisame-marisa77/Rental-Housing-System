package cn.iocoder.yudao.module.rental.controller.admin.moveout.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "管理后台 - 退租申请创建/修改 Request VO")
@Data
public class MoveOutApplicationSaveReqVO {

    @Schema(description = "申请编号", example = "1")
    private Long id;

    @Schema(description = "合同 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "合同不能为空")
    private Long contractId;

    @Schema(description = "租客 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "租客不能为空")
    private Long tenantUserId;

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "退租类型：0-到期退租，1-提前退租", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "退租类型不能为空")
    private Integer moveOutType;

    @Schema(description = "退租原因说明")
    private String moveOutReason;

    @Schema(description = "预计退租日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预计退租日期不能为空")
    private LocalDate expectedMoveOutDate;

}
