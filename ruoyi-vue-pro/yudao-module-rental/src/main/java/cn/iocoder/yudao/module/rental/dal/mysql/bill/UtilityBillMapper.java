package cn.iocoder.yudao.module.rental.dal.mysql.bill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.UtilityBillDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UtilityBillMapper extends BaseMapperX<UtilityBillDO> {

    default PageResult<UtilityBillDO> selectPage(UtilityBillPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UtilityBillDO>()
                .likeIfPresent(UtilityBillDO::getBillNo, reqVO.getBillNo())
                .eqIfPresent(UtilityBillDO::getContractId, reqVO.getContractId())
                .eqIfPresent(UtilityBillDO::getHouseId, reqVO.getHouseId())
                .eqIfPresent(UtilityBillDO::getPayStatus, reqVO.getPayStatus())
                .inIfPresent(UtilityBillDO::getHouseId, reqVO.getHouseIds())
                .orderByDesc(UtilityBillDO::getId));
    }

}
