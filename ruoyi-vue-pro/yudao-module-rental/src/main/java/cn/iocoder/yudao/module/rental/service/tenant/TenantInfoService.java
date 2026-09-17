package cn.iocoder.yudao.module.rental.service.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantRegisterReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;

/**
 * 租客信息 Service 接口
 *
 * @author yudao
 */
public interface TenantInfoService {

    /**
     * 创建租客
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTenantInfo(TenantInfoSaveReqVO createReqVO);

    /**
     * 更新租客
     *
     * @param updateReqVO 更新信息
     */
    void updateTenantInfo(TenantInfoSaveReqVO updateReqVO);

    /**
     * 删除租客
     *
     * @param id 编号
     */
    void deleteTenantInfo(Long id);

    /**
     * 获得租客分页
     *
     * @param pageReqVO 分页查询
     * @return 租客分页
     */
    PageResult<TenantInfoDO> getTenantInfoPage(TenantInfoPageReqVO pageReqVO);

    /**
     * 获得租客
     *
     * @param id 编号
     * @return 租客
     */
    TenantInfoDO getTenantInfo(Long id);

    /**
     * 租客登录（手机号 + 密码）
     *
     * @param phone    手机号
     * @param password 密码
     * @return 租客
     */
    TenantInfoDO login(String phone, String password);

    /**
     * 租客注册
     *
     * @param registerReqVO 注册信息
     * @return 编号
     */
    Long register(TenantRegisterReqVO registerReqVO);

    /**
     * 修改密码
     *
     * @param tenantId    租客编号
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long tenantId, String oldPassword, String newPassword);

}
