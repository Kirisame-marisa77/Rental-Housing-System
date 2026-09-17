package cn.iocoder.yudao.module.rental.controller.admin.announcement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 公告分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementPageReqVO extends PageParam {

    @Schema(description = "公告标题，模糊匹配")
    private String title;

    @Schema(description = "公告分类", example = "社区公告")
    private String category;

    @Schema(description = "状态：0-草稿，1-已发布，2-已删除", example = "1")
    private Integer status;

}
