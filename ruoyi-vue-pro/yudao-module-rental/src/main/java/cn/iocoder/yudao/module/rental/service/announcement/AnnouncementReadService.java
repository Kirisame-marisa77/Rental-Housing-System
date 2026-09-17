package cn.iocoder.yudao.module.rental.service.announcement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementDetailRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementRespVO;

/**
 * 租客端公告阅读 Service 接口
 *
 * 刻意从 AnnouncementService（管理端 CRUD）里独立出来：两者生命周期不同，
 * 且本服务只依赖 Mapper，不会和 AnnouncementService 形成循环依赖。
 *
 * @author yudao
 */
public interface AnnouncementReadService {

    /**
     * 获得已发布公告分页（带当前租客的已读标记）
     *
     * @param pageReqVO    分页查询
     * @param tenantUserId 租客编号
     * @return 公告分页
     */
    PageResult<TenantAnnouncementRespVO> getPublishedAnnouncementPage(TenantAnnouncementPageReqVO pageReqVO,
                                                                     Long tenantUserId);

    /**
     * 获得已发布公告详情（未发布/已删除一律按不存在处理）
     *
     * @param id           公告编号
     * @param tenantUserId 租客编号
     * @return 公告详情
     */
    TenantAnnouncementDetailRespVO getPublishedAnnouncement(Long id, Long tenantUserId);

    /**
     * 标记公告已读（幂等，已读不更新首次阅读时间）
     *
     * @param tenantUserId   租客编号
     * @param announcementId 公告编号
     */
    void markRead(Long tenantUserId, Long announcementId);

    /**
     * 获得当前租客的未读公告数
     *
     * @param tenantUserId 租客编号
     * @return 未读数
     */
    Long getUnreadCount(Long tenantUserId);

}
