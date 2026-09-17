package cn.iocoder.yudao.module.rental.service.contract;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.apply.ApplyDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contracttemplate.ContractTemplateDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.apply.ApplyMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.RentBillMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.tenant.TenantInfoMapper;
import cn.iocoder.yudao.module.rental.service.contract.ContractService.Activation;
import cn.iocoder.yudao.module.rental.service.contracttemplate.ContractTemplateService;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_HOUSE_NOT_AVAILABLE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.APPLY_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.CONTRACT_CANCELLED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.CONTRACT_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_NOT_EXISTS;

/**
 * 租房合同 Service 实现类
 *
 * @author yudao
 */
@Service
public class ContractServiceImpl implements ContractService {

    /**
     * 申请落选（房源已被其他租客承租）时写入的原因
     */
    private static final String HOUSE_TAKEN_REASON = "该房源已被其他租客承租";

    @Resource
    private ContractMapper contractMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private ApplyMapper applyMapper;
    /**
     * 注意：这里必须用 RentBillMapper 而不是 RentBillService —— 账单侧注入了 ContractService，
     * 反向注入 Service 会造成循环依赖，Spring Boot 2.6+ 会启动失败
     */
    @Resource
    private RentBillMapper rentBillMapper;
    /**
     * 合同模板 Service。依赖方向是单向的（本类 → 模板 Service），
     * 模板 Service 只依赖 Mapper，不会反向注入本类，因此不构成循环依赖
     */
    @Resource
    private ContractTemplateService contractTemplateService;
    @Resource
    private OwnerInfoMapper ownerInfoMapper;
    @Resource
    private TenantInfoMapper tenantInfoMapper;

    @Override
    public Long createContract(ContractSaveReqVO createReqVO) {
        ContractDO contract = BeanUtils.toBean(createReqVO, ContractDO.class);
        // 合同编号为空时自动生成
        if (StrUtil.isBlank(contract.getContractNo())) {
            contract.setContractNo(generateContractNo());
        }
        fillContentFromTemplate(contract);
        contractMapper.insert(contract);
        return contract.getId();
    }

    @Override
    public void updateContract(ContractSaveReqVO updateReqVO) {
        // 校验是否存在
        validateContractExists(updateReqVO.getId());
        // 更新合同
        ContractDO updateObj = BeanUtils.toBean(updateReqVO, ContractDO.class);
        fillContentFromTemplate(updateObj);
        contractMapper.updateById(updateObj);
    }

    /**
     * 选了模板但正文为空时，按模板渲染填充
     *
     * 只在正文为空时渲染：正文有值说明是「已固化的快照」或人工微调过的内容，
     * 覆盖它会让管理员的修改白做
     */
    private void fillContentFromTemplate(ContractDO contract) {
        if (contract == null || contract.getTemplateId() == null || StrUtil.isNotBlank(contract.getContent())) {
            return;
        }
        contract.setContent(contractTemplateService.renderForContract(contract.getTemplateId(), contract));
    }

    @Override
    public void deleteContract(Long id) {
        // 校验是否存在
        validateContractExists(id);
        // 删除合同
        contractMapper.deleteById(id);
    }

    @Override
    public PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO) {
        return contractMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ContractRespVO> getContractRespPage(ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractMapper.selectPage(pageReqVO);
        return new PageResult<>(enrichContracts(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public ContractDO getContract(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    public void renewContract(Long id, LocalDate newEndDate) {
        ContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        contract.setRentEndDate(newEndDate);
        contract.setStatus(2); // 生效中
        contractMapper.updateById(contract);
    }

    @Override
    public void cancelContract(Long id) {
        ContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        contract.setStatus(7); // 已取消
        contractMapper.updateById(contract);
    }

    @Override
    public List<ContractDO> getExpiringContractList(Integer days) {
        int n = days == null ? 60 : days;
        return contractMapper.selectExpiring(LocalDate.now().plusDays(n));
    }

    @Override
    public List<ContractDO> getContractsByOwnerId(Long ownerId) {
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>()
                .eq(HouseDO::getOwnerId, ownerId));
        if (houses.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> houseIds = houses.stream().map(HouseDO::getId).collect(Collectors.toList());
        return contractMapper.selectList(new LambdaQueryWrapperX<ContractDO>()
                .in(ContractDO::getHouseId, houseIds)
                .orderByDesc(ContractDO::getId));
    }

    @Override
    public List<ContractRespVO> getOwnerContractRespList(Long ownerId) {
        return enrichContracts(getContractsByOwnerId(ownerId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractDO createContractByApply(ApplyDO apply) {
        if (apply == null || apply.getHouseId() == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        if (houseMapper.selectById(apply.getHouseId()) == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        if (apply.getMoveInDate() == null || apply.getLeaseTerm() == null || apply.getLeaseTerm() <= 0) {
            throw exception(APPLY_HOUSE_NOT_AVAILABLE);
        }
        ContractDO contract = new ContractDO();
        // 编号带上申请 ID：同一申请至多一份合同，避免同一秒生成的编号撞 uk_contract_no 唯一索引
        contract.setContractNo("HT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + apply.getId());
        contract.setHouseId(apply.getHouseId());
        contract.setTenantUserId(apply.getTenantUserId());
        contract.setRentStartDate(apply.getMoveInDate());
        contract.setRentEndDate(apply.getMoveInDate().plusMonths(apply.getLeaseTerm()));
        contract.setMonthlyRent(apply.getMonthlyRent());
        contract.setDepositAmount(apply.getDepositAmount() == null ? BigDecimal.ZERO : apply.getDepositAmount());
        contract.setPaymentMethod(apply.getPaymentMethod());
        contract.setStatus(0); // 待签署
        contract.setSourceApplyId(apply.getId());
        // 自动套用默认模板生成正文；一个启用的模板都没有时 content 留空，不影响下单流程
        ContractTemplateDO defaultTemplate = contractTemplateService.getDefaultTemplate();
        if (defaultTemplate != null) {
            contract.setTemplateId(defaultTemplate.getId());
            contract.setContent(contractTemplateService.renderForContract(defaultTemplate.getId(), contract));
        }
        contractMapper.insert(contract);
        // 注意：此处刻意不修改房源状态 —— 房东同意只是生成合同，
        // 房源要到有租客「双方签署 + 缴清首期账单」后才被占用（先到先得）
        return contract;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activation tryActivateContract(Long contractId) {
        ContractDO contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return Activation.NOT_READY;
        }
        // 已生效或已取消，无需处理
        if (!Integer.valueOf(0).equals(contract.getStatus())) {
            return Activation.NOT_READY;
        }
        // 双方都还没签完，未到成交时机
        if (contract.getSignTime() == null || contract.getOwnerSignTime() == null) {
            return Activation.NOT_READY;
        }
        // 首期账单必须已缴费
        RentBillDO firstBill = rentBillMapper.selectFirstBillByContractId(contractId);
        if (firstBill == null || !Integer.valueOf(1).equals(firstBill.getPayStatus())) {
            return Activation.NOT_READY;
        }
        // 先到先得：CAS 抢占房源（1-上架 → 3-已出租），并发下只有一个赢家
        if (houseMapper.updateStatusByIdAndStatus(contract.getHouseId(), 1, 3) == 0) {
            // 房源已被其他租客承租或已下架，本合同落选
            markContractAsTaken(contract);
            return Activation.LOST_RACE;
        }
        // 抢占成功，合同生效
        contract.setStatus(2); // 生效中
        contractMapper.updateById(contract);
        // 来源申请 → 已签约
        if (contract.getSourceApplyId() != null) {
            ApplyDO apply = new ApplyDO();
            apply.setId(contract.getSourceApplyId());
            apply.setStatus(3); // 已签约
            apply.setContractId(contract.getId());
            applyMapper.updateById(apply);
        }
        // 同一房源的其他未生效合同全部作废
        cancelSiblingContracts(contract.getHouseId(), contract.getId());
        return Activation.ACTIVATED;
    }

    /**
     * 合同落选：置为「已取消」，来源申请置为「已失效」
     */
    private void markContractAsTaken(ContractDO contract) {
        ContractDO updateObj = new ContractDO();
        updateObj.setId(contract.getId());
        updateObj.setStatus(7); // 已取消
        contractMapper.updateById(updateObj);
        if (contract.getSourceApplyId() == null) {
            return;
        }
        ApplyDO apply = new ApplyDO();
        apply.setId(contract.getSourceApplyId());
        // 申请状态表里没有独立的「已取消」，复用 4-已超时 并写入原因
        apply.setStatus(4);
        apply.setTimeoutReason(HOUSE_TAKEN_REASON);
        applyMapper.updateById(apply);
    }

    /**
     * 房源被租出后，同一房源其余「待签署」合同全部作废
     *
     * 已缴纳的首期账单不做自动退款，原样保留作为人工退款的凭据
     */
    private void cancelSiblingContracts(Long houseId, Long winnerContractId) {
        List<ContractDO> siblings = contractMapper.selectList(new LambdaQueryWrapperX<ContractDO>()
                .eq(ContractDO::getHouseId, houseId)
                .eq(ContractDO::getStatus, 0)
                .ne(ContractDO::getId, winnerContractId));
        for (ContractDO sibling : siblings) {
            markContractAsTaken(sibling);
        }
    }

    /**
     * 已取消的合同不能再签署
     *
     * 「先到先得」落选的合同会被自动置为已取消，若此处不拦，租客再点签约会拿到
     * 「签署成功」—— 但合同其实已作废，属于让用户误以为签成的假成功
     */
    private void validateSignable(ContractDO contract) {
        if (Integer.valueOf(7).equals(contract.getStatus())) {
            throw exception(CONTRACT_CANCELLED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activation tenantSignContract(Long id) {
        ContractDO contract = validateContractExists(id);
        validateSignable(contract);
        if (contract.getSignTime() == null) {
            contract.setSignTime(LocalDateTime.now());
        }
        contractMapper.updateById(contract);
        return tryActivateContract(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activation ownerSignContract(Long id) {
        ContractDO contract = validateContractExists(id);
        validateSignable(contract);
        if (contract.getOwnerSignTime() == null) {
            contract.setOwnerSignTime(LocalDateTime.now());
        }
        contractMapper.updateById(contract);
        return tryActivateContract(id);
    }

    /**
     * 批量补全合同的首期账单、房源、房东、租客信息（固定 4 次查询，与行数无关）
     */
    private List<ContractRespVO> enrichContracts(List<ContractDO> contracts) {
        if (contracts == null || contracts.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 首期账单
        List<Long> contractIds = contracts.stream().map(ContractDO::getId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toList());
        Map<Long, RentBillDO> firstBillMap = contractIds.isEmpty() ? Collections.emptyMap()
                : rentBillMapper.selectFirstBillsByContractIds(contractIds).stream()
                .collect(Collectors.toMap(RentBillDO::getContractId, Function.identity(), (a, b) -> a));
        // 2. 房源
        Set<Long> houseIds = contracts.stream().map(ContractDO::getHouseId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        // 3. 租客
        Set<Long> tenantIds = contracts.stream().map(ContractDO::getTenantUserId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, TenantInfoDO> tenantMap = tenantIds.isEmpty() ? Collections.emptyMap()
                : tenantInfoMapper.selectBatchIds(tenantIds).stream()
                .collect(Collectors.toMap(TenantInfoDO::getId, Function.identity(), (a, b) -> a));
        // 4. 房东：从已加载的房源里取 ownerId，省一次往返
        Set<Long> ownerIds = houseMap.values().stream().map(HouseDO::getOwnerId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, OwnerInfoDO> ownerMap = ownerIds.isEmpty() ? Collections.emptyMap()
                : ownerInfoMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(OwnerInfoDO::getId, Function.identity(), (a, b) -> a));
        List<ContractRespVO> list = new ArrayList<>(contracts.size());
        for (ContractDO contract : contracts) {
            ContractRespVO vo = BeanUtils.toBean(contract, ContractRespVO.class);
            RentBillDO firstBill = firstBillMap.get(contract.getId());
            if (firstBill != null) {
                vo.setFirstBillNo(firstBill.getBillNo());
                vo.setFirstBillPaid(Integer.valueOf(1).equals(firstBill.getPayStatus()));
            } else {
                vo.setFirstBillPaid(false);
            }
            HouseDO house = houseMap.get(contract.getHouseId());
            if (house != null) {
                vo.setHouseNo(house.getHouseNo());
                vo.setCommunityName(house.getCommunityName());
                vo.setArea(house.getArea());
                vo.setBuildingNo(house.getBuildingNo());
                vo.setRoomNo(house.getRoomNo());
                vo.setLayout(house.getLayout());
                vo.setSquareArea(house.getSquareArea());
                vo.setOwnerId(house.getOwnerId());
                OwnerInfoDO owner = ownerMap.get(house.getOwnerId());
                if (owner != null) {
                    vo.setOwnerName(owner.getName());
                    vo.setOwnerPhone(owner.getPhone());
                }
            }
            TenantInfoDO tenant = tenantMap.get(contract.getTenantUserId());
            if (tenant != null) {
                vo.setTenantName(tenant.getName());
                vo.setTenantPhone(tenant.getPhone());
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<ContractRespVO> getExpiringContractRespList(Integer days) {
        return enrichContracts(getExpiringContractList(days));
    }

    @Override
    public ContractRespVO getContractResp(Long id) {
        ContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return enrichContracts(Collections.singletonList(contract)).get(0);
    }

    @Override
    public ContractRespVO getOwnerContractDetail(Long ownerId, Long contractId) {
        if (ownerId == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        ContractRespVO vo = getContractResp(contractId);
        // 只能看自己房源的合同：不校验的话，业主端改一下 id 就能读到别人的合同正文
        if (!ownerId.equals(vo.getOwnerId())) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return vo;
    }

    @Override
    public ContractRespVO getTenantContractDetail(Long tenantId, Long contractId) {
        if (tenantId == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        ContractRespVO vo = getContractResp(contractId);
        if (!tenantId.equals(vo.getTenantUserId())) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return vo;
    }

    @VisibleForTesting
    public ContractDO validateContractExists(Long id) {
        if (id == null) {
            return null;
        }
        ContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
        return contract;
    }

    /**
     * 生成合同编号：HT + 年月日时分秒
     */
    private String generateContractNo() {
        return "HT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
