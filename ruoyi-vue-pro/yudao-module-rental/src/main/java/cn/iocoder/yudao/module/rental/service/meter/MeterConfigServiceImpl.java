package cn.iocoder.yudao.module.rental.service.meter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterConfigSettingsReqVO;
import cn.iocoder.yudao.module.rental.controller.admin.meter.vo.MeterConfigSettingsRespVO;
import cn.iocoder.yudao.module.rental.dal.dataobject.meter.MeterConfigDO;
import cn.iocoder.yudao.module.rental.dal.mysql.meter.MeterConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 抄表配置 Service 实现类
 *
 * @author yudao
 */
@Service
public class MeterConfigServiceImpl implements MeterConfigService {

    @Resource
    private MeterConfigMapper meterConfigMapper;

    @Override
    public String getConfigValue(String key) {
        MeterConfigDO config = meterConfigMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public MeterConfigSettingsRespVO getSettings() {
        MeterConfigSettingsRespVO resp = new MeterConfigSettingsRespVO();
        resp.setWaterPrice(parseDecimal(getConfigValue("water_price")));
        resp.setElectricityPrice(parseDecimal(getConfigValue("electricity_price")));
        resp.setMeterPeriod(getConfigValue("meter_period"));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSettings(MeterConfigSettingsReqVO reqVO) {
        Long userId = getLoginUserId();
        if (reqVO.getWaterPrice() != null) {
            saveConfig("water_price", reqVO.getWaterPrice().toPlainString(), userId);
        }
        if (reqVO.getElectricityPrice() != null) {
            saveConfig("electricity_price", reqVO.getElectricityPrice().toPlainString(), userId);
        }
        if (StrUtil.isNotBlank(reqVO.getMeterPeriod())) {
            saveConfig("meter_period", reqVO.getMeterPeriod(), userId);
        }
    }

    private void saveConfig(String key, String value, Long userId) {
        MeterConfigDO existing = meterConfigMapper.selectByKey(key);
        if (existing != null) {
            existing.setConfigValue(value);
            existing.setEffectiveTime(LocalDateTime.now());
            existing.setConfigUser(userId);
            meterConfigMapper.updateById(existing);
        } else {
            MeterConfigDO config = new MeterConfigDO();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setEffectiveTime(LocalDateTime.now());
            config.setConfigUser(userId);
            meterConfigMapper.insert(config);
        }
    }

    private BigDecimal parseDecimal(String value) {
        return StrUtil.isBlank(value) ? BigDecimal.ZERO : new BigDecimal(value);
    }

}
