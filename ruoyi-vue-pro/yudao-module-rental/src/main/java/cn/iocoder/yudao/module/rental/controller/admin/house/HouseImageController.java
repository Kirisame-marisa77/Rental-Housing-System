package cn.iocoder.yudao.module.rental.controller.admin.house;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseImageRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseImageSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseImageDO;
import cn.iocoder.yudao.module.rental.service.house.HouseImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 房源图片")
@RestController
@RequestMapping("/rental/house-image")
@Validated
public class HouseImageController {

    @Resource
    private HouseImageService houseImageService;

    @PostMapping("/save")
    @Operation(summary = "保存房源图片（整体覆盖）")
    @PreAuthorize("@ss.hasPermission('rental:house:update')")
    public CommonResult<Boolean> saveHouseImage(@Valid @RequestBody HouseImageSaveReqVO saveReqVO) {
        houseImageService.saveHouseImage(saveReqVO);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得房源图片列表")
    @Parameter(name = "houseId", description = "房源编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:house:query')")
    public CommonResult<List<HouseImageRespVO>> getHouseImageList(@RequestParam("houseId") Long houseId) {
        List<HouseImageDO> list = houseImageService.getHouseImageList(houseId);
        return success(BeanUtils.toBean(list, HouseImageRespVO.class));
    }

}
