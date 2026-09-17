package cn.iocoder.yudao.module.rental.controller.admin.moveout;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.*;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.MoveOutApplicationDO;
import cn.iocoder.yudao.module.rental.service.moveout.MoveOutApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 退租申请")
@RestController
@RequestMapping("/rental/move-out")
@Validated
public class MoveOutApplicationController {

    @Resource
    private MoveOutApplicationService moveOutApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建退租申请")
    @PreAuthorize("@ss.hasPermission('rental:moveout:create')")
    public CommonResult<Long> createMoveOutApplication(@Valid @RequestBody MoveOutApplicationSaveReqVO createReqVO) {
        return success(moveOutApplicationService.createMoveOutApplication(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新退租申请")
    @PreAuthorize("@ss.hasPermission('rental:moveout:update')")
    public CommonResult<Boolean> updateMoveOutApplication(@Valid @RequestBody MoveOutApplicationSaveReqVO updateReqVO) {
        moveOutApplicationService.updateMoveOutApplication(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除退租申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:moveout:delete')")
    public CommonResult<Boolean> deleteMoveOutApplication(@RequestParam("id") Long id) {
        moveOutApplicationService.deleteMoveOutApplication(id);
        return success(true);
    }

    @PutMapping("/confirm")
    @Operation(summary = "处理退租申请（房屋验收 + 费用结算）")
    @PreAuthorize("@ss.hasPermission('rental:moveout:update')")
    public CommonResult<Long> confirmMoveOutApplication(@Valid @RequestBody MoveOutConfirmReqVO confirmReqVO) {
        return success(moveOutApplicationService.confirmMoveOutApplication(confirmReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得退租申请分页")
    @PreAuthorize("@ss.hasPermission('rental:moveout:query')")
    public CommonResult<PageResult<MoveOutApplicationRespVO>> getMoveOutApplicationPage(@Validated MoveOutApplicationPageReqVO pageReqVO) {
        PageResult<MoveOutApplicationDO> pageResult = moveOutApplicationService.getMoveOutApplicationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MoveOutApplicationRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得退租申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:moveout:query')")
    public CommonResult<MoveOutApplicationRespVO> getMoveOutApplication(@RequestParam("id") Long id) {
        MoveOutApplicationDO application = moveOutApplicationService.getMoveOutApplication(id);
        return success(BeanUtils.toBean(application, MoveOutApplicationRespVO.class));
    }

}
