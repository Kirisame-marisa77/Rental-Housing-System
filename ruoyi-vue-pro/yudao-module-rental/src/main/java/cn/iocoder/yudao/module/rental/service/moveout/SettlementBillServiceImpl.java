package cn.iocoder.yudao.module.rental.service.moveout;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.SettlementBillPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.SettlementBillDO;
import cn.iocoder.yudao.module.rental.dal.mysql.moveout.SettlementBillMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 退租结算单 Service 实现类
 *
 * @author yudao
 */
@Service
public class SettlementBillServiceImpl implements SettlementBillService {

    @Resource
    private SettlementBillMapper settlementBillMapper;

    @Override
    public PageResult<SettlementBillDO> getSettlementBillPage(SettlementBillPageReqVO pageReqVO) {
        return settlementBillMapper.selectPage(pageReqVO);
    }

    @Override
    public SettlementBillDO getSettlementBill(Long id) {
        return settlementBillMapper.selectById(id);
    }

}
