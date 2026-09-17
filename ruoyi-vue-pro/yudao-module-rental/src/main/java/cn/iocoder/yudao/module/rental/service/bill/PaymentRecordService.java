package cn.iocoder.yudao.module.rental.service.bill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.PaymentRecordPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.PaymentRecordDO;

/**
 * 缴费记录 Service 接口
 *
 * @author yudao
 */
public interface PaymentRecordService {

    /**
     * 获得缴费记录分页
     *
     * @param pageReqVO 分页查询
     * @return 缴费记录分页
     */
    PageResult<PaymentRecordDO> getPaymentRecordPage(PaymentRecordPageReqVO pageReqVO);

    /**
     * 获得缴费记录
     *
     * @param id 编号
     * @return 缴费记录
     */
    PaymentRecordDO getPaymentRecord(Long id);

}
