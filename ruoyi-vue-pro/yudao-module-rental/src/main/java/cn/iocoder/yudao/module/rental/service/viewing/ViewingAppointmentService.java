package cn.iocoder.yudao.module.rental.service.viewing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentSaveReqVO;

/**
 * 预约看房 Service 接口
 *
 * 状态机：0-待确认 --房东确认--> 1-已确认 --房东完成--> 2-已完成
 *              待确认/已确认 都可被租客取消 --> 3-已取消
 *
 * @author yudao
 */
public interface ViewingAppointmentService {

    /**
     * 租客提交预约看房
     *
     * @param tenantUserId 租客编号
     * @param createReqVO  预约信息
     * @return 预约编号
     */
    Long createAppointment(Long tenantUserId, ViewingAppointmentSaveReqVO createReqVO);

    /**
     * 租客取消预约（仅限自己的预约，且未完成/未取消）
     *
     * @param tenantUserId 租客编号
     * @param id           预约编号
     */
    void cancelAppointment(Long tenantUserId, Long id);

    /**
     * 房东确认预约（0-待确认 → 1-已确认）
     *
     * @param ownerId 房东编号
     * @param id      预约编号
     */
    void confirmAppointment(Long ownerId, Long id);

    /**
     * 房东完成看房（1-已确认 → 2-已完成）
     *
     * @param ownerId  房东编号
     * @param id       预约编号
     * @param feedback 看房反馈
     */
    void completeAppointment(Long ownerId, Long id, String feedback);

    /**
     * 获得预约分页（含房源、房东、租客信息）
     *
     * @param pageReqVO 分页查询
     * @return 预约分页
     */
    PageResult<ViewingAppointmentRespVO> getAppointmentPage(ViewingAppointmentPageReqVO pageReqVO);

    /**
     * 获得某房东自己房源的预约分页
     *
     * @param ownerId   房东编号
     * @param pageReqVO 分页查询
     * @return 预约分页
     */
    PageResult<ViewingAppointmentRespVO> getOwnerAppointmentPage(Long ownerId, ViewingAppointmentPageReqVO pageReqVO);

    /**
     * 获得单条预约（含房源、房东、租客信息）
     *
     * @param id 预约编号
     * @return 预约
     */
    ViewingAppointmentRespVO getAppointment(Long id);

}
