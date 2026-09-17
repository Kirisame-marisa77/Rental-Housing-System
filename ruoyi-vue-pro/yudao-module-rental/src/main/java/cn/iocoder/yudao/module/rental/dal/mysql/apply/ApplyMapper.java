package cn.iocoder.yudao.module.rental.dal.mysql.apply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.apply.ApplyDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApplyMapper extends BaseMapperX<ApplyDO> {

    default PageResult<ApplyDO> selectPage(ApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ApplyDO>()
                .eqIfPresent(ApplyDO::getHouseId, reqVO.getHouseId())
                .inIfPresent(ApplyDO::getHouseId, reqVO.getHouseIds())
                .eqIfPresent(ApplyDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(ApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(ApplyDO::getId));
    }

    /**
     * 乐观锁式的状态流转：仅当当前状态为 fromStatus 时才更新，返回影响行数
     *
     * 用于保证「同一申请只会被审批一次」，避免并发重复审批产生两份合同
     */
    default int updateStatusByIdAndStatus(Long id, Integer fromStatus, ApplyDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ApplyDO>()
                .eq(ApplyDO::getId, id)
                .eq(ApplyDO::getStatus, fromStatus));
    }

    /**
     * 统计某租客对某房源处于「待审批」的申请数
     */
    default Long selectPendingCount(Long houseId, Long tenantUserId) {
        return selectCount(new LambdaQueryWrapperX<ApplyDO>()
                .eq(ApplyDO::getHouseId, houseId)
                .eq(ApplyDO::getTenantUserId, tenantUserId)
                .eq(ApplyDO::getStatus, 0));
    }

}
