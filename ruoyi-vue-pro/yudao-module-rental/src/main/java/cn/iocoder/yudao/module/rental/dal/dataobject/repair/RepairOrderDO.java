package cn.iocoder.yudao.module.rental.dal.dataobject.repair;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 维修工单表
 *
 * @author yudao
 */
@TableName("rental_repair_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class RepairOrderDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 工单编号，规则：WX-年月-流水号
     */
    private String orderNo;
    /**
     * 租客 ID，关联 rental_tenant_info
     */
    private Long tenantUserId;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 报修类型：水电维修/家电维修/管道疏通/门窗维修/墙面地面/其他
     */
    private String repairType;
    /**
     * 问题描述
     */
    private String description;
    /**
     * 问题图片，JSON 数组
     */
    private String images;
    /**
     * 期望上门时间
     */
    private LocalDateTime expectedTime;
    /**
     * 工单状态：0-待处理，1-处理中，2-待验收，3-已处理，4-已驳回(退回重做)
     */
    private Integer status;
    /**
     * 优先级：0-普通，1-紧急，2-特急
     */
    private Integer priority;
    /**
     * 维修人员 ID，关联 sys_user
     */
    private Long repairerId;
    /**
     * 处理业主 ID，关联 rental_owner_info
     */
    private Long ownerId;
    /**
     * 分配时间
     */
    private LocalDateTime assignTime;
    /**
     * 维修完成时间
     */
    private LocalDateTime completeTime;
    /**
     * 维修说明（处理过程和结果）
     */
    private String repairDescription;
    /**
     * 处理证据图片，JSON 数组
     */
    private String handleEvidence;
    /**
     * 业主处理完成（上传证据）时间
     */
    private LocalDateTime handleTime;
    /**
     * 验收不合格原因
     */
    private String reviewReason;
    /**
     * 评价星级：1-5
     */
    private Integer rating;
    /**
     * 评价内容
     */
    private String evaluationContent;
    /**
     * 评价时间
     */
    private LocalDateTime evaluationTime;

}
