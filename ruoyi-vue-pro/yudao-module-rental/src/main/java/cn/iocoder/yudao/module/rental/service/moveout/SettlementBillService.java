package cn.iocoder.yudao.module.rental.service.moveout;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.moveout.vo.SettlementBillPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.moveout.SettlementBillDO;

/**
 * 退租结算单 Service 接口
 *
 * @author yudao
 */
public interface SettlementBillService {

    /**
     * 获得退租结算单分页
     *
     * @param pageReqVO 分页查询
     * @return 结算单分页
     */
    PageResult<SettlementBillDO> getSettlementBillPage(SettlementBillPageReqVO pageReqVO);

    /**
     * 获得退租结算单
     *
     * @param id 编号
     * @return 结算单
     */
    SettlementBillDO getSettlementBill(Long id);

}
