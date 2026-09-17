package cn.iocoder.yudao.module.rental.controller.admin.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "租客端 - 公告详情 Response VO")
@Data
public class TenantAnnouncementDetailRespVO {

    @Schema(description = "公告编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "公告编号", example = "GG202606240001")
    private String noticeNo;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "富文本内容")
    private String content;

    @Schema(description = "公告分类")
    private String category;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶：0-否，1-是")
    private Integer isTop;

    @Schema(description = "当前租客是否已读")
    private Boolean isRead;

    @Schema(description = "首次阅读时间")
    private LocalDateTime readTime;

}
