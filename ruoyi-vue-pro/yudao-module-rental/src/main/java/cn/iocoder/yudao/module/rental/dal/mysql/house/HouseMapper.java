package cn.iocoder.yudao.module.rental.dal.mysql.house;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HousePageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseMapper extends BaseMapperX<HouseDO> {

    default PageResult<HouseDO> selectPage(HousePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HouseDO>()
                .likeIfPresent(HouseDO::getCommunityName, reqVO.getCommunityName())
                .likeIfPresent(HouseDO::getArea, reqVO.getArea())
                .likeIfPresent(HouseDO::getLayout, reqVO.getLayout())
                .eqIfPresent(HouseDO::getStatus, reqVO.getStatus())
                .eqIfPresent(HouseDO::getOwnerId, reqVO.getOwnerId())
                .eqIfPresent(HouseDO::getReviewStatus, reqVO.getReviewStatus())
                .orderByDesc(HouseDO::getId));
    }

    default HouseDO selectByHouseNo(String houseNo) {
        return selectOne(HouseDO::getHouseNo, houseNo);
    }

    /**
     * 乐观锁式的房源状态流转：仅当当前状态为 fromStatus 时才改为 toStatus，返回影响行数
     *
     * 「先到先得」抢占房源靠它：CAS 1-上架 → 3-已出租，只有一个并发请求能成功
     */
    default int updateStatusByIdAndStatus(Long id, Integer fromStatus, Integer toStatus) {
        return update(new LambdaUpdateWrapper<HouseDO>()
                .eq(HouseDO::getId, id)
                .eq(HouseDO::getStatus, fromStatus)
                .set(HouseDO::getStatus, toStatus));
    }

}
