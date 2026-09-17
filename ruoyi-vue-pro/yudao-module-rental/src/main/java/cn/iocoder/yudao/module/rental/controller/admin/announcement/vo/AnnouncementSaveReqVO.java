package cn.iocoder.yudao.module.rental.controller.admin.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 公告创建/修改 Request VO")
@Data
public class AnnouncementSaveReqVO {

    @Schema(description = "公告编号", example = "1")
    private Long id;

    @Schema(description = "公告标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "公告标题不能为空")
    private String title;

    @Schema(description = "富文本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "公告内容不能为空")
    private String content;

    @Schema(description = "公告分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "社区公告")
    @NotEmpty(message = "公告分类不能为空")
    private String category;

    @Schema(description = "状态：0-草稿，1-已发布", example = "0")
    private Integer status;

    @Schema(description = "是否置顶：0-否，1-是", example = "0")
    private Integer isTop;

    @Schema(description = "定向推送范围，JSON 格式")
    private String targetScope;

}
