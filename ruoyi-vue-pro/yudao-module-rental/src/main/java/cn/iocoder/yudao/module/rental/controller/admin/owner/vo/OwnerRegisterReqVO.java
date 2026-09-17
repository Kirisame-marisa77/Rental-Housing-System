package cn.iocoder.yudao.module.rental.controller.admin.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "业主端 - 业主注册 Request VO")
@Data
public class OwnerRegisterReqVO {

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "310101198001011234")
    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13700137000")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "银行卡号", requiredMode = Schema.RequiredMode.REQUIRED, example = "6222021234567890")
    @NotBlank(message = "银行卡号不能为空")
    private String bankCard;

    @Schema(description = "紧急联系人", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotBlank(message = "紧急联系人不能为空")
    private String emergencyContact;

    @Schema(description = "紧急联系电话", example = "13600136000")
    private String emergencyPhone;

    @Schema(description = "性别：0-未知 1-男 2-女", example = "1")
    private Integer gender;

}
