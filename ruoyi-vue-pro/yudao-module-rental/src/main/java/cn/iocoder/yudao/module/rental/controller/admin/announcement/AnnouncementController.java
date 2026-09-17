package cn.iocoder.yudao.module.rental.controller.admin.announcement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.announcement.AnnouncementDO;
import cn.iocoder.yudao.module.rental.service.announcement.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 公告管理")
@RestController
@RequestMapping("/rental/announcement")
@Validated
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @PostMapping("/create")
    @Operation(summary = "创建公告")
    @PreAuthorize("@ss.hasPermission('rental:announcement:create')")
    public CommonResult<Long> createAnnouncement(@Valid @RequestBody AnnouncementSaveReqVO createReqVO) {
        return success(announcementService.createAnnouncement(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新公告")
    @PreAuthorize("@ss.hasPermission('rental:announcement:update')")
    public CommonResult<Boolean> updateAnnouncement(@Valid @RequestBody AnnouncementSaveReqVO updateReqVO) {
        announcementService.updateAnnouncement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:announcement:delete')")
    public CommonResult<Boolean> deleteAnnouncement(@RequestParam("id") Long id) {
        announcementService.deleteAnnouncement(id);
        return success(true);
    }

    @PutMapping("/publish")
    @Operation(summary = "发布公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:announcement:update')")
    public CommonResult<Boolean> publishAnnouncement(@RequestParam("id") Long id) {
        announcementService.publishAnnouncement(id);
        return success(true);
    }

    @PutMapping("/update-top")
    @Operation(summary = "置顶/取消置顶")
    @PreAuthorize("@ss.hasPermission('rental:announcement:update')")
    public CommonResult<Boolean> updateAnnouncementTop(@RequestParam("id") Long id,
                                                       @RequestParam("isTop") Integer isTop) {
        announcementService.updateAnnouncementTop(id, isTop);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得公告分页")
    @PreAuthorize("@ss.hasPermission('rental:announcement:query')")
    public CommonResult<PageResult<AnnouncementRespVO>> getAnnouncementPage(@Validated AnnouncementPageReqVO pageReqVO) {
        PageResult<AnnouncementDO> pageResult = announcementService.getAnnouncementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AnnouncementRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('rental:announcement:query')")
    public CommonResult<AnnouncementRespVO> getAnnouncement(@RequestParam("id") Long id) {
        AnnouncementDO announcement = announcementService.getAnnouncement(id);
        return success(BeanUtils.toBean(announcement, AnnouncementRespVO.class));
    }

}
