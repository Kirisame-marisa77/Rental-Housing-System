package cn.iocoder.yudao.module.rental.dal.mysql.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ContractMapper extends BaseMapperX<ContractDO> {

    default PageResult<ContractDO> selectPage(ContractPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContractDO>()
                .likeIfPresent(ContractDO::getContractNo, reqVO.getContractNo())
                .eqIfPresent(ContractDO::getHouseId, reqVO.getHouseId())
                .eqIfPresent(ContractDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(ContractDO::getStatus, reqVO.getStatus())
                .orderByDesc(ContractDO::getId));
    }

    default ContractDO selectByContractNo(String contractNo) {
        return selectOne(ContractDO::getContractNo, contractNo);
    }

    default List<ContractDO> selectExpiring(LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<ContractDO>()
                .le(ContractDO::getRentEndDate, endDate)
                .in(ContractDO::getStatus, 2, 3)
                .orderByAsc(ContractDO::getRentEndDate));
    }

}
