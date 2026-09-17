package cn.iocoder.yudao.module.rental.service.tenant;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 租客登录 Token 服务（内存版，课程演示用）
 *
 * @author yudao
 */
@Component
public class TenantAuthService {

    private final Map<String, Long> tokenTenantMap = new ConcurrentHashMap<>();

    public String createToken(Long tenantId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenTenantMap.put(token, tenantId);
        return token;
    }

    public Long getTenantId(String token) {
        return token == null ? null : tokenTenantMap.get(token);
    }

    public void logout(String token) {
        if (token != null) {
            tokenTenantMap.remove(token);
        }
    }

}
