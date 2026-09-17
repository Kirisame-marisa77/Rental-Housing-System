package cn.iocoder.yudao.module.rental.dal.mysql.meter;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.meter.MeterReadingDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeterReadingMapper extends BaseMapperX<MeterReadingDO> {

    default PageResult<MeterReadingDO> selectPage(MeterReadingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MeterReadingDO>()
                .eqIfPresent(MeterReadingDO::getHouseId, reqVO.getHouseId())
                .eqIfPresent(MeterReadingDO::getContractId, reqVO.getContractId())
                .eqIfPresent(MeterReadingDO::getMeterType, reqVO.getMeterType())
                .eqIfPresent(MeterReadingDO::getOwnerId, reqVO.getOwnerId())
                .orderByDesc(MeterReadingDO::getId));
    }

    default MeterReadingDO selectLatest(Long houseId, Long contractId, Integer meterType) {
        return selectOne(new LambdaQueryWrapperX<MeterReadingDO>()
                .eq(MeterReadingDO::getHouseId, houseId)
                .eq(MeterReadingDO::getContractId, contractId)
                .eq(MeterReadingDO::getMeterType, meterType)
                .orderByDesc(MeterReadingDO::getReadingDate)
                .orderByDesc(MeterReadingDO::getId)
                .last("LIMIT 1"));
    }

}
