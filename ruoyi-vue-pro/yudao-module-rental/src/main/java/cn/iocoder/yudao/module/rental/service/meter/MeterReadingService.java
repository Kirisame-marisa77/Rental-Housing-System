package cn.iocoder.yudao.module.rental.service.meter;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterReadingSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.meter.MeterReadingDO;

/**
 * 抄表记录 Service 接口
 *
 * @author yudao
 */
public interface MeterReadingService {

    /**
     * 录入抄表记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMeterReading(MeterReadingSaveReqVO createReqVO);

    /**
     * 更新抄表记录
     *
     * @param updateReqVO 更新信息
     */
    void updateMeterReading(MeterReadingSaveReqVO updateReqVO);

    /**
     * 删除抄表记录
     *
     * @param id 编号
     */
    void deleteMeterReading(Long id);

    /**
     * 确认抄表并生成水电费账单
     *
     * @param id 抄表记录编号
     * @return 生成的水电费账单编号
     */
    Long confirmMeterReading(Long id);

    /**
     * 获得抄表记录分页
     *
     * @param pageReqVO 分页查询
     * @return 抄表记录分页
     */
    PageResult<MeterReadingDO> getMeterReadingPage(MeterReadingPageReqVO pageReqVO);

    /**
     * 获得抄表记录
     *
     * @param id 编号
     * @return 抄表记录
     */
    MeterReadingDO getMeterReading(Long id);

    /**
     * 获得最新抄表记录（用于获取上期读数）
     *
     * @param houseId    房源编号
     * @param contractId 合同编号
     * @param meterType  抄表类型
     * @return 最新抄表记录
     */
    MeterReadingDO getLatestMeterReading(Long houseId, Long contractId, Integer meterType);

    /**
     * 业主上传抄表读数（含截图，待审核）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMeterReadingByOwner(MeterReadingSaveReqVO createReqVO);

    /**
     * 审核抄表记录（通过则按房源计价规则生成水电费账单）
     *
     * @param id     抄表记录编号
     * @param pass   是否通过
     * @param reason 驳回原因
     * @return 生成的水电费账单编号（驳回时为 null）
     */
    Long reviewMeterReading(Long id, Boolean pass, String reason);

}
