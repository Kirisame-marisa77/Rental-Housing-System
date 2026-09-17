package cn.iocoder.yudao.module.rental.controller.admin.contracttemplate;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplatePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplateRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplateSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contracttemplate.ContractTemplateDO;
import cn.iocoder.yudao.module.rental.service.contracttemplate.ContractTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 合同模板管理")
@RestController
@RequestMapping("/rental/contract-template")
@Validated
public class ContractTemplateController {

    @Resource
    private ContractTemplateService contractTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建合同模板")
    @PreAuthorize("@ss.hasPermission('rental:contract-template:create')")
    public CommonResult<Long> createContractTemplate(@Valid @RequestBody ContractTemplateSaveReqVO createReqVO) {
        return success(contractTemplateService.createContractTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同模板")
    @PreAuthorize("@ss.hasPermission('rental:contract-template:update')")
    public CommonResult<Boolean> updateContractTemplate(@Valid @RequestBody ContractTemplateSaveReqVO updateReqVO) {
        contractTemplateService.updateContractTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:contract-template:delete')")
    public CommonResult<Boolean> deleteContractTemplate(@RequestParam("id") Long id) {
        contractTemplateService.deleteContractTemplate(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同模板分页")
    @PreAuthorize("@ss.hasPermission('rental:contract-template:query')")
    public CommonResult<PageResult<ContractTemplateRespVO>> getContractTemplatePage(
            @Validated ContractTemplatePageReqVO pageReqVO) {
        PageResult<ContractTemplateDO> pageResult = contractTemplateService.getContractTemplatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ContractTemplateRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:contract-template:query')")
    public CommonResult<ContractTemplateRespVO> getContractTemplate(@RequestParam("id") Long id) {
        ContractTemplateDO template = contractTemplateService.getContractTemplate(id);
        return success(BeanUtils.toBean(template, ContractTemplateRespVO.class));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得启用中的合同模板精简列表", description = "用于「新建合同选模板」下拉")
    @PreAuthorize("@ss.hasPermission('rental:contract-template:query')")
    public CommonResult<List<ContractTemplateRespVO>> getEnabledTemplateList() {
        // 下拉只需要 id/名称/类型，不带 content —— 模板正文可能有几十 KB，
        // 列表接口带上会让每次打开合同表单都传一堆用不上的富文本
        List<ContractTemplateDO> list = contractTemplateService.getEnabledTemplateList();
        list.forEach(t -> t.setContent(null));
        return success(BeanUtils.toBean(list, ContractTemplateRespVO.class));
    }

    @GetMapping("/variables")
    @Operation(summary = "获得内置模板变量清单", description = "供管理端「插入变量」使用")
    @PreAuthorize("@ss.hasPermission('rental:contract-template:query')")
    public CommonResult<List<Map<String, String>>> getBuiltInVariables() {
        return success(contractTemplateService.getBuiltInVariables());
    }

}
