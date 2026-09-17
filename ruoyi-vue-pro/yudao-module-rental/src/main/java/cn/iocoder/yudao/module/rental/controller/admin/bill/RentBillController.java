package cn.iocoder.yudao.module.rental.controller.admin.bill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import cn.iocoder.yudao.module.rental.service.bill.RentBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租金账单")
@RestController
@RequestMapping("/rental/rent-bill")
@Validated
public class RentBillController {

    @Resource
    private RentBillService rentBillService;

    @PostMapping("/create")
    @Operation(summary = "创建租金账单")
    @PreAuthorize("@ss.hasPermission('rental:bill:create')")
    public CommonResult<Long> createRentBill(@Valid @RequestBody RentBillSaveReqVO createReqVO) {
        return success(rentBillService.createRentBill(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新租金账单")
    @PreAuthorize("@ss.hasPermission('rental:bill:update')")
    public CommonResult<Boolean> updateRentBill(@Valid @RequestBody RentBillSaveReqVO updateReqVO) {
        rentBillService.updateRentBill(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除租金账单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:bill:delete')")
    public CommonResult<Boolean> deleteRentBill(@RequestParam("id") Long id) {
        rentBillService.deleteRentBill(id);
        return success(true);
    }

    @PutMapping("/pay")
    @Operation(summary = "租金账单缴费登记")
    @PreAuthorize("@ss.hasPermission('rental:bill:update')")
    public CommonResult<Boolean> payRentBill(@RequestParam("id") Long id,
                                             @RequestParam(value = "payMethod", required = false) String payMethod,
                                             @RequestParam(value = "payee", required = false) Long payee,
                                             @RequestParam(value = "transactionNo", required = false) String transactionNo,
                                             @RequestParam(value = "remark", required = false) String remark) {
        // tenantId 传 null 表示管理员代收，不做租客归属校验
        rentBillService.payRentBill(id, null, payMethod, payee, transactionNo, remark);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得租金账单分页")
    @PreAuthorize("@ss.hasPermission('rental:bill:query')")
    public CommonResult<PageResult<RentBillRespVO>> getRentBillPage(@Validated RentBillPageReqVO pageReqVO) {
        PageResult<RentBillDO> pageResult = rentBillService.getRentBillPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RentBillRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得租金账单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:bill:query')")
    public CommonResult<RentBillRespVO> getRentBill(@RequestParam("id") Long id) {
        RentBillDO bill = rentBillService.getRentBill(id);
        return success(BeanUtils.toBean(bill, RentBillRespVO.class));
    }

}
