package cn.iocoder.yudao.module.rental.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo.DashboardDistributionRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo.DashboardOverviewRespVO;
import cn.iocoder.yudao.module.rental.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 首页看板")
@RestController
@RequestMapping("/rental/dashboard")
@Validated
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/get-overview")
    @Operation(summary = "获得首页综合数据")
    @PreAuthorize("@ss.hasPermission('rental:dashboard:query')")
    public CommonResult<DashboardOverviewRespVO> getOverview() {
        return success(dashboardService.getOverview());
    }

    @GetMapping("/get-distribution")
    @Operation(summary = "获得片区房源分布")
    @PreAuthorize("@ss.hasPermission('rental:dashboard:query')")
    public CommonResult<List<DashboardDistributionRespVO>> getDistribution() {
        return success(dashboardService.getDistribution());
    }

}
