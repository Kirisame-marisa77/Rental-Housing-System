package cn.iocoder.yudao.module.rental.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 合同创建/修改 Request VO")
@Data
public class ContractSaveReqVO {

    @Schema(description = "合同编号", example = "1")
    private Long id;

    @Schema(description = "合同编号", example = "HT-202606-0001")
    private String contractNo;

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "租客 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "租客不能为空")
    private Long tenantUserId;

    @Schema(description = "租期开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租期开始日期不能为空")
    private LocalDate rentStartDate;

    @Schema(description = "租期结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "租期结束日期不能为空")
    private LocalDate rentEndDate;

    @Schema(description = "签约月租金（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "3500.00")
    @NotNull(message = "月租金不能为空")
    private BigDecimal monthlyRent;

    @Schema(description = "押金金额（元）", example = "3500.00")
    private BigDecimal depositAmount;

    @Schema(description = "付款方式：押一付一/押一付三/押二付一/押二付三/自定义", requiredMode = Schema.RequiredMode.REQUIRED, example = "押一付三")
    @NotNull(message = "付款方式不能为空")
    private String paymentMethod;

    @Schema(description = "物业费单价（元/㎡/月）", example = "2.50")
    private BigDecimal propertyFeeUnit;

    @Schema(description = "合同状态：0-待签署，1-待缴费，2-生效中，3-即将到期，4-退租处理中，5-已到期，6-已退租，7-已取消", example = "2")
    private Integer status;

    @Schema(description = "租客签署时间")
    private LocalDateTime signTime;

    @Schema(description = "来源申请 ID", example = "1024")
    private Long sourceApplyId;

    @Schema(description = "合同模板 ID，关联 rental_contract_template", example = "1")
    private Long templateId;

    @Schema(description = "合同正文（HTML）。为空且填了模板 ID 时，由后端按模板渲染填充")
    private String content;

}
