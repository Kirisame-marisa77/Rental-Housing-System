package cn.iocoder.yudao.module.rental.service.announcement;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.announcement.AnnouncementDO;
import cn.iocoder.yudao.module.rental.dal.mysql.announcement.AnnouncementMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.ANNOUNCEMENT_NOT_EXISTS;

/**
 * 公告 Service 实现类
 *
 * @author yudao
 */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;

    @Override
    public Long createAnnouncement(AnnouncementSaveReqVO createReqVO) {
        AnnouncementDO announcement = BeanUtils.toBean(createReqVO, AnnouncementDO.class);
        if (StrUtil.isBlank(announcement.getNoticeNo())) {
            announcement.setNoticeNo(generateNoticeNo());
        }
        announcement.setPublisherId(getLoginUserId());
        announcement.setStatus(announcement.getStatus() == null ? 0 : announcement.getStatus());
        if (Integer.valueOf(1).equals(announcement.getStatus())) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Override
    public void updateAnnouncement(AnnouncementSaveReqVO updateReqVO) {
        validateAnnouncementExists(updateReqVO.getId());
        AnnouncementDO updateObj = BeanUtils.toBean(updateReqVO, AnnouncementDO.class);
        announcementMapper.updateById(updateObj);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        validateAnnouncementExists(id);
        announcementMapper.deleteById(id);
    }

    @Override
    public void publishAnnouncement(Long id) {
        AnnouncementDO announcement = validateAnnouncementExists(id);
        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
    }

    @Override
    public void updateAnnouncementTop(Long id, Integer isTop) {
        AnnouncementDO announcement = validateAnnouncementExists(id);
        announcement.setIsTop(isTop);
        announcementMapper.updateById(announcement);
    }

    @Override
    public PageResult<AnnouncementDO> getAnnouncementPage(AnnouncementPageReqVO pageReqVO) {
        return announcementMapper.selectPage(pageReqVO);
    }

    @Override
    public AnnouncementDO getAnnouncement(Long id) {
        return announcementMapper.selectById(id);
    }

    @VisibleForTesting
    public AnnouncementDO validateAnnouncementExists(Long id) {
        if (id == null) {
            return null;
        }
        AnnouncementDO announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw exception(ANNOUNCEMENT_NOT_EXISTS);
        }
        return announcement;
    }

    private String generateNoticeNo() {
        return "GG-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
