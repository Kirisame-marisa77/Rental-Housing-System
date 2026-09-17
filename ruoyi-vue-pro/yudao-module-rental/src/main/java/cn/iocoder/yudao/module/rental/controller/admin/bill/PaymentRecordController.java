package cn.iocoder.yudao.module.rental.controller.admin.bill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.PaymentRecordPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.PaymentRecordRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.PaymentRecordDO;
import cn.iocoder.yudao.module.rental.service.bill.PaymentRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 缴费记录")
@RestController
@RequestMapping("/rental/payment-record")
@Validated
public class PaymentRecordController {

    @Resource
    private PaymentRecordService paymentRecordService;

    @GetMapping("/page")
    @Operation(summary = "获得缴费记录分页")
    @PreAuthorize("@ss.hasPermission('rental:bill:query')")
    public CommonResult<PageResult<PaymentRecordRespVO>> getPaymentRecordPage(@Validated PaymentRecordPageReqVO pageReqVO) {
        PageResult<PaymentRecordDO> pageResult = paymentRecordService.getPaymentRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PaymentRecordRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得缴费记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:bill:query')")
    public CommonResult<PaymentRecordRespVO> getPaymentRecord(@RequestParam("id") Long id) {
        PaymentRecordDO record = paymentRecordService.getPaymentRecord(id);
        return success(BeanUtils.toBean(record, PaymentRecordRespVO.class));
    }

}
