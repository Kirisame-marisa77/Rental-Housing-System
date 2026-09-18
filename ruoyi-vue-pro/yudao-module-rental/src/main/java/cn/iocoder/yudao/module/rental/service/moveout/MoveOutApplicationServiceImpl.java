package cn.iocoder.yudao.module.rental.service.moveout;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
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
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.moveout.MoveOutApplicationMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.moveout.SettlementBillMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.tenant.TenantInfoMapper;
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
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_APPLICATION_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_APPLICATION_HANDLED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_APPLICATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_CONTRACT_NOT_TENANT;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_CONTRACT_STATUS_INVALID;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_NOT_OWNER;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.MOVE_OUT_REJECT_REASON_REQUIRED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_NOT_LOGIN;
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
    @Resource
    private TenantInfoMapper tenantInfoMapper;

    /** 合同状态：2-生效中 */
    private static final int CONTRACT_STATUS_ACTIVE = 2;
    /** 合同状态：3-即将到期 */
    private static final int CONTRACT_STATUS_EXPIRING = 3;
    /** 合同状态：4-退租处理中 */
    private static final int CONTRACT_STATUS_MOVING_OUT = 4;
    /** 退租申请状态：0-待处理 */
    private static final int MOVE_OUT_STATUS_PENDING = 0;
    /** 退租申请状态：1-已处理 */
    private static final int MOVE_OUT_STATUS_HANDLED = 1;
    /** 退租申请状态：2-已驳回 */
    private static final int MOVE_OUT_STATUS_REJECTED = 2;
    /** 处理人类型：0-管理员 */
    private static final int HANDLER_TYPE_ADMIN = 0;
    /** 处理人类型：1-业主 */
    private static final int HANDLER_TYPE_OWNER = 1;
    /** 房源状态：0-下架 */
    private static final int HOUSE_STATUS_OFFLINE = 0;
    /** 房源状态：3-已出租 */
    private static final int HOUSE_STATUS_RENTED = 3;

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
        // 只有生效中(2)/即将到期(3)/退租处理中(4)的合同可退租。
        // 4 也要放行：合同可能因为上一条申请被驳回而短暂停留在 4（正常会被驳回流程改回 2），
        // 卡住的话租客就再也发不出退租申请了
        Integer status = contract.getStatus();
        if (!Integer.valueOf(CONTRACT_STATUS_ACTIVE).equals(status)
                && !Integer.valueOf(CONTRACT_STATUS_EXPIRING).equals(status)
                && !Integer.valueOf(CONTRACT_STATUS_MOVING_OUT).equals(status)) {
            throw exception(MOVE_OUT_CONTRACT_STATUS_INVALID);
        }
        // 同一合同不允许有两条进行中的申请
        Long pending = moveOutApplicationMapper.selectCount(new LambdaQueryWrapperX<MoveOutApplicationDO>()
                .eq(MoveOutApplicationDO::getContractId, contract.getId())
                .eq(MoveOutApplicationDO::getStatus, MOVE_OUT_STATUS_PENDING));
        if (pending != null && pending > 0) {
            throw exception(MOVE_OUT_APPLICATION_DUPLICATE);
        }
        MoveOutApplicationDO application = BeanUtils.toBean(createReqVO, MoveOutApplicationDO.class);
        application.setApplyNo(generateApplyNo());
        application.setTenantUserId(tenantId);
        application.setHouseId(contract.getHouseId());
        application.setStatus(MOVE_OUT_STATUS_PENDING);
        moveOutApplicationMapper.insert(application);
        // 合同置「4-退租处理中」，让房东列表一眼看出这单在退租中。
        // CAS 置不上就忽略：说明合同刚刚被别处改过（比如已经退租完成），不该因此让提交失败。
        // 与之一配套的是 ownerRejectMoveOutApplication —— 没有它，合同会永久卡在 4。
        contractMapper.updateStatusToMovingOut(contract.getId());
        return application.getId();
    }

    @Override
    public PageResult<MoveOutApplicationRespVO> getTenantMoveOutRespPage(Long tenantId,
                                                                        MoveOutApplicationPageReqVO pageReqVO) {
        // 强制覆盖成登录态里的租客编号，忽略前端传的 tenantUserId
        pageReqVO.setTenantUserId(tenantId);
        // 租客端不该被 houseIds 过滤，清掉防止被构造请求利用
        pageReqVO.setHouseIds(null);
        return buildRespPage(pageReqVO);
    }

    @Override
    public PageResult<MoveOutApplicationRespVO> getOwnerMoveOutRespPage(Long ownerId,
                                                                        MoveOutApplicationPageReqVO pageReqVO) {
        if (ownerId == null) {
            throw exception(OWNER_NOT_LOGIN);
        }
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>()
                .eq(HouseDO::getOwnerId, ownerId));
        // 没有房源的房东必须直接返回空页：inIfPresent 在集合为空时不拼条件，
        // 直接查库会把全系统的退租申请都返回给他（同 ApplyServiceImpl.getOwnerApplyPage 的坑）
        if (houses.isEmpty()) {
            return PageResult.empty();
        }
        // 无条件覆盖，防止前端自带 houseIds 越权看别人的退租申请；
        // 同时清掉 tenantUserId —— 业主端不按租客过滤，留着只会被构造请求利用
        pageReqVO.setHouseIds(houses.stream().map(HouseDO::getId).collect(Collectors.toList()));
        pageReqVO.setTenantUserId(null);
        return buildRespPage(pageReqVO);
    }

    /**
     * 分页查询并批量补全房源地址与租客信息（固定 2 次补全查询，与行数无关）
     */
    private PageResult<MoveOutApplicationRespVO> buildRespPage(MoveOutApplicationPageReqVO pageReqVO) {
        PageResult<MoveOutApplicationDO> pageResult = moveOutApplicationMapper.selectPage(pageReqVO);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1. 房源
        Set<Long> houseIds = pageResult.getList().stream().map(MoveOutApplicationDO::getHouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        // 2. 租客：房东要联系租客谈押金与交接，只有裸 ID 没法用
        //    selectBatchIds 对空集合会生成 IN ()，必须先判空
        Set<Long> tenantIds = pageResult.getList().stream().map(MoveOutApplicationDO::getTenantUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, TenantInfoDO> tenantMap = tenantIds.isEmpty() ? Collections.emptyMap()
                : tenantInfoMapper.selectBatchIds(tenantIds).stream()
                .collect(Collectors.toMap(TenantInfoDO::getId, Function.identity(), (a, b) -> a));
        // 3. 组装
        List<MoveOutApplicationRespVO> list = new ArrayList<>(pageResult.getList().size());
        for (MoveOutApplicationDO application : pageResult.getList()) {
            MoveOutApplicationRespVO vo = BeanUtils.toBean(application, MoveOutApplicationRespVO.class);
            HouseDO house = houseMap.get(application.getHouseId());
            if (house != null) {
                vo.setCommunityName(house.getCommunityName());
                vo.setBuildingNo(house.getBuildingNo());
                vo.setRoomNo(house.getRoomNo());
            }
            TenantInfoDO tenant = tenantMap.get(application.getTenantUserId());
            if (tenant != null) {
                vo.setTenantName(tenant.getName());
                vo.setTenantPhone(tenant.getPhone());
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
        return doConfirm(application, confirmReqVO, getLoginUserId(), HANDLER_TYPE_ADMIN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long ownerConfirmMoveOutApplication(Long ownerId, MoveOutConfirmReqVO confirmReqVO) {
        if (ownerId == null) {
            throw exception(OWNER_NOT_LOGIN);
        }
        MoveOutApplicationDO application = validateMoveOutApplicationExists(confirmReqVO.getId());
        validateHouseBelongsToOwner(application.getHouseId(), ownerId);
        return doConfirm(application, confirmReqVO, ownerId, HANDLER_TYPE_OWNER);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ownerRejectMoveOutApplication(Long ownerId, Long id, String reason) {
        if (ownerId == null) {
            throw exception(OWNER_NOT_LOGIN);
        }
        if (StrUtil.isBlank(reason)) {
            throw exception(MOVE_OUT_REJECT_REASON_REQUIRED);
        }
        MoveOutApplicationDO application = validateMoveOutApplicationExists(id);
        validateHouseBelongsToOwner(application.getHouseId(), ownerId);
        if (!Integer.valueOf(MOVE_OUT_STATUS_PENDING).equals(application.getStatus())) {
            throw exception(MOVE_OUT_APPLICATION_HANDLED);
        }
        MoveOutApplicationDO updateObj = new MoveOutApplicationDO();
        updateObj.setStatus(MOVE_OUT_STATUS_REJECTED);
        updateObj.setHandlerId(ownerId);
        updateObj.setHandlerType(HANDLER_TYPE_OWNER);
        updateObj.setHandleTime(LocalDateTime.now());
        updateObj.setRemark(reason);
        if (moveOutApplicationMapper.updateStatusByIdAndStatus(id, MOVE_OUT_STATUS_PENDING, updateObj) == 0) {
            throw exception(MOVE_OUT_APPLICATION_HANDLED);
        }
        // 合同从 4-退租处理中 回退到 2-生效中。
        // 这一步是「提交退租时置合同为 4」的必要配套，没有它房东一直不处理就永久卡死
        contractMapper.updateStatusFromMoveOutBackToActive(application.getContractId());
    }

    /**
     * 校验退租申请对应的房源属于该业主
     *
     * 退租申请表上没有 owner 字段，house_id → rental_house.owner_id 是唯一可靠的归属链。
     * 不要图省事用「租客当前的合同」去反推 —— 租客同时有多份合同时那个链路会判错。
     */
    private void validateHouseBelongsToOwner(Long houseId, Long ownerId) {
        HouseDO house = houseMapper.selectById(houseId);
        if (house == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        if (!ownerId.equals(house.getOwnerId())) {
            throw exception(MOVE_OUT_NOT_OWNER);
        }
    }

    /**
     * 退租处理的实际逻辑，管理员端与业主端共用
     *
     * @param handlerId   处理人编号（管理员是 sys_user id，业主是 rental_owner_info id）
     * @param handlerType 处理人类型，见 HANDLER_TYPE_*
     * @return 结算单编号
     */
    private Long doConfirm(MoveOutApplicationDO application, MoveOutConfirmReqVO confirmReqVO,
                           Long handlerId, int handlerType) {
        // 只有「待处理」能处理。已处理会重复结算，已驳回不该再被处理
        if (!Integer.valueOf(MOVE_OUT_STATUS_PENDING).equals(application.getStatus())) {
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
        // 先用 CAS 占住（0-待处理 → 1-已处理），再做任何写操作。
        // 管理员端和业主端现在都能处理退租，两个并发请求若不 CAS，
        // 会各插一条结算单 —— 一次退租结算两次。
        MoveOutApplicationDO updateObj = new MoveOutApplicationDO();
        updateObj.setStatus(MOVE_OUT_STATUS_HANDLED);
        updateObj.setHandlerId(handlerId);
        updateObj.setHandlerType(handlerType);
        updateObj.setHandleTime(LocalDateTime.now());
        updateObj.setInspectionResult(confirmReqVO.getInspectionResult());
        updateObj.setRepairFee(repairFee);
        updateObj.setRepairFeeDesc(confirmReqVO.getRepairFeeDesc());
        updateObj.setRemark(confirmReqVO.getRemark());
        updateObj.setDepositHandle(depositHandle);
        updateObj.setRemainingRent(remainingRent);
        updateObj.setDeductionAmount(deductionAmount);
        updateObj.setRefundOrPay(refundOrPay);
        if (moveOutApplicationMapper.updateStatusByIdAndStatus(
                application.getId(), MOVE_OUT_STATUS_PENDING, updateObj) == 0) {
            throw exception(MOVE_OUT_APPLICATION_HANDLED);
        }
        // 生成结算单（必须在 CAS 成功之后，否则并发的失败方也会插一条）
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
        settlement.setHandlerId(handlerId);
        settlement.setHandlerType(handlerType);
        settlement.setHandleTime(LocalDateTime.now());
        settlement.setRemark(confirmReqVO.getRemark());
        settlementBillMapper.insert(settlement);
        // 回填结算单编号到申请上
        MoveOutApplicationDO settlementRef = new MoveOutApplicationDO();
        settlementRef.setId(application.getId());
        settlementRef.setSettlementId(settlement.getId());
        moveOutApplicationMapper.updateById(settlementRef);
        // 合同与房源的收尾流转
        settleContractAndHouse(application);
        return settlement.getId();
    }

    /**
     * 退租完成后的合同 / 房源状态回写
     *
     * 合同：2-生效中 / 3-即将到期 / 4-退租处理中 → 6-已退租
     * 房源：3-已出租 → 0-下架
     *
     * 房源置「下架」而不是「上架」：验收可能是「轻微/严重损坏」，押金结算也可能还在
     * 争议中（refundOrPay 为负 = 租客要补缴），此时自动重新招租等于把风险丢给下一个租客；
     * 是否重新出租是业主的经营决策，业主可以在「我的房源」里手动上架。
     *
     * 两步都用 CAS，失败不抛异常：结算已经落库，状态回写属于补偿性动作，
     * 让整个事务回滚会把已经算好的钱丢掉，得不偿失。
     */
    private void settleContractAndHouse(MoveOutApplicationDO application) {
        contractMapper.updateStatusToMoveOutDone(application.getContractId());
        houseMapper.updateStatusByIdAndStatus(application.getHouseId(),
                HOUSE_STATUS_RENTED, HOUSE_STATUS_OFFLINE);
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

    /**
     * 生成结算单号：JS- + 年月日时分秒 + 4 位随机数
     *
     * 随机部分不能省：settlement_no 上有唯一键 uk_settlement_no，同一秒内的两笔结算
     * （房东连点、或房东与管理员同时处理）会生成完全相同的编号，后一条插入直接 500。
     * apply_no 与退租的 apply_no 当初都栽在这里，结算单漏改了 —— 业主端上线后
     * 房东批量处理待办，撞车的概率显著上升。
     */
    private String generateSettlementNo() {
        return "JS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(4);
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
