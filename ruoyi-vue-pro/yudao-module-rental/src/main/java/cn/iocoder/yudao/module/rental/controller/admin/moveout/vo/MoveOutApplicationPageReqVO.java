package cn.iocoder.yudao.module.rental.controller.admin.moveout.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - 退租申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MoveOutApplicationPageReqVO extends PageParam {

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "租客 ID：租客端由后端从登录态强制写入，管理端一般不传", example = "2048")
    private Long tenantUserId;

    @Schema(description = "退租类型：0-到期退租，1-提前退租", example = "0")
    private Integer moveOutType;

    @Schema(description = "状态：0-待处理，1-已处理，2-已驳回", example = "0")
    private Integer status;

    /**
     * 业主端内部使用：只查自己名下房源的退租申请。
     *
     * hidden = true 只影响 Swagger 文档，参数依然能从 query string 绑定进来，
     * 所以业主端的 Service 必须**无条件覆盖**这个字段，绝不能信任入参 ——
     * 否则构造一个 ?houseIds=别人的房源 就能看到别人的退租申请。
     */
    @Schema(description = "房源 ID 集合（业主端内部使用，前端无需传）", hidden = true)
    private List<Long> houseIds;

}
