package cn.iocoder.yudao.module.rental.dal.mysql.moveout;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.MoveOutApplicationDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MoveOutApplicationMapper extends BaseMapperX<MoveOutApplicationDO> {

    default PageResult<MoveOutApplicationDO> selectPage(MoveOutApplicationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MoveOutApplicationDO>()
                .eqIfPresent(MoveOutApplicationDO::getContractId, reqVO.getContractId())
                .eqIfPresent(MoveOutApplicationDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(MoveOutApplicationDO::getMoveOutType, reqVO.getMoveOutType())
                .eqIfPresent(MoveOutApplicationDO::getStatus, reqVO.getStatus())
                // 业主端靠它做数据隔离。注意 inIfPresent 在集合为空时不拼条件，
                // 所以调用方必须先保证集合非空（见 MoveOutApplicationServiceImpl）
                .inIfPresent(MoveOutApplicationDO::getHouseId, reqVO.getHouseIds())
                .orderByDesc(MoveOutApplicationDO::getId));
    }

    /**
     * 乐观锁式的状态流转：仅当当前状态为 fromStatus 时才更新，返回影响行数
     *
     * 与 ApplyMapper.updateStatusByIdAndStatus 同构。退租处理必须靠它占位：
     * 管理员端和业主端现在都能处理退租，两个并发请求若不 CAS，
     * 会各插一条结算单 —— 一次退租结算两次。
     */
    default int updateStatusByIdAndStatus(Long id, Integer fromStatus, MoveOutApplicationDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<MoveOutApplicationDO>()
                .eq(MoveOutApplicationDO::getId, id)
                .eq(MoveOutApplicationDO::getStatus, fromStatus));
    }

}
