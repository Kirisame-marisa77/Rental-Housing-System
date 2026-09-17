package cn.iocoder.yudao.module.rental.dal.dataobject.contracttemplate;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合同模板表
 *
 * @author yudao
 */
@TableName("rental_contract_template")
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractTemplateDO extends BaseDO {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 模板类型：0-标准租赁合同，1-短期租赁合同，2-商业租赁合同
     */
    private Integer templateType;
    /**
     * 合同模板内容（HTML 格式，支持 ${变量名} 占位符）
     */
    private String content;
    /**
     * 模板变量定义，JSON 格式：[{"key":"tenantName","label":"租客姓名"}]
     */
    private String variables;
    /**
     * 模板版本号，内容每次修改自增
     */
    private Integer version;
    /**
     * 状态：0-启用，1-禁用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
