package cn.iocoder.yudao.module.rental.service.meter;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.meter.MeterReadingDO;
import cn.iocoder.yudao.module.rental.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.meter.MeterReadingMapper;
import cn.iocoder.yudao.module.rental.service.bill.UtilityBillService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.*;

/**
 * 抄表记录 Service 实现类
 *
 * @author yudao
 */
@Service
public class MeterReadingServiceImpl implements MeterReadingService {

    @Resource
    private MeterReadingMapper meterReadingMapper;
    @Resource
    private MeterConfigService meterConfigService;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private UtilityBillService utilityBillService;

    @Override
    public Long createMeterReading(MeterReadingSaveReqVO createReqVO) {
        MeterReadingDO reading = BeanUtils.toBean(createReqVO, MeterReadingDO.class);
        // 上期读数：取该房源/合同/类型最新一条的本期读数
        MeterReadingDO latest = meterReadingMapper.selectLatest(
                createReqVO.getHouseId(), createReqVO.getContractId(), createReqVO.getMeterType());
        BigDecimal lastReading = latest != null && latest.getCurrentReading() != null
                ? latest.getCurrentReading() : BigDecimal.ZERO;
        // 校验：本期读数不能小于上期
        if (reading.getCurrentReading().compareTo(lastReading) < 0) {
            throw exception(METER_READING_INVALID);
        }
        // 单价：未填则取抄表配置
        BigDecimal unitPrice = reading.getUnitPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            String configValue = meterConfigService.getConfigValue(
                    Integer.valueOf(0).equals(reading.getMeterType()) ? "water_price" : "electricity_price");
            unitPrice = configValue == null ? BigDecimal.ZERO : new BigDecimal(configValue);
        }
        // 计算用量与费用
        BigDecimal usage = reading.getCurrentReading().subtract(lastReading);
        reading.setLastReading(lastReading);
        reading.setUsageAmount(usage);
        reading.setUnitPrice(unitPrice);
        reading.setFeeAmount(usage.multiply(unitPrice));
        reading.setStatus(reading.getStatus() == null ? 0 : reading.getStatus());
        reading.setReviewStatus(1); // 管理员录入，视为已审核
        reading.setOperatorId(getLoginUserId());
        meterReadingMapper.insert(reading);
        return reading.getId();
    }

    @Override
    public Long createMeterReadingByOwner(MeterReadingSaveReqVO createReqVO) {
        MeterReadingDO reading = BeanUtils.toBean(createReqVO, MeterReadingDO.class);
        // 上期读数
        MeterReadingDO latest = meterReadingMapper.selectLatest(
                createReqVO.getHouseId(), createReqVO.getContractId(), createReqVO.getMeterType());
        BigDecimal lastReading = latest != null && latest.getCurrentReading() != null
                ? latest.getCurrentReading() : BigDecimal.ZERO;
        BigDecimal lastValley = latest != null && latest.getValleyReading() != null
                ? latest.getValleyReading() : BigDecimal.ZERO;
        // 校验：本期读数不能小于上期
        if (reading.getCurrentReading().compareTo(lastReading) < 0) {
            throw exception(METER_READING_INVALID);
        }
        reading.setLastReading(lastReading);
        reading.setUsageAmount(reading.getCurrentReading().subtract(lastReading));
        // 电表：峰谷则校验并计算谷段，统一价则无谷段
        if (Integer.valueOf(1).equals(reading.getMeterType())) {
            HouseDO house = houseMapper.selectById(createReqVO.getHouseId());
            boolean peakValley = house != null && Integer.valueOf(1).equals(house.getElectricityBillType());
            if (peakValley) {
                if (reading.getValleyReading() == null) {
                    throw exception(METER_READING_INVALID);
                }
                if (reading.getValleyReading().compareTo(lastValley) < 0) {
                    throw exception(METER_READING_INVALID);
                }
                reading.setLastValleyReading(lastValley);
                reading.setValleyUsage(reading.getValleyReading().subtract(lastValley));
            }
        }
        reading.setReviewStatus(0); // 待审核
        reading.setOperatorId(null); // 业主上传，无 sys_user 操作人
        meterReadingMapper.insert(reading);
        return reading.getId();
    }

    @Override
    public void updateMeterReading(MeterReadingSaveReqVO updateReqVO) {
        validateMeterReadingExists(updateReqVO.getId());
        MeterReadingDO updateObj = BeanUtils.toBean(updateReqVO, MeterReadingDO.class);
        meterReadingMapper.updateById(updateObj);
    }

    @Override
    public void deleteMeterReading(Long id) {
        validateMeterReadingExists(id);
        meterReadingMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmMeterReading(Long id) {
        MeterReadingDO reading = validateMeterReadingExists(id);
        if (reading.getBillId() != null) {
            throw exception(METER_READING_BILL_GENERATED);
        }
        // 从合同取租客
        ContractDO contract = contractMapper.selectById(reading.getContractId());
        Long tenantUserId = contract != null && contract.getTenantUserId() != null
                ? contract.getTenantUserId() : 0L;
        // 生成水电费账单
        UtilityBillSaveReqVO billVO = new UtilityBillSaveReqVO();
        billVO.setContractId(reading.getContractId());
        billVO.setTenantUserId(tenantUserId);
        billVO.setHouseId(reading.getHouseId());
        billVO.setMeterReadingId(reading.getId());
        billVO.setFeeType(reading.getMeterType());
        if (Integer.valueOf(0).equals(reading.getMeterType())) {
            billVO.setWaterAmount(reading.getFeeAmount());
            billVO.setElectricityAmount(BigDecimal.ZERO);
        } else {
            billVO.setWaterAmount(BigDecimal.ZERO);
            billVO.setElectricityAmount(reading.getFeeAmount());
        }
        billVO.setTotalAmount(reading.getFeeAmount());
        billVO.setPayStatus(0);
        billVO.setDueDate(reading.getReadingDate().plusDays(15));
        Long billId = utilityBillService.createUtilityBill(billVO);
        // 回填账单 ID
        reading.setBillId(billId);
        meterReadingMapper.updateById(reading);
        return billId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reviewMeterReading(Long id, Boolean pass, String reason) {
        MeterReadingDO reading = validateMeterReadingExists(id);
        if (reading.getReviewStatus() != null && reading.getReviewStatus() != 0) {
            throw exception(METER_READING_ALREADY_REVIEWED);
        }
        // 驳回
        if (Boolean.FALSE.equals(pass)) {
            reading.setReviewStatus(2);
            reading.setReviewReason(reason);
            reading.setReviewerId(getLoginUserId());
            reading.setReviewTime(LocalDateTime.now());
            meterReadingMapper.updateById(reading);
            return null;
        }
        // 通过：按房源计价规则计算费用
        HouseDO house = houseMapper.selectById(reading.getHouseId());
        BigDecimal fee = calcFee(reading, house);
        reading.setFeeAmount(fee);
        // 从合同取租客
        ContractDO contract = contractMapper.selectById(reading.getContractId());
        Long tenantUserId = contract != null && contract.getTenantUserId() != null
                ? contract.getTenantUserId() : 0L;
        // 生成水电费账单
        UtilityBillSaveReqVO billVO = new UtilityBillSaveReqVO();
        billVO.setContractId(reading.getContractId());
        billVO.setTenantUserId(tenantUserId);
        billVO.setHouseId(reading.getHouseId());
        billVO.setMeterReadingId(reading.getId());
        billVO.setFeeType(reading.getMeterType());
        if (Integer.valueOf(0).equals(reading.getMeterType())) {
            billVO.setWaterAmount(fee);
            billVO.setElectricityAmount(BigDecimal.ZERO);
        } else {
            billVO.setWaterAmount(BigDecimal.ZERO);
            billVO.setElectricityAmount(fee);
        }
        billVO.setTotalAmount(fee);
        billVO.setPayStatus(0);
        billVO.setDueDate(reading.getReadingDate().plusDays(15));
        Long billId = utilityBillService.createUtilityBill(billVO);
        // 回写审核结果
        reading.setBillId(billId);
        reading.setReviewStatus(1);
        reading.setReviewerId(getLoginUserId());
        reading.setReviewTime(LocalDateTime.now());
        meterReadingMapper.updateById(reading);
        return billId;
    }

    @Override
    public PageResult<MeterReadingDO> getMeterReadingPage(MeterReadingPageReqVO pageReqVO) {
        return meterReadingMapper.selectPage(pageReqVO);
    }

    @Override
    public MeterReadingDO getMeterReading(Long id) {
        return meterReadingMapper.selectById(id);
    }

    @Override
    public MeterReadingDO getLatestMeterReading(Long houseId, Long contractId, Integer meterType) {
        return meterReadingMapper.selectLatest(houseId, contractId, meterType);
    }

    /**
     * 按房源计价规则计算费用
     */
    private BigDecimal calcFee(MeterReadingDO reading, HouseDO house) {
        if (house == null) {
            return BigDecimal.ZERO;
        }
        if (Integer.valueOf(0).equals(reading.getMeterType())) {
            // 水费
            BigDecimal usage = nvl(reading.getUsageAmount());
            if (Integer.valueOf(1).equals(house.getWaterBillType())) {
                return calcTieredWaterFee(usage, house);
            }
            return usage.multiply(nvl(house.getWaterUnitPrice()));
        }
        // 电费：峰谷两价 或 统一单价
        BigDecimal usage = nvl(reading.getUsageAmount());
        if (Integer.valueOf(1).equals(house.getElectricityBillType())) {
            BigDecimal valleyUsage = nvl(reading.getValleyUsage());
            return usage.multiply(nvl(house.getElectricityPeakPrice()))
                    .add(valleyUsage.multiply(nvl(house.getElectricityValleyPrice())));
        }
        return usage.multiply(nvl(house.getElectricityUnitPrice()));
    }

    /**
     * 水费三档梯度计费
     */
    private BigDecimal calcTieredWaterFee(BigDecimal usage, HouseDO house) {
        BigDecimal limit1 = nvl(house.getWaterTier1Limit());
        BigDecimal limit2 = nvl(house.getWaterTier2Limit());
        BigDecimal p1 = nvl(house.getWaterTier1Price());
        BigDecimal p2 = nvl(house.getWaterTier2Price());
        BigDecimal p3 = nvl(house.getWaterTier3Price());
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal remaining = usage;
        // 档一：0 ~ limit1
        BigDecimal tier1 = remaining.min(limit1);
        fee = fee.add(tier1.multiply(p1));
        remaining = remaining.subtract(tier1);
        // 档二：limit1 ~ limit2
        if (remaining.compareTo(BigDecimal.ZERO) > 0 && limit2.compareTo(limit1) > 0) {
            BigDecimal tier2 = remaining.min(limit2.subtract(limit1));
            fee = fee.add(tier2.multiply(p2));
            remaining = remaining.subtract(tier2);
        }
        // 档三：> limit2
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            fee = fee.add(remaining.multiply(p3));
        }
        return fee;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @VisibleForTesting
    public MeterReadingDO validateMeterReadingExists(Long id) {
        if (id == null) {
            return null;
        }
        MeterReadingDO reading = meterReadingMapper.selectById(id);
        if (reading == null) {
            throw exception(METER_READING_NOT_EXISTS);
        }
        return reading;
    }

}
