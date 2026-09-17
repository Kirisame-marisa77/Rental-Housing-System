package cn.iocoder.yudao.module.rental.service.owner;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerRegisterReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_INFO_ID_CARD_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_INFO_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_INFO_PHONE_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_LOGIN_FAILED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_OLD_PASSWORD_ERROR;

/**
 * 业主信息 Service 实现类
 *
 * @author yudao
 */
@Service
public class OwnerInfoServiceImpl implements OwnerInfoService {

    @Resource
    private OwnerInfoMapper ownerInfoMapper;

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
