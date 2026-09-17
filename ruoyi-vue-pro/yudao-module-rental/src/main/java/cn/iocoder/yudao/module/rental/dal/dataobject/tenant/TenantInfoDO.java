package cn.iocoder.yudao.module.rental.dal.dataobject.tenant;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租客信息表
 *
 * @author yudao
 */
@TableName("rental_tenant_info")
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantInfoDO extends BaseDO {

    /**
     * 主键
     */
    private Long id;
    /**
     * 关联会员用户 ID（member_user.id），未接入会员模块时为 null
     */
    private Long userId;
    /**
     * 租客姓名
     */
    private String name;
    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 登录密码（演示明文）
     */
    private String password;
    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;
    /**
     * 紧急联系人
     */
    private String emergencyContact;
    /**
     * 紧急联系电话
     */
    private String emergencyPhone;
    /**
     * 工作单位
     */
    private String workUnit;
    /**
     * 实名认证状态：0-未认证 1-认证中 2-已认证 3-认证失败 4-已锁定
     */
    private Integer authStatus;
    /**
     * 连续认证失败次数
     */
    private Integer authFailCount;
    /**
     * 最近认证时间
     */
    private LocalDateTime authTime;
    /**
     * 备注
     */
    private String remark;

}
