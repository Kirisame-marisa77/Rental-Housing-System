package cn.iocoder.yudao.module.rental.controller.admin.owner;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HousePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutApplicationRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.MoveOutConfirmReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerDeregisterReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoSaveReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerRegisterReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.repair.vo.RepairOrderRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.viewing.vo.ViewingAppointmentRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.meter.MeterReadingDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.repair.RepairOrderDO;
import cn.iocoder.yudao.module.rental.service.apply.ApplyService;
import cn.iocoder.yudao.module.rental.service.bill.RentBillService;
import cn.iocoder.yudao.module.rental.service.bill.UtilityBillService;
import cn.iocoder.yudao.module.rental.service.contract.ContractService;
import cn.iocoder.yudao.module.rental.service.house.HouseService;
import cn.iocoder.yudao.module.rental.service.meter.MeterReadingService;
import cn.iocoder.yudao.module.rental.service.moveout.MoveOutApplicationService;
import cn.iocoder.yudao.module.rental.service.owner.OwnerAuthService;
import cn.iocoder.yudao.module.rental.service.owner.OwnerInfoService;
import cn.iocoder.yudao.module.rental.service.repair.RepairOrderService;
import cn.iocoder.yudao.module.rental.service.viewing.ViewingAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.FILE_EMPTY;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.OWNER_NOT_LOGIN;

@Tag(name = "业主端 - 业主自助")
@RestController
@RequestMapping("/rental/owner-app")
@Validated
public class OwnerAppController {

    @Resource
    private OwnerInfoService ownerInfoService;
    @Resource
    private OwnerAuthService ownerAuthService;
    @Resource
    private FileService fileService;
    @Resource
    private HouseService houseService;
    @Resource
    private MeterReadingService meterReadingService;
    @Resource
    private MoveOutApplicationService moveOutApplicationService;
    @Resource
    private RepairOrderService repairOrderService;
    @Resource
    private ContractService contractService;
    @Resource
    private ApplyService applyService;
    @Resource
    private ViewingAppointmentService viewingAppointmentService;
    @Resource
    private RentBillService rentBillService;
    @Resource
    private UtilityBillService utilityBillService;
    @Resource
    private HttpServletRequest request;

    // ========== 登录 ==========

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "业主登录（手机号 + 密码）")
    public CommonResult<Map<String, Object>> login(@RequestParam("phone") String phone,
                                                   @RequestParam("password") String password) {
        OwnerInfoDO owner = ownerInfoService.login(phone, password);
        String token = ownerAuthService.createToken(owner.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", token);
        result.put("ownerId", owner.getId());
        result.put("name", owner.getName());
        return success(result);
    }

    @PostMapping("/register")
    @PermitAll
    @Operation(summary = "业主注册（注册后自动登录）")
    public CommonResult<Map<String, Object>> register(@Valid @RequestBody OwnerRegisterReqVO reqVO) {
        Long ownerId = ownerInfoService.register(reqVO);
        String token = ownerAuthService.createToken(ownerId);
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", token);
        result.put("ownerId", ownerId);
        result.put("name", reqVO.getName());
        return success(result);
    }

    // ========== 个人信息 ==========

    @GetMapping("/get")
    @PermitAll
    @Operation(summary = "获得业主信息")
    public CommonResult<OwnerInfoRespVO> getOwnerInfo() {
        OwnerInfoDO owner = ownerInfoService.getOwnerInfo(getCurrentOwnerId());
        return success(BeanUtils.toBean(owner, OwnerInfoRespVO.class));
    }

    @PutMapping("/update")
    @PermitAll
    @Operation(summary = "更新业主个人信息")
    public CommonResult<Boolean> updateOwnerInfo(@Valid @RequestBody OwnerInfoSaveReqVO reqVO) {
        reqVO.setId(getCurrentOwnerId());
        ownerInfoService.updateOwnerInfo(reqVO);
        return success(true);
    }

    @PutMapping("/update-password")
    @PermitAll
    @Operation(summary = "修改密码")
    public CommonResult<Boolean> updatePassword(@RequestParam("oldPassword") String oldPassword,
                                                @RequestParam("newPassword") String newPassword) {
        ownerInfoService.changePassword(getCurrentOwnerId(), oldPassword, newPassword);
        return success(true);
    }

    @PutMapping("/deregister")
    @PermitAll
    @Operation(summary = "注销账号（名下房源全部下架，逻辑删除并释放手机号）")
    public CommonResult<Boolean> deregister(@Valid @RequestBody OwnerDeregisterReqVO reqVO) {
        Long ownerId = getCurrentOwnerId();
        ownerInfoService.deregister(ownerId, reqVO.getPassword());
        // 必须清 token：内存映射不校验账号是否还存在，不清的话注销后还能继续操作
        ownerAuthService.logoutAll(ownerId);
        return success(true);
    }

    // ========== 房源 ==========

    @PostMapping("/house/create")
    @PermitAll
    @Operation(summary = "业主上传房源（待审核）")
    public CommonResult<Long> createHouse(@Valid @RequestBody HouseSaveReqVO reqVO) {
        reqVO.setOwnerId(getCurrentOwnerId());
        return success(houseService.createHouseByOwner(reqVO));
    }

    @GetMapping("/house/page")
    @PermitAll
    @Operation(summary = "我的房源分页")
    public CommonResult<PageResult<HouseRespVO>> getHousePage(@Validated HousePageReqVO pageReqVO) {
        pageReqVO.setOwnerId(getCurrentOwnerId());
        PageResult<HouseDO> pageResult = houseService.getHousePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, HouseRespVO.class));
    }

    @PostMapping("/house/upload-image")
    @PermitAll
    @Operation(summary = "业主上传房源图片，返回可直接渲染的 URL")
    public CommonResult<String> uploadHouseImage(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw exception(FILE_EMPTY);
        }
        byte[] content = IoUtil.readBytes(file.getInputStream());
        // directory 固定 rental/house：便于运维按目录清理，也避免上传方自选路径
        //
        // 不能复用 /infra/file/upload —— 那个端点没有 @PermitAll，走的是 yudao 的
        // OAuth2 鉴权，而 H5 用的是 rental 自己签发的内存 token，一定会 401。
        // 返回的 URL 由 GET /infra/file/{configId}/get/** 提供，该端点有 @PermitAll，
        // 所以 <img> 能直接渲染、不需要任何请求头。
        return success(fileService.createFile(content, file.getOriginalFilename(),
                "rental/house", file.getContentType()));
    }

    @PutMapping("/house/status")
    @PermitAll
    @Operation(summary = "业主上架/下架自己的房源")
    public CommonResult<Boolean> updateHouseStatus(@RequestParam("id") Long id,
                                                   @RequestParam("online") Boolean online) {
        houseService.updateHouseStatusByOwner(getCurrentOwnerId(), id, online);
        return success(true);
    }

    // ========== 退租处理 ==========

    @GetMapping("/move-out/page")
    @PermitAll
    @Operation(summary = "我的房源收到的退租申请")
    public CommonResult<PageResult<MoveOutApplicationRespVO>> getMoveOutPage(
            @Validated MoveOutApplicationPageReqVO pageReqVO) {
        return success(moveOutApplicationService.getOwnerMoveOutRespPage(getCurrentOwnerId(), pageReqVO));
    }

    @PutMapping("/move-out/confirm")
    @PermitAll
    @Operation(summary = "房东处理退租（房屋验收 + 费用结算，生成结算单）")
    public CommonResult<Long> confirmMoveOut(@Valid @RequestBody MoveOutConfirmReqVO confirmReqVO) {
        return success(moveOutApplicationService.ownerConfirmMoveOutApplication(
                getCurrentOwnerId(), confirmReqVO));
    }

    @PutMapping("/move-out/reject")
    @PermitAll
    @Operation(summary = "房东驳回退租申请")
    public CommonResult<Boolean> rejectMoveOut(@RequestParam("id") Long id,
                                               @RequestParam("reason") String reason) {
        moveOutApplicationService.ownerRejectMoveOutApplication(getCurrentOwnerId(), id, reason);
        return success(true);
    }

    // ========== 抄表 ==========

    @PostMapping("/meter/create")
    @PermitAll
    @Operation(summary = "业主上传水电读数（含截图，待审核）")
    public CommonResult<Long> createMeterReading(@Valid @RequestBody MeterReadingSaveReqVO reqVO) {
        reqVO.setOwnerId(getCurrentOwnerId());
        return success(meterReadingService.createMeterReadingByOwner(reqVO));
    }

    @GetMapping("/meter/page")
    @PermitAll
    @Operation(summary = "我的抄表记录分页")
    public CommonResult<PageResult<MeterReadingRespVO>> getMeterReadingPage(@Validated MeterReadingPageReqVO pageReqVO) {
        pageReqVO.setOwnerId(getCurrentOwnerId());
        PageResult<MeterReadingDO> pageResult = meterReadingService.getMeterReadingPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MeterReadingRespVO.class));
    }

    // ========== 租房申请 ==========

    @GetMapping("/apply/page")
    @PermitAll
    @Operation(summary = "我的房源收到的租房申请")
    public CommonResult<PageResult<ApplyRespVO>> getApplyPage(@Validated ApplyPageReqVO pageReqVO) {
        return success(applyService.getOwnerApplyPage(getCurrentOwnerId(), pageReqVO));
    }

    @PutMapping("/apply/approve")
    @PermitAll
    @Operation(summary = "房东审批租房申请（同意则自动生成合同与首期账单）")
    public CommonResult<Boolean> approveApply(@RequestParam("id") Long id,
                                              @RequestParam("pass") Boolean pass,
                                              @RequestParam(value = "reason", required = false) String reason) {
        applyService.ownerApproveApply(getCurrentOwnerId(), id, pass, reason);
        return success(true);
    }

    // ========== 看房预约 ==========

    @GetMapping("/viewing-appointment/page")
    @PermitAll
    @Operation(summary = "我的房源收到的看房预约")
    public CommonResult<PageResult<ViewingAppointmentRespVO>> getAppointmentPage(
            @Validated ViewingAppointmentPageReqVO pageReqVO) {
        return success(viewingAppointmentService.getOwnerAppointmentPage(getCurrentOwnerId(), pageReqVO));
    }

    @PutMapping("/viewing-appointment/confirm")
    @PermitAll
    @Operation(summary = "房东确认看房预约")
    public CommonResult<Boolean> confirmAppointment(@RequestParam("id") Long id) {
        viewingAppointmentService.confirmAppointment(getCurrentOwnerId(), id);
        return success(true);
    }

    @PutMapping("/viewing-appointment/complete")
    @PermitAll
    @Operation(summary = "房东完成看房并填写反馈")
    public CommonResult<Boolean> completeAppointment(@RequestParam("id") Long id,
                                                     @RequestParam(value = "feedback", required = false) String feedback) {
        viewingAppointmentService.completeAppointment(getCurrentOwnerId(), id, feedback);
        return success(true);
    }

    // ========== 维修 ==========

    @GetMapping("/repair/page")
    @PermitAll
    @Operation(summary = "我的维修工单分页")
    public CommonResult<PageResult<RepairOrderRespVO>> getRepairOrderPage(@Validated RepairOrderPageReqVO pageReqVO) {
        pageReqVO.setOwnerId(getCurrentOwnerId());
        PageResult<RepairOrderDO> pageResult = repairOrderService.getRepairOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RepairOrderRespVO.class));
    }

    @PutMapping("/repair/handle")
    @PermitAll
    @Operation(summary = "业主处理维修并上传证据")
    public CommonResult<Boolean> handleRepairOrder(@RequestParam("id") Long id,
                                                   @RequestParam(value = "repairDescription", required = false) String repairDescription,
                                                   @RequestParam(value = "handleEvidence", required = false) String handleEvidence) {
        repairOrderService.handleRepairOrder(id, getCurrentOwnerId(), repairDescription, handleEvidence);
        return success(true);
    }

    // ========== 合同签约 ==========

    @GetMapping("/contract/page")
    @PermitAll
    @Operation(summary = "我的合同（按房源归属）")
    public CommonResult<List<ContractRespVO>> getContractPage() {
        return success(contractService.getOwnerContractRespList(getCurrentOwnerId()));
    }

    @GetMapping("/contract/get")
    @PermitAll
    @Operation(summary = "合同详情")
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        return success(contractService.getOwnerContractDetail(getCurrentOwnerId(), id));
    }

    @PutMapping("/contract/sign")
    @PermitAll
    @Operation(summary = "业主确认签约")
    public CommonResult<Boolean> signContract(@RequestParam("id") Long id) {
        // 双方签署 + 缴清首期账单即成交；只有落选才是错误，
        // 「对方还没签」/「还没缴费」都只是没到时机，签约本身已成功
        if (contractService.ownerSignContract(id) == ContractService.Activation.LOST_RACE) {
            throw exception(CONTRACT_HOUSE_TAKEN);
        }
        return success(true);
    }

    // ========== 我的账单（只读） ==========

    @GetMapping("/rent-bill/page")
    @PermitAll
    @Operation(summary = "我的租金/物业费账单（按房源归属）")
    public CommonResult<PageResult<RentBillRespVO>> getRentBillPage(@Validated RentBillPageReqVO pageReqVO) {
        return success(rentBillService.getOwnerRentBillPage(getCurrentOwnerId(), pageReqVO));
    }

    @GetMapping("/utility-bill/page")
    @PermitAll
    @Operation(summary = "我的水电费账单（按房源归属）")
    public CommonResult<PageResult<UtilityBillRespVO>> getUtilityBillPage(@Validated UtilityBillPageReqVO pageReqVO) {
        return success(utilityBillService.getOwnerUtilityBillPage(getCurrentOwnerId(), pageReqVO));
    }

    /**
     * 从请求头解析当前业主 ID
     */
    private Long getCurrentOwnerId() {
        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        Long ownerId = ownerAuthService.getOwnerId(token);
        if (ownerId == null) {
            throw exception(OWNER_NOT_LOGIN);
        }
        return ownerId;
    }

}
