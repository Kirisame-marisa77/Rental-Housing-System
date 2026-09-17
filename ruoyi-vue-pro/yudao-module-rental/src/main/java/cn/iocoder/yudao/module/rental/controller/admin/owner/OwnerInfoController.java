package cn.iocoder.yudao.module.rental.controller.admin.owner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.service.owner.OwnerInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 业主")
@RestController
@RequestMapping("/rental/owner")
@Validated
public class OwnerInfoController {

    @Resource
    private OwnerInfoService ownerInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建业主")
    @PreAuthorize("@ss.hasPermission('rental:owner:create')")
    public CommonResult<Long> createOwnerInfo(@Valid @RequestBody OwnerInfoSaveReqVO createReqVO) {
        return success(ownerInfoService.createOwnerInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新业主")
    @PreAuthorize("@ss.hasPermission('rental:owner:update')")
    public CommonResult<Boolean> updateOwnerInfo(@Valid @RequestBody OwnerInfoSaveReqVO updateReqVO) {
        ownerInfoService.updateOwnerInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除业主")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:owner:delete')")
    public CommonResult<Boolean> deleteOwnerInfo(@RequestParam("id") Long id) {
        ownerInfoService.deleteOwnerInfo(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得业主分页")
    @PreAuthorize("@ss.hasPermission('rental:owner:query')")
    public CommonResult<PageResult<OwnerInfoRespVO>> getOwnerInfoPage(@Validated OwnerInfoPageReqVO pageReqVO) {
        PageResult<OwnerInfoDO> pageResult = ownerInfoService.getOwnerInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OwnerInfoRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得业主")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:owner:query')")
    public CommonResult<OwnerInfoRespVO> getOwnerInfo(@RequestParam("id") Long id) {
        OwnerInfoDO owner = ownerInfoService.getOwnerInfo(id);
        return success(BeanUtils.toBean(owner, OwnerInfoRespVO.class));
    }

}
