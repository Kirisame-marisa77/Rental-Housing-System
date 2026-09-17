package cn.iocoder.yudao.module.rental.controller.admin.repair.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 维修工单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RepairOrderPageReqVO extends PageParam {

    @Schema(description = "工单编号，模糊匹配", example = "WX-")
    private String orderNo;

    @Schema(description = "报修类型", example = "水电维修")
    private String repairType;

    @Schema(description = "工单状态：0-待处理，1-处理中，2-待验收，3-已处理，4-已驳回", example = "0")
    private Integer status;

    @Schema(description = "优先级：0-普通，1-紧急，2-特急", example = "0")
    private Integer priority;

    @Schema(description = "租客 ID", example = "1")
    private Long tenantUserId;

    @Schema(description = "处理业主 ID", example = "1")
    private Long ownerId;

}
