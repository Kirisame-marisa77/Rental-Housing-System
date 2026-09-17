package cn.iocoder.yudao.module.rental.service.house;

import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseImageSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseImageDO;

import java.util.List;

/**
 * 房源图片 Service 接口
 *
 * @author yudao
 */
public interface HouseImageService {

    /**
     * 保存房源图片（整体覆盖：先删后增）
     *
     * @param saveReqVO 图片信息
     */
    void saveHouseImage(HouseImageSaveReqVO saveReqVO);

    /**
     * 获得房源图片列表
     *
     * @param houseId 房源编号
     * @return 图片列表
     */
    List<HouseImageDO> getHouseImageList(Long houseId);

}
