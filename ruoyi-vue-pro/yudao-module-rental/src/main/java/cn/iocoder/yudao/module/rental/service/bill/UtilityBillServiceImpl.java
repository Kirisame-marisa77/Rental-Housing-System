package cn.iocoder.yudao.module.rental.service.bill;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.PaymentRecordDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.UtilityBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.PaymentRecordMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.UtilityBillMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.UTILITY_BILL_NOT_EXISTS;

/**
 * 水电费账单 Service 实现类
 *
 * @author yudao
 */
@Service
public class UtilityBillServiceImpl implements UtilityBillService {

    @Resource
    private UtilityBillMapper utilityBillMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    /**
     * 只用来查「业主名下的房源」。Mapper 是叶子节点，不构成 Service → Service 的环
     */
    @Resource
    private HouseMapper houseMapper;

    @Override
    public Long createUtilityBill(UtilityBillSaveReqVO createReqVO) {
        UtilityBillDO bill = BeanUtils.toBean(createReqVO, UtilityBillDO.class);
        if (StrUtil.isBlank(bill.getBillNo())) {
            bill.setBillNo(generateBillNo());
        }
        utilityBillMapper.insert(bill);
        return bill.getId();
    }

    @Override
    public void updateUtilityBill(UtilityBillSaveReqVO updateReqVO) {
        validateUtilityBillExists(updateReqVO.getId());
        UtilityBillDO updateObj = BeanUtils.toBean(updateReqVO, UtilityBillDO.class);
        utilityBillMapper.updateById(updateObj);
    }

    @Override
    public void deleteUtilityBill(Long id) {
        validateUtilityBillExists(id);
        utilityBillMapper.deleteById(id);
    }

    @Override
    public PageResult<UtilityBillRespVO> getUtilityBillRespPage(UtilityBillPageReqVO pageReqVO) {
        PageResult<UtilityBillDO> pageResult = utilityBillMapper.selectPage(pageReqVO);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(enrichUtilityBills(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public PageResult<UtilityBillRespVO> getOwnerUtilityBillPage(Long ownerId, UtilityBillPageReqVO pageReqVO) {
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>()
                .eq(HouseDO::getOwnerId, ownerId));
        // 没有房源的房东必须直接返回空页：inIfPresent 在集合为空时不会拼接条件，
        // 直接查库会把全系统的账单都返回给他
        if (houses.isEmpty()) {
            return PageResult.empty();
        }
        // 无条件覆盖，不能信任前端传来的 houseIds（@Schema(hidden) 只隐藏文档，不拦参数）
        pageReqVO.setHouseIds(houses.stream().map(HouseDO::getId).collect(Collectors.toList()));
        return getUtilityBillRespPage(pageReqVO);
    }

    /**
     * 批量回填房源地址，供账单列表展示「小区 楼栋 房号」
     */
    private List<UtilityBillRespVO> enrichUtilityBills(List<UtilityBillDO> bills) {
        Set<Long> houseIds = bills.stream().map(UtilityBillDO::getHouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        List<UtilityBillRespVO> list = new ArrayList<>(bills.size());
        for (UtilityBillDO bill : bills) {
            UtilityBillRespVO vo = BeanUtils.toBean(bill, UtilityBillRespVO.class);
            HouseDO house = houseMap.get(bill.getHouseId());
            if (house != null) {
                vo.setCommunityName(house.getCommunityName());
                vo.setBuildingNo(house.getBuildingNo());
                vo.setRoomNo(house.getRoomNo());
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payUtilityBill(Long id, String payMethod, Long payee, String transactionNo, String remark) {
        UtilityBillDO bill = validateUtilityBillExists(id);
        bill.setPayStatus(1);
        bill.setPayTime(LocalDateTime.now());
        bill.setPayMethod(payMethod);
        bill.setPayee(payee);
        utilityBillMapper.updateById(bill);
        // 写入缴费记录
        PaymentRecordDO record = new PaymentRecordDO();
        record.setBillType(1); // 水电费
        record.setBillId(id);
        record.setPayAmount(bill.getTotalAmount());
        record.setPayMethod(payMethod);
        record.setPayTime(LocalDateTime.now());
        record.setPayee(payee);
        record.setTransactionNo(transactionNo);
        record.setRemark(remark);
        paymentRecordMapper.insert(record);
    }

    @Override
    public PageResult<UtilityBillDO> getUtilityBillPage(UtilityBillPageReqVO pageReqVO) {
        return utilityBillMapper.selectPage(pageReqVO);
    }

    @Override
    public UtilityBillDO getUtilityBill(Long id) {
        return utilityBillMapper.selectById(id);
    }

    @VisibleForTesting
    public UtilityBillDO validateUtilityBillExists(Long id) {
        if (id == null) {
            return null;
        }
        UtilityBillDO bill = utilityBillMapper.selectById(id);
        if (bill == null) {
            throw exception(UTILITY_BILL_NOT_EXISTS);
        }
        return bill;
    }

    /**
     * 生成账单编号：SD + 年月日时分秒
     */
    private String generateBillNo() {
        return "SD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
