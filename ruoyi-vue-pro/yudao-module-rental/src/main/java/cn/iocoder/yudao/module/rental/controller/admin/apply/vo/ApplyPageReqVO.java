package cn.iocoder.yudao.module.rental.controller.admin.apply.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - 租房申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyPageReqVO extends PageParam {

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "房源 ID 集合（业主端内部使用，前端无需传）", hidden = true)
    private List<Long> houseIds;

}
