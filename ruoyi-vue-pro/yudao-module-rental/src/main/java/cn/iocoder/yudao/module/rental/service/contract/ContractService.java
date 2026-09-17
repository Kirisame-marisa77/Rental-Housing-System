package cn.iocoder.yudao.module.rental.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.apply.ApplyDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;

import java.time.LocalDate;
import java.util.List;

/**
 * 租房合同 Service 接口
 *
 * @author yudao
 */
public interface ContractService {

    /**
     * 合同成交判定的三种结果
     *
     * 必须区分「还没到时机」与「房源被抢走」：前者是正常状态（比如只有一方签了字），
     * 后者才是错误。用 boolean 会把两者混为一谈，导致刚签完字就误报「房源已被他人承租」。
     */
    enum Activation {
        /** 合同已生效，房源被本合同占用 */
        ACTIVATED,
        /** 还没到成交时机（未双方签署 / 首期账单未缴），数据未改动，不是错误 */
        NOT_READY,
        /** 房源已被其他租客抢先承租，本合同已置为「已取消」 */
        LOST_RACE
    }

    /**
     * 创建合同
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContract(ContractSaveReqVO createReqVO);

    /**
     * 更新合同
     *
     * @param updateReqVO 更新信息
     */
    void updateContract(ContractSaveReqVO updateReqVO);

    /**
     * 删除合同
     *
     * @param id 编号
     */
    void deleteContract(Long id);

    /**
     * 获得合同分页
     *
     * @param pageReqVO 分页查询
     * @return 合同分页
     */
    PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO);

    /**
     * 获得合同
     *
     * @param id 编号
     * @return 合同
     */
    ContractDO getContract(Long id);

    /**
     * 租约续签
     *
     * @param id         合同编号
     * @param newEndDate 新的租期结束日期
     */
    void renewContract(Long id, LocalDate newEndDate);

    /**
     * 取消合同
     *
     * @param id 合同编号
     */
    void cancelContract(Long id);

    /**
     * 获得即将到期的合同列表
     *
     * @param days 未来天数
     * @return 合同列表
     */
    List<ContractDO> getExpiringContractList(Integer days);

    /**
     * 租客签署合同（签署后顺带尝试成交）
     *
     * @param id 合同编号
     * @return 成交判定结果
     */
    Activation tenantSignContract(Long id);

    /**
     * 业主签署合同（签署后顺带尝试成交）
     *
     * @param id 合同编号
     * @return 成交判定结果
     */
    Activation ownerSignContract(Long id);

    /**
     * 根据已通过的租房申请生成待签署合同
     *
     * 仅创建合同本身，不修改房源与申请状态。房东同意后房源仍保持「上架」，
     * 直到有租客完成签约并缴费才被占用（先到先得）
     *
     * @param apply 租房申请
     * @return 生成的合同
     */
    ContractDO createContractByApply(ApplyDO apply);

    /**
     * 尝试让合同生效（唯一的成交入口）
     *
     * 需同时满足：合同为「待签署」、双方均已签署、首期账单已缴费。
     * 满足后以先到先得的方式抢占房源：抢占成功则合同生效并自动取消同房源的其他未生效合同；
     * 抢占失败（房源已被他人承租或已下架）则本合同置为「已取消」、来源申请置为「已失效」。
     *
     * 未到成交时机不算错误，返回 NOT_READY；落选时数据已写入（由调用方决定是否抛业务异常，
     * 注意不能在本方法内抛 —— 会把落选的写操作一起回滚）
     *
     * @param contractId 合同编号
     * @return 成交判定结果
     */
    Activation tryActivateContract(Long contractId);

    /**
     * 获得某业主的合同列表（通过房源归属）
     *
     * @param ownerId 业主编号
     * @return 合同列表
     */
    List<ContractDO> getContractsByOwnerId(Long ownerId);

    /**
     * 获得合同分页（含首期账单缴费情况）
     *
     * @param pageReqVO 分页查询
     * @return 合同分页
     */
    PageResult<ContractRespVO> getContractRespPage(ContractPageReqVO pageReqVO);

    /**
     * 获得某业主的合同列表（含首期账单缴费情况）
     *
     * @param ownerId 业主编号
     * @return 合同列表
     */
    List<ContractRespVO> getOwnerContractRespList(Long ownerId);

    /**
     * 获得合同详情（含首期账单、房源、房东、租客信息），供管理端合同详情使用
     *
     * @param id 合同编号
     * @return 合同详情
     */
    ContractRespVO getContractResp(Long id);

    /**
     * 获得即将到期的合同列表（含房源地址，避免管理端只看到裸房源 ID）
     *
     * @param days 未来天数
     * @return 合同列表
     */
    List<ContractRespVO> getExpiringContractRespList(Integer days);

    /**
     * 获得合同详情（业主端），并校验该合同属于该业主的房源
     *
     * @param ownerId    业主编号
     * @param contractId 合同编号
     * @return 合同详情
     */
    ContractRespVO getOwnerContractDetail(Long ownerId, Long contractId);

    /**
     * 获得合同详情（租客端），并校验该合同属于该租客
     *
     * @param tenantId   租客编号
     * @param contractId 合同编号
     * @return 合同详情
     */
    ContractRespVO getTenantContractDetail(Long tenantId, Long contractId);

}
