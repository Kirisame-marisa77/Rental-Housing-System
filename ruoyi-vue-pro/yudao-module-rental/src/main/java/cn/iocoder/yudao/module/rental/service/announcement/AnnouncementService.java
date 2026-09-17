package cn.iocoder.yudao.module.rental.service.announcement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.announcement.AnnouncementDO;

/**
 * 公告 Service 接口
 *
 * @author yudao
 */
public interface AnnouncementService {

    /**
     * 创建公告
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAnnouncement(AnnouncementSaveReqVO createReqVO);

    /**
     * 更新公告
     *
     * @param updateReqVO 更新信息
     */
    void updateAnnouncement(AnnouncementSaveReqVO updateReqVO);

    /**
     * 删除公告
     *
     * @param id 编号
     */
    void deleteAnnouncement(Long id);

    /**
     * 发布公告
     *
     * @param id 编号
     */
    void publishAnnouncement(Long id);

    /**
     * 置顶/取消置顶
     *
     * @param id    编号
     * @param isTop 是否置顶：0-否，1-是
     */
    void updateAnnouncementTop(Long id, Integer isTop);

    /**
     * 获得公告分页
     *
     * @param pageReqVO 分页查询
     * @return 公告分页
     */
    PageResult<AnnouncementDO> getAnnouncementPage(AnnouncementPageReqVO pageReqVO);

    /**
     * 获得公告
     *
     * @param id 编号
     * @return 公告
     */
    AnnouncementDO getAnnouncement(Long id);

}
