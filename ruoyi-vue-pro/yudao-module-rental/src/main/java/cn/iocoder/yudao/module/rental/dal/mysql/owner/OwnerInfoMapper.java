package cn.iocoder.yudao.module.rental.dal.mysql.owner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.owner.vo.OwnerInfoPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.owner.OwnerInfoDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

    /**
     * 注销账号：释放手机号与身份证号的唯一键占用，并清空密码
     *
     * 业主表比租客表更严格 —— phone 是 UNIQUE KEY uk_phone（租客表只是普通索引
     * idx_phone，租客的手机号能重注册纯属建表时的侥幸），id_card 也是 UNIQUE。
     * 所以这里不释放的话，重新注册一定 1062。
     *
     * 释放策略见 TenantInfoMapper#releaseUniqueKeysForDeregister 的注释。
     *
     * 注意：只改本表，**不要**去动 rental_tenant_info 里同号的行 ——
     * 两张表是独立命名空间，同一个人既是业主又是租客、共用手机号是合法的
     * （登录页分角色 tab 的前提就是这一点）。
     *
     * @return 影响行数。已注销的行因 deleted = 0 不匹配而返回 0，天然幂等
     */
    default int releaseUniqueKeysForDeregister(Long id) {
        return update(null, new LambdaUpdateWrapper<OwnerInfoDO>()
                .eq(OwnerInfoDO::getId, id)
                .set(OwnerInfoDO::getPhone, "D" + id)
                .set(OwnerInfoDO::getIdCard, null)
                .set(OwnerInfoDO::getPassword, null));
    }

}
