package cn.iocoder.yudao.module.rental.dal.mysql.moveout;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.MoveOutApplicationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MoveOutApplicationMapper extends BaseMapperX<MoveOutApplicationDO> {

    default PageResult<MoveOutApplicationDO> selectPage(MoveOutApplicationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MoveOutApplicationDO>()
                .eqIfPresent(MoveOutApplicationDO::getContractId, reqVO.getContractId())
                .eqIfPresent(MoveOutApplicationDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(MoveOutApplicationDO::getMoveOutType, reqVO.getMoveOutType())
                .eqIfPresent(MoveOutApplicationDO::getStatus, reqVO.getStatus())
                .orderByDesc(MoveOutApplicationDO::getId));
    }

}
