package cn.iocoder.yudao.module.rental.service.contracttemplate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplatePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplateSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contracttemplate.ContractTemplateDO;

import java.util.List;
import java.util.Map;

/**
 * 合同模板 Service 接口
 *
 * 注意依赖方向：本 Service 只依赖 Mapper，绝不注入 ContractService ——
 * 是 ContractServiceImpl 单向依赖本 Service（用于渲染合同正文）。
 * 反向注入会踩 RentBillService → ContractService 那条链，形成循环依赖，
 * Spring Boot 2.6+ 会直接启动失败。
 *
 * @author yudao
 */
public interface ContractTemplateService {

    /**
     * 创建合同模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContractTemplate(ContractTemplateSaveReqVO createReqVO);

    /**
     * 更新合同模板（内容有变化时版本号自增）
     *
     * @param updateReqVO 更新信息
     */
    void updateContractTemplate(ContractTemplateSaveReqVO updateReqVO);

    /**
     * 删除合同模板
     *
     * @param id 编号
     */
    void deleteContractTemplate(Long id);

    /**
     * 获得合同模板分页
     *
     * @param pageReqVO 分页查询
     * @return 合同模板分页
     */
    PageResult<ContractTemplateDO> getContractTemplatePage(ContractTemplatePageReqVO pageReqVO);

    /**
     * 获得合同模板
     *
     * @param id 编号
     * @return 合同模板
     */
    ContractTemplateDO getContractTemplate(Long id);

    /**
     * 获得全部启用中的模板（供「新建合同选模板」下拉使用）
     *
     * @return 模板列表
     */
    List<ContractTemplateDO> getEnabledTemplateList();

    /**
     * 获得默认模板：优先标准租赁合同（type=0），没有则取第一个启用的模板
     *
     * @return 默认模板；没有任何启用的模板时返回 null
     */
    ContractTemplateDO getDefaultTemplate();

    /**
     * 获得内置模板变量清单（key + 中文名），供管理端「插入变量」使用
     *
     * @return 变量清单
     */
    List<Map<String, String>> getBuiltInVariables();

    /**
     * 用模板渲染出合同正文
     *
     * 返回的是「渲染后的快照」，调用方需要把它固化进合同里 —— 模板后续改版
     * 不应该改写已签合同的内容。
     *
     * @param templateId 模板编号，为空时返回 null
     * @param contract   合同
     * @return 渲染后的 HTML；模板不存在 / 没有正文 / 参数不全时返回 null
     */
    String renderForContract(Long templateId, ContractDO contract);

}
