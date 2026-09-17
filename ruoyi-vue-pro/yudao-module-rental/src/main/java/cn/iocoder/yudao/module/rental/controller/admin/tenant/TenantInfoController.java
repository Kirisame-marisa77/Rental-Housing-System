package cn.iocoder.yudao.module.rental.controller.admin.tenant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.service.tenant.TenantInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租客")
@RestController
@RequestMapping("/rental/tenant")
@Validated
public class TenantInfoController {

    @Resource
    private TenantInfoService tenantInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建租客")
    @PreAuthorize("@ss.hasPermission('rental:tenant:create')")
    public CommonResult<Long> createTenantInfo(@Valid @RequestBody TenantInfoSaveReqVO createReqVO) {
        return success(tenantInfoService.createTenantInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新租客")
    @PreAuthorize("@ss.hasPermission('rental:tenant:update')")
    public CommonResult<Boolean> updateTenantInfo(@Valid @RequestBody TenantInfoSaveReqVO updateReqVO) {
        tenantInfoService.updateTenantInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除租客")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:tenant:delete')")
    public CommonResult<Boolean> deleteTenantInfo(@RequestParam("id") Long id) {
        tenantInfoService.deleteTenantInfo(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得租客分页")
    @PreAuthorize("@ss.hasPermission('rental:tenant:query')")
    public CommonResult<PageResult<TenantInfoRespVO>> getTenantInfoPage(@Validated TenantInfoPageReqVO pageReqVO) {
        PageResult<TenantInfoDO> pageResult = tenantInfoService.getTenantInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, TenantInfoRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得租客")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:tenant:query')")
    public CommonResult<TenantInfoRespVO> getTenantInfo(@RequestParam("id") Long id) {
        TenantInfoDO tenant = tenantInfoService.getTenantInfo(id);
        return success(BeanUtils.toBean(tenant, TenantInfoRespVO.class));
    }

}
