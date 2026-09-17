package cn.iocoder.yudao.module.rental.controller.admin.moveout.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @Schema(description = "状态：0-待处理，1-已处理", example = "0")
    private Integer status;

}
