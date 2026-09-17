package cn.iocoder.yudao.module.rental.controller.admin.tenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 租客信息 Response VO")
@Data
public class TenantInfoRespVO {

    @Schema(description = "租客编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "关联会员用户 ID", example = "1024")
    private Long userId;

    @Schema(description = "租客姓名", example = "张三")
    private String name;

    @Schema(description = "身份证号", example = "310101199001011234")
    private String idCard;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "性别：0-未知 1-男 2-女", example = "1")
    private Integer gender;

    @Schema(description = "紧急联系人", example = "李四")
    private String emergencyContact;

    @Schema(description = "紧急联系电话", example = "13900139000")
    private String emergencyPhone;

    @Schema(description = "工作单位", example = "某公司")
    private String workUnit;

    @Schema(description = "实名认证状态：0-未认证 1-认证中 2-已认证 3-认证失败 4-已锁定", example = "2")
    private Integer authStatus;

    @Schema(description = "连续认证失败次数", example = "0")
    private Integer authFailCount;

    @Schema(description = "最近认证时间")
    private LocalDateTime authTime;

    @Schema(description = "备注", example = "无")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
