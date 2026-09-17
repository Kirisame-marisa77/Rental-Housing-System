package cn.iocoder.yudao.module.rental.controller.admin.house.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 房源分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HousePageReqVO extends PageParam {

    @Schema(description = "小区名称，模糊匹配", example = "万科")
    private String communityName;

    @Schema(description = "所在区域，模糊匹配", example = "浦东")
    private String area;

    @Schema(description = "户型，模糊匹配", example = "两室")
    private String layout;

    @Schema(description = "房源状态：0-下架，1-上架，2-已锁定，3-已出租", example = "1")
    private Integer status;

    @Schema(description = "业主 ID", example = "1")
    private Long ownerId;

    @Schema(description = "审核状态：0-待审核 1-已通过 2-已驳回", example = "0")
    private Integer reviewStatus;

}
