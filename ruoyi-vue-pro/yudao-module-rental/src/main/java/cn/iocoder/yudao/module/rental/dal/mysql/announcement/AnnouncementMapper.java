package cn.iocoder.yudao.module.rental.dal.mysql.announcement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.AnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.announcement.vo.TenantAnnouncementPageReqVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.announcement.AnnouncementDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapperX<AnnouncementDO> {

    /**
     * 管理端：可按任意状态查询
     */
    default PageResult<AnnouncementDO> selectPage(AnnouncementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AnnouncementDO>()
                .likeIfPresent(AnnouncementDO::getTitle, reqVO.getTitle())
                .eqIfPresent(AnnouncementDO::getCategory, reqVO.getCategory())
                .eqIfPresent(AnnouncementDO::getStatus, reqVO.getStatus())
                .orderByDesc(AnnouncementDO::getIsTop)
                .orderByDesc(AnnouncementDO::getId));
    }

    /**
     * 租客端：只返回「已发布」公告
     *
     * status 硬编码在这里而不是靠 Controller 传参，草稿/已删除公告对租客永不可见。
     * 对应的 TenantAnnouncementPageReqVO 里也不提供 status 字段，从类型上再堵一层。
     */
    default PageResult<AnnouncementDO> selectPublishedPage(TenantAnnouncementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AnnouncementDO>()
                .eq(AnnouncementDO::getStatus, 1)
                .eqIfPresent(AnnouncementDO::getCategory, reqVO.getCategory())
                .orderByDesc(AnnouncementDO::getIsTop)
                .orderByDesc(AnnouncementDO::getId));
    }

}
