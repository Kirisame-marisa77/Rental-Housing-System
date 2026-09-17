package cn.iocoder.yudao.module.rental.controller.admin.bill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.UtilityBillDO;
import cn.iocoder.yudao.module.rental.service.bill.UtilityBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 水电费账单")
@RestController
@RequestMapping("/rental/utility-bill")
@Validated
public class UtilityBillController {

    @Resource
    private UtilityBillService utilityBillService;

    @PostMapping("/create")
    @Operation(summary = "创建水电费账单")
    @PreAuthorize("@ss.hasPermission('rental:bill:create')")
    public CommonResult<Long> createUtilityBill(@Valid @RequestBody UtilityBillSaveReqVO createReqVO) {
        return success(utilityBillService.createUtilityBill(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新水电费账单")
    @PreAuthorize("@ss.hasPermission('rental:bill:update')")
    public CommonResult<Boolean> updateUtilityBill(@Valid @RequestBody UtilityBillSaveReqVO updateReqVO) {
        utilityBillService.updateUtilityBill(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除水电费账单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:bill:delete')")
    public CommonResult<Boolean> deleteUtilityBill(@RequestParam("id") Long id) {
        utilityBillService.deleteUtilityBill(id);
        return success(true);
    }

    @PutMapping("/pay")
    @Operation(summary = "水电费缴费登记")
    @PreAuthorize("@ss.hasPermission('rental:bill:update')")
    public CommonResult<Boolean> payUtilityBill(@RequestParam("id") Long id,
                                                @RequestParam("payMethod") String payMethod,
                                                @RequestParam(value = "payee", required = false) Long payee,
                                                @RequestParam(value = "transactionNo", required = false) String transactionNo,
                                                @RequestParam(value = "remark", required = false) String remark) {
        utilityBillService.payUtilityBill(id, payMethod, payee, transactionNo, remark);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得水电费账单分页")
    @PreAuthorize("@ss.hasPermission('rental:bill:query')")
    public CommonResult<PageResult<UtilityBillRespVO>> getUtilityBillPage(@Validated UtilityBillPageReqVO pageReqVO) {
        PageResult<UtilityBillDO> pageResult = utilityBillService.getUtilityBillPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UtilityBillRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得水电费账单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:bill:query')")
    public CommonResult<UtilityBillRespVO> getUtilityBill(@RequestParam("id") Long id) {
        UtilityBillDO bill = utilityBillService.getUtilityBill(id);
        return success(BeanUtils.toBean(bill, UtilityBillRespVO.class));
    }

}
