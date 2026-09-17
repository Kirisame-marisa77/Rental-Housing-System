package cn.iocoder.yudao.module.rental.service.apply;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplySaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.apply.ApplyDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.apply.ApplyMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.tenant.TenantInfoMapper;
import cn.iocoder.yudao.module.rental.service.bill.RentBillService;
import cn.iocoder.yudao.module.rental.service.contract.ContractService;
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
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_ALREADY_PROCESSED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_DUPLICATE_PENDING;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_HOUSE_NOT_AVAILABLE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_NOT_OWNER;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_REJECT_REASON_REQUIRED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_NOT_LOGIN;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_NOT_LOGIN;

/**
 * 租房申请 Service 实现类
 *
 * @author yudao
 */
@Service
public class ApplyServiceImpl implements ApplyService {

    /**
     * 房源「上架」状态
     */
    private static final int HOUSE_STATUS_ONLINE = 1;

    @Resource
    private ApplyMapper applyMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private TenantInfoMapper tenantInfoMapper;
    @Resource
    private OwnerInfoMapper ownerInfoMapper;
    @Resource
    private ContractService contractService;
    @Resource
    private RentBillService rentBillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApply(ApplySaveReqVO createReqVO) {
        Long tenantId = createReqVO.getTenantUserId();
        if (tenantId == null) {
            throw exception(TENANT_NOT_LOGIN);
        }
        // 只能申请「上架」中的房源
        HouseDO house = houseMapper.selectById(createReqVO.getHouseId());
        if (house == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        if (!Integer.valueOf(HOUSE_STATUS_ONLINE).equals(house.getStatus())) {
            throw exception(APPLY_HOUSE_NOT_AVAILABLE);
        }
        // 同一租客对同一房源不能有进行中的申请
        Long pendingCount = applyMapper.selectPendingCount(createReqVO.getHouseId(), tenantId);
        if (pendingCount != null && pendingCount > 0) {
            throw exception(APPLY_DUPLICATE_PENDING);
        }
        ApplyDO apply = BeanUtils.toBean(createReqVO, ApplyDO.class);
        // 申请编号为空时自动生成
        apply.setApplyNo(StrUtil.isBlank(apply.getApplyNo()) ? generateApplyNo() : apply.getApplyNo());
        // 审批结果相关字段一律由服务端决定，忽略客户端传入（接口是 @PermitAll，需防止构造请求赋权）
        apply.setId(null);
        apply.setStatus(0); // 待审批
        apply.setApproverId(null);
        apply.setApproveTime(null);
        apply.setRejectReason(null);
        apply.setContractId(null);
        apply.setTimeoutReason(null);
        applyMapper.insert(apply);
        return apply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ownerApproveApply(Long ownerId, Long id, Boolean pass, String reason) {
        if (ownerId == null) {
            throw exception(OWNER_NOT_LOGIN);
        }
        ApplyDO apply = applyMapper.selectById(id);
        if (apply == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        if (!Integer.valueOf(0).equals(apply.getStatus())) {
            throw exception(APPLY_ALREADY_PROCESSED);
        }
        HouseDO house = houseMapper.selectById(apply.getHouseId());
        if (house == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        // 只能审批自己房源的申请
        if (!ownerId.equals(house.getOwnerId())) {
            throw exception(APPLY_NOT_OWNER);
        }
        doApprove(apply, Boolean.TRUE.equals(pass), reason, ownerId);
    }

    private void doApprove(ApplyDO apply, boolean pass, String reason, Long ownerId) {
        LocalDateTime now = LocalDateTime.now();
        if (!pass) {
            if (StrUtil.isBlank(reason)) {
                throw exception(APPLY_REJECT_REASON_REQUIRED);
            }
            ApplyDO updateObj = new ApplyDO();
            updateObj.setStatus(2); // 已驳回
            updateObj.setRejectReason(reason);
            updateObj.setApproverId(ownerId);
            updateObj.setApproveTime(now);
            // 乐观锁：并发重复审批只有一个能成功。驳回不改动房源，其他租客仍可申请
            if (applyMapper.updateStatusByIdAndStatus(apply.getId(), 0, updateObj) == 0) {
                throw exception(APPLY_ALREADY_PROCESSED);
            }
            return;
        }
        // 同意：先以乐观锁占用申请，保证同一申请只会生成一份合同
        ApplyDO updateObj = new ApplyDO();
        updateObj.setStatus(1); // 已通过
        updateObj.setApproverId(ownerId);
        updateObj.setApproveTime(now);
        if (applyMapper.updateStatusByIdAndStatus(apply.getId(), 0, updateObj) == 0) {
            throw exception(APPLY_ALREADY_PROCESSED);
        }
        // 生成待签署合同（不占用房源，房源要到签约并缴费后才被占用）
        ContractDO contract = contractService.createContractByApply(apply);
        ApplyDO linkObj = new ApplyDO();
        linkObj.setId(apply.getId());
        linkObj.setContractId(contract.getId());
        applyMapper.updateById(linkObj);
        // 生成首期账单（押金 + 首月租金），租客缴费后合同才具备生效条件
        rentBillService.createFirstRentBillForContract(contract);
    }

    @Override
    public PageResult<ApplyDO> getApplyPage(ApplyPageReqVO pageReqVO) {
        return applyMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ApplyRespVO> getApplyPageWithDetail(ApplyPageReqVO pageReqVO) {
        PageResult<ApplyDO> pageResult = applyMapper.selectPage(pageReqVO);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(enrichApplies(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public PageResult<ApplyRespVO> getOwnerApplyPage(Long ownerId, ApplyPageReqVO pageReqVO) {
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>()
                .eq(HouseDO::getOwnerId, ownerId));
        // 没有房源的房东必须直接返回空页：inIfPresent 在集合为空时不会拼接条件，
        // 直接查库会把全系统的申请都返回给他
        if (houses.isEmpty()) {
            return PageResult.empty();
        }
        pageReqVO.setHouseIds(houses.stream().map(HouseDO::getId).collect(Collectors.toList()));
        return getApplyPageWithDetail(pageReqVO);
    }

    @Override
    public ApplyDO getApply(Long id) {
        return applyMapper.selectById(id);
    }

    /**
     * 批量补全申请的房源、房东、租客信息（固定 3 次查询，与行数无关）
     */
    private List<ApplyRespVO> enrichApplies(List<ApplyDO> applies) {
        // 1. 房源
        Set<Long> houseIds = applies.stream().map(ApplyDO::getHouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        // 2. 租客
        Set<Long> tenantIds = applies.stream().map(ApplyDO::getTenantUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, TenantInfoDO> tenantMap = tenantIds.isEmpty() ? Collections.emptyMap()
                : tenantInfoMapper.selectBatchIds(tenantIds).stream()
                .collect(Collectors.toMap(TenantInfoDO::getId, Function.identity(), (a, b) -> a));
        // 3. 房东（从已加载的房源里取 ownerId，省一次往返）
        Set<Long> ownerIds = houseMap.values().stream().map(HouseDO::getOwnerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, OwnerInfoDO> ownerMap = ownerIds.isEmpty() ? Collections.emptyMap()
                : ownerInfoMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(OwnerInfoDO::getId, Function.identity(), (a, b) -> a));
        // 4. 组装
        List<ApplyRespVO> list = new ArrayList<>(applies.size());
        for (ApplyDO apply : applies) {
            ApplyRespVO vo = BeanUtils.toBean(apply, ApplyRespVO.class);
            HouseDO house = houseMap.get(apply.getHouseId());
            if (house != null) {
                vo.setHouseNo(house.getHouseNo());
                vo.setCommunityName(house.getCommunityName());
                vo.setArea(house.getArea());
                vo.setBuildingNo(house.getBuildingNo());
                vo.setRoomNo(house.getRoomNo());
                vo.setLayout(house.getLayout());
                vo.setSquareArea(house.getSquareArea());
                vo.setHouseStatus(house.getStatus());
                vo.setOwnerId(house.getOwnerId());
                OwnerInfoDO owner = ownerMap.get(house.getOwnerId());
                if (owner != null) {
                    vo.setOwnerName(owner.getName());
                    vo.setOwnerPhone(owner.getPhone());
                }
            }
            TenantInfoDO tenant = tenantMap.get(apply.getTenantUserId());
            if (tenant != null) {
                vo.setTenantName(tenant.getName());
                vo.setTenantPhone(tenant.getPhone());
            }
            list.add(vo);
        }
        return list;
    }

    @VisibleForTesting
    public void validateApplyExists(Long id) {
        if (id == null) {
            return;
        }
        if (applyMapper.selectById(id) == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
    }

    /**
     * 生成申请编号：SQ + 年月日时分秒 + 4 位随机数
     *
     * 必须带随机部分：apply_no 上有唯一键 uk_apply_no，而多个租客在同一秒内申请
     * （先到先得场景下很常见）会生成完全相同的编号，导致后一条插入直接 500。
     * 建表注释里写的也是「SQ+时间戳+随机数」，此前实现漏了随机数。
     */
    private String generateApplyNo() {
        return "SQ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(4);
    }

}
