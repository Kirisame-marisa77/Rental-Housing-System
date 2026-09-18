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

    /**
     * 使某租客的全部 token 失效（注销账号时用）
     *
     * 只调 logout(token) 不够：同一个账号可能在多台设备 / 多个标签页登录，
     * 内存里存着多条 token → id 的映射。不按 id 全清，被注销的账号还能继续下单。
     */
    public void logoutAll(Long tenantId) {
        if (tenantId == null) {
            return;
        }
        tokenTenantMap.entrySet().removeIf(entry -> tenantId.equals(entry.getValue()));
    }

}
