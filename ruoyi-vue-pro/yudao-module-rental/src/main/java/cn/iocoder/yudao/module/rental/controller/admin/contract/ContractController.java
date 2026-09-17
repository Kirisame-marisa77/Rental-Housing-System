package cn.iocoder.yudao.module.rental.controller.admin.contract;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.service.contract.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租房合同")
@RestController
@RequestMapping("/rental/contract")
@Validated
public class ContractController {

    @Resource
    private ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('rental:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody ContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('rental:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody ContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthorize("@ss.hasPermission('rental:contract:query')")
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Validated ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractService.getContractPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ContractRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:contract:query')")
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        // 走 getContractResp 而不是 getContract：前者会补全房源/双方/首期账单信息，
        // 详情弹窗要展示这些，裸 DO 里只有 ID
        return success(contractService.getContractResp(id));
    }

    @PutMapping("/renew")
    @Operation(summary = "租约续签")
    @PreAuthorize("@ss.hasPermission('rental:contract:update')")
    public CommonResult<Boolean> renewContract(@RequestParam("id") Long id,
                                               @RequestParam("newEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate newEndDate) {
        contractService.renewContract(id, newEndDate);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:contract:update')")
    public CommonResult<Boolean> cancelContract(@RequestParam("id") Long id) {
        contractService.cancelContract(id);
        return success(true);
    }

    @GetMapping("/get-expiring")
    @Operation(summary = "查询即将到期的合同")
    @PreAuthorize("@ss.hasPermission('rental:contract:query')")
    public CommonResult<List<ContractRespVO>> getExpiringContractList(@RequestParam(value = "days", defaultValue = "60") Integer days) {
        return success(contractService.getExpiringContractRespList(days));
    }

}
