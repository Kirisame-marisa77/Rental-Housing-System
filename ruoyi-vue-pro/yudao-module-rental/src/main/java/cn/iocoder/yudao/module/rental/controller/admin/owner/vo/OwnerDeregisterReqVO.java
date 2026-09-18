package cn.iocoder.yudao.module.rental.controller.admin.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "业主端 - 注销账号 Request VO")
@Data
public class OwnerDeregisterReqVO {

    /**
     * 用请求体而不是 query param：密码不该出现在 URL 和访问日志里
     */
    @Schema(description = "登录密码（二次确认）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请输入登录密码")
    private String password;

}
