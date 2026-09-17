package cn.iocoder.yudao.module.rental.service.moveout;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationTenantCreateReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutConfirmReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.MoveOutApplicationDO;

/**
 * 退租申请 Service 接口
 *
 * @author yudao
 */
public interface MoveOutApplicationService {

    /**
     * 创建退租申请
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMoveOutApplication(MoveOutApplicationSaveReqVO createReqVO);

    /**
     * 更新退租申请
     *
     * @param updateReqVO 更新信息
     */
    void updateMoveOutApplication(MoveOutApplicationSaveReqVO updateReqVO);

    /**
     * 删除退租申请
     *
     * @param id 编号
     */
    void deleteMoveOutApplication(Long id);

    /**
     * 处理退租申请（房屋验收 + 费用结算，生成结算单）
     *
     * @param confirmReqVO 处理信息
     * @return 结算单编号
     */
    Long confirmMoveOutApplication(MoveOutConfirmReqVO confirmReqVO);

    /**
     * 租客发起退租申请
     *
     * 租客编号与房源编号一律由合同反查，不采信入参里的值，避免越权退别人的房
     *
     * @param tenantId   租客编号（来自登录态）
     * @param createReqVO 创建信息，只需要 contractId / moveOutType / expectedMoveOutDate / moveOutReason
     * @return 编号
     */
    Long createMoveOutApplicationByTenant(Long tenantId, MoveOutApplicationTenantCreateReqVO createReqVO);

    /**
     * 获得退租申请分页
     *
     * @param pageReqVO 分页查询
     * @return 退租申请分页
     */
    PageResult<MoveOutApplicationDO> getMoveOutApplicationPage(MoveOutApplicationPageReqVO pageReqVO);

    /**
     * 获得某租客的退租申请分页（含房源地址，避免租客端只看到「房源 3」这种裸 ID）
     *
     * @param tenantId   租客编号
     * @param pageReqVO  分页查询
     * @return 退租申请分页
     */
    PageResult<MoveOutApplicationRespVO> getTenantMoveOutRespPage(Long tenantId,
                                                                 MoveOutApplicationPageReqVO pageReqVO);

    /**
     * 获得退租申请
     *
     * @param id 编号
     * @return 退租申请
     */
    MoveOutApplicationDO getMoveOutApplication(Long id);

}
