package cn.iocoder.yudao.module.rental.service.bill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.RentBillSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.RentBillDO;
import cn.iocoder.yudao.module.rental.dal.dataobject.contract.ContractDO;

/**
 * 租金账单 Service 接口
 *
 * @author yudao
 */
public interface RentBillService {

    /**
     * 创建租金账单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRentBill(RentBillSaveReqVO createReqVO);

    /**
     * 更新租金账单
     *
     * @param updateReqVO 更新信息
     */
    void updateRentBill(RentBillSaveReqVO updateReqVO);

    /**
     * 删除租金账单
     *
     * @param id 编号
     */
    void deleteRentBill(Long id);

    /**
     * 获得租金账单分页
     *
     * @param pageReqVO 分页查询
     * @return 租金账单分页
     */
    PageResult<RentBillDO> getRentBillPage(RentBillPageReqVO pageReqVO);

    /**
     * 获得租金账单分页（含房源地址展示字段）
     *
     * @param pageReqVO 分页查询
     * @return 租金账单分页
     */
    PageResult<RentBillRespVO> getRentBillRespPage(RentBillPageReqVO pageReqVO);

    /**
     * 获得业主名下房源的租金账单分页（业主端「我的账单」，只读）
     *
     * 账单表本身没有 owner_id，只能按「业主名下的房源 id 集合」过滤。
     * 业主没有任何房源时必须直接返回空页：inIfPresent 在集合为空时不拼条件，会把全系统账单返回给他。
     *
     * @param ownerId   业主编号
     * @param pageReqVO 分页查询
     * @return 租金账单分页
     */
    PageResult<RentBillRespVO> getOwnerRentBillPage(Long ownerId, RentBillPageReqVO pageReqVO);

    /**
     * 获得租金账单
     *
     * @param id 编号
     * @return 租金账单
     */
    RentBillDO getRentBill(Long id);

    /**
     * 为合同生成首期账单（押金 + 首月租金）
     *
     * 房东同意租房申请后调用，账单处于「待缴费」；租客缴费后合同才具备生效条件
     *
     * @param contract 合同
     * @return 账单编号
     */
    Long createFirstRentBillForContract(ContractDO contract);

    /**
     * 租客缴纳租金账单
     *
     * 缴费成功后若合同已双方签署，则自动尝试让合同生效（先到先得）
     *
     * @param id            账单编号
     * @param tenantId      租客编号，用于校验归属
     * @param payMethod     缴费方式：现金/微信/支付宝/银行转账
     * @param payee         收款人 ID
     * @param transactionNo 在线支付流水号
     * @param remark        备注
     */
    void payRentBill(Long id, Long tenantId, String payMethod, Long payee, String transactionNo, String remark);

}
