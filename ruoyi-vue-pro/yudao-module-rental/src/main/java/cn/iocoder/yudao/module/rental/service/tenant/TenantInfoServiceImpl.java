package cn.iocoder.yudao.module.rental.service.tenant;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantRegisterReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.apply.ApplyDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.MoveOutApplicationDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.apply.ApplyMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.favorite.HouseFavoriteMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.moveout.MoveOutApplicationMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.tenant.TenantInfoMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.viewing.ViewingAppointmentMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.PASSWORD_CONFIRM_ERROR;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_HAS_ACTIVE_RENTAL;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_HAS_PENDING_APPLY;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_HAS_PENDING_MOVE_OUT;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_INFO_ID_CARD_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_INFO_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_LOGIN_FAILED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_OLD_PASSWORD_ERROR;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_PHONE_DUPLICATE;

/**
 * 租客信息 Service 实现类
 *
 * @author yudao
 */
@Service
public class TenantInfoServiceImpl implements TenantInfoService {

    /** 合同状态：5-已到期 */
    private static final int CONTRACT_STATUS_EXPIRED = 5;
    /** 合同状态：6-已退租 */
    private static final int CONTRACT_STATUS_MOVE_OUT_DONE = 6;
    /** 合同状态：7-已取消 */
    private static final int CONTRACT_STATUS_CANCELLED = 7;

    @Resource
    private TenantInfoMapper tenantInfoMapper;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private ApplyMapper applyMapper;
    @Resource
    private MoveOutApplicationMapper moveOutApplicationMapper;
    @Resource
    private HouseFavoriteMapper favoriteMapper;
    @Resource
    private ViewingAppointmentMapper appointmentMapper;

    @Override
    public Long createTenantInfo(TenantInfoSaveReqVO createReqVO) {
        // 校验身份证号、手机号唯一
        validateIdCardUnique(null, createReqVO.getIdCard());
        validatePhoneUnique(null, createReqVO.getPhone());
        TenantInfoDO tenant = BeanUtils.toBean(createReqVO, TenantInfoDO.class);
        // 密码为空时给默认密码
        if (StrUtil.isBlank(tenant.getPassword())) {
            tenant.setPassword("123456");
        }
        tenantInfoMapper.insert(tenant);
        return tenant.getId();
    }

    @Override
    public Long register(TenantRegisterReqVO registerReqVO) {
        // 校验身份证号、手机号唯一
        validateIdCardUnique(null, registerReqVO.getIdCard());
        validatePhoneUnique(null, registerReqVO.getPhone());
        TenantInfoDO tenant = BeanUtils.toBean(registerReqVO, TenantInfoDO.class);
        tenantInfoMapper.insert(tenant);
        return tenant.getId();
    }

    @Override
    public void updateTenantInfo(TenantInfoSaveReqVO updateReqVO) {
        // 校验是否存在
        validateTenantInfoExists(updateReqVO.getId());
        // 校验身份证号、手机号唯一
        validateIdCardUnique(updateReqVO.getId(), updateReqVO.getIdCard());
        validatePhoneUnique(updateReqVO.getId(), updateReqVO.getPhone());
        // 更新租客
        TenantInfoDO updateObj = BeanUtils.toBean(updateReqVO, TenantInfoDO.class);
        // 密码为空则不修改（保留原密码）
        if (StrUtil.isBlank(updateObj.getPassword())) {
            updateObj.setPassword(null);
        }
        tenantInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteTenantInfo(Long id) {
        // 校验是否存在
        validateTenantInfoExists(id);
        // 删除租客
        tenantInfoMapper.deleteById(id);
    }

    @Override
    public PageResult<TenantInfoDO> getTenantInfoPage(TenantInfoPageReqVO pageReqVO) {
        return tenantInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public TenantInfoDO getTenantInfo(Long id) {
        return tenantInfoMapper.selectById(id);
    }

    @Override
    public TenantInfoDO login(String phone, String password) {
        TenantInfoDO tenant = tenantInfoMapper.selectByPhone(phone);
        if (tenant == null || !Objects.equals(tenant.getPassword(), password)) {
            throw exception(TENANT_LOGIN_FAILED);
        }
        return tenant;
    }

    @Override
    public void changePassword(Long tenantId, String oldPassword, String newPassword) {
        TenantInfoDO tenant = tenantInfoMapper.selectById(tenantId);
        if (tenant == null) {
            throw exception(TENANT_INFO_NOT_EXISTS);
        }
        // 校验旧密码
        if (!Objects.equals(tenant.getPassword(), oldPassword)) {
            throw exception(TENANT_OLD_PASSWORD_ERROR);
        }
        tenant.setPassword(newPassword);
        tenantInfoMapper.updateById(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deregister(Long tenantId, String password) {
        TenantInfoDO tenant = tenantInfoMapper.selectById(tenantId);
        if (tenant == null) {
            throw exception(TENANT_INFO_NOT_EXISTS);
        }
        if (!Objects.equals(tenant.getPassword(), password)) {
            throw exception(PASSWORD_CONFIRM_ERROR);
        }
        // ① 没有正在租房。合同不在终态 {5-已到期, 6-已退租, 7-已取消} 的都算进行中。
        //    刻意把 0-待签署 / 1-待缴费 也算进来：它们虽然还没「租」，但合同与首期账单
        //    已经生成，注销会让合同变成无人可签的孤儿单
        Long activeContracts = contractMapper.selectCount(new LambdaQueryWrapperX<ContractDO>()
                .eq(ContractDO::getTenantUserId, tenantId)
                .notIn(ContractDO::getStatus, CONTRACT_STATUS_EXPIRED,
                        CONTRACT_STATUS_MOVE_OUT_DONE, CONTRACT_STATUS_CANCELLED));
        if (activeContracts != null && activeContracts > 0) {
            throw exception(TENANT_HAS_ACTIVE_RENTAL);
        }
        // ② 没有待房东审批的租房申请，否则房东批的时候就找不到人了
        Long pendingApplies = applyMapper.selectCount(new LambdaQueryWrapperX<ApplyDO>()
                .eq(ApplyDO::getTenantUserId, tenantId)
                .eq(ApplyDO::getStatus, 0));
        if (pendingApplies != null && pendingApplies > 0) {
            throw exception(TENANT_HAS_PENDING_APPLY);
        }
        // ③ 没有待处理的退租申请
        Long pendingMoveOut = moveOutApplicationMapper.selectCount(new LambdaQueryWrapperX<MoveOutApplicationDO>()
                .eq(MoveOutApplicationDO::getTenantUserId, tenantId)
                .eq(MoveOutApplicationDO::getStatus, 0));
        if (pendingMoveOut != null && pendingMoveOut > 0) {
            throw exception(TENANT_HAS_PENDING_MOVE_OUT);
        }

        // ④ 清理「纯偏好 / 未发生」的关联数据。历史合同与各类账单**一律不动** ——
        //    那是钱的凭据，而且靠主键关联，行还在则关联链完整（只是显示的名字会变空）
        favoriteMapper.deletePhysicallyByTenant(tenantId);
        appointmentMapper.cancelActiveByTenant(tenantId);

        // ⑤ 先释放唯一键，再打逻辑删除标记。顺序不能反：deleteById 之后再调
        //    releaseUniqueKeysForDeregister 会因为 deleted = 0 不匹配而更新 0 行，
        //    唯一键永远释放不掉
        if (tenantInfoMapper.releaseUniqueKeysForDeregister(tenantId) == 0) {
            // 并发双提交：另一个请求已经注销掉了
            throw exception(TENANT_INFO_NOT_EXISTS);
        }
        tenantInfoMapper.deleteById(tenantId);
    }

    @VisibleForTesting
    public void validateTenantInfoExists(Long id) {
        if (id == null) {
            return;
        }
        if (tenantInfoMapper.selectById(id) == null) {
            throw exception(TENANT_INFO_NOT_EXISTS);
        }
    }

    private void validateIdCardUnique(Long id, String idCard) {
        if (idCard == null) {
            return;
        }
        TenantInfoDO exist = tenantInfoMapper.selectByIdCard(idCard);
        if (exist != null && !exist.getId().equals(id)) {
            throw exception(TENANT_INFO_ID_CARD_DUPLICATE);
        }
    }

    private void validatePhoneUnique(Long id, String phone) {
        if (phone == null) {
            return;
        }
        TenantInfoDO exist = tenantInfoMapper.selectByPhone(phone);
        if (exist != null && !exist.getId().equals(id)) {
            throw exception(TENANT_PHONE_DUPLICATE);
        }
    }

}
