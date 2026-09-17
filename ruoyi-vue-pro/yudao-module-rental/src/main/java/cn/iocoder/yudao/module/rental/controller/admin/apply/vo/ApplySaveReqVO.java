package cn.iocoder.yudao.module.rental.controller.admin.apply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 租房申请创建/修改 Request VO")
@Data
public class ApplySaveReqVO {

    @Schema(description = "申请编号", example = "1")
    private Long id;

    @Schema(description = "申请编号", example = "SQ202606240001")
    private String applyNo;

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "期望入住日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "期望入住日期不能为空")
    private LocalDate moveInDate;

    @Schema(description = "租期（月）", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @NotNull(message = "租期不能为空")
    private Integer leaseTerm;

    @Schema(description = "付款方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "押一付三")
    @NotNull(message = "付款方式不能为空")
    private String paymentMethod;

    @Schema(description = "签约月租金（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "3500.00")
    @NotNull(message = "月租金不能为空")
    private BigDecimal monthlyRent;

    @Schema(description = "押金金额（元）", example = "3500.00")
    private BigDecimal depositAmount;

    @Schema(description = "租客备注", example = "无")
    private String tenantRemark;

    @Schema(description = "状态：0-待审批，1-已通过，2-已驳回，3-已签约，4-已超时", example = "0")
    private Integer status;

    @Schema(description = "审批人 ID", example = "1")
    private Long approverId;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "驳回原因", example = "资料不全")
    private String rejectReason;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "超时原因", example = "未签署")
    private String timeoutReason;

}
