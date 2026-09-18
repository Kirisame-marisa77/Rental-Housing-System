package cn.iocoder.yudao.module.rental.service.house;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseDetailRespVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HousePageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.house.vo.HouseSaveReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.house.HouseDO;

/**
 * 房源 Service 接口
 *
 * @author yudao
 */
public interface HouseService {

    /**
     * 创建房源
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHouse(HouseSaveReqVO createReqVO);

    /**
     * 更新房源
     *
     * @param updateReqVO 更新信息
     */
    void updateHouse(HouseSaveReqVO updateReqVO);

    /**
     * 删除房源
     *
     * @param id 编号
     */
    void deleteHouse(Long id);

    /**
     * 获得房源分页
     *
     * @param pageReqVO 分页查询
     * @return 房源分页
     */
    PageResult<HouseDO> getHousePage(HousePageReqVO pageReqVO);

    /**
     * 获得房源
     *
     * @param id 编号
     * @return 房源
     */
    HouseDO getHouse(Long id);

    /**
     * 业主上传房源（待审核）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHouseByOwner(HouseSaveReqVO createReqVO);

    /**
     * 审核房源
     *
     * @param id     房源编号
     * @param pass   是否通过
     * @param reason 驳回原因
     */
    void reviewHouse(Long id, Boolean pass, String reason);

    /**
     * 获得租客端的房源详情，含图片与房东信息
     *
     * 已下架的房源一律按「不存在」处理，不对外泄露其存在性。
     *
     * @param id 房源编号
     * @return 房源详情
     */
    HouseDetailRespVO getTenantHouseDetail(Long id);

    /**
     * 业主上架 / 下架自己的房源
     *
     * 只允许在「0-下架」与「1-上架」之间切换；「2-已锁定（签约中）」「3-已出租」
     * 由签约与退租流程驱动，不接受手动修改，否则会和先到先得的抢占逻辑打架。
     *
     * @param ownerId 业主编号
     * @param houseId 房源编号
     * @param online  true-上架，false-下架
     */
    void updateHouseStatusByOwner(Long ownerId, Long houseId, Boolean online);

}
