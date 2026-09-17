package cn.iocoder.yudao.module.rental.service.viewing;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.viewing.ViewingAppointmentDO;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.tenant.TenantInfoMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.viewing.ViewingAppointmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.HOUSE_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_ALREADY_PROCESSED;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_CANNOT_CANCEL;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_DATE_INVALID;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_DUPLICATE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_HOUSE_NOT_AVAILABLE;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_NOT_OWNER;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_SLOT_TAKEN;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.VIEWING_APPOINTMENT_TIME_INVALID;

/**
 * 预约看房 Service 实现类
 *
 * 注意：本类只注入 Mapper，不注入其它 Service（避免引入 Spring 循环依赖）
 *
 * @author yudao
 */
@Service
public class ViewingAppointmentServiceImpl implements ViewingAppointmentService {

    /**
     * 房源「上架」状态
     */
    private static final int HOUSE_STATUS_ONLINE = 1;
    /**
     * 预约状态
     */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_CONFIRMED = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int STATUS_CANCELLED = 3;
    /**
     * 占用时段的状态：待确认与已确认都算占用，已完成/已取消不再占用
     */
    private static final List<Integer> ACTIVE_STATUSES = Arrays.asList(STATUS_PENDING, STATUS_CONFIRMED);
    /**
     * 时间格式：零填充 24 小时制 HH:mm
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    @Resource
    private ViewingAppointmentMapper appointmentMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private OwnerInfoMapper ownerInfoMapper;
    @Resource
    private TenantInfoMapper tenantInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAppointment(Long tenantUserId, ViewingAppointmentSaveReqVO createReqVO) {
        HouseDO house = houseMapper.selectById(createReqVO.getHouseId());
        if (house == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        if (!Integer.valueOf(HOUSE_STATUS_ONLINE).equals(house.getStatus())) {
            throw exception(VIEWING_APPOINTMENT_HOUSE_NOT_AVAILABLE);
        }
        if (createReqVO.getAppointmentDate().isBefore(LocalDate.now())) {
            throw exception(VIEWING_APPOINTMENT_DATE_INVALID);
        }
        String startTime = createReqVO.getStartTime();
        String endTime = createReqVO.getEndTime();
        if (!isValidTime(startTime) || !isValidTime(endTime)) {
            throw exception(VIEWING_APPOINTMENT_TIME_INVALID);
        }
        // "HH:mm" 零填充后，字典序与时间序一致，无需解析成时间对象比较
        if (startTime.compareTo(endTime) >= 0) {
            throw exception(VIEWING_APPOINTMENT_TIME_INVALID);
        }
        // 同一房源同一时段只能有一个进行中的预约
        Long slotCount = appointmentMapper.selectCountBySlot(house.getId(), createReqVO.getAppointmentDate(),
                startTime, ACTIVE_STATUSES);
        if (slotCount != null && slotCount > 0) {
            throw exception(VIEWING_APPOINTMENT_SLOT_TAKEN);
        }
        // 同一租客不能重复预约同一房源的同一时段
        Long dupCount = appointmentMapper.selectCountByTenantSlot(tenantUserId, house.getId(),
                createReqVO.getAppointmentDate(), startTime, ACTIVE_STATUSES);
        if (dupCount != null && dupCount > 0) {
            throw exception(VIEWING_APPOINTMENT_DUPLICATE);
        }

        ViewingAppointmentDO appointment = BeanUtils.toBean(createReqVO, ViewingAppointmentDO.class);
        // 状态与归属一律由服务端决定，忽略客户端传入（接口是 @PermitAll，需防止构造请求赋权）
        appointment.setId(null);
        appointment.setTenantUserId(tenantUserId);
        appointment.setStatus(STATUS_PENDING);
        appointment.setFeedback(null);
        appointmentMapper.insert(appointment);
        return appointment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long tenantUserId, Long id) {
        // 查询按租客收窄：别人的预约会自然落到「不存在」，不泄露他人预约的存在性
        ViewingAppointmentDO appointment = appointmentMapper.selectByIdAndTenant(id, tenantUserId);
        if (appointment == null) {
            throw exception(VIEWING_APPOINTMENT_NOT_EXISTS);
        }
        // 已完成 / 已取消的不允许再取消
        if (!Integer.valueOf(STATUS_PENDING).equals(appointment.getStatus())
                && !Integer.valueOf(STATUS_CONFIRMED).equals(appointment.getStatus())) {
            throw exception(VIEWING_APPOINTMENT_CANNOT_CANCEL);
        }
        // 乐观锁：并发下只有一个能取消成功。from 可能是 0 或 1，逐个尝试
        if (appointmentMapper.updateStatusByIdAndStatus(id, appointment.getStatus(), statusUpdate(STATUS_CANCELLED)) == 0) {
            throw exception(VIEWING_APPOINTMENT_ALREADY_PROCESSED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(Long ownerId, Long id) {
        validateOwnerAppointment(ownerId, id);
        if (appointmentMapper.updateStatusByIdAndStatus(id, STATUS_PENDING, statusUpdate(STATUS_CONFIRMED)) == 0) {
            throw exception(VIEWING_APPOINTMENT_ALREADY_PROCESSED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAppointment(Long ownerId, Long id, String feedback) {
        validateOwnerAppointment(ownerId, id);
        ViewingAppointmentDO updateObj = statusUpdate(STATUS_COMPLETED);
        if (StrUtil.isNotBlank(feedback)) {
            updateObj.setFeedback(feedback);
        }
        // 严格要求 1-已确认 → 2-已完成，这样「确认」这一步才有业务含义
        if (appointmentMapper.updateStatusByIdAndStatus(id, STATUS_CONFIRMED, updateObj) == 0) {
            throw exception(VIEWING_APPOINTMENT_ALREADY_PROCESSED);
        }
    }

    @Override
    public PageResult<ViewingAppointmentRespVO> getAppointmentPage(ViewingAppointmentPageReqVO pageReqVO) {
        PageResult<ViewingAppointmentDO> pageResult = appointmentMapper.selectPage(pageReqVO);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        return new PageResult<>(enrichAppointments(pageResult.getList()), pageResult.getTotal());
    }

    @Override
    public PageResult<ViewingAppointmentRespVO> getOwnerAppointmentPage(Long ownerId,
                                                                        ViewingAppointmentPageReqVO pageReqVO) {
        List<HouseDO> houses = houseMapper.selectList(new LambdaQueryWrapperX<HouseDO>()
                .eq(HouseDO::getOwnerId, ownerId));
        // 没有房源的房东必须直接返回空页：inIfPresent 在集合为空时不拼接条件，
        // 直接查库会把全系统的预约都返回给他
        if (houses.isEmpty()) {
            return PageResult.empty();
        }
        pageReqVO.setHouseIds(houses.stream().map(HouseDO::getId).collect(Collectors.toList()));
        return getAppointmentPage(pageReqVO);
    }

    @Override
    public ViewingAppointmentRespVO getAppointment(Long id) {
        ViewingAppointmentDO appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw exception(VIEWING_APPOINTMENT_NOT_EXISTS);
        }
        return enrichAppointments(Collections.singletonList(appointment)).get(0);
    }

    /**
     * 校验预约存在且属于该房东的房源
     */
    private void validateOwnerAppointment(Long ownerId, Long id) {
        ViewingAppointmentDO appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw exception(VIEWING_APPOINTMENT_NOT_EXISTS);
        }
        HouseDO house = houseMapper.selectById(appointment.getHouseId());
        if (house == null) {
            throw exception(HOUSE_NOT_EXISTS);
        }
        if (!ownerId.equals(house.getOwnerId())) {
            throw exception(VIEWING_APPOINTMENT_NOT_OWNER);
        }
    }

    /**
     * 构造只带状态（及可选反馈）的更新对象，其余字段为 null 不会被更新
     */
    private ViewingAppointmentDO statusUpdate(Integer status) {
        ViewingAppointmentDO updateObj = new ViewingAppointmentDO();
        updateObj.setStatus(status);
        return updateObj;
    }

    private boolean isValidTime(String time) {
        return time != null && TIME_PATTERN.matcher(time).matches();
    }

    /**
     * 批量补全房源、房东、租客信息（固定 3 次查询，与行数无关）
     */
    private List<ViewingAppointmentRespVO> enrichAppointments(List<ViewingAppointmentDO> appointments) {
        Set<Long> houseIds = appointments.stream().map(ViewingAppointmentDO::getHouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // selectBatchIds 对空集合会生成 IN ()，必须先判空
        Map<Long, HouseDO> houseMap = houseIds.isEmpty() ? Collections.emptyMap()
                : houseMapper.selectBatchIds(houseIds).stream()
                .collect(Collectors.toMap(HouseDO::getId, Function.identity(), (a, b) -> a));
        Set<Long> ownerIds = houseMap.values().stream().map(HouseDO::getOwnerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, OwnerInfoDO> ownerMap = ownerIds.isEmpty() ? Collections.emptyMap()
                : ownerInfoMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(OwnerInfoDO::getId, Function.identity(), (a, b) -> a));
        Set<Long> tenantIds = appointments.stream().map(ViewingAppointmentDO::getTenantUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, TenantInfoDO> tenantMap = tenantIds.isEmpty() ? Collections.emptyMap()
                : tenantInfoMapper.selectBatchIds(tenantIds).stream()
                .collect(Collectors.toMap(TenantInfoDO::getId, Function.identity(), (a, b) -> a));

        List<ViewingAppointmentRespVO> list = new ArrayList<>(appointments.size());
        for (ViewingAppointmentDO appointment : appointments) {
            ViewingAppointmentRespVO vo = BeanUtils.toBean(appointment, ViewingAppointmentRespVO.class);
            HouseDO house = houseMap.get(appointment.getHouseId());
            if (house != null) {
                vo.setHouseNo(house.getHouseNo());
                vo.setCommunityName(house.getCommunityName());
                vo.setBuildingNo(house.getBuildingNo());
                vo.setRoomNo(house.getRoomNo());
                vo.setLayout(house.getLayout());
                vo.setSquareArea(house.getSquareArea());
                vo.setMonthlyRent(house.getMonthlyRent());
                vo.setHouseStatus(house.getStatus());
                vo.setOwnerId(house.getOwnerId());
                OwnerInfoDO owner = ownerMap.get(house.getOwnerId());
                if (owner != null) {
                    vo.setOwnerName(owner.getName());
                    vo.setOwnerPhone(owner.getPhone());
                }
            }
            TenantInfoDO tenant = tenantMap.get(appointment.getTenantUserId());
            if (tenant != null) {
                vo.setTenantName(tenant.getName());
                vo.setTenantPhone(tenant.getPhone());
            }
            list.add(vo);
        }
        return list;
    }

}
