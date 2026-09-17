package cn.iocoder.yudao.module.rental.controller.admin.owner.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 业主分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OwnerInfoPageReqVO extends PageParam {

    @Schema(description = "业主姓名，模糊匹配", example = "王")
    private String name;

    @Schema(description = "手机号，模糊匹配", example = "137")
    private String phone;

    @Schema(description = "身份证号，模糊匹配", example = "310101")
    private String idCard;

}
