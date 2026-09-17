package cn.iocoder.yudao.module.rental.dal.mysql.house;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseImageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HouseImageMapper extends BaseMapperX<HouseImageDO> {

    default List<HouseImageDO> selectListByHouseId(Long houseId) {
        return selectList(new LambdaQueryWrapperX<HouseImageDO>()
                .eq(HouseImageDO::getHouseId, houseId)
                .orderByAsc(HouseImageDO::getSort));
    }

    default void deleteByHouseId(Long houseId) {
        delete(new LambdaQueryWrapperX<HouseImageDO>()
                .eq(HouseImageDO::getHouseId, houseId));
    }

}
