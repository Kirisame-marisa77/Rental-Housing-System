package cn.iocoder.yudao.module.rental.dal.mysql.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.favorite.vo.HouseFavoritePageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.favorite.HouseFavoriteDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface HouseFavoriteMapper extends BaseMapperX<HouseFavoriteDO> {

    default PageResult<HouseFavoriteDO> selectPage(HouseFavoritePageReqVO reqVO, Long tenantUserId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HouseFavoriteDO>()
                .eq(HouseFavoriteDO::getTenantUserId, tenantUserId)
                .orderByDesc(HouseFavoriteDO::getId));
    }

    default HouseFavoriteDO selectByHouseAndTenant(Long houseId, Long tenantUserId) {
        return selectOne(new LambdaQueryWrapperX<HouseFavoriteDO>()
                .eq(HouseFavoriteDO::getHouseId, houseId)
                .eq(HouseFavoriteDO::getTenantUserId, tenantUserId));
    }

    /**
     * 某租客收藏的全部房源 ID
     */
    default List<Long> selectHouseIdsByTenant(Long tenantUserId) {
        return selectList(new LambdaQueryWrapperX<HouseFavoriteDO>()
                .eq(HouseFavoriteDO::getTenantUserId, tenantUserId))
                .stream().map(HouseFavoriteDO::getHouseId).collect(Collectors.toList());
    }

    /**
     * 物理删除收藏
     *
     * 必须物理删除：唯一键 uk_house_tenant 不含 deleted 列，逻辑删除会让槽位一直被占用，
     * 导致「取消收藏后再收藏同一房源」撞唯一键报错。收藏是可丢弃数据，不需要留痕。
     */
    @Delete("DELETE FROM rental_house_favorite WHERE house_id = #{houseId} AND tenant_user_id = #{tenantUserId}")
    int deletePhysicallyByHouseAndTenant(@Param("houseId") Long houseId, @Param("tenantUserId") Long tenantUserId);

}
