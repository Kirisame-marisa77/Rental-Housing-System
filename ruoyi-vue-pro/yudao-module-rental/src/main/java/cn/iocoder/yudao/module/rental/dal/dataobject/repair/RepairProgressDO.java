package cn.iocoder.yudao.module.rental.dal.dataobject.repair;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 维修进度表
 *
 * @author yudao
 */
@TableName("rental_repair_progress")
@Data
@EqualsAndHashCode(callSuper = true)
public class RepairProgressDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 工单 ID，关联 rental_repair_order
     */
    private Long orderId;
    /**
     * 操作人 ID
     */
    private Long operatorId;
    /**
     * 操作人类型：0-租客，1-管理员，2-维修人员
     */
    private Integer operatorType;
    /**
     * 操作类型：提交报修/分配工单/接单/开始维修/维修完成/确认完成/驳回返工/评价
     */
    private String actionType;
    /**
     * 操作描述
     */
    private String description;

}
