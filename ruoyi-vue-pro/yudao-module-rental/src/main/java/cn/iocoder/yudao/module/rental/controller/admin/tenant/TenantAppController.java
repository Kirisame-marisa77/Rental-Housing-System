package cn.iocoder.yudao.module.rental.controller.admin.tenant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementDetailRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplySaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.favorite.vo.HouseFavoritePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.favorite.vo.HouseFavoriteRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseDetailRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HousePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationTenantCreateReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantDeregisterReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantRegisterReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairOrderDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.service.announcement.AnnouncementReadService;
import cn.iocoder.yudao.module.rental.service.apply.ApplyService;
import cn.iocoder.yudao.module.rental.service.bill.RentBillService;
import cn.iocoder.yudao.module.rental.service.contract.ContractService;
import cn.iocoder.yudao.module.rental.service.favorite.HouseFavoriteService;
import cn.iocoder.yudao.module.rental.service.house.HouseService;
import cn.iocoder.yudao.module.rental.service.moveout.MoveOutApplicationService;
import cn.iocoder.yudao.module.rental.service.repair.RepairOrderService;
import cn.iocoder.yudao.module.rental.service.tenant.TenantAuthService;
import cn.iocoder.yudao.module.rental.service.tenant.TenantInfoService;
import cn.iocoder.yudao.module.rental.service.viewing.ViewingAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.CONTRACT_HOUSE_TAKEN;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.TENANT_NOT_LOGIN;

@Tag(name = "租客端 - 租客自助")
@RestController
@RequestMapping("/rental/tenant-app")
@Validated
public class TenantAppController {

    @Resource
    private TenantInfoService tenantInfoService;
    @Resource
    private TenantAuthService tenantAuthService;
    @Resource
    private HouseService houseService;
    @Resource
    private ApplyService applyService;
    @Resource
    private ContractService contractService;
    @Resource
    private RentBillService rentBillService;
    @Resource
    private HouseFavoriteService houseFavoriteService;
    @Resource
    private ViewingAppointmentService viewingAppointmentService;
    @Resource
    private AnnouncementReadService announcementReadService;
    @Resource
    private RepairOrderService repairOrderService;
    @Resource
    private MoveOutApplicationService moveOutApplicationService;
    @Resource
    private HttpServletRequest request;

    // ========== 登录 / 注册 ==========

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "租客登录（手机号 + 密码）")
    public CommonResult<Map<String, Object>> login(@RequestParam("phone") String phone,
                                                   @RequestParam("password") String password) {
        TenantInfoDO tenant = tenantInfoService.login(phone, password);
        String token = tenantAuthService.createToken(tenant.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", token);
        result.put("tenantId", tenant.getId());
        result.put("name", tenant.getName());
        return success(result);
    }

    @PostMapping("/register")
    @PermitAll
    @Operation(summary = "租客注册（注册后自动登录）")
    public CommonResult<Map<String, Object>> register(@Valid @RequestBody TenantRegisterReqVO reqVO) {
        Long tenantId = tenantInfoService.register(reqVO);
        String token = tenantAuthService.createToken(tenantId);
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", token);
        result.put("tenantId", tenantId);
        result.put("name", reqVO.getName());
        return success(result);
    }

    // ========== 个人信息 ==========

    @GetMapping("/get")
    @PermitAll
    @Operation(summary = "获得租客信息")
    public CommonResult<TenantInfoRespVO> getTenantInfo() {
        TenantInfoDO tenant = tenantInfoService.getTenantInfo(getCurrentTenantId());
        return success(BeanUtils.toBean(tenant, TenantInfoRespVO.class));
    }

    @PutMapping("/update")
    @PermitAll
    @Operation(summary = "更新租客个人信息")
    public CommonResult<Boolean> updateTenantInfo(@Valid @RequestBody TenantInfoSaveReqVO reqVO) {
        reqVO.setId(getCurrentTenantId());
        tenantInfoService.updateTenantInfo(reqVO);
        return success(true);
    }

    @PutMapping("/update-password")
    @PermitAll
    @Operation(summary = "修改密码")
    public CommonResult<Boolean> updatePassword(@RequestParam("oldPassword") String oldPassword,
                                                @RequestParam("newPassword") String newPassword) {
        tenantInfoService.changePassword(getCurrentTenantId(), oldPassword, newPassword);
        return success(true);
    }

    @PutMapping("/deregister")
    @PermitAll
    @Operation(summary = "注销账号（逻辑删除并释放手机号，可重新注册）")
    public CommonResult<Boolean> deregister(@Valid @RequestBody TenantDeregisterReqVO reqVO) {
        Long tenantId = getCurrentTenantId();
        tenantInfoService.deregister(tenantId, reqVO.getPassword());
        // 必须清 token：内存里的 token → tenantId 映射没有反向校验，
        // 不清的话被注销的账号还能继续收藏、报修、下单
        tenantAuthService.logoutAll(tenantId);
        return success(true);
    }

    // ========== 房源 ==========

    @GetMapping("/house/page")
    @PermitAll
    @Operation(summary = "浏览上架房源")
    public CommonResult<PageResult<HouseRespVO>> getHousePage(@Validated HousePageReqVO pageReqVO) {
        pageReqVO.setStatus(1); // 只展示上架房源
        PageResult<HouseDO> pageResult = houseService.getHousePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, HouseRespVO.class));
    }

    @GetMapping("/house/get")
    @PermitAll
    @Operation(summary = "查看房源详情（含图片、户型、地址与房东信息）")
    public CommonResult<HouseDetailRespVO> getHouseDetail(@RequestParam("id") Long id) {
        // 与上面的 /house/page 保持一致：详情不需要身份，未登录也能看
        return success(houseService.getTenantHouseDetail(id));
    }

    // ========== 租房申请 ==========

    @PostMapping("/apply/create")
    @PermitAll
    @Operation(summary = "提交租房申请")
    public CommonResult<Long> createApply(@Valid @RequestBody ApplySaveReqVO reqVO) {
        reqVO.setTenantUserId(getCurrentTenantId());
        reqVO.setStatus(0); // 待审批
        return success(applyService.createApply(reqVO));
    }

    @GetMapping("/apply/page")
    @PermitAll
    @Operation(summary = "我的租房申请")
    public CommonResult<PageResult<ApplyRespVO>> getApplyPage(@Validated ApplyPageReqVO pageReqVO) {
        pageReqVO.setTenantUserId(getCurrentTenantId());
        return success(applyService.getApplyPageWithDetail(pageReqVO));
    }

    // ========== 合同 ==========

    @GetMapping("/contract/page")
    @PermitAll
    @Operation(summary = "我的合同")
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Validated ContractPageReqVO pageReqVO) {
        pageReqVO.setTenantUserId(getCurrentTenantId());
        return success(contractService.getContractRespPage(pageReqVO));
    }

    @GetMapping("/contract/get")
    @PermitAll
    @Operation(summary = "合同详情")
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        return success(contractService.getTenantContractDetail(getCurrentTenantId(), id));
    }

    @PutMapping("/contract/sign")
    @PermitAll
    @Operation(summary = "租客确认签约")
    public CommonResult<Boolean> signContract(@RequestParam("id") Long id) {
        // 双方签署 + 缴清首期账单即成交；只有落选才是错误，
        // 「对方还没签」/「还没缴费」都只是没到时机，签约本身已成功
        if (contractService.tenantSignContract(id) == ContractService.Activation.LOST_RACE) {
            throw exception(CONTRACT_HOUSE_TAKEN);
        }
        return success(true);
    }

    // ========== 退租申请 ==========

    @PostMapping("/move-out/create")
    @PermitAll
    @Operation(summary = "租客发起退租申请")
    public CommonResult<Long> createMoveOutApplication(
            @Valid @RequestBody MoveOutApplicationTenantCreateReqVO createReqVO) {
        // 租客编号与房源编号都由 Service 从合同反查，不采信请求体里的值
        return success(moveOutApplicationService.createMoveOutApplicationByTenant(
                getCurrentTenantId(), createReqVO));
    }

    @GetMapping("/move-out/page")
    @PermitAll
    @Operation(summary = "我的退租申请")
    public CommonResult<PageResult<MoveOutApplicationRespVO>> getMoveOutPage(
            @Validated MoveOutApplicationPageReqVO pageReqVO) {
        return success(moveOutApplicationService.getTenantMoveOutRespPage(getCurrentTenantId(), pageReqVO));
    }

    // ========== 租金账单 ==========

    @GetMapping("/rent-bill/page")
    @PermitAll
    @Operation(summary = "我的租金账单")
    public CommonResult<PageResult<RentBillRespVO>> getRentBillPage(@Validated RentBillPageReqVO pageReqVO) {
        pageReqVO.setTenantUserId(getCurrentTenantId());
        PageResult<RentBillDO> pageResult = rentBillService.getRentBillPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RentBillRespVO.class));
    }

    @PutMapping("/rent-bill/pay")
    @PermitAll
    @Operation(summary = "租客缴纳租金账单")
    public CommonResult<Boolean> payRentBill(@RequestParam("id") Long id,
                                             @RequestParam(value = "payMethod", required = false) String payMethod,
                                             @RequestParam(value = "transactionNo", required = false) String transactionNo,
                                             @RequestParam(value = "remark", required = false) String remark) {
        rentBillService.payRentBill(id, getCurrentTenantId(), payMethod, null, transactionNo, remark);
        return success(true);
    }

    // ========== 房源收藏 ==========

    @PostMapping("/house-favorite/add")
    @PermitAll
    @Operation(summary = "收藏房源")
    public CommonResult<Long> addFavorite(@RequestParam("houseId") Long houseId) {
        return success(houseFavoriteService.addFavorite(getCurrentTenantId(), houseId));
    }

    @PutMapping("/house-favorite/cancel")
    @PermitAll
    @Operation(summary = "取消收藏")
    public CommonResult<Boolean> cancelFavorite(@RequestParam("houseId") Long houseId) {
        houseFavoriteService.cancelFavorite(getCurrentTenantId(), houseId);
        return success(true);
    }

    @GetMapping("/house-favorite/page")
    @PermitAll
    @Operation(summary = "我的收藏分页")
    public CommonResult<PageResult<HouseFavoriteRespVO>> getFavoritePage(
            @Validated HouseFavoritePageReqVO pageReqVO) {
        return success(houseFavoriteService.getFavoritePage(pageReqVO, getCurrentTenantId()));
    }

    @GetMapping("/house-favorite/ids")
    @PermitAll
    @Operation(summary = "我收藏的房源 ID 列表（供找房列表本地比对）")
    public CommonResult<List<Long>> getFavoriteHouseIds() {
        return success(houseFavoriteService.getFavoriteHouseIds(getCurrentTenantId()));
    }

    // ========== 预约看房 ==========

    @PostMapping("/viewing-appointment/create")
    @PermitAll
    @Operation(summary = "预约看房")
    public CommonResult<Long> createAppointment(@Valid @RequestBody ViewingAppointmentSaveReqVO reqVO) {
        return success(viewingAppointmentService.createAppointment(getCurrentTenantId(), reqVO));
    }

    @GetMapping("/viewing-appointment/page")
    @PermitAll
    @Operation(summary = "我的看房预约")
    public CommonResult<PageResult<ViewingAppointmentRespVO>> getAppointmentPage(
            @Validated ViewingAppointmentPageReqVO pageReqVO) {
        pageReqVO.setTenantUserId(getCurrentTenantId());
        return success(viewingAppointmentService.getAppointmentPage(pageReqVO));
    }

    @PutMapping("/viewing-appointment/cancel")
    @PermitAll
    @Operation(summary = "取消看房预约")
    public CommonResult<Boolean> cancelAppointment(@RequestParam("id") Long id) {
        viewingAppointmentService.cancelAppointment(getCurrentTenantId(), id);
        return success(true);
    }

    // ========== 公告 ==========

    @GetMapping("/announcement/page")
    @PermitAll
    @Operation(summary = "公告分页（仅已发布）")
    public CommonResult<PageResult<TenantAnnouncementRespVO>> getAnnouncementPage(
            @Validated TenantAnnouncementPageReqVO pageReqVO) {
        return success(announcementReadService.getPublishedAnnouncementPage(pageReqVO, getCurrentTenantId()));
    }

    @GetMapping("/announcement/get")
    @PermitAll
    @Operation(summary = "公告详情（仅已发布）")
    public CommonResult<TenantAnnouncementDetailRespVO> getAnnouncement(@RequestParam("id") Long id) {
        return success(announcementReadService.getPublishedAnnouncement(id, getCurrentTenantId()));
    }

    @PutMapping("/announcement/read")
    @PermitAll
    @Operation(summary = "标记公告已读")
    public CommonResult<Boolean> readAnnouncement(@RequestParam("id") Long id) {
        announcementReadService.markRead(getCurrentTenantId(), id);
        return success(true);
    }

    @GetMapping("/announcement/unread-count")
    @PermitAll
    @Operation(summary = "我的未读公告数")
    public CommonResult<Long> getUnreadAnnouncementCount() {
        return success(announcementReadService.getUnreadCount(getCurrentTenantId()));
    }

    // ========== 报修 ==========

    @PostMapping("/repair/create")
    @PermitAll
    @Operation(summary = "租客报修")
    public CommonResult<Long> createRepairOrder(@Valid @RequestBody RepairOrderSaveReqVO reqVO) {
        reqVO.setTenantUserId(getCurrentTenantId());
        return success(repairOrderService.createRepairOrderByTenant(reqVO));
    }

    @GetMapping("/repair/page")
    @PermitAll
    @Operation(summary = "我的报修工单")
    public CommonResult<PageResult<RepairOrderRespVO>> getRepairOrderPage(@Validated RepairOrderPageReqVO pageReqVO) {
        pageReqVO.setTenantUserId(getCurrentTenantId());
        PageResult<RepairOrderDO> pageResult = repairOrderService.getRepairOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RepairOrderRespVO.class));
    }

    @PutMapping("/repair/confirm")
    @PermitAll
    @Operation(summary = "租客确认维修完成")
    public CommonResult<Boolean> confirmRepairOrder(@RequestParam("id") Long id) {
        repairOrderService.confirmRepairOrderByTenant(getCurrentTenantId(), id);
        return success(true);
    }

    /**
     * 从请求头解析当前租客 ID
     */
    private Long getCurrentTenantId() {
        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        Long tenantId = tenantAuthService.getTenantId(token);
        if (tenantId == null) {
            throw exception(TENANT_NOT_LOGIN);
        }
        return tenantId;
    }

}
