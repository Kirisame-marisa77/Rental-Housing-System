package cn.iocoder.yudao.module.rental.service.bill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.PaymentRecordPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.PaymentRecordDO;
import cn.iocoder.yudao.module.rental.dal.mysql.bill.PaymentRecordMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 缴费记录 Service 实现类
 *
 * @author yudao
 */
@Service
public class PaymentRecordServiceImpl implements PaymentRecordService {

    @Resource
    private PaymentRecordMapper paymentRecordMapper;

    @Override
    public PageResult<PaymentRecordDO> getPaymentRecordPage(PaymentRecordPageReqVO pageReqVO) {
        return paymentRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public PaymentRecordDO getPaymentRecord(Long id) {
        return paymentRecordMapper.selectById(id);
    }

}
