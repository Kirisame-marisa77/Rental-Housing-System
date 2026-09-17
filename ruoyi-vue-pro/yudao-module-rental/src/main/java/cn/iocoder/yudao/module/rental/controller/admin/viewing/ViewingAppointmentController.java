package cn.iocoder.yudao.module.rental.controller.admin.viewing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentRespVO;
import cn.iocoder.yudao.module.rental.service.viewing.ViewingAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理端 - 看房预约（只读监管）
 *
 * 预约由租客在租客端发起、房东在业主端确认/完成，管理端只提供查看。
 * 因此这里刻意不提供 create/update/delete 接口。
 */
@Tag(name = "管理后台 - 看房预约")
@RestController
@RequestMapping("/rental/viewing-appointment")
@Validated
public class ViewingAppointmentController {

    @Resource
    private ViewingAppointmentService viewingAppointmentService;

    @GetMapping("/page")
    @Operation(summary = "获得看房预约分页")
    @PreAuthorize("@ss.hasPermission('rental:viewing-appointment:query')")
    public CommonResult<PageResult<ViewingAppointmentRespVO>> getAppointmentPage(
            @Validated ViewingAppointmentPageReqVO pageReqVO) {
        return success(viewingAppointmentService.getAppointmentPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得看房预约")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:viewing-appointment:query')")
    public CommonResult<ViewingAppointmentRespVO> getAppointment(@RequestParam("id") Long id) {
        return success(viewingAppointmentService.getAppointment(id));
    }

}
