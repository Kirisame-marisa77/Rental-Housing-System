package cn.iocoder.yudao.module.rental.controller.admin.announcement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租客端公告分页请求
 *
 * 刻意不提供 status 字段：租客只能看到「已发布」公告，从类型上杜绝草稿泄露。
 * 查询条件里也硬编码了 status=1 兜底。
 */
@Schema(description = "租客端 - 公告分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantAnnouncementPageReqVO extends PageParam {

    @Schema(description = "公告分类", example = "社区公告")
    private String category;

}
