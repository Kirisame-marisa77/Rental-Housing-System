package cn.iocoder.yudao.module.rental.dal.mysql.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.tenant.vo.TenantInfoPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.tenant.TenantInfoDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

    /**
     * 注销账号：释放手机号与身份证号的唯一键占用，并清空密码
     *
     * 为什么光置 deleted=1 不够：selectByPhone 走 MyBatis-Plus，会自动追加 deleted = 0，
     * 所以逻辑删除后 Service 层的唯一性校验确实会放行 ——
     * 但**数据库的唯一索引不看 deleted**，物理行还占着值，重新注册直接 1062。
     *
     * 释放策略：
     *   phone    → 'D' + id   （1 + 19 = 20，正好等于 varchar(20) 上限；
     *                          用主键保证永不重复；D 前缀不是合法手机号形态，一眼看出是注销占位）
     *   id_card  → NULL       （InnoDB 唯一索引允许多个 NULL，比 CONCAT 更安全 ——
     *                          id 超过 17 位时 'D'+id 会溢出 varchar(18)）
     *   password → NULL       （纵深防御：万一将来有人加了「按 id 直查再比密码」的登录路径）
     *
     * 刻意不在这里写 deleted：与 MyBatis-Plus 逻辑删除拦截器的语义重叠，行为不保证。
     * 由调用方在同一事务内先调本方法、再 deleteById。
     *
     * @return 影响行数。已注销的行因 deleted = 0 不匹配而返回 0，天然幂等
     */
    default int releaseUniqueKeysForDeregister(Long id) {
        return update(null, new LambdaUpdateWrapper<TenantInfoDO>()
                .eq(TenantInfoDO::getId, id)
                .set(TenantInfoDO::getPhone, "D" + id)
                .set(TenantInfoDO::getIdCard, null)
                .set(TenantInfoDO::getPassword, null));
    }

}
