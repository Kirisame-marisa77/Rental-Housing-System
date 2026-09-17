package cn.iocoder.yudao.module.rental.dal.mysql.announcement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.dal.dataobject.announcement.AnnouncementReadDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface AnnouncementReadMapper extends BaseMapperX<AnnouncementReadDO> {

    default AnnouncementReadDO selectByAnnouncementAndUser(Long announcementId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<AnnouncementReadDO>()
                .eq(AnnouncementReadDO::getAnnouncementId, announcementId)
                .eq(AnnouncementReadDO::getUserId, userId));
    }

    /**
     * 在给定公告范围内，查某用户已读的公告 ID 集合
     *
     * 刻意用 in 而不是 inIfPresent：空集合的守空由调用方负责，
     * inIfPresent 会把「守空代码被挪走」这种 bug 静默隐藏成「所有公告都显示已读」
     */
    default Set<Long> selectReadIdsByUser(Long userId, Collection<Long> announcementIds) {
        if (announcementIds == null || announcementIds.isEmpty()) {
            return Collections.emptySet();
        }
        return selectList(new LambdaQueryWrapperX<AnnouncementReadDO>()
                .eq(AnnouncementReadDO::getUserId, userId)
                .in(AnnouncementReadDO::getAnnouncementId, announcementIds))
                .stream().map(AnnouncementReadDO::getAnnouncementId).collect(Collectors.toSet());
    }

    /**
     * 统计某用户已读的「已发布」公告数
     *
     * 不能简单用「已发布总数 − 已读总数」：公告下架或删除后已读记录仍在，差值会算错甚至为负。
     *
     * 注意：手写 SQL 时 MyBatis-Plus 不会自动追加逻辑删除条件，r.deleted / a.deleted 必须自己写
     */
    @Select("SELECT COUNT(*) FROM rental_announcement_read r "
            + "JOIN rental_announcement a ON a.id = r.announcement_id "
            + "WHERE r.user_id = #{userId} AND r.deleted = 0 AND a.deleted = 0 AND a.status = 1")
    Long selectReadCountOfPublished(@Param("userId") Long userId);

}
