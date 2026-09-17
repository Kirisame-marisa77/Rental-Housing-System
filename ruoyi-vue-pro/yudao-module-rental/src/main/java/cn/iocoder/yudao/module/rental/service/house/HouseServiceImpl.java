package cn.iocoder.yudao.module.rental.service.house;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HousePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_ALREADY_REVIEWED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_NOT_EXISTS;

/**
 * 房源 Service 实现类
 *
 * @author yudao
 */
@Service
public class HouseServiceImpl implements HouseService {

    @Resource
    private HouseMapper houseMapper;

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
    public Long createHouseByOwner(HouseSaveReqVO createReqVO) {
        HouseDO house = BeanUtils.toBean(createReqVO, HouseDO.class);
        if (StrUtil.isBlank(house.getHouseNo())) {
            house.setHouseNo(generateHouseNo());
        }
        house.setReviewStatus(0); // 待审核
        house.setStatus(0);       // 下架
        houseMapper.insert(house);
        return house.getId();
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
