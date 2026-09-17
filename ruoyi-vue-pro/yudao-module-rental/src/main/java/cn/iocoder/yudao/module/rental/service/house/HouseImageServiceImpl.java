package cn.iocoder.yudao.module.rental.service.house;

import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseImageSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseImageDO;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseImageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 房源图片 Service 实现类
 *
 * @author yudao
 */
@Service
public class HouseImageServiceImpl implements HouseImageService {

    @Resource
    private HouseImageMapper houseImageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveHouseImage(HouseImageSaveReqVO saveReqVO) {
        // 先删除原有图片，再整体插入（简化：覆盖式保存）
        houseImageMapper.deleteByHouseId(saveReqVO.getHouseId());
        List<HouseImageDO> images = new ArrayList<>();
        addImages(images, saveReqVO.getHouseId(), saveReqVO.getRealityImages(), 0);
        addImages(images, saveReqVO.getHouseId(), saveReqVO.getLayoutImages(), 1);
        if (!images.isEmpty()) {
            houseImageMapper.insertBatch(images);
        }
    }

    @Override
    public List<HouseImageDO> getHouseImageList(Long houseId) {
        return houseImageMapper.selectListByHouseId(houseId);
    }

    /**
     * 组装图片列表
     *
     * @param images    目标集合
     * @param houseId   房源编号
     * @param urls      图片 URL 列表
     * @param imageType 图片类型
     */
    private void addImages(List<HouseImageDO> images, Long houseId, List<String> urls, Integer imageType) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        for (int i = 0; i < urls.size(); i++) {
            HouseImageDO image = new HouseImageDO();
            image.setHouseId(houseId);
            image.setImageUrl(urls.get(i));
            image.setImageType(imageType);
            image.setSort(i);
            images.add(image);
        }
    }

}
