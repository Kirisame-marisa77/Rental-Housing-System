package cn.iocoder.yudao.module.rental.service.apply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplyRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.apply.vo.ApplySaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.apply.ApplyDO;

/**
 * 租房申请 Service 接口
 *
 * @author yudao
 */
public interface ApplyService {

    /**
     * 创建租房申请
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createApply(ApplySaveReqVO createReqVO);

    /**
     * 获得租房申请分页
     *
     * @param pageReqVO 分页查询
     * @return 租房申请分页
     */
    PageResult<ApplyDO> getApplyPage(ApplyPageReqVO pageReqVO);

    /**
     * 获得租房申请分页（含房源、房东、租客信息）
     *
     * @param pageReqVO 分页查询
     * @return 租房申请分页
     */
    PageResult<ApplyRespVO> getApplyPageWithDetail(ApplyPageReqVO pageReqVO);

    /**
     * 获得某房东收到的租房申请分页（按房源归属过滤）
     *
     * @param ownerId   房东编号
     * @param pageReqVO 分页查询
     * @return 租房申请分页
     */
    PageResult<ApplyRespVO> getOwnerApplyPage(Long ownerId, ApplyPageReqVO pageReqVO);

    /**
     * 获得租房申请
     *
     * @param id 编号
     * @return 租房申请
     */
    ApplyDO getApply(Long id);

    /**
     * 房东审批租房申请
     *
     * 同意：生成待签署合同与首期账单（押金 + 首月租金），房源保持「上架」不变
     * 驳回：仅置为「已驳回」并记录原因，无其它副作用
     *
     * @param ownerId 房东编号
     * @param id      申请编号
     * @param pass    是否同意
     * @param reason  驳回原因
     */
    void ownerApproveApply(Long ownerId, Long id, Boolean pass, String reason);

}
