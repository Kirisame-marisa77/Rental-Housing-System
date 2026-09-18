package cn.iocoder.yudao.module.rental.controller.admin.moveout.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 退租申请信息 Response VO")
@Data
public class MoveOutApplicationRespVO {

    @Schema(description = "申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "申请编号，规则：TZ + 时间戳 + 随机数", example = "TZ202609161200001234")
    private String applyNo;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "退租类型：0-到期退租，1-提前退租", example = "0")
    private Integer moveOutType;

    @Schema(description = "退租原因说明")
    private String moveOutReason;

    @Schema(description = "预计退租日期")
    private LocalDate expectedMoveOutDate;

    @Schema(description = "关联退租结算单 ID", example = "1024")
    private Long settlementId;

    @Schema(description = "状态：0-待处理，1-已处理，2-已驳回", example = "0")
    private Integer status;

    @Schema(description = "处理人 ID", example = "1")
    private Long handlerId;

    @Schema(description = "处理人类型：0-管理员，1-业主", example = "1")
    private Integer handlerType;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "房屋验收结果：0-验收通过，1-轻微损坏，2-严重损坏", example = "0")
    private Integer inspectionResult;

    @Schema(description = "损坏维修费用（元）", example = "100.00")
    private BigDecimal repairFee;

    @Schema(description = "维修费用明细说明")
    private String repairFeeDesc;

    @Schema(description = "处理备注")
    private String remark;

    @Schema(description = "押金处理方式")
    private String depositHandle;

    @Schema(description = "剩余租金（元）", example = "0.00")
    private BigDecimal remainingRent;

    @Schema(description = "欠费抵扣金额（元）", example = "0.00")
    private BigDecimal deductionAmount;

    @Schema(description = "退还/补缴金额（元），正数=退还，负数=补缴", example = "500.00")
    private BigDecimal refundOrPay;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 以下为补全信息，由 Service 填充，非数据库字段 ==========

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "楼栋")
    private String buildingNo;

    @Schema(description = "房号")
    private String roomNo;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客手机号")
    private String tenantPhone;

}
