package cn.iocoder.yudao.module.rental.service.meter;

import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterConfigSettingsReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterConfigSettingsRespVO;

/**
 * 抄表配置 Service 接口
 *
 * @author yudao
 */
public interface MeterConfigService {

    /**
     * 获得配置项值
     *
     * @param key 配置项：meter_period / water_price / electricity_price
     * @return 配置值，不存在返回 null
     */
    String getConfigValue(String key);

    /**
     * 获得抄表配置（周期、单价）
     *
     * @return 配置
     */
    MeterConfigSettingsRespVO getSettings();

    /**
     * 更新抄表配置
     *
     * @param reqVO 配置
     */
    void updateSettings(MeterConfigSettingsReqVO reqVO);

}
