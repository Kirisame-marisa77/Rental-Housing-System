package cn.iocoder.yudao.module.rental.dal.dataobject.meter;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 抄表配置表
 *
 * @author yudao
 */
@TableName("rental_meter_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class MeterConfigDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 配置项：meter_period / water_price / electricity_price
     */
    private String configKey;
    /**
     * 配置值
     */
    private String configValue;
    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;
    /**
     * 配置人 ID，关联 sys_user
     */
    private Long configUser;

}
