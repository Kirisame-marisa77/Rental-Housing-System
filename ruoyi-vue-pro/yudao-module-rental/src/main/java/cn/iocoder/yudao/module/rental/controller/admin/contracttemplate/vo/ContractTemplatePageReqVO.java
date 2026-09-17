package cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 合同模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称，模糊匹配", example = "标准租赁合同")
    private String templateName;

    @Schema(description = "模板类型：0-标准租赁合同，1-短期租赁合同，2-商业租赁合同", example = "0")
    private Integer templateType;

    @Schema(description = "状态：0-启用，1-禁用", example = "0")
    private Integer status;

}
