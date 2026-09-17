package cn.iocoder.yudao.module.rental.controller.admin.bill.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 缴费记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentRecordPageReqVO extends PageParam {

    @Schema(description = "账单类型：0-租金/物业费，1-水电费，2-退租结算", example = "1")
    private Integer billType;

    @Schema(description = "关联账单 ID", example = "1024")
    private Long billId;

}
