package cn.iocoder.yudao.module.rental.service.dashboard;

import cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo.DashboardDistributionRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo.DashboardOverviewRespVO;

import java.util.List;

/**
 * 首页看板 Service 接口
 *
 * @author yudao
 */
public interface DashboardService {

    /**
     * 获得首页综合数据
     *
     * @return 综合数据
     */
    DashboardOverviewRespVO getOverview();

    /**
     * 获得片区房源分布
     *
     * @return 分布数据
     */
    List<DashboardDistributionRespVO> getDistribution();

}
