package cn.iocoder.yudao.module.rental.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 合同信息 Response VO")
@Data
public class ContractRespVO {

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "合同编号", example = "HT-202606-0001")
    private String contractNo;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "租期开始日期")
    private LocalDate rentStartDate;

    @Schema(description = "租期结束日期")
    private LocalDate rentEndDate;

    @Schema(description = "签约月租金（元）", example = "3500.00")
    private BigDecimal monthlyRent;

    @Schema(description = "押金金额（元）", example = "3500.00")
    private BigDecimal depositAmount;

    @Schema(description = "付款方式", example = "押一付三")
    private String paymentMethod;

    @Schema(description = "物业费单价（元/㎡/月）", example = "2.50")
    private BigDecimal propertyFeeUnit;

    @Schema(description = "合同状态", example = "2")
    private Integer status;

    @Schema(description = "租客签署时间")
    private LocalDateTime signTime;

    @Schema(description = "业主签署时间")
    private LocalDateTime ownerSignTime;

    @Schema(description = "来源申请 ID", example = "1024")
    private Long sourceApplyId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "合同模板 ID", example = "1")
    private Long templateId;

    @Schema(description = "合同正文快照（HTML）")
    private String content;

    // ========== 以下为补全信息，由 Service 填充，非数据库字段 ==========

    @Schema(description = "首期账单编号")
    private String firstBillNo;

    @Schema(description = "首期账单是否已缴费")
    private Boolean firstBillPaid;

    @Schema(description = "房源编号")
    private String houseNo;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "区域")
    private String area;

    @Schema(description = "楼栋")
    private String buildingNo;

    @Schema(description = "房号")
    private String roomNo;

    @Schema(description = "户型")
    private String layout;

    @Schema(description = "建筑面积（㎡）")
    private BigDecimal squareArea;

    @Schema(description = "房东 ID")
    private Long ownerId;

    @Schema(description = "房东姓名")
    private String ownerName;

    @Schema(description = "房东电话")
    private String ownerPhone;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客电话")
    private String tenantPhone;

}
