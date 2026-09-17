package cn.iocoder.yudao.module.rental.dal.mysql.contracttemplate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.contracttemplate.vo.ContractTemplatePageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contracttemplate.ContractTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContractTemplateMapper extends BaseMapperX<ContractTemplateDO> {

    default PageResult<ContractTemplateDO> selectPage(ContractTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContractTemplateDO>()
                .likeIfPresent(ContractTemplateDO::getTemplateName, reqVO.getTemplateName())
                .eqIfPresent(ContractTemplateDO::getTemplateType, reqVO.getTemplateType())
                .eqIfPresent(ContractTemplateDO::getStatus, reqVO.getStatus())
                .orderByDesc(ContractTemplateDO::getId));
    }

    /**
     * 查询全部启用中的模板（供「新建合同选模板」下拉使用）
     */
    default List<ContractTemplateDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<ContractTemplateDO>()
                .eq(ContractTemplateDO::getStatus, 0)
                .orderByAsc(ContractTemplateDO::getTemplateType)
                .orderByDesc(ContractTemplateDO::getId));
    }

}
