package cn.iocoder.yudao.module.rental.controller.admin.house.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 房源图片信息 Response VO")
@Data
public class HouseImageRespVO {

    @Schema(description = "图片编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "图片 URL", example = "http://xxx/1.jpg")
    private String imageUrl;

    @Schema(description = "排序序号", example = "0")
    private Integer sort;

    @Schema(description = "图片类型：0-实景图，1-户型图", example = "0")
    private Integer imageType;

}
