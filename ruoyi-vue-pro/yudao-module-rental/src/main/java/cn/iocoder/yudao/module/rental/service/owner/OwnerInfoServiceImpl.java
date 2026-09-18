package cn.iocoder.yudao.module.rental.service.owner;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerRegisterReqVO;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_HAS_ACTIVE_CONTRACT;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_HAS_ACTIVE_HOUSE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_INFO_ID_CARD_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_INFO_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_INFO_PHONE_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_LOGIN_FAILED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_OLD_PASSWORD_ERROR;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.PASSWORD_CONFIRM_ERROR;

/**
 * 业主信息 Service 实现类
 *
 * @author yudao
 */
@Service
public class OwnerInfoServiceImpl implements OwnerInfoService {

    /** 合同状态：5-已到期 */
    private static final int CONTRACT_STATUS_EXPIRED = 5;
    /** 合同状态：6-已退租 */
    private static final int CONTRACT_STATUS_MOVE_OUT_DONE = 6;
    /** 合同状态：7-已取消 */
    private static final int CONTRACT_STATUS_CANCELLED = 7;
    /** 房源状态：0-下架 */
    private static final int HOUSE_STATUS_OFFLINE = 0;
    /** 房源状态：1-上架 */
    private static final int HOUSE_STATUS_ONLINE = 1;
    /** 房源状态：2-已锁定（签约中） */
    private static final int HOUSE_STATUS_LOCKED = 2;
    /** 房源状态：3-已出租 */
    private static final int HOUSE_STATUS_RENTED = 3;

    @Resource
    private OwnerInfoMapper ownerInfoMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private ContractMapper contractMapper;

    @Override
    public Long createOwnerInfo(OwnerInfoSaveReqVO createReqVO) {
        // 校验身份证号、手机号唯一
        validateIdCardUnique(null, createReqVO.getIdCard());
        validatePhoneUnique(null, createReqVO.getPhone());
        OwnerInfoDO owner = BeanUtils.toBean(createReqVO, OwnerInfoDO.class);
        // 密码为空时给默认密码
        if (StrUtil.isBlank(owner.getPassword())) {
            owner.setPassword("123456");
        }
        ownerInfoMapper.insert(owner);
        return owner.getId();
    }

    @Override
    public void updateOwnerInfo(OwnerInfoSaveReqVO updateReqVO) {
        // 校验是否存在
        validateOwnerInfoExists(updateReqVO.getId());
        // 校验身份证号、手机号唯一
        validateIdCardUnique(updateReqVO.getId(), updateReqVO.getIdCard());
        validatePhoneUnique(updateReqVO.getId(), updateReqVO.getPhone());
        // 更新业主
        OwnerInfoDO updateObj = BeanUtils.toBean(updateReqVO, OwnerInfoDO.class);
        // 密码为空则不修改（保留原密码）
        if (StrUtil.isBlank(updateObj.getPassword())) {
            updateObj.setPassword(null);
        }
        ownerInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteOwnerInfo(Long id) {
        // 校验是否存在
        validateOwnerInfoExists(id);
        // 删除业主
        ownerInfoMapper.deleteById(id);
    }

    @Override
    public PageResult<OwnerInfoDO> getOwnerInfoPage(OwnerInfoPageReqVO pageReqVO) {
        return ownerInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public OwnerInfoDO getOwnerInfo(Long id) {
        return ownerInfoMapper.selectById(id);
    }

    @Override
    public OwnerInfoDO getOwnerInfoByPhone(String phone) {
        return ownerInfoMapper.selectByPhone(phone);
    }

    @Override
    public OwnerInfoDO login(String phone, String password) {
        OwnerInfoDO owner = ownerInfoMapper.selectByPhone(phone);
        if (owner == null || !Objects.equals(owner.getPassword(), password)) {
            throw exception(OWNER_LOGIN_FAILED);
        }
        return owner;
    }

    @Override
    public Long register(OwnerRegisterReqVO registerReqVO) {
        // 校验身份证号、手机号唯一
        validateIdCardUnique(null, registerReqVO.getIdCard());
        validatePhoneUnique(null, registerReqVO.getPhone());
        OwnerInfoDO owner = BeanUtils.toBean(registerReqVO, OwnerInfoDO.class);
        ownerInfoMapper.insert(owner);
        return owner.getId();
    }

    @Override
    public void changePassword(Long ownerId, String oldPassword, String newPassword) {
        OwnerInfoDO owner = ownerInfoMapper.selectById(ownerId);
        if (owner == null) {
            throw exception(OWNER_INFO_NOT_EXISTS);
        }
        // 校验旧密码
        if (!Objects.equals(owner.getPassword(), oldPassword)) {
            throw exception(OWNER_OLD_PASSWORD_ERROR);
        }
        owner.setPassword(newPassword);
        ownerInfoMapper.updateById(owner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deregister(Long ownerId, String password) {
        OwnerInfoDO owner = ownerInfoMapper.selectById(ownerId);
        if (owner == null) {
            throw exception(OWNER_INFO_NOT_EXISTS);
        }
        if (!Objects.equals(owner.getPassword(), password)) {
            throw exception(PASSWORD_CONFIRM_ERROR);
        }
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>()
                .eq(HouseDO::getOwnerId, ownerId));
        // ① 没有房子出租出去：名下房源不得处于 2-已锁定（签约中）或 3-已出租
        boolean hasActiveHouse = houses.stream().anyMatch(house ->
                Integer.valueOf(HOUSE_STATUS_LOCKED).equals(house.getStatus())
                        || Integer.valueOf(HOUSE_STATUS_RENTED).equals(house.getStatus()));
        if (hasActiveHouse) {
            throw exception(OWNER_HAS_ACTIVE_HOUSE);
        }
        // ② 名下房源不得有未结束的合同。
        //    这一道不能省：下面会把房源全部下架，那份待签署/待缴费的合同在租客点签约时
        //    会走 HouseMapper.updateStatusByIdAndStatus(1 → 3) 抢占房源并返回 0，
        //    租客于是莫名收到「该房源已被其他租客承租」—— 等于静默杀死一笔在途交易。
        if (!houses.isEmpty()) {
            List<Long> houseIds = houses.stream().map(HouseDO::getId).collect(Collectors.toList());
            Long activeContracts = contractMapper.selectCount(new LambdaQueryWrapperX<ContractDO>()
                    .in(ContractDO::getHouseId, houseIds) // houseIds 已判非空，可以安全用 in
                    .notIn(ContractDO::getStatus, CONTRACT_STATUS_EXPIRED,
                            CONTRACT_STATUS_MOVE_OUT_DONE, CONTRACT_STATUS_CANCELLED));
            if (activeContracts != null && activeContracts > 0) {
                throw exception(OWNER_HAS_ACTIVE_CONTRACT);
            }
        }
        // ③ 名下房源全部下架。走 CAS(1 → 0)：返回 0 说明该房源在本次校验之后被并发租出去了
        //    （1 → 3），此时必须中止整笔注销，否则会留下「已出租但业主已注销」的房源
        for (HouseDO house : houses) {
            if (Integer.valueOf(HOUSE_STATUS_ONLINE).equals(house.getStatus())) {
                if (houseMapper.updateStatusByIdAndStatus(house.getId(),
                        HOUSE_STATUS_ONLINE, HOUSE_STATUS_OFFLINE) == 0) {
                    throw exception(OWNER_HAS_ACTIVE_HOUSE);
                }
            }
            // 已是下架的不用动
        }
        // ④ 先释放唯一键，再打逻辑删除标记（顺序不能反，理由见 TenantInfoServiceImpl#deregister）
        if (ownerInfoMapper.releaseUniqueKeysForDeregister(ownerId) == 0) {
            throw exception(OWNER_INFO_NOT_EXISTS);
        }
        ownerInfoMapper.deleteById(ownerId);
    }

    @VisibleForTesting
    public void validateOwnerInfoExists(Long id) {
        if (id == null) {
            return;
        }
        if (ownerInfoMapper.selectById(id) == null) {
            throw exception(OWNER_INFO_NOT_EXISTS);
        }
    }

    private void validateIdCardUnique(Long id, String idCard) {
        if (idCard == null) {
            return;
        }
        OwnerInfoDO exist = ownerInfoMapper.selectByIdCard(idCard);
        if (exist != null && !exist.getId().equals(id)) {
            throw exception(OWNER_INFO_ID_CARD_DUPLICATE);
        }
    }

    private void validatePhoneUnique(Long id, String phone) {
        if (phone == null) {
            return;
        }
        OwnerInfoDO exist = ownerInfoMapper.selectByPhone(phone);
        if (exist != null && !exist.getId().equals(id)) {
            throw exception(OWNER_INFO_PHONE_DUPLICATE);
        }
    }

}
