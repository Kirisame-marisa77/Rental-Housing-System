package cn.iocoder.yudao.module.rental.service.house;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseDetailRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseImageRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HousePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseImageDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseImageMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_ALREADY_REVIEWED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_STATUS_INVALID;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_NOT_LOGIN;

/**
 * 房源 Service 实现类
 *
 * @author yudao
 */
@Service
public class HouseServiceImpl implements HouseService {

    /**
     * 房源状态：0-下架
     *
     * 详情页对下架房源一律按「不存在」处理，不对外泄露其存在性。
     */
    private static final int HOUSE_STATUS_OFFLINE = 0;
    /**
     * 房源状态：1-上架
     */
    private static final int HOUSE_STATUS_ONLINE = 1;
    /**
     * 图片类型：0-实景图
     */
    private static final int IMAGE_TYPE_REALITY = 0;
    /**
     * 图片类型：1-户型图
     */
    private static final int IMAGE_TYPE_LAYOUT = 1;

    @Resource
    private HouseMapper houseMapper;

    @Resource
    private HouseImageMapper houseImageMapper;

    @Resource
    private OwnerInfoMapper ownerInfoMapper;

    @Override
    public Long createHouse(HouseSaveReqVO createReqVO) {
        HouseDO house = BeanUtils.toBean(createReqVO, HouseDO.class);
        // 房源编号为空时自动生成
        if (StrUtil.isBlank(house.getHouseNo())) {
            house.setHouseNo(generateHouseNo());
        }
        houseMapper.insert(house);
        return house.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createHouseByOwner(HouseSaveReqVO createReqVO) {
        HouseDO house = BeanUtils.toBean(createReqVO, HouseDO.class);
        if (StrUtil.isBlank(house.getHouseNo())) {
            house.setHouseNo(generateHouseNo());
        }
        house.setReviewStatus(0); // 待审核
        house.setStatus(0);       // 下架
        houseMapper.insert(house);
        // 房源与图片必须同事务：否则房源插入成功、图片插入失败会留下一条没有图的半成品房源
        saveHouseImages(house.getId(), createReqVO);
        return house.getId();
    }

    /**
     * 保存房源新建时带的图片
     *
     * 只在创建路径调用，房源是新的、必然没有旧图，所以直接插入不做覆盖。
     * 用 Mapper 直接操作而非注入 HouseImageService，遵循本项目「新写的 Service 只依赖 Mapper」的约定。
     */
    private void saveHouseImages(Long houseId, HouseSaveReqVO reqVO) {
        List<HouseImageDO> images = new ArrayList<>();
        appendImages(images, houseId, reqVO.getRealityImages(), IMAGE_TYPE_REALITY);
        appendImages(images, houseId, reqVO.getLayoutImages(), IMAGE_TYPE_LAYOUT);
        if (CollUtil.isNotEmpty(images)) {
            houseImageMapper.insertBatch(images);
        }
    }

    private void appendImages(List<HouseImageDO> target, Long houseId, List<String> urls, int imageType) {
        if (CollUtil.isEmpty(urls)) {
            return;
        }
        int sort = 0;
        for (String url : urls) {
            // 前端可能提交空行，跳过；sort 只对有效图片递增，避免出现空洞
            if (StrUtil.isBlank(url)) {
                continue;
            }
            HouseImageDO image = new HouseImageDO();
            image.setHouseId(houseId);
            image.setImageUrl(url.trim());
            image.setImageType(imageType);
            image.setSort(sort++);
            target.add(image);
        }
    }

    @Override
    public void updateHouse(HouseSaveReqVO updateReqVO) {
        // 校验是否存在
        validateHouseExists(updateReqVO.getId());
        // 更新房源
        HouseDO updateObj = BeanUtils.toBean(updateReqVO, HouseDO.class);
        houseMapper.updateById(updateObj);
    }

    @Override
    public void deleteHouse(Long id) {
        // 校验是否存在
        validateHouseExists(id);
        // 删除房源
        houseMapper.deleteById(id);
    }

    @Override
    public void updateHouseStatusByOwner(Long ownerId, Long houseId, Boolean online) {
        if (ownerId == null) {
            throw exception(OWNER_NOT_LOGIN);
        }
        HouseDO house = houseMapper.selectById(houseId);
        // 不是自己的房源一律按「不存在」处理，避免靠遍历 id 探测别人名下有哪些房源
        if (house == null || !ownerId.equals(house.getOwnerId())) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        int fromStatus = house.getStatus() == null ? HOUSE_STATUS_OFFLINE : house.getStatus();
        // 2-已锁定（签约中）/ 3-已出租 由签约与退租流程驱动，不接受手动修改：
        // 手动把「已出租」改成「上架」会让同一房源被重复租出去
        if (fromStatus != HOUSE_STATUS_OFFLINE && fromStatus != HOUSE_STATUS_ONLINE) {
            throw exception(HOUSE_STATUS_INVALID);
        }
        int toStatus = Boolean.TRUE.equals(online) ? HOUSE_STATUS_ONLINE : HOUSE_STATUS_OFFLINE;
        if (fromStatus == toStatus) {
            return; // 已经是目标状态，幂等返回
        }
        // CAS：校验之后、更新之前若被并发租走（1→3），这里返回 0，状态保持不变
        houseMapper.updateStatusWithOnlineTime(houseId, fromStatus, toStatus,
                toStatus == HOUSE_STATUS_ONLINE ? LocalDateTime.now() : null);
    }

    @Override
    public void reviewHouse(Long id, Boolean pass, String reason) {
        HouseDO house = validateHouseExists(id);
        int reviewStatus = house.getReviewStatus() == null ? 0 : house.getReviewStatus();
        if (Boolean.TRUE.equals(pass)) {
            // 通过：仅待审核的房源
            if (reviewStatus != 0) {
                throw exception(HOUSE_ALREADY_REVIEWED);
            }
            house.setReviewStatus(1); // 已通过
            house.setStatus(1);       // 上架
            house.setOnlineTime(LocalDateTime.now());
            house.setReviewReason(null);
        } else {
            // 驳回：待审核 或 已通过的房源
            if (reviewStatus != 0 && reviewStatus != 1) {
                throw exception(HOUSE_ALREADY_REVIEWED);
            }
            house.setReviewStatus(2); // 已驳回
            house.setReviewReason(reason);
            house.setStatus(0);       // 下架
        }
        houseMapper.updateById(house);
    }

    @Override
    public PageResult<HouseDO> getHousePage(HousePageReqVO pageReqVO) {
        return houseMapper.selectPage(pageReqVO);
    }

    @Override
    public HouseDO getHouse(Long id) {
        return houseMapper.selectById(id);
    }

    @Override
    public HouseDetailRespVO getTenantHouseDetail(Long id) {
        HouseDO house = houseMapper.selectById(id);
        // 下架房源按「不存在」处理：否则租客能靠遍历 id 探测到尚未上架/已退回的房源
        if (house == null || Integer.valueOf(HOUSE_STATUS_OFFLINE).equals(house.getStatus())) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        HouseDetailRespVO vo = BeanUtils.toBean(house, HouseDetailRespVO.class);
        // 房东信息：只给姓名与电话，不暴露身份证、银行卡（与 HouseFavoriteRespVO 口径一致）
        if (house.getOwnerId() != null) {
            OwnerInfoDO owner = ownerInfoMapper.selectById(house.getOwnerId());
            if (owner != null) {
                vo.setOwnerName(owner.getName());
                vo.setOwnerPhone(owner.getPhone());
            }
        }
        // 图片只查一次，在内存里按类型分组
        List<HouseImageDO> images = houseImageMapper.selectListByHouseId(id);
        vo.setRealityImages(BeanUtils.toBean(filterByImageType(images, IMAGE_TYPE_REALITY), HouseImageRespVO.class));
        vo.setLayoutImages(BeanUtils.toBean(filterByImageType(images, IMAGE_TYPE_LAYOUT), HouseImageRespVO.class));
        return vo;
    }

    private List<HouseImageDO> filterByImageType(List<HouseImageDO> images, int imageType) {
        if (CollUtil.isEmpty(images)) {
            return new ArrayList<>();
        }
        return images.stream()
                // 历史数据里 image_type 可能为 NULL，按实景图处理
                .filter(image -> Integer.valueOf(imageType).equals(
                        image.getImageType() == null ? IMAGE_TYPE_REALITY : image.getImageType()))
                .sorted((a, b) -> Integer.compare(
                        a.getSort() == null ? 0 : a.getSort(),
                        b.getSort() == null ? 0 : b.getSort()))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    public HouseDO validateHouseExists(Long id) {
        if (id == null) {
            return null;
        }
        HouseDO house = houseMapper.selectById(id);
        if (house == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        return house;
    }

    /**
     * 生成房源编号：FW + 年月日时分秒
     */
    private String generateHouseNo() {
        return "FW" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
