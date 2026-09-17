package cn.iocoder.yudao.module.rental.controller.admin.apply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 租房申请信息 Response VO")
@Data
public class ApplyRespVO {

    @Schema(description = "申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "申请编号", example = "SQ202606240001")
    private String applyNo;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "期望入住日期")
    private LocalDate moveInDate;

    @Schema(description = "租期（月）", example = "12")
    private Integer leaseTerm;

    @Schema(description = "付款方式", example = "押一付三")
    private String paymentMethod;

    @Schema(description = "签约月租金（元）", example = "3500.00")
    private BigDecimal monthlyRent;

    @Schema(description = "押金金额（元）", example = "3500.00")
    private BigDecimal depositAmount;

    @Schema(description = "租客备注", example = "无")
    private String tenantRemark;

    @Schema(description = "状态", example = "0")
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

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 以下为列表补全信息，由 Service 批量填充，非数据库字段 ==========

    @Schema(description = "房源编号")
    private String houseNo;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "所在区域")
    private String area;

    @Schema(description = "楼栋号")
    private String buildingNo;

    @Schema(description = "房号")
    private String roomNo;

    @Schema(description = "户型")
    private String layout;

    @Schema(description = "建筑面积（㎡）")
    private BigDecimal squareArea;

    @Schema(description = "房源状态：0-下架，1-上架，2-已锁定，3-已出租")
    private Integer houseStatus;

    @Schema(description = "房东 ID", example = "1")
    private Long ownerId;

    @Schema(description = "房东姓名")
    private String ownerName;

    @Schema(description = "房东联系电话")
    private String ownerPhone;

    @Schema(description = "租客姓名")
    private String tenantName;

    @Schema(description = "租客联系电话")
    private String tenantPhone;

}
