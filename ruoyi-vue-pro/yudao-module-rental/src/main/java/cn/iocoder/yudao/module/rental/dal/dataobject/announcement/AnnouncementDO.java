package cn.iocoder.yudao.module.rental.dal.dataobject.announcement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告信息表
 *
 * @author yudao
 */
@TableName("rental_announcement")
@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 公告编号，规则：GG-年月-流水号
     */
    private String noticeNo;
    /**
     * 公告标题
     */
    private String title;
    /**
     * 富文本内容
     */
    private String content;
    /**
     * 公告分类：缴费通知/维修通知/社区公告/紧急通知
     */
    private String category;
    /**
     * 发布人 ID，关联 sys_user
     */
    private Long publisherId;
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    /**
     * 状态：0-草稿，1-已发布，2-已删除
     */
    private Integer status;
    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;
    /**
     * 定向推送范围，JSON 格式
     */
    private String targetScope;

}
