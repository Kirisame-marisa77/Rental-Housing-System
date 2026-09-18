package cn.iocoder.yudao.module.rental.service.owner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;

/**
 * 业主信息 Service 接口
 *
 * @author yudao
 */
public interface OwnerInfoService {

    /**
     * 创建业主
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOwnerInfo(OwnerInfoSaveReqVO createReqVO);

    /**
     * 更新业主
     *
     * @param updateReqVO 更新信息
     */
    void updateOwnerInfo(OwnerInfoSaveReqVO updateReqVO);

    /**
     * 删除业主
     *
     * @param id 编号
     */
    void deleteOwnerInfo(Long id);

    /**
     * 获得业主分页
     *
     * @param pageReqVO 分页查询
     * @return 业主分页
     */
    PageResult<OwnerInfoDO> getOwnerInfoPage(OwnerInfoPageReqVO pageReqVO);

    /**
     * 获得业主
     *
     * @param id 编号
     * @return 业主
     */
    OwnerInfoDO getOwnerInfo(Long id);

    /**
     * 按手机号获得业主（业主端登录）
     *
     * @param phone 手机号
     * @return 业主
     */
    OwnerInfoDO getOwnerInfoByPhone(String phone);

    /**
     * 业主登录（手机号 + 密码）
     *
     * @param phone    手机号
     * @param password 密码
     * @return 业主
     */
    OwnerInfoDO login(String phone, String password);

    /**
     * 业主注册
     *
     * @param registerReqVO 注册信息
     * @return 编号
     */
    Long register(cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerRegisterReqVO registerReqVO);

    /**
     * 修改密码
     *
     * @param ownerId     业主编号
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long ownerId, String oldPassword, String newPassword);

    /**
     * 注销账号（逻辑删除，并释放手机号与身份证号以便重新注册）
     *
     * 前置条件：名下没有出租中/签约中的房源，且名下房源没有未结束的合同。
     * 通过校验后会把名下房源全部下架。历史合同与账单不受影响。
     *
     * @param ownerId  业主编号（来自登录态）
     * @param password 登录密码，二次确认
     */
    void deregister(Long ownerId, String password);

}
