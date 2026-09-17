package cn.iocoder.yudao.module.rental.controller.admin.house;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HousePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.service.house.HouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 房源")
@RestController
@RequestMapping("/rental/house")
@Validated
public class HouseController {

    @Resource
    private HouseService houseService;

    @PostMapping("/create")
    @Operation(summary = "创建房源")
    @PreAuthorize("@ss.hasPermission('rental:house:create')")
    public CommonResult<Long> createHouse(@Valid @RequestBody HouseSaveReqVO createReqVO) {
        return success(houseService.createHouse(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新房源")
    @PreAuthorize("@ss.hasPermission('rental:house:update')")
    public CommonResult<Boolean> updateHouse(@Valid @RequestBody HouseSaveReqVO updateReqVO) {
        houseService.updateHouse(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除房源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:house:delete')")
    public CommonResult<Boolean> deleteHouse(@RequestParam("id") Long id) {
        houseService.deleteHouse(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得房源分页")
    @PreAuthorize("@ss.hasPermission('rental:house:query')")
    public CommonResult<PageResult<HouseRespVO>> getHousePage(@Validated HousePageReqVO pageReqVO) {
        PageResult<HouseDO> pageResult = houseService.getHousePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, HouseRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得房源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:house:query')")
    public CommonResult<HouseRespVO> getHouse(@RequestParam("id") Long id) {
        HouseDO house = houseService.getHouse(id);
        return success(BeanUtils.toBean(house, HouseRespVO.class));
    }

    @PutMapping("/review")
    @Operation(summary = "审核房源（业主上传的房源）")
    @PreAuthorize("@ss.hasPermission('rental:house:update')")
    public CommonResult<Boolean> reviewHouse(@RequestParam("id") Long id,
                                             @RequestParam("pass") Boolean pass,
                                             @RequestParam(value = "reason", required = false) String reason) {
        houseService.reviewHouse(id, pass, reason);
        return success(true);
    }

}
