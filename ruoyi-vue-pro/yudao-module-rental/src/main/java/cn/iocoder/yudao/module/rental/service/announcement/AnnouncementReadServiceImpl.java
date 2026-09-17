package cn.iocoder.yudao.module.rental.service.announcement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementDetailRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.announcement.AnnouncementDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.announcement.AnnouncementReadDO;
import cn.iocoder.yudao.module.rental.dal.mysql.announcement.AnnouncementMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.announcement.AnnouncementReadMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.ANNOUNCEMENT_NOT_EXISTS;

/**
 * 租客端公告阅读 Service 实现类
 *
 * 注意：本类只注入 Mapper，不注入 AnnouncementService —— 一旦注入，
 * 将来 AnnouncementService 想反查已读数就会形成循环依赖。
 *
 * @author yudao
 */
@Service
public class AnnouncementReadServiceImpl implements AnnouncementReadService {

    /**
     * 公告「已发布」状态
     */
    private static final int STATUS_PUBLISHED = 1;

    @Resource
    private AnnouncementMapper announcementMapper;
    @Resource
    private AnnouncementReadMapper announcementReadMapper;

    @Override
    public PageResult<TenantAnnouncementRespVO> getPublishedAnnouncementPage(TenantAnnouncementPageReqVO pageReqVO,
                                                                            Long tenantUserId) {
        PageResult<AnnouncementDO> pageResult = announcementMapper.selectPublishedPage(pageReqVO);
        if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }
        Set<Long> announcementIds = pageResult.getList().stream().map(AnnouncementDO::getId)
                .collect(Collectors.toSet());
        // 页内已读集合，一次查询搞定（selectReadIdsByUser 内部守了空集合）
        Set<Long> readIds = announcementReadMapper.selectReadIdsByUser(tenantUserId, announcementIds);

        List<TenantAnnouncementRespVO> list = new ArrayList<>(pageResult.getList().size());
        for (AnnouncementDO announcement : pageResult.getList()) {
            TenantAnnouncementRespVO vo = BeanUtils.toBean(announcement, TenantAnnouncementRespVO.class);
            vo.setIsRead(readIds.contains(announcement.getId()));
            list.add(vo);
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public TenantAnnouncementDetailRespVO getPublishedAnnouncement(Long id, Long tenantUserId) {
        AnnouncementDO announcement = announcementMapper.selectById(id);
        // 未发布/已删除一律按「不存在」处理，避免通过错误信息差异探测草稿
        if (announcement == null || !Integer.valueOf(STATUS_PUBLISHED).equals(announcement.getStatus())) {
            throw exception(ANNOUNCEMENT_NOT_EXISTS);
        }
        TenantAnnouncementDetailRespVO vo = BeanUtils.toBean(announcement, TenantAnnouncementDetailRespVO.class);
        AnnouncementReadDO read = announcementReadMapper.selectByAnnouncementAndUser(id, tenantUserId);
        vo.setIsRead(read != null);
        vo.setReadTime(read == null ? null : read.getReadTime());
        return vo;
    }

    /**
     * 幂等操作，刻意不加 @Transactional：
     * 并发重复标记时落败一方拿到 DuplicateKeyException，恰好表示「已读记录已存在」＝期望终态。
     * 若在事务内吞掉数据完整性异常，事务会被标记为 rollback-only，提交时反抛 UnexpectedRollbackException。
     */
    @Override
    public void markRead(Long tenantUserId, Long announcementId) {
        AnnouncementDO announcement = announcementMapper.selectById(announcementId);
        if (announcement == null || !Integer.valueOf(STATUS_PUBLISHED).equals(announcement.getStatus())) {
            throw exception(ANNOUNCEMENT_NOT_EXISTS);
        }
        // 已读直接返回：列注释是「首次阅读时间」，不覆盖
        if (announcementReadMapper.selectByAnnouncementAndUser(announcementId, tenantUserId) != null) {
            return;
        }
        AnnouncementReadDO read = new AnnouncementReadDO();
        read.setAnnouncementId(announcementId);
        read.setUserId(tenantUserId);
        // 显式赋值，不依赖 DDL 的 DEFAULT CURRENT_TIMESTAMP（插入后 DO 上就有值）
        read.setReadTime(LocalDateTime.now());
        try {
            announcementReadMapper.insert(read);
        } catch (DuplicateKeyException e) {
            // 并发重复标记，唯一键 uk_announcement_user 保证只有一行，忽略即可
        }
    }

    @Override
    public Long getUnreadCount(Long tenantUserId) {
        // 不能用「已发布总数 − 已读总数」：公告下架后已读记录仍在，差值会算错甚至为负
        Long publishedTotal = announcementMapper.selectCount(new LambdaQueryWrapperX<AnnouncementDO>()
                .eq(AnnouncementDO::getStatus, STATUS_PUBLISHED));
        Long readOfPublished = announcementReadMapper.selectReadCountOfPublished(tenantUserId);
        long unread = (publishedTotal == null ? 0L : publishedTotal) - (readOfPublished == null ? 0L : readOfPublished);
        return Math.max(0L, unread);
    }

}
