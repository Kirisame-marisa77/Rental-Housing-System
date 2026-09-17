package cn.iocoder.yudao.module.rental.dal.dataobject.announcement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告阅读记录表
 *
 * @author yudao
 */
@TableName("rental_announcement_read")
@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementReadDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 公告 ID，关联 rental_announcement
     */
    private Long announcementId;
    /**
     * 用户 ID，关联 rental_tenant_info.id
     *
     * 注意：列名沿用建表时的 user_id，但 member 模块未启用，实际存的是租客 ID。
     * 唯一键 uk_announcement_user(announcement_id, user_id) 不含角色，而租客与业主是两条独立自增序列，
     * 若将来开放业主端读公告，租客 id=5 与业主 id=5 会撞同一行 —— 届时必须先加 user_type 列并重建唯一键。
     * 本期仅租客端写入。
     */
    private Long userId;
    /**
     * 首次阅读时间
     */
    private LocalDateTime readTime;

}
