package cn.iocoder.yudao.module.rental.service.repair;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairOrderDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairProgressDO;
import cn.iocoder.yudao.module.rental.dal.mysql.repair.RepairOrderMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.repair.RepairProgressMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.REPAIR_ORDER_NOT_EXISTS;

/**
 * 维修工单 Service 实现类
 *
 * @author yudao
 */
@Service
public class RepairOrderServiceImpl implements RepairOrderService {

    @Resource
    private RepairOrderMapper repairOrderMapper;
    @Resource
    private RepairProgressMapper repairProgressMapper;

    @Override
    public Long createRepairOrder(RepairOrderSaveReqVO createReqVO) {
        RepairOrderDO order = BeanUtils.toBean(createReqVO, RepairOrderDO.class);
        if (StrUtil.isBlank(order.getOrderNo())) {
            order.setOrderNo(generateOrderNo());
        }
        order.setStatus(0); // 待处理
        repairOrderMapper.insert(order);
        logProgress(order.getId(), "提交报修", "租客提交报修");
        return order.getId();
    }

    @Override
    public void updateRepairOrder(RepairOrderSaveReqVO updateReqVO) {
        validateRepairOrderExists(updateReqVO.getId());
        RepairOrderDO updateObj = BeanUtils.toBean(updateReqVO, RepairOrderDO.class);
        repairOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteRepairOrder(Long id) {
        validateRepairOrderExists(id);
        repairOrderMapper.deleteById(id);
    }

    @Override
    public void assignRepairOrder(Long id, Long repairerId) {
        RepairOrderDO order = validateRepairOrderExists(id);
        order.setRepairerId(repairerId);
        order.setAssignTime(LocalDateTime.now());
        order.setStatus(1); // 处理中
        repairOrderMapper.updateById(order);
        logProgress(id, "分配工单", "分配处理人");
    }

    @Override
    public void updateRepairOrderStatus(Long id, Integer status) {
        RepairOrderDO order = validateRepairOrderExists(id);
        order.setStatus(status);
        repairOrderMapper.updateById(order);
        logProgress(id, "更新工单状态", "状态更新为 " + status);
    }

    @Override
    public void completeRepairOrder(Long id, String repairDescription) {
        RepairOrderDO order = validateRepairOrderExists(id);
        order.setStatus(2); // 待验收
        order.setCompleteTime(LocalDateTime.now());
        order.setRepairDescription(repairDescription);
        repairOrderMapper.updateById(order);
        logProgress(id, "维修完成", repairDescription);
    }

    @Override
    public void handleRepairOrder(Long id, Long ownerId, String repairDescription, String handleEvidence) {
        RepairOrderDO order = validateRepairOrderExists(id);
        order.setOwnerId(ownerId);
        order.setRepairDescription(repairDescription);
        order.setHandleEvidence(handleEvidence);
        order.setHandleTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        order.setStatus(2); // 待验收
        repairOrderMapper.updateById(order);
        logProgress(order.getId(), ownerId, 0, "业主处理完成", repairDescription);
    }

    @Override
    public void reviewRepairOrder(Long id, Boolean pass, String reason) {
        RepairOrderDO order = validateRepairOrderExists(id);
        if (Boolean.TRUE.equals(pass)) {
            order.setStatus(3); // 已处理
            order.setReviewReason(null);
            logProgress(id, "验收合格", "管理员判定维修合格");
        } else {
            order.setStatus(4); // 已驳回（退回重做）
            order.setReviewReason(reason);
            logProgress(id, "验收不合格", reason);
        }
        repairOrderMapper.updateById(order);
    }

    @Override
    public void confirmRepairOrder(Long id) {
        RepairOrderDO order = validateRepairOrderExists(id);
        order.setStatus(3); // 已处理
        repairOrderMapper.updateById(order);
        logProgress(id, "确认完成", "租客确认维修完成");
    }

    @Override
    public Long createRepairOrderByTenant(RepairOrderSaveReqVO createReqVO) {
        RepairOrderDO order = BeanUtils.toBean(createReqVO, RepairOrderDO.class);
        if (StrUtil.isBlank(order.getOrderNo())) {
            order.setOrderNo(generateOrderNo());
        }
        order.setStatus(0); // 待处理
        repairOrderMapper.insert(order);
        logProgress(order.getId(), order.getTenantUserId(), 0, "提交报修", "租客提交报修");
        return order.getId();
    }

    @Override
    public void confirmRepairOrderByTenant(Long tenantId, Long id) {
        RepairOrderDO order = validateRepairOrderExists(id);
        order.setStatus(3); // 已处理
        repairOrderMapper.updateById(order);
        logProgress(id, tenantId, 0, "确认完成", "租客确认维修完成");
    }

    @Override
    public PageResult<RepairOrderDO> getRepairOrderPage(RepairOrderPageReqVO pageReqVO) {
        return repairOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public RepairOrderDO getRepairOrder(Long id) {
        return repairOrderMapper.selectById(id);
    }

    @Override
    public List<RepairProgressDO> getRepairProgressList(Long orderId) {
        return repairProgressMapper.selectListByOrderId(orderId);
    }

    @VisibleForTesting
    public RepairOrderDO validateRepairOrderExists(Long id) {
        if (id == null) {
            return null;
        }
        RepairOrderDO order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw exception(REPAIR_ORDER_NOT_EXISTS);
        }
        return order;
    }

    private void logProgress(Long orderId, String actionType, String description) {
        logProgress(orderId, getLoginUserId(), 1, actionType, description);
    }

    private void logProgress(Long orderId, Long operatorId, Integer operatorType, String actionType, String description) {
        RepairProgressDO progress = new RepairProgressDO();
        progress.setOrderId(orderId);
        progress.setOperatorId(operatorId);
        progress.setOperatorType(operatorType);
        progress.setActionType(actionType);
        progress.setDescription(description);
        repairProgressMapper.insert(progress);
    }

    private String generateOrderNo() {
        return "WX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
