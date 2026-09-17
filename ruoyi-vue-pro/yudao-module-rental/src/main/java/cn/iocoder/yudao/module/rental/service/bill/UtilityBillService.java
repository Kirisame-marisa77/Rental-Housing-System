package cn.iocoder.yudao.module.rental.service.bill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.UtilityBillSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.UtilityBillDO;

/**
 * 水电费账单 Service 接口
 *
 * @author yudao
 */
public interface UtilityBillService {

    /**
     * 创建水电费账单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUtilityBill(UtilityBillSaveReqVO createReqVO);

    /**
     * 更新水电费账单
     *
     * @param updateReqVO 更新信息
     */
    void updateUtilityBill(UtilityBillSaveReqVO updateReqVO);

    /**
     * 删除水电费账单
     *
     * @param id 编号
     */
    void deleteUtilityBill(Long id);

    /**
     * 缴费登记
     *
     * @param id            账单编号
     * @param payMethod     缴费方式
     * @param payee         收款人 ID
     * @param transactionNo 交易流水号
     * @param remark        备注
     */
    void payUtilityBill(Long id, String payMethod, Long payee, String transactionNo, String remark);

    /**
     * 获得水电费账单分页
     *
     * @param pageReqVO 分页查询
     * @return 账单分页
     */
    PageResult<UtilityBillDO> getUtilityBillPage(UtilityBillPageReqVO pageReqVO);

    /**
     * 获得水电费账单分页（含房源地址展示字段）
     *
     * @param pageReqVO 分页查询
     * @return 账单分页
     */
    PageResult<UtilityBillRespVO> getUtilityBillRespPage(UtilityBillPageReqVO pageReqVO);

    /**
     * 获得业主名下房源的水电费账单分页（业主端「我的账单」，只读）
     *
     * 账单表本身没有 owner_id，只能按「业主名下的房源 id 集合」过滤。
     * 业主没有任何房源时必须直接返回空页：inIfPresent 在集合为空时不拼条件，会把全系统账单返回给他。
     *
     * @param ownerId   业主编号
     * @param pageReqVO 分页查询
     * @return 账单分页
     */
    PageResult<UtilityBillRespVO> getOwnerUtilityBillPage(Long ownerId, UtilityBillPageReqVO pageReqVO);

    /**
     * 获得水电费账单
     *
     * @param id 编号
     * @return 账单
     */
    UtilityBillDO getUtilityBill(Long id);

}
