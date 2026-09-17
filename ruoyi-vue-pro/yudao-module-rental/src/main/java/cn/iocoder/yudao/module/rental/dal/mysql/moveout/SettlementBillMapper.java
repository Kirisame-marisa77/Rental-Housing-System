package cn.iocoder.yudao.module.rental.dal.mysql.moveout;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.SettlementBillPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.SettlementBillDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SettlementBillMapper extends BaseMapperX<SettlementBillDO> {

    default PageResult<SettlementBillDO> selectPage(SettlementBillPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SettlementBillDO>()
                .eqIfPresent(SettlementBillDO::getContractId, reqVO.getContractId())
                .eqIfPresent(SettlementBillDO::getMoveOutType, reqVO.getMoveOutType())
                .orderByDesc(SettlementBillDO::getId));
    }

}
