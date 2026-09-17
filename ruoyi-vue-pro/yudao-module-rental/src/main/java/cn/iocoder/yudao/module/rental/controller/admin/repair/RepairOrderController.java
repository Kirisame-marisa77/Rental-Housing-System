package cn.iocoder.yudao.module.rental.controller.admin.repair;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairProgressRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairOrderDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairProgressDO;
import cn.iocoder.yudao.module.rental.service.repair.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 维修工单")
@RestController
@RequestMapping("/rental/repair-order")
@Validated
public class RepairOrderController {

    @Resource
    private RepairOrderService repairOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建维修工单")
    @PreAuthorize("@ss.hasPermission('rental:repair:create')")
    public CommonResult<Long> createRepairOrder(@Valid @RequestBody RepairOrderSaveReqVO createReqVO) {
        return success(repairOrderService.createRepairOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新维修工单")
    @PreAuthorize("@ss.hasPermission('rental:repair:update')")
    public CommonResult<Boolean> updateRepairOrder(@Valid @RequestBody RepairOrderSaveReqVO updateReqVO) {
        repairOrderService.updateRepairOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除维修工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:repair:delete')")
    public CommonResult<Boolean> deleteRepairOrder(@RequestParam("id") Long id) {
        repairOrderService.deleteRepairOrder(id);
        return success(true);
    }

    @PutMapping("/assign")
    @Operation(summary = "分配维修人员")
    @PreAuthorize("@ss.hasPermission('rental:repair:update')")
    public CommonResult<Boolean> assignRepairOrder(@RequestParam("id") Long id,
                                                   @RequestParam("repairerId") Long repairerId) {
        repairOrderService.assignRepairOrder(id, repairerId);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新工单状态")
    @PreAuthorize("@ss.hasPermission('rental:repair:update')")
    public CommonResult<Boolean> updateRepairOrderStatus(@RequestParam("id") Long id,
                                                         @RequestParam("status") Integer status) {
        repairOrderService.updateRepairOrderStatus(id, status);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "维修完成（待验收）")
    @PreAuthorize("@ss.hasPermission('rental:repair:update')")
    public CommonResult<Boolean> completeRepairOrder(@RequestParam("id") Long id,
                                                     @RequestParam(value = "repairDescription", required = false) String repairDescription) {
        repairOrderService.completeRepairOrder(id, repairDescription);
        return success(true);
    }

    @PutMapping("/handle")
    @Operation(summary = "业主处理维修并上传证据（进入待验收）")
    @PreAuthorize("@ss.hasPermission('rental:repair:update')")
    public CommonResult<Boolean> handleRepairOrder(@RequestParam("id") Long id,
                                                   @RequestParam("ownerId") Long ownerId,
                                                   @RequestParam(value = "repairDescription", required = false) String repairDescription,
                                                   @RequestParam(value = "handleEvidence", required = false) String handleEvidence) {
        repairOrderService.handleRepairOrder(id, ownerId, repairDescription, handleEvidence);
        return success(true);
    }

    @PutMapping("/review")
    @Operation(summary = "管理员验收判定维修是否合格")
    @PreAuthorize("@ss.hasPermission('rental:repair:update')")
    public CommonResult<Boolean> reviewRepairOrder(@RequestParam("id") Long id,
                                                   @RequestParam("pass") Boolean pass,
                                                   @RequestParam(value = "reason", required = false) String reason) {
        repairOrderService.reviewRepairOrder(id, pass, reason);
        return success(true);
    }

    @PutMapping("/confirm")
    @Operation(summary = "租客确认维修完成（本期由管理端代操作）")
    @PreAuthorize("@ss.hasPermission('rental:repair:update')")
    public CommonResult<Boolean> confirmRepairOrder(@RequestParam("id") Long id) {
        repairOrderService.confirmRepairOrder(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得维修工单分页")
    @PreAuthorize("@ss.hasPermission('rental:repair:query')")
    public CommonResult<PageResult<RepairOrderRespVO>> getRepairOrderPage(@Validated RepairOrderPageReqVO pageReqVO) {
        PageResult<RepairOrderDO> pageResult = repairOrderService.getRepairOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RepairOrderRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得维修工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:repair:query')")
    public CommonResult<RepairOrderRespVO> getRepairOrder(@RequestParam("id") Long id) {
        RepairOrderDO order = repairOrderService.getRepairOrder(id);
        return success(BeanUtils.toBean(order, RepairOrderRespVO.class));
    }

    @GetMapping("/get-progress")
    @Operation(summary = "获得维修进度列表")
    @Parameter(name = "orderId", description = "工单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:repair:query')")
    public CommonResult<List<RepairProgressRespVO>> getRepairProgressList(@RequestParam("orderId") Long orderId) {
        List<RepairProgressDO> list = repairOrderService.getRepairProgressList(orderId);
        return success(BeanUtils.toBean(list, RepairProgressRespVO.class));
    }

}
