package cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 合同模板信息 Response VO")
@Data
public class ContractTemplateRespVO {

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模板名称", example = "标准租赁合同")
    private String templateName;

    @Schema(description = "模板类型：0-标准租赁合同，1-短期租赁合同，2-商业租赁合同", example = "0")
    private Integer templateType;

    @Schema(description = "合同模板内容（HTML）")
    private String content;

    @Schema(description = "模板变量定义，JSON 格式")
    private String variables;

    @Schema(description = "模板版本号", example = "1")
    private Integer version;

    @Schema(description = "状态：0-启用，1-禁用", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
