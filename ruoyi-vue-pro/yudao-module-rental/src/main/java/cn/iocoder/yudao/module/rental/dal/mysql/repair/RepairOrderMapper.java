package cn.iocoder.yudao.module.rental.dal.mysql.repair;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairOrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepairOrderMapper extends BaseMapperX<RepairOrderDO> {

    default PageResult<RepairOrderDO> selectPage(RepairOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RepairOrderDO>()
                .likeIfPresent(RepairOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(RepairOrderDO::getRepairType, reqVO.getRepairType())
                .eqIfPresent(RepairOrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(RepairOrderDO::getPriority, reqVO.getPriority())
                .eqIfPresent(RepairOrderDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(RepairOrderDO::getOwnerId, reqVO.getOwnerId())
                .orderByDesc(RepairOrderDO::getId));
    }

}
