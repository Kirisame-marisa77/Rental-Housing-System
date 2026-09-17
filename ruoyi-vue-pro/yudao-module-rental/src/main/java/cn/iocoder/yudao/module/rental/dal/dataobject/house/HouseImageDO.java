package cn.iocoder.yudao.module.rental.dal.dataobject.house;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房源图片表
 *
 * @author yudao
 */
@TableName("rental_house_image")
@Data
@EqualsAndHashCode(callSuper = true)
public class HouseImageDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 图片 URL
     */
    private String imageUrl;
    /**
     * 排序序号，数值越小越靠前
     */
    private Integer sort;
    /**
     * 图片类型：0-实景图，1-户型图
     */
    private Integer imageType;

}
