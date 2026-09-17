package cn.iocoder.yudao.module.rental.service.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.favorite.vo.HouseFavoritePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.favorite.vo.HouseFavoriteRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.favorite.HouseFavoriteDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.favorite.HouseFavoriteMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_NOT_EXISTS;

/**
 * 房源收藏 Service 实现类
 *
 * 注意：本类只注入 Mapper，不注入其它 Service。项目里已有一条 Service → Service 的依赖链
 * （RentBillService → ContractService），为避免继续加环，新 Service 一律只依赖 Mapper。
 *
 * @author yudao
 */
@Service
public class HouseFavoriteServiceImpl implements HouseFavoriteService {

    @Resource
    private HouseFavoriteMapper favoriteMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private OwnerInfoMapper ownerInfoMapper;

    /**
     * 收藏是幂等操作，刻意不加 @Transactional：并发重复收藏时落败的一方会拿到 DuplicateKeyException，
     * 而该异常恰好表示「行已存在」＝期望终态。若在事务内吞掉数据完整性异常，
     * 事务会被标记为 rollback-only，提交时反而抛 UnexpectedRollbackException。
     */
    @Override
    public Long addFavorite(Long tenantUserId, Long houseId) {
        HouseDO house = houseMapper.selectById(houseId);
        if (house == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        // 快速路径：已收藏直接返回
        HouseFavoriteDO exist = favoriteMapper.selectByHouseAndTenant(houseId, tenantUserId);
        if (exist != null) {
            return exist.getId();
        }
        HouseFavoriteDO favorite = new HouseFavoriteDO();
        favorite.setHouseId(houseId);
        favorite.setTenantUserId(tenantUserId);
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException e) {
            // 并发下抢输了，行已由另一个请求写入
            HouseFavoriteDO created = favoriteMapper.selectByHouseAndTenant(houseId, tenantUserId);
            return created == null ? null : created.getId();
        }
        return favorite.getId();
    }

    @Override
    public void cancelFavorite(Long tenantUserId, Long houseId) {
        // 物理删除（见 Mapper 注释）：影响 0 行说明本来就没收藏，同样是成功，天然幂等
        favoriteMapper.deletePhysicallyByHouseAndTenant(houseId, tenantUserId);
    }

    @Override
    public PageResult<HouseFavoriteRespVO> getFavoritePage(HouseFavoritePageReqVO pageReqVO, Long tenantUserId) {
        PageResult<HouseFavoriteDO> pageResult = favoriteMapper.selectPage(pageReqVO, tenantUserId);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(enrichFavorites(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public List<Long> getFavoriteHouseIds(Long tenantUserId) {
        return favoriteMapper.selectHouseIdsByTenant(tenantUserId);
    }

    /**
     * 批量补全房源与房东信息（固定 2 次查询，与行数无关）
     */
    private List<HouseFavoriteRespVO> enrichFavorites(List<HouseFavoriteDO> favorites) {
        Set<Long> houseIds = favorites.stream().map(HouseFavoriteDO::getHouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // selectBatchIds 对空集合会生成 IN ()，必须先判空
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        Set<Long> ownerIds = houseMap.values().stream().map(HouseDO::getOwnerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, OwnerInfoDO> ownerMap = ownerIds.isEmpty() ? Collections.emptyMap()
                : ownerInfoMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(OwnerInfoDO::getId, Function.identity(), (a, b) -> a));

        List<HouseFavoriteRespVO> list = new ArrayList<>(favorites.size());
        for (HouseFavoriteDO favorite : favorites) {
            HouseFavoriteRespVO vo = BeanUtils.toBean(favorite, HouseFavoriteRespVO.class);
            HouseDO house = houseMap.get(favorite.getHouseId());
            if (house != null) {
                vo.setHouseNo(house.getHouseNo());
                vo.setCommunityName(house.getCommunityName());
                vo.setArea(house.getArea());
                vo.setBuildingNo(house.getBuildingNo());
                vo.setRoomNo(house.getRoomNo());
                vo.setLayout(house.getLayout());
                vo.setSquareArea(house.getSquareArea());
                vo.setMonthlyRent(house.getMonthlyRent());
                vo.setDeposit(house.getDeposit());
                vo.setDecoration(house.getDecoration());
                vo.setHouseStatus(house.getStatus());
                vo.setOwnerId(house.getOwnerId());
                OwnerInfoDO owner = ownerMap.get(house.getOwnerId());
                if (owner != null) {
                    vo.setOwnerName(owner.getName());
                    vo.setOwnerPhone(owner.getPhone());
                }
            }
            list.add(vo);
        }
        return list;
    }

}
