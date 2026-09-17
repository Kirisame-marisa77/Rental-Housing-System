package cn.iocoder.yudao.module.rental.service.tenant;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantRegisterReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.tenant.TenantInfoMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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

    @Resource
    private TenantInfoMapper tenantInfoMapper;

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
