package cn.iocoder.yudao.module.rental.controller.admin.meter.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 抄表记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MeterReadingPageReqVO extends PageParam {

    @Schema(description = "房源 ID", example = "1024")
    private Long houseId;

    @Schema(description = "合同 ID", example = "1024")
    private Long contractId;

    @Schema(description = "抄表类型：0-水表，1-电表", example = "0")
    private Integer meterType;

    @Schema(description = "上传业主 ID", example = "1")
    private Long ownerId;

}
