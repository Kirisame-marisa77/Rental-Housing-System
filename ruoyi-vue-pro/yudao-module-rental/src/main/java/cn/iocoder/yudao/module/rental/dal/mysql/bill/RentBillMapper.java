package cn.iocoder.yudao.module.rental.dal.mysql.bill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface RentBillMapper extends BaseMapperX<RentBillDO> {

    default PageResult<RentBillDO> selectPage(RentBillPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RentBillDO>()
                .likeIfPresent(RentBillDO::getBillNo, reqVO.getBillNo())
                .eqIfPresent(RentBillDO::getContractId, reqVO.getContractId())
                .eqIfPresent(RentBillDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(RentBillDO::getPayStatus, reqVO.getPayStatus())
                .inIfPresent(RentBillDO::getHouseId, reqVO.getHouseIds())
                .orderByDesc(RentBillDO::getId));
    }

    /**
     * 查询合同的首期账单（billType = 0-首期账单）
     */
    default RentBillDO selectFirstBillByContractId(Long contractId) {
        if (contractId == null) {
            return null;
        }
        List<RentBillDO> bills = selectList(new LambdaQueryWrapperX<RentBillDO>()
                .eq(RentBillDO::getContractId, contractId)
                .eq(RentBillDO::getBillType, 0)
                .orderByAsc(RentBillDO::getId));
        return bills.isEmpty() ? null : bills.get(0);
    }

    /**
     * 批量查询多个合同的首期账单（billType = 0），用于合同列表补全
     */
    default List<RentBillDO> selectFirstBillsByContractIds(Collection<Long> contractIds) {
        if (contractIds == null || contractIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<RentBillDO>()
                .eq(RentBillDO::getBillType, 0)
                .in(RentBillDO::getContractId, contractIds)
                .orderByAsc(RentBillDO::getId));
    }

    /**
     * 查询合同下的全部账单
     */
    default List<RentBillDO> selectListByContractId(Long contractId) {
        return selectList(new LambdaQueryWrapperX<RentBillDO>()
                .eq(RentBillDO::getContractId, contractId)
                .orderByAsc(RentBillDO::getId));
    }

    /**
     * 乐观锁式的缴费状态流转：仅当当前状态为 fromPayStatus 时才更新，返回影响行数
     *
     * 用于防止同一账单被重复缴费、重复写入缴费记录
     */
    default int updatePayStatusByIdAndPayStatus(Long id, Integer fromPayStatus, RentBillDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<RentBillDO>()
                .eq(RentBillDO::getId, id)
                .eq(RentBillDO::getPayStatus, fromPayStatus));
    }

}
