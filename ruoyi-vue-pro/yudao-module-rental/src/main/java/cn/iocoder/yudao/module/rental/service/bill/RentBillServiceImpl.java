package cn.iocoder.yudao.module.rental.service.bill;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.PaymentRecordDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.PaymentRecordMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.RentBillMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.service.contract.ContractService;
import cn.iocoder.yudao.module.rental.util.PaymentMethodUtils;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.RENT_BILL_ALREADY_PAID;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.RENT_BILL_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.RENT_BILL_NOT_OWNER;

/**
 * 租金账单 Service 实现类
 *
 * @author yudao
 */
@Service
public class RentBillServiceImpl implements RentBillService {

    /**
     * 首期账单的缴费期限（天）
     */
    private static final int FIRST_BILL_DUE_DAYS = 7;

    @Resource
    private RentBillMapper rentBillMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    /**
     * 注意：账单 → 合同是单向依赖。合同侧查账单走的是 RentBillMapper，不能反向注入本类，否则循环依赖
     */
    @Resource
    private ContractService contractService;
    /**
     * 只用来查「业主名下的房源」。Mapper 是叶子节点，不构成 Service → Service 的环
     */
    @Resource
    private HouseMapper houseMapper;

    @Override
    public Long createRentBill(RentBillSaveReqVO createReqVO) {
        RentBillDO bill = BeanUtils.toBean(createReqVO, RentBillDO.class);
        // 账单编号为空时自动生成
        if (StrUtil.isBlank(bill.getBillNo())) {
            bill.setBillNo(generateBillNo());
        }
        rentBillMapper.insert(bill);
        return bill.getId();
    }

    @Override
    public void updateRentBill(RentBillSaveReqVO updateReqVO) {
        // 校验是否存在
        validateRentBillExists(updateReqVO.getId());
        // 更新租金账单
        RentBillDO updateObj = BeanUtils.toBean(updateReqVO, RentBillDO.class);
        rentBillMapper.updateById(updateObj);
    }

    @Override
    public void deleteRentBill(Long id) {
        // 校验是否存在
        validateRentBillExists(id);
        // 删除租金账单
        rentBillMapper.deleteById(id);
    }

    @Override
    public PageResult<RentBillDO> getRentBillPage(RentBillPageReqVO pageReqVO) {
        return rentBillMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<RentBillRespVO> getRentBillRespPage(RentBillPageReqVO pageReqVO) {
        PageResult<RentBillDO> pageResult = rentBillMapper.selectPage(pageReqVO);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(enrichRentBills(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public PageResult<RentBillRespVO> getOwnerRentBillPage(Long ownerId, RentBillPageReqVO pageReqVO) {
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>()
                .eq(HouseDO::getOwnerId, ownerId));
        // 没有房源的房东必须直接返回空页：inIfPresent 在集合为空时不会拼接条件，
        // 直接查库会把全系统的账单都返回给他
        if (houses.isEmpty()) {
            return PageResult.empty();
        }
        // 无条件覆盖，不能信任前端传来的 houseIds（@Schema(hidden) 只隐藏文档，不拦参数）
        pageReqVO.setHouseIds(houses.stream().map(HouseDO::getId).collect(Collectors.toList()));
        return getRentBillRespPage(pageReqVO);
    }

    /**
     * 批量回填房源地址，供账单列表展示「小区 楼栋 房号」
     */
    private List<RentBillRespVO> enrichRentBills(List<RentBillDO> bills) {
        Set<Long> houseIds = bills.stream().map(RentBillDO::getHouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        List<RentBillRespVO> list = new ArrayList<>(bills.size());
        for (RentBillDO bill : bills) {
            RentBillRespVO vo = BeanUtils.toBean(bill, RentBillRespVO.class);
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
    public RentBillDO getRentBill(Long id) {
        return rentBillMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFirstRentBillForContract(ContractDO contract) {
        LocalDate rentStartDate = contract.getRentStartDate() == null ? LocalDate.now() : contract.getRentStartDate();
        BigDecimal monthlyRent = contract.getMonthlyRent() == null ? BigDecimal.ZERO : contract.getMonthlyRent();
        BigDecimal depositAmount = contract.getDepositAmount() == null ? BigDecimal.ZERO : contract.getDepositAmount();
        // 「押N付M」的付数决定首期一次交几个月租金，账期也跟着走 M 个月。
        // 原实现写死 1 个月，导致押一付三的租客首期只交了 1 个月租金。
        int payMonths = PaymentMethodUtils.payMonths(contract.getPaymentMethod());
        BigDecimal rentAmount = monthlyRent.multiply(BigDecimal.valueOf(payMonths));

        RentBillDO bill = new RentBillDO();
        // 编号带上合同 ID，避免同一秒内生成的账单编号重复（uk_bill_no 唯一索引）
        bill.setBillNo("ZD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + contract.getId());
        bill.setContractId(contract.getId());
        bill.setTenantUserId(contract.getTenantUserId());
        bill.setHouseId(contract.getHouseId());
        bill.setBillType(0); // 首期账单
        bill.setPeriodStart(rentStartDate);
        bill.setPeriodEnd(rentStartDate.plusMonths(payMonths).minusDays(1));
        bill.setRentAmount(rentAmount);
        bill.setDepositAmount(depositAmount);
        bill.setTotalAmount(rentAmount.add(depositAmount));
        bill.setFeeDetail(String.format("首期：%d个月租金%s + 押金%s（%s）",
                payMonths, rentAmount.toPlainString(), depositAmount.toPlainString(),
                StrUtil.blankToDefault(contract.getPaymentMethod(), PaymentMethodUtils.DEFAULT)));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setPayStatus(0); // 待缴费
        bill.setDueDate(LocalDate.now().plusDays(FIRST_BILL_DUE_DAYS));
        bill.setLateFee(BigDecimal.ZERO);
        bill.setLateFeeWaived(BigDecimal.ZERO);
        rentBillMapper.insert(bill);
        return bill.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payRentBill(Long id, Long tenantId, String payMethod, Long payee, String transactionNo, String remark) {
        RentBillDO bill = rentBillMapper.selectById(id);
        if (bill == null) {
            throw exception(RENT_BILL_NOT_EXISTS);
        }
        // 只能缴自己的账单
        if (tenantId != null && !tenantId.equals(bill.getTenantUserId())) {
            throw exception(RENT_BILL_NOT_OWNER);
        }
        if (!Integer.valueOf(0).equals(bill.getPayStatus())) {
            throw exception(RENT_BILL_ALREADY_PAID);
        }
        // 乐观锁：并发重复缴费只有一个能成功
        RentBillDO updateObj = new RentBillDO();
        updateObj.setPayStatus(1);
        updateObj.setPayTime(LocalDateTime.now());
        updateObj.setPayMethod(payMethod);
        updateObj.setPayee(payee);
        updateObj.setPaidAmount(bill.getTotalAmount());
        if (rentBillMapper.updatePayStatusByIdAndPayStatus(id, 0, updateObj) == 0) {
            throw exception(RENT_BILL_ALREADY_PAID);
        }
        // 写入缴费记录
        PaymentRecordDO record = new PaymentRecordDO();
        record.setBillType(0); // 租金/物业费
        record.setBillId(id);
        record.setPayAmount(bill.getTotalAmount());
        record.setPayMethod(payMethod);
        record.setPayTime(LocalDateTime.now());
        record.setPayee(payee);
        record.setTransactionNo(transactionNo);
        record.setRemark(remark);
        paymentRecordMapper.insert(record);
        // 缴费可能正是合同成交的最后一环，顺手尝试让其生效（先到先得）
        if (bill.getContractId() != null) {
            contractService.tryActivateContract(bill.getContractId());
        }
    }

    @VisibleForTesting
    public void validateRentBillExists(Long id) {
        if (id == null) {
            return;
        }
        if (rentBillMapper.selectById(id) == null) {
            throw exception(RENT_BILL_NOT_EXISTS);
        }
    }

    /**
     * 生成账单编号：ZD + 年月日时分秒
     */
    private String generateBillNo() {
        return "ZD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
