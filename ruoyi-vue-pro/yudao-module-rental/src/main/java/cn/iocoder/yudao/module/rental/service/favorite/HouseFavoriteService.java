package cn.iocoder.yudao.module.rental.service.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.favorite.vo.HouseFavoritePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.favorite.vo.HouseFavoriteRespVO;

import java.util.List;

/**
 * 房源收藏 Service 接口
 *
 * @author yudao
 */
public interface HouseFavoriteService {

    /**
     * 收藏房源（幂等，重复收藏返回已有记录的编号）
     *
     * @param tenantUserId 租客编号
     * @param houseId      房源编号
     * @return 收藏记录编号
     */
    Long addFavorite(Long tenantUserId, Long houseId);

    /**
     * 取消收藏（幂等，本来就没收藏也算成功）
     *
     * @param tenantUserId 租客编号
     * @param houseId      房源编号
     */
    void cancelFavorite(Long tenantUserId, Long houseId);

    /**
     * 获得某租客的收藏分页（含房源信息）
     *
     * @param pageReqVO    分页查询
     * @param tenantUserId 租客编号
     * @return 收藏分页
     */
    PageResult<HouseFavoriteRespVO> getFavoritePage(HouseFavoritePageReqVO pageReqVO, Long tenantUserId);

    /**
     * 获得某租客收藏的全部房源编号
     *
     * 供租客端「找房」列表本地比对是否已收藏，避免逐行请求
     *
     * @param tenantUserId 租客编号
     * @return 房源编号列表
     */
    List<Long> getFavoriteHouseIds(Long tenantUserId);

}
