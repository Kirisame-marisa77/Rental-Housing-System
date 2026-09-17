package cn.iocoder.yudao.module.rental.service.repair;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairOrderDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairProgressDO;

import java.util.List;

/**
 * 维修工单 Service 接口
 *
 * @author yudao
 */
public interface RepairOrderService {

    /**
     * 创建维修工单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRepairOrder(RepairOrderSaveReqVO createReqVO);

    /**
     * 更新维修工单
     *
     * @param updateReqVO 更新信息
     */
    void updateRepairOrder(RepairOrderSaveReqVO updateReqVO);

    /**
     * 删除维修工单
     *
     * @param id 编号
     */
    void deleteRepairOrder(Long id);

    /**
     * 分配维修人员
     *
     * @param id         工单编号
     * @param repairerId 维修人员 ID
     */
    void assignRepairOrder(Long id, Long repairerId);

    /**
     * 更新工单状态
     *
     * @param id     工单编号
     * @param status 目标状态
     */
    void updateRepairOrderStatus(Long id, Integer status);

    /**
     * 维修完成（待验收）
     *
     * @param id                工单编号
     * @param repairDescription 维修说明
     */
    void completeRepairOrder(Long id, String repairDescription);

    /**
     * 业主处理维修并上传证据（进入待验收）
     *
     * @param id                 工单编号
     * @param ownerId            业主 ID
     * @param repairDescription  维修说明
     * @param handleEvidence     处理证据图片 JSON
     */
    void handleRepairOrder(Long id, Long ownerId, String repairDescription, String handleEvidence);

    /**
     * 管理员验收判定维修是否合格
     *
     * @param id     工单编号
     * @param pass   是否合格
     * @param reason 不合格原因
     */
    void reviewRepairOrder(Long id, Boolean pass, String reason);

    /**
     * 租客确认维修完成（本期由管理端代操作）
     *
     * @param id 工单编号
     */
    void confirmRepairOrder(Long id);

    /**
     * 租客报修
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRepairOrderByTenant(RepairOrderSaveReqVO createReqVO);

    /**
     * 租客确认维修完成
     *
     * @param tenantId 租客 ID
     * @param id       工单编号
     */
    void confirmRepairOrderByTenant(Long tenantId, Long id);

    /**
     * 获得维修工单分页
     *
     * @param pageReqVO 分页查询
     * @return 工单分页
     */
    PageResult<RepairOrderDO> getRepairOrderPage(RepairOrderPageReqVO pageReqVO);

    /**
     * 获得维修工单
     *
     * @param id 编号
     * @return 工单
     */
    RepairOrderDO getRepairOrder(Long id);

    /**
     * 获得维修进度列表
     *
     * @param orderId 工单编号
     * @return 进度列表
     */
    List<RepairProgressDO> getRepairProgressList(Long orderId);

}
