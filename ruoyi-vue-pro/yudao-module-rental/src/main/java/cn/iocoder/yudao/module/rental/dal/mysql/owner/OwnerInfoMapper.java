package cn.iocoder.yudao.module.rental.dal.mysql.owner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OwnerInfoMapper extends BaseMapperX<OwnerInfoDO> {

    default PageResult<OwnerInfoDO> selectPage(OwnerInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OwnerInfoDO>()
                .likeIfPresent(OwnerInfoDO::getName, reqVO.getName())
                .likeIfPresent(OwnerInfoDO::getPhone, reqVO.getPhone())
                .likeIfPresent(OwnerInfoDO::getIdCard, reqVO.getIdCard())
                .orderByDesc(OwnerInfoDO::getId));
    }

    default OwnerInfoDO selectByIdCard(String idCard) {
        return selectOne(OwnerInfoDO::getIdCard, idCard);
    }

    default OwnerInfoDO selectByPhone(String phone) {
        return selectOne(OwnerInfoDO::getPhone, phone);
    }

}
