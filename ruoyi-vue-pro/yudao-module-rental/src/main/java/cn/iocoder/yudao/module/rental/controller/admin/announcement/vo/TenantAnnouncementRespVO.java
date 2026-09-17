package cn.iocoder.yudao.module.rental.controller.admin.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租客端公告列表项
 *
 * 刻意不含 content：富文本正文动辄几 KB，列表带上纯属浪费，正文由详情接口单独返回。
 * 也不含 status —— 租客看到的一律是已发布公告。
 */
@Schema(description = "租客端 - 公告列表 Response VO")
@Data
public class TenantAnnouncementRespVO {

    @Schema(description = "公告编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "公告编号", example = "GG202606240001")
    private String noticeNo;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告分类")
    private String category;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶：0-否，1-是")
    private Integer isTop;

    @Schema(description = "当前租客是否已读")
    private Boolean isRead;

}
