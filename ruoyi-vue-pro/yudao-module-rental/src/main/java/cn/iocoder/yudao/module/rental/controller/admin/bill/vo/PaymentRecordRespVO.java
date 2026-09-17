package cn.iocoder.yudao.module.rental.controller.admin.bill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 缴费记录信息 Response VO")
@Data
public class PaymentRecordRespVO {

    @Schema(description = "缴费记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "账单类型：0-租金/物业费，1-水电费，2-退租结算", example = "1")
    private Integer billType;

    @Schema(description = "关联账单 ID", example = "1024")
    private Long billId;

    @Schema(description = "缴费金额（元）", example = "110.00")
    private BigDecimal payAmount;

    @Schema(description = "缴费方式", example = "微信")
    private String payMethod;

    @Schema(description = "缴费日期")
    private LocalDateTime payTime;

    @Schema(description = "收款人 ID", example = "1")
    private Long payee;

    @Schema(description = "交易流水号")
    private String transactionNo;

    @Schema(description = "缴费凭证编号")
    private String voucherNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
