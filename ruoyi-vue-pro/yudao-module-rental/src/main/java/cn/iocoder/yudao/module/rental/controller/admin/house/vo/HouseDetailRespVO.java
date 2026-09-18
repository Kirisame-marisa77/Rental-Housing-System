package cn.iocoder.yudao.module.rental.controller.admin.house.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "租客端 - 房源详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HouseDetailRespVO extends HouseRespVO {

    @Schema(description = "房东姓名", example = "张建国")
    private String ownerName;

    @Schema(description = "房东联系电话", example = "13800000001")
    private String ownerPhone;

    /**
     * 实景图与户型图刻意拆成两个列表：rental_house_image 查询只按 sort 升序，
     * 而两类图片的 sort 各自从 0 开始，合并成一个列表会交错，前端还得二次排序。
     */
    @Schema(description = "实景图列表，按 sort 升序")
    private List<HouseImageRespVO> realityImages;

    @Schema(description = "户型图列表，按 sort 升序")
    private List<HouseImageRespVO> layoutImages;

}
