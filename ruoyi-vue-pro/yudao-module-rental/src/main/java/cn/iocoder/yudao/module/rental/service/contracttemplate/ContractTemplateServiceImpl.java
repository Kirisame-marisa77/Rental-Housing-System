package cn.iocoder.yudao.module.rental.service.contracttemplate;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplatePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplateSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contracttemplate.ContractTemplateDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.RentBillMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.contracttemplate.ContractTemplateMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.house.HouseMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.owner.OwnerInfoMapper;
import cn.iocoder.yudao.module.rental.dal.mysql.tenant.TenantInfoMapper;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rental.enums.ErrorCodeConstants.CONTRACT_TEMPLATE_NOT_EXISTS;

/**
 * 合同模板 Service 实现类
 *
 * @author yudao
 */
@Service
public class ContractTemplateServiceImpl implements ContractTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 内置可解析变量（顺序即前端展示顺序）
     *
     * 与 buildVariables() 里 put 的 key 必须一一对应，否则前端会展示出点不出值的占位符
     */
    private static final Map<String, String> BUILT_IN_VARIABLES;

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("contractNo", "合同编号");
        m.put("communityName", "小区名称");
        m.put("area", "区域");
        m.put("buildingNo", "楼栋");
        m.put("roomNo", "房号");
        m.put("layout", "户型");
        m.put("squareArea", "建筑面积(㎡)");
        m.put("ownerName", "房东姓名");
        m.put("ownerPhone", "房东电话");
        m.put("tenantName", "租客姓名");
        m.put("tenantPhone", "租客电话");
        m.put("rentStartDate", "租期开始");
        m.put("rentEndDate", "租期结束");
        m.put("monthlyRent", "月租金(元)");
        m.put("depositAmount", "押金(元)");
        m.put("paymentMethod", "付款方式");
        m.put("propertyFeeUnit", "物业费单价");
        m.put("firstBillNo", "首期账单编号");
        m.put("signDate", "签署日期");
        BUILT_IN_VARIABLES = Collections.unmodifiableMap(m);
    }

    @Resource
    private ContractTemplateMapper contractTemplateMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private OwnerInfoMapper ownerInfoMapper;
    @Resource
    private TenantInfoMapper tenantInfoMapper;
    @Resource
    private RentBillMapper rentBillMapper;

    @Override
    public Long createContractTemplate(ContractTemplateSaveReqVO createReqVO) {
        ContractTemplateDO template = BeanUtils.toBean(createReqVO, ContractTemplateDO.class);
        template.setVersion(1);
        if (template.getStatus() == null) {
            template.setStatus(0); // 默认启用
        }
        contractTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateContractTemplate(ContractTemplateSaveReqVO updateReqVO) {
        ContractTemplateDO old = validateContractTemplateExists(updateReqVO.getId());
        ContractTemplateDO updateObj = BeanUtils.toBean(updateReqVO, ContractTemplateDO.class);
        // 正文变了才递增版本号：只改名称/备注不该造出一个新版本，
        // 否则「版本」会退化成无意义的修改次数
        if (StrUtil.isNotBlank(updateReqVO.getContent())
                && !Objects.equals(old.getContent(), updateReqVO.getContent())) {
            updateObj.setVersion((old.getVersion() == null ? 1 : old.getVersion()) + 1);
        }
        contractTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteContractTemplate(Long id) {
        validateContractTemplateExists(id);
        contractTemplateMapper.deleteById(id);
    }

    @Override
    public PageResult<ContractTemplateDO> getContractTemplatePage(ContractTemplatePageReqVO pageReqVO) {
        return contractTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public ContractTemplateDO getContractTemplate(Long id) {
        return contractTemplateMapper.selectById(id);
    }

    @Override
    public List<ContractTemplateDO> getEnabledTemplateList() {
        return contractTemplateMapper.selectEnabledList();
    }

    @Override
    public ContractTemplateDO getDefaultTemplate() {
        List<ContractTemplateDO> enabled = contractTemplateMapper.selectEnabledList();
        if (enabled.isEmpty()) {
            return null;
        }
        // 优先标准租赁合同（type=0）；selectEnabledList 已按类型升序，这里再显式过滤一次，
        // 避免将来改了排序规则就悄悄换掉默认模板
        return enabled.stream()
                .filter(t -> Integer.valueOf(0).equals(t.getTemplateType()))
                .findFirst()
                .orElse(enabled.get(0));
    }

    @Override
    public String renderForContract(Long templateId, ContractDO contract) {
        if (templateId == null || contract == null) {
            return null;
        }
        ContractTemplateDO template = contractTemplateMapper.selectById(templateId);
        if (template == null || StrUtil.isBlank(template.getContent())) {
            return null;
        }
        return render(template.getContent(), contract);
    }

    /**
     * 占位符替换
     *
     * 刻意用手写 replace 而不是 StrUtil.format：模板正文是 HTML，里面必然有
     * &lt;style&gt; 这类 CSS 花括号，而 StrUtil.format 会把任意 {} 当占位符解析，
     * 遇到 CSS 就抛异常。这里只认严格形态的 ${key}，其它花括号原样保留。
     */
    private String render(String content, ContractDO contract) {
        String result = content;
        for (Map.Entry<String, String> entry : buildVariables(contract).entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        // 未识别的占位符原样留着：宁可让使用者看见 ${xxx} 知道自己拼错了，
        // 也不要静默清空成空白
        return result;
    }

    /**
     * 组装变量取值。取不到的字段一律给空串，保证 replace 能命中、不留 ${} 残骸
     */
    private Map<String, String> buildVariables(ContractDO contract) {
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("contractNo", text(contract.getContractNo()));
        vars.put("rentStartDate", date(contract.getRentStartDate()));
        vars.put("rentEndDate", date(contract.getRentEndDate()));
        vars.put("monthlyRent", money(contract.getMonthlyRent()));
        vars.put("depositAmount", money(contract.getDepositAmount()));
        vars.put("paymentMethod", text(contract.getPaymentMethod()));
        vars.put("propertyFeeUnit", money(contract.getPropertyFeeUnit()));
        // 签署日期：双方都还没签时留空，不要写成「今天」——那会造出一份日期不实的合同
        vars.put("signDate", contract.getOwnerSignTime() != null
                ? contract.getOwnerSignTime().toLocalDate().format(DATE_FORMATTER) : "");

        HouseDO house = contract.getHouseId() == null ? null : houseMapper.selectById(contract.getHouseId());
        if (house != null) {
            vars.put("communityName", text(house.getCommunityName()));
            vars.put("area", text(house.getArea()));
            vars.put("buildingNo", text(house.getBuildingNo()));
            vars.put("roomNo", text(house.getRoomNo()));
            vars.put("layout", text(house.getLayout()));
            vars.put("squareArea", money(house.getSquareArea()));
            OwnerInfoDO owner = house.getOwnerId() == null ? null : ownerInfoMapper.selectById(house.getOwnerId());
            if (owner != null) {
                vars.put("ownerName", text(owner.getName()));
                vars.put("ownerPhone", text(owner.getPhone()));
            }
        }
        TenantInfoDO tenant = contract.getTenantUserId() == null ? null
                : tenantInfoMapper.selectById(contract.getTenantUserId());
        if (tenant != null) {
            vars.put("tenantName", text(tenant.getName()));
            vars.put("tenantPhone", text(tenant.getPhone()));
        }
        RentBillDO firstBill = contract.getId() == null ? null
                : rentBillMapper.selectFirstBillByContractId(contract.getId());
        vars.put("firstBillNo", firstBill == null ? "" : text(firstBill.getBillNo()));
        return vars;
    }

    @VisibleForTesting
    public ContractTemplateDO validateContractTemplateExists(Long id) {
        ContractTemplateDO template = id == null ? null : contractTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(CONTRACT_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @Override
    public List<Map<String, String>> getBuiltInVariables() {
        List<Map<String, String>> list = new ArrayList<>(BUILT_IN_VARIABLES.size());
        BUILT_IN_VARIABLES.forEach((key, label) -> {
            Map<String, String> item = new LinkedHashMap<>(2);
            item.put("key", key);
            item.put("label", label);
            list.add(item);
        });
        return list;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static String date(LocalDate value) {
        return value == null ? "" : value.format(DATE_FORMATTER);
    }

    private static String money(BigDecimal value) {
        // toPlainString 必须留着：stripTrailingZeros 会把 3500.00 变成 3.5E+3
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

}
