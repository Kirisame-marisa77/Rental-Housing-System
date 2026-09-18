package cn.iocoder.yudao.module.rental.dal.mysql.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ContractMapper extends BaseMapperX<ContractDO> {

    default PageResult<ContractDO> selectPage(ContractPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContractDO>()
                .likeIfPresent(ContractDO::getContractNo, reqVO.getContractNo())
                .eqIfPresent(ContractDO::getHouseId, reqVO.getHouseId())
                .eqIfPresent(ContractDO::getTenantUserId, reqVO.getTenantUserId())
                .eqIfPresent(ContractDO::getStatus, reqVO.getStatus())
                .orderByDesc(ContractDO::getId));
    }

    default ContractDO selectByContractNo(String contractNo) {
        return selectOne(ContractDO::getContractNo, contractNo);
    }

    default List<ContractDO> selectExpiring(LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<ContractDO>()
                .le(ContractDO::getRentEndDate, endDate)
                .in(ContractDO::getStatus, 2, 3)
                .orderByAsc(ContractDO::getRentEndDate));
    }

    /**
     * 租客提交退租申请：合同 2-生效中 / 3-即将到期 → 4-退租处理中
     *
     * CAS 语义：合同已被别处改过时返回 0，调用方应忽略（退租申请本身已经落库，
     * 不该因为合同状态抢不过而让提交失败）。
     */
    default int updateStatusToMovingOut(Long id) {
        return update(null, new LambdaUpdateWrapper<ContractDO>()
                .eq(ContractDO::getId, id)
                .in(ContractDO::getStatus, 2, 3)
                .set(ContractDO::getStatus, 4));
    }

    /**
     * 退租处理完成：合同置 6-已退租
     *
     * from 用集合 {2,3,4} 而不是只取 4（退租处理中）：
     * 租客提交退租时会把合同置为 4，但历史上（以及置 4 失败时）合同可能还停在
     * 2-生效中 或 3-即将到期，两条路径都要能收敛到终态。
     */
    default int updateStatusToMoveOutDone(Long id) {
        return update(null, new LambdaUpdateWrapper<ContractDO>()
                .eq(ContractDO::getId, id)
                .in(ContractDO::getStatus, 2, 3, 4)
                .set(ContractDO::getStatus, 6));
    }

    /**
     * 退租申请被房东驳回：合同从 4-退租处理中 回退到 2-生效中
     *
     * 这是「提交退租时置合同为 4」的必要配套：置了 4 之后如果没有回退路径，
     * 房东一直不处理，合同就永久卡在退租处理中。
     */
    default int updateStatusFromMoveOutBackToActive(Long id) {
        return update(null, new LambdaUpdateWrapper<ContractDO>()
                .eq(ContractDO::getId, id)
                .eq(ContractDO::getStatus, 4)
                .set(ContractDO::getStatus, 2));
    }

}
