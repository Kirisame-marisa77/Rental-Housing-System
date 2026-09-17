package cn.iocoder.yudao.module.rental.controller.admin.bill.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - 租金账单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RentBillPageReqVO extends PageParam {

    @Schema(description = "账单编号，模糊匹配", example = "ZD-")
    private String billNo;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "缴费状态：0-待缴费，1-已缴费，2-已逾期", example = "0")
    private Integer payStatus;

    @Schema(description = "房源 ID 集合（业主端内部使用，前端无需传）", hidden = true)
    private List<Long> houseIds;

}
