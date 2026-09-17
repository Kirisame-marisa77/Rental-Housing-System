package cn.iocoder.yudao.module.rental.dal.dataobject.owner;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业主信息表
 *
 * @author yudao
 */
@TableName("rental_owner_info")
@Data
@EqualsAndHashCode(callSuper = true)
public class OwnerInfoDO extends BaseDO {

    /**
     * 主键
     */
    private Long id;
    /**
     * 关联会员用户 ID（member_user.id），未接入会员模块时为 null
     */
    private Long userId;
    /**
     * 业主姓名
     */
    private String name;
    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 银行卡号
     */
    private String bankCard;
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
     * 备注
     */
    private String remark;

}
