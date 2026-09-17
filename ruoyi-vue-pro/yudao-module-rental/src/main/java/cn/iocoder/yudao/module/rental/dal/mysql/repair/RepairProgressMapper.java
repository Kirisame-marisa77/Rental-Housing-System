package cn.iocoder.yudao.module.rental.dal.mysql.repair;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairProgressDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RepairProgressMapper extends BaseMapperX<RepairProgressDO> {

    default List<RepairProgressDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<RepairProgressDO>()
                .eq(RepairProgressDO::getOrderId, orderId)
                .orderByAsc(RepairProgressDO::getId));
    }

}
