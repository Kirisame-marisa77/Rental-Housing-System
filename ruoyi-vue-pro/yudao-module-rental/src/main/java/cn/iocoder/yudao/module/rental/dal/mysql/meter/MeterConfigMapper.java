package cn.iocoder.yudao.module.rental.dal.mysql.meter;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.dal.dataobject.meter.MeterConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeterConfigMapper extends BaseMapperX<MeterConfigDO> {

    default MeterConfigDO selectByKey(String configKey) {
        return selectOne(new LambdaQueryWrapperX<MeterConfigDO>()
                .eq(MeterConfigDO::getConfigKey, configKey)
                .orderByDesc(MeterConfigDO::getEffectiveTime)
                .last("LIMIT 1"));
    }

}
