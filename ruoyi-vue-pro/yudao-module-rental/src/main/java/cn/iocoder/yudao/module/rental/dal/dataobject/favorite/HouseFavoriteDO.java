package cn.iocoder.yudao.module.rental.dal.dataobject.favorite;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房源收藏表
 *
 * 注意：存在唯一键 uk_house_tenant(house_id, tenant_user_id)，且该键不含 deleted 列。
 * 逻辑删除会让槽位一直被占用（取消后再收藏会撞唯一键），因此取消防费走的是物理删除。
 *
 * @author yudao
 */
@TableName("rental_house_favorite")
@Data
@EqualsAndHashCode(callSuper = true)
public class HouseFavoriteDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 房源 ID，关联 rental_house
     */
    private Long houseId;
    /**
     * 租客 ID，关联 rental_tenant_info.id
     */
    private Long tenantUserId;

}
