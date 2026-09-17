package cn.iocoder.yudao.module.rental.controller.admin.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 公告信息 Response VO")
@Data
public class AnnouncementRespVO {

    @Schema(description = "公告编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "公告编号", example = "GG-202606-0001")
    private String noticeNo;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "富文本内容")
    private String content;

    @Schema(description = "公告分类")
    private String category;

    @Schema(description = "发布人 ID", example = "1")
    private Long publisherId;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "状态：0-草稿，1-已发布，2-已删除", example = "1")
    private Integer status;

    @Schema(description = "是否置顶：0-否，1-是", example = "0")
    private Integer isTop;

    @Schema(description = "定向推送范围，JSON 格式")
    private String targetScope;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
