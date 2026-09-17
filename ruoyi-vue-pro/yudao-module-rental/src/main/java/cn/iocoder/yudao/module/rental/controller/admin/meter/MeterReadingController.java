package cn.iocoder.yudao.module.rental.controller.admin.meter;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.*;
import cn.iocoder.yudao.module.rental.dal.dataobject.meter.MeterReadingDO;
import cn.iocoder.yudao.module.rental.service.meter.MeterConfigService;
import cn.iocoder.yudao.module.rental.service.meter.MeterReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 抄表管理")
@RestController
@RequestMapping("/rental/meter")
@Validated
public class MeterReadingController {

    @Resource
    private MeterReadingService meterReadingService;
    @Resource
    private MeterConfigService meterConfigService;

    @PostMapping("/create")
    @Operation(summary = "录入抄表读数")
    @PreAuthorize("@ss.hasPermission('rental:meter:create')")
    public CommonResult<Long> createMeterReading(@Valid @RequestBody MeterReadingSaveReqVO createReqVO) {
        return success(meterReadingService.createMeterReading(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新抄表记录")
    @PreAuthorize("@ss.hasPermission('rental:meter:update')")
    public CommonResult<Boolean> updateMeterReading(@Valid @RequestBody MeterReadingSaveReqVO updateReqVO) {
        meterReadingService.updateMeterReading(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除抄表记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:meter:delete')")
    public CommonResult<Boolean> deleteMeterReading(@RequestParam("id") Long id) {
        meterReadingService.deleteMeterReading(id);
        return success(true);
    }

    @PutMapping("/confirm")
    @Operation(summary = "确认抄表并生成水电费账单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:meter:update')")
    public CommonResult<Long> confirmMeterReading(@RequestParam("id") Long id) {
        return success(meterReadingService.confirmMeterReading(id));
    }

    @PutMapping("/review")
    @Operation(summary = "审核抄表（业主上传，通过则按房源计价规则生成水电费账单）")
    @PreAuthorize("@ss.hasPermission('rental:meter:update')")
    public CommonResult<Long> reviewMeterReading(@RequestParam("id") Long id,
                                                 @RequestParam("pass") Boolean pass,
                                                 @RequestParam(value = "reason", required = false) String reason) {
        return success(meterReadingService.reviewMeterReading(id, pass, reason));
    }

    @GetMapping("/page")
    @Operation(summary = "获得抄表记录分页")
    @PreAuthorize("@ss.hasPermission('rental:meter:query')")
    public CommonResult<PageResult<MeterReadingRespVO>> getMeterReadingPage(@Validated MeterReadingPageReqVO pageReqVO) {
        PageResult<MeterReadingDO> pageResult = meterReadingService.getMeterReadingPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MeterReadingRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得抄表记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:meter:query')")
    public CommonResult<MeterReadingRespVO> getMeterReading(@RequestParam("id") Long id) {
        MeterReadingDO reading = meterReadingService.getMeterReading(id);
        return success(BeanUtils.toBean(reading, MeterReadingRespVO.class));
    }

    @GetMapping("/get-latest")
    @Operation(summary = "获得最新抄表记录（用于获取上期读数）")
    @PreAuthorize("@ss.hasPermission('rental:meter:query')")
    public CommonResult<MeterReadingRespVO> getLatestMeterReading(@RequestParam("houseId") Long houseId,
                                                                  @RequestParam("contractId") Long contractId,
                                                                  @RequestParam("meterType") Integer meterType) {
        MeterReadingDO reading = meterReadingService.getLatestMeterReading(houseId, contractId, meterType);
        return success(BeanUtils.toBean(reading, MeterReadingRespVO.class));
    }

    @GetMapping("/get-settings")
    @Operation(summary = "获得抄表配置（周期、单价）")
    @PreAuthorize("@ss.hasPermission('rental:meter:query')")
    public CommonResult<MeterConfigSettingsRespVO> getSettings() {
        return success(meterConfigService.getSettings());
    }

    @PutMapping("/update-settings")
    @Operation(summary = "更新抄表配置")
    @PreAuthorize("@ss.hasPermission('rental:meter:update')")
    public CommonResult<Boolean> updateSettings(@Valid @RequestBody MeterConfigSettingsReqVO reqVO) {
        meterConfigService.updateSettings(reqVO);
        return success(true);
    }

}
