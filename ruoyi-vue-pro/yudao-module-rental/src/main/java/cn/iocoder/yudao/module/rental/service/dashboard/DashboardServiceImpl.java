package cn.iocoder.yudao.module.rental.service.dashboard;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo.DashboardDistributionRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.dashboard.vo.DashboardOverviewRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.apply.ApplyDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.UtilityBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairOrderDO;
import cn.iocoder.yudao.module.rental.dal.mysql.apply.ApplyMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.RentBillMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.UtilityBillMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.repair.RepairOrderMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页看板 Service 实现类
 *
 * @author yudao
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private HouseMapper houseMapper;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private RepairOrderMapper repairOrderMapper;
    @Resource
    private ApplyMapper applyMapper;
    @Resource
    private RentBillMapper rentBillMapper;
    @Resource
    private UtilityBillMapper utilityBillMapper;

    @Override
    public DashboardOverviewRespVO getOverview() {
        DashboardOverviewRespVO vo = new DashboardOverviewRespVO();
        vo.setHouseCount(houseMapper.selectCount(null));
        vo.setRentedCount(houseMapper.selectCount(new LambdaQueryWrapperX<HouseDO>().eq(HouseDO::getStatus, 3)));
        vo.setOnSaleCount(houseMapper.selectCount(new LambdaQueryWrapperX<HouseDO>().eq(HouseDO::getStatus, 1)));
        vo.setOffSaleCount(houseMapper.selectCount(new LambdaQueryWrapperX<HouseDO>().eq(HouseDO::getStatus, 0)));
        vo.setPendingApplyCount(applyMapper.selectCount(new LambdaQueryWrapperX<ApplyDO>().eq(ApplyDO::getStatus, 0)));
        vo.setPendingRepairCount(repairOrderMapper.selectCount(new LambdaQueryWrapperX<RepairOrderDO>().in(RepairOrderDO::getStatus, 0, 1, 2, 3)));
        Long pendingRentBill = rentBillMapper.selectCount(new LambdaQueryWrapperX<RentBillDO>().in(RentBillDO::getPayStatus, 0, 2));
        Long pendingUtilityBill = utilityBillMapper.selectCount(new LambdaQueryWrapperX<UtilityBillDO>().in(UtilityBillDO::getPayStatus, 0, 2));
        vo.setPendingBillCount(pendingRentBill + pendingUtilityBill);
        vo.setExpiringContractCount(contractMapper.selectCount(new LambdaQueryWrapperX<ContractDO>()
                .le(ContractDO::getRentEndDate, LocalDate.now().plusDays(60))
                .in(ContractDO::getStatus, 2, 3)));
        return vo;
    }

    @Override
    public List<DashboardDistributionRespVO> getDistribution() {
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>());
        Map<String, Long> group = houses.stream()
                .collect(Collectors.groupingBy(HouseDO::getArea, Collectors.counting()));
        return group.entrySet().stream()
                .map(entry -> {
                    DashboardDistributionRespVO vo = new DashboardDistributionRespVO();
                    vo.setArea(entry.getKey());
                    vo.setCount(entry.getValue());
                    return vo;
                })
                .collect(Collectors.toList());
    }

}
