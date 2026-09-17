package cn.iocoder.yudao.module.rental.dal.mysql.bill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.bill.vo.PaymentRecordPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.bill.PaymentRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentRecordMapper extends BaseMapperX<PaymentRecordDO> {

    default PageResult<PaymentRecordDO> selectPage(PaymentRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PaymentRecordDO>()
                .eqIfPresent(PaymentRecordDO::getBillType, reqVO.getBillType())
                .eqIfPresent(PaymentRecordDO::getBillId, reqVO.getBillId())
                .orderByDesc(PaymentRecordDO::getId));
    }

}
