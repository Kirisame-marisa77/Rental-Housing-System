package cn.iocoder.yudao.module.rental.controller.admin.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 业主创建/修改 Request VO")
@Data
public class OwnerInfoSaveReqVO {

    @Schema(description = "业主编号", example = "1")
    private Long id;

    @Schema(description = "关联会员用户 ID", example = "1024")
    private Long userId;

    @Schema(description = "业主姓名", example = "王五")
    private String name;

    @Schema(description = "身份证号", example = "310101198001011234")
    private String idCard;

    @Schema(description = "银行卡号", example = "6222021234567890")
    private String bankCard;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13700137000")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "登录密码", example = "123456")
    private String password;

    @Schema(description = "性别：0-未知 1-男 2-女", example = "1")
    private Integer gender;

    @Schema(description = "紧急联系人", example = "赵六")
    private String emergencyContact;

    @Schema(description = "紧急联系电话", example = "13600136000")
    private String emergencyPhone;

    @Schema(description = "备注", example = "无")
    private String remark;

}
