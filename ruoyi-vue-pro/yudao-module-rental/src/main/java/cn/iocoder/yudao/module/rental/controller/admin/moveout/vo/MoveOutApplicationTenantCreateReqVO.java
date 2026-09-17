package cn.iocoder.yudao.module.rental.controller.admin.moveout.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 租客端 - 发起退租申请 Request VO
 *
 * 刻意不复用 MoveOutApplicationSaveReqVO：那个 VO 把 tenantUserId / houseId 标成了
 * @NotNull（管理端代录时需要填），而租客端这两个值必须由后端从合同反查出来，
 * 复用会让租客端的请求直接卡在校验上，或者逼前端把这两个 ID 传上来 —— 后者等于
 * 把「退谁的房」的决定权交给了客户端。
 *
 * @author yudao
 */
@Schema(description = "租客端 - 发起退租申请 Request VO")
@Data
public class MoveOutApplicationTenantCreateReqVO {

    @Schema(description = "合同 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "合同不能为空")
    private Long contractId;

    @Schema(description = "退租类型：0-到期退租，1-提前退租", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "退租类型不能为空")
    private Integer moveOutType;

    @Schema(description = "预计退租日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预计退租日期不能为空")
    private LocalDate expectedMoveOutDate;

    @Schema(description = "退租原因说明")
    private String moveOutReason;

}
