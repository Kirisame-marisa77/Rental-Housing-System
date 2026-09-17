package cn.iocoder.yudao.module.rental.service.moveout;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationTenantCreateReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutConfirmReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.MoveOutApplicationDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.SettlementBillDO;
import cn.iocoder.yudao.module.rental.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.moveout.MoveOutApplicationMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.moveout.SettlementBillMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.CONTRACT_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_APPLICATION_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_APPLICATION_HANDLED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_APPLICATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_CONTRACT_NOT_TENANT;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_CONTRACT_STATUS_INVALID;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_NOT_LOGIN;

/**
 * 退租申请 Service 实现类
 *
 * @author yudao
 */
@Service
public class MoveOutApplicationServiceImpl implements MoveOutApplicationService {

    @Resource
    private MoveOutApplicationMapper moveOutApplicationMapper;
    @Resource
    private SettlementBillMapper settlementBillMapper;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private HouseMapper houseMapper;

    @Override
    public Long createMoveOutApplication(MoveOutApplicationSaveReqVO createReqVO) {
        MoveOutApplicationDO application = BeanUtils.toBean(createReqVO, MoveOutApplicationDO.class);
        application.setApplyNo(generateApplyNo());
        application.setStatus(0); // 待处理
        moveOutApplicationMapper.insert(application);
        return application.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMoveOutApplicationByTenant(Long tenantId, MoveOutApplicationTenantCreateReqVO createReqVO) {
        if (tenantId == null) {
            throw exception(TENANT_NOT_LOGIN);
        }
        ContractDO contract = contractMapper.selectById(createReqVO.getContractId());
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        // 只能对自己的合同发起退租。租客编号与房源编号一律从合同反查，
        // 不采信前端传的值 —— 否则改一下 houseId 就能给别人的房子发退租申请
        if (!tenantId.equals(contract.getTenantUserId())) {
            throw exception(MOVE_OUT_CONTRACT_NOT_TENANT);
        }
        // 只有生效中(2)/即将到期(3)的合同可退租
        if (!Integer.valueOf(2).equals(contract.getStatus()) && !Integer.valueOf(3).equals(contract.getStatus())) {
            throw exception(MOVE_OUT_CONTRACT_STATUS_INVALID);
        }
        // 同一合同不允许有两条进行中的申请
        Long pending = moveOutApplicationMapper.selectCount(new LambdaQueryWrapperX<MoveOutApplicationDO>()
                .eq(MoveOutApplicationDO::getContractId, contract.getId())
                .eq(MoveOutApplicationDO::getStatus, 0));
        if (pending != null && pending > 0) {
            throw exception(MOVE_OUT_APPLICATION_DUPLICATE);
        }
        MoveOutApplicationDO application = BeanUtils.toBean(createReqVO, MoveOutApplicationDO.class);
        application.setApplyNo(generateApplyNo());
        application.setTenantUserId(tenantId);
        application.setHouseId(contract.getHouseId());
        application.setStatus(0); // 待处理
        moveOutApplicationMapper.insert(application);
        return application.getId();
    }

    @Override
    public PageResult<MoveOutApplicationRespVO> getTenantMoveOutRespPage(Long tenantId,
                                                                        MoveOutApplicationPageReqVO pageReqVO) {
        // 强制覆盖成登录态里的租客编号，忽略前端传的 tenantUserId
        pageReqVO.setTenantUserId(tenantId);
        PageResult<MoveOutApplicationDO> pageResult = moveOutApplicationMapper.selectPage(pageReqVO);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 批量补房源地址：固定 1 次查询，与行数无关
        Set<Long> houseIds = pageResult.getList().stream().map(MoveOutApplicationDO::getHouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        List<MoveOutApplicationRespVO> list = new ArrayList<>(pageResult.getList().size());
        for (MoveOutApplicationDO application : pageResult.getList()) {
            MoveOutApplicationRespVO vo = BeanUtils.toBean(application, MoveOutApplicationRespVO.class);
            HouseDO house = houseMap.get(application.getHouseId());
            if (house != null) {
                vo.setCommunityName(house.getCommunityName());
                vo.setBuildingNo(house.getBuildingNo());
                vo.setRoomNo(house.getRoomNo());
            }
            list.add(vo);
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public void updateMoveOutApplication(MoveOutApplicationSaveReqVO updateReqVO) {
        validateMoveOutApplicationExists(updateReqVO.getId());
        MoveOutApplicationDO updateObj = BeanUtils.toBean(updateReqVO, MoveOutApplicationDO.class);
        moveOutApplicationMapper.updateById(updateObj);
    }

    @Override
    public void deleteMoveOutApplication(Long id) {
        validateMoveOutApplicationExists(id);
        moveOutApplicationMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmMoveOutApplication(MoveOutConfirmReqVO confirmReqVO) {
        MoveOutApplicationDO application = validateMoveOutApplicationExists(confirmReqVO.getId());
        if (Integer.valueOf(1).equals(application.getStatus())) {
            throw exception(MOVE_OUT_APPLICATION_HANDLED);
        }
        // 从合同取押金
        ContractDO contract = contractMapper.selectById(application.getContractId());
        BigDecimal depositAmount = contract != null && contract.getDepositAmount() != null
                ? contract.getDepositAmount() : BigDecimal.ZERO;
        // 计算费用
        BigDecimal propertyFeeArrears = nvl(confirmReqVO.getPropertyFeeArrears());
        BigDecimal utilityFeeArrears = nvl(confirmReqVO.getUtilityFeeArrears());
        BigDecimal repairFee = nvl(confirmReqVO.getRepairFee());
        BigDecimal remainingRent = nvl(confirmReqVO.getRemainingRent());
        BigDecimal deductionAmount = propertyFeeArrears.add(utilityFeeArrears).add(repairFee);
        // 结算规则
        String depositHandle;
        BigDecimal refundOrPay;
        if (Integer.valueOf(0).equals(application.getMoveOutType())) {
            // 到期退租：押金扣欠费后退还
            depositHandle = "扣除欠费后退还";
            refundOrPay = depositAmount.subtract(deductionAmount);
        } else {
            // 提前退租：押金不退（违约金），剩余租金抵扣欠费后多退少补
            depositHandle = "不退（违约金）";
            refundOrPay = remainingRent.subtract(deductionAmount);
        }
        // 生成结算单
        SettlementBillDO settlement = new SettlementBillDO();
        settlement.setSettlementNo(generateSettlementNo());
        settlement.setContractId(application.getContractId());
        settlement.setHouseId(application.getHouseId());
        settlement.setTenantUserId(application.getTenantUserId());
        settlement.setMoveOutApplicationId(application.getId());
        settlement.setMoveOutType(application.getMoveOutType());
        settlement.setDepositAmount(depositAmount);
        settlement.setDepositHandle(depositHandle);
        settlement.setRemainingRent(remainingRent);
        settlement.setPropertyFeeArrears(propertyFeeArrears);
        settlement.setUtilityFeeArrears(utilityFeeArrears);
        settlement.setRepairFee(repairFee);
        settlement.setDeductionAmount(deductionAmount);
        settlement.setRefundOrPay(refundOrPay);
        settlement.setHandlerId(getLoginUserId());
        settlement.setHandleTime(LocalDateTime.now());
        settlement.setRemark(confirmReqVO.getRemark());
        settlementBillMapper.insert(settlement);
        // 回写申请
        application.setStatus(1);
        application.setSettlementId(settlement.getId());
        application.setHandlerId(getLoginUserId());
        application.setHandleTime(LocalDateTime.now());
        application.setInspectionResult(confirmReqVO.getInspectionResult());
        application.setRepairFee(repairFee);
        application.setRepairFeeDesc(confirmReqVO.getRepairFeeDesc());
        application.setRemark(confirmReqVO.getRemark());
        application.setDepositHandle(depositHandle);
        application.setRemainingRent(remainingRent);
        application.setDeductionAmount(deductionAmount);
        application.setRefundOrPay(refundOrPay);
        moveOutApplicationMapper.updateById(application);
        return settlement.getId();
    }

    @Override
    public PageResult<MoveOutApplicationDO> getMoveOutApplicationPage(MoveOutApplicationPageReqVO pageReqVO) {
        return moveOutApplicationMapper.selectPage(pageReqVO);
    }

    @Override
    public MoveOutApplicationDO getMoveOutApplication(Long id) {
        return moveOutApplicationMapper.selectById(id);
    }

    @VisibleForTesting
    public MoveOutApplicationDO validateMoveOutApplicationExists(Long id) {
        if (id == null) {
            return null;
        }
        MoveOutApplicationDO application = moveOutApplicationMapper.selectById(id);
        if (application == null) {
            throw exception(MOVE_OUT_APPLICATION_NOT_EXISTS);
        }
        return application;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateSettlementNo() {
        return "JS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 生成退租申请编号：TZ + 年月日时分秒 + 4 位随机数
     *
     * 随机部分不能省：apply_no 上有唯一键 uk_apply_no，同一秒内的两条申请
     * （比如管理员连点、或租客端重复提交）会生成完全相同的编号，
     * 后一条插入直接 500。申请编号当初就是栽在这里，退租沿用了同样的规则
     */
    private String generateApplyNo() {
        return "TZ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(4);
    }

}
