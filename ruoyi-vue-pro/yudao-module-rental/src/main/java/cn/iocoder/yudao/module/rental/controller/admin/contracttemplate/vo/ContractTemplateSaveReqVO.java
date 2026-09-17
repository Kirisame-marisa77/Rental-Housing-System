package cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 合同模板创建/修改 Request VO")
@Data
public class ContractTemplateSaveReqVO {

    @Schema(description = "模板编号", example = "1")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "标准租赁合同")
    @NotEmpty(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "模板类型：0-标准租赁合同，1-短期租赁合同，2-商业租赁合同", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "模板类型不能为空")
    private Integer templateType;

    @Schema(description = "合同模板内容（HTML，支持 ${变量名} 占位符）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "模板内容不能为空")
    private String content;

    @Schema(description = "模板变量定义，JSON 格式")
    private String variables;

    @Schema(description = "状态：0-启用，1-禁用", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
