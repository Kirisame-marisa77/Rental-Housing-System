package cn.iocoder.yudao.module.rental.controller.admin.moveout;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.SettlementBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.SettlementBillRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.SettlementBillDO;
import cn.iocoder.yudao.module.rental.service.moveout.SettlementBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 退租结算单")
@RestController
@RequestMapping("/rental/settlement-bill")
@Validated
public class SettlementBillController {

    @Resource
    private SettlementBillService settlementBillService;

    @GetMapping("/page")
    @Operation(summary = "获得退租结算单分页")
    @PreAuthorize("@ss.hasPermission('rental:moveout:query')")
    public CommonResult<PageResult<SettlementBillRespVO>> getSettlementBillPage(@Validated SettlementBillPageReqVO pageReqVO) {
        PageResult<SettlementBillDO> pageResult = settlementBillService.getSettlementBillPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SettlementBillRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得退租结算单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:moveout:query')")
    public CommonResult<SettlementBillRespVO> getSettlementBill(@RequestParam("id") Long id) {
        SettlementBillDO settlement = settlementBillService.getSettlementBill(id);
        return success(BeanUtils.toBean(settlement, SettlementBillRespVO.class));
    }

}
