package cn.iocoder.yudao.module.rental.controller.admin.house.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 房源图片保存 Request VO")
@Data
public class HouseImageSaveReqVO {

    @Schema(description = "房源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "房源不能为空")
    private Long houseId;

    @Schema(description = "实景图 URL 列表")
    private List<String> realityImages;

    @Schema(description = "户型图 URL 列表")
    private List<String> layoutImages;

}
