package cn.iocoder.yudao.module.rental.controller.admin.tenant.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 租客分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantInfoPageReqVO extends PageParam {

    @Schema(description = "租客姓名，模糊匹配", example = "张")
    private String name;

    @Schema(description = "手机号，模糊匹配", example = "138")
    private String phone;

    @Schema(description = "身份证号，模糊匹配", example = "310101")
    private String idCard;

    @Schema(description = "实名认证状态", example = "2")
    private Integer authStatus;

}
