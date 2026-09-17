package cn.iocoder.yudao.module.rental.controller.admin.contract.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 合同分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractPageReqVO extends PageParam {

    @Schema(description = "合同编号，模糊匹配", example = "HT-")
    private String contractNo;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "合同状态", example = "2")
    private Integer status;

}
