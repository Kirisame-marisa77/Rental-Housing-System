package cn.iocoder.yudao.module.rental.controller.admin.favorite.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "租客端 - 我的收藏分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class HouseFavoritePageReqVO extends PageParam {

}
