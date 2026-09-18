package cn.iocoder.yudao.module.rental.dal.mysql.viewing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.viewing.ViewingAppointmentDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Collection;

@Mapper
public interface ViewingAppointmentMapper extends BaseMapperX<ViewingAppointmentDO> {

    default PageResult<ViewingAppointmentDO> selectPage(ViewingAppointmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ViewingAppointmentDO>()
                .eqIfPresent(ViewingAppointmentDO::getHouseId, reqVO.getHouseId())
                .inIfPresent(ViewingAppointmentDO::getHouseId, reqVO.getHouseIds())
                .eqIfPresent(ViewingAppointmentDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(ViewingAppointmentDO::getStatus, reqVO.getStatus())
                .orderByDesc(ViewingAppointmentDO::getAppointmentDate)
                .orderByDesc(ViewingAppointmentDO::getId));
    }

    /**
     * 按 ID + 租客查询
     *
     * 租客侧的查询一律用它收窄：别人的预约会自然落到「不存在」，
     * 既不泄露他人预约的存在性，也不需要额外的越权错误码
     */
    default ViewingAppointmentDO selectByIdAndTenant(Long id, Long tenantUserId) {
        return selectOne(new LambdaQueryWrapperX<ViewingAppointmentDO>()
                .eq(ViewingAppointmentDO::getId, id)
                .eq(ViewingAppointmentDO::getTenantUserId, tenantUserId));
    }

    /**
     * 统计某房源某日某时段处于指定状态的预约数（用于时段占用校验）
     */
    default Long selectCountBySlot(Long houseId, LocalDate appointmentDate, String startTime,
                                   Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<ViewingAppointmentDO>()
                .eq(ViewingAppointmentDO::getHouseId, houseId)
                .eq(ViewingAppointmentDO::getAppointmentDate, appointmentDate)
                .eq(ViewingAppointmentDO::getStartTime, startTime)
                .in(ViewingAppointmentDO::getStatus, statuses));
    }

    /**
     * 统计某租客对某房源某日某时段处于指定状态的预约数（用于重复预约校验）
     */
    default Long selectCountByTenantSlot(Long tenantUserId, Long houseId, LocalDate appointmentDate,
                                         String startTime, Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<ViewingAppointmentDO>()
                .eq(ViewingAppointmentDO::getTenantUserId, tenantUserId)
                .eq(ViewingAppointmentDO::getHouseId, houseId)
                .eq(ViewingAppointmentDO::getAppointmentDate, appointmentDate)
                .eq(ViewingAppointmentDO::getStartTime, startTime)
                .in(ViewingAppointmentDO::getStatus, statuses));
    }

    /**
     * 乐观锁式的状态流转：仅当当前状态为 fromStatus 时才更新，返回影响行数
     *
     * 注意 LambdaQueryWrapperX 只扩展查询、不支持 update，必须用 LambdaUpdateWrapper
     */
    default int updateStatusByIdAndStatus(Long id, Integer fromStatus, ViewingAppointmentDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ViewingAppointmentDO>()
                .eq(ViewingAppointmentDO::getId, id)
                .eq(ViewingAppointmentDO::getStatus, fromStatus));
    }

    /**
     * 取消某租客所有未结束的看房预约（注销账号时调用）
     *
     * 0-待确认 / 1-已确认 → 3-已取消。
     * 不处理的话房东端会挂着一条永远联系不上人的预约，而且该时段仍被
     * selectCountBySlot 算作占用，会挡住别的租客预约。
     */
    default int cancelActiveByTenant(Long tenantUserId) {
        return update(null, new LambdaUpdateWrapper<ViewingAppointmentDO>()
                .eq(ViewingAppointmentDO::getTenantUserId, tenantUserId)
                .in(ViewingAppointmentDO::getStatus, 0, 1)
                .set(ViewingAppointmentDO::getStatus, 3));
    }

}
