package cn.iocoder.yudao.module.rental.dal.mysql.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantInfoMapper extends BaseMapperX<TenantInfoDO> {

    default PageResult<TenantInfoDO> selectPage(TenantInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TenantInfoDO>()
                .likeIfPresent(TenantInfoDO::getName, reqVO.getName())
                .likeIfPresent(TenantInfoDO::getPhone, reqVO.getPhone())
                .likeIfPresent(TenantInfoDO::getIdCard, reqVO.getIdCard())
                .eqIfPresent(TenantInfoDO::getAuthStatus, reqVO.getAuthStatus())
                .orderByDesc(TenantInfoDO::getId));
    }

    default TenantInfoDO selectByIdCard(String idCard) {
        return selectOne(TenantInfoDO::getIdCard, idCard);
    }

    default TenantInfoDO selectByPhone(String phone) {
        return selectOne(TenantInfoDO::getPhone, phone);
    }

}
