package cn.iocoder.yudao.module.rental.controller.admin.viewing.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "租客端/业主端/管理端 - 预约看房分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ViewingAppointmentPageReqVO extends PageParam {

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "租客 ID", example = "2048")
    private Long tenantUserId;

    @Schema(description = "状态：0-待确认，1-已确认，2-已完成，3-已取消", example = "0")
    private Integer status;

    @Schema(description = "房源 ID 集合（业主端内部使用，前端无需传）", hidden = true)
    private List<Long> houseIds;

}
