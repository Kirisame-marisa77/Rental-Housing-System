package cn.iocoder.yudao.module.rental.dal.dataobject.viewing;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 预约看房记录表
 *
 * @author yudao
 */
@TableName("rental_viewing_appointment")
@Data
@EqualsAndHashCode(callSuper = true)
public class ViewingAppointmentDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 租客 ID，关联 rental_tenant_info.id
     */
    private Long tenantUserId;
    /**
     * 预约日期
     */
    private LocalDate appointmentDate;
    /**
     * 开始时间，零填充 24 小时制 "HH:mm"，如 "09:00"
     */
    private String startTime;
    /**
     * 结束时间，零填充 24 小时制 "HH:mm"，如 "10:00"
     */
    private String endTime;
    /**
     * 状态：0-待确认，1-已确认，2-已完成，3-已取消
     */
    private Integer status;
    /**
     * 看房反馈，由房东在「完成」时填写
     */
    private String feedback;

}
