package cn.iocoder.yudao.module.rental.service.owner;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业主登录 Token 服务（内存版，课程演示用）
 *
 * @author yudao
 */
@Component
public class OwnerAuthService {

    private final Map<String, Long> tokenOwnerMap = new ConcurrentHashMap<>();

    /**
     * 创建 Token
     *
     * @param ownerId 业主 ID
     * @return Token
     */
    public String createToken(Long ownerId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenOwnerMap.put(token, ownerId);
        return token;
    }

    /**
     * 根据 Token 获得业主 ID
     *
     * @param token Token
     * @return 业主 ID，不存在返回 null
     */
    public Long getOwnerId(String token) {
        return token == null ? null : tokenOwnerMap.get(token);
    }

    /**
     * 退出登录
     *
     * @param token Token
     */
    public void logout(String token) {
        if (token != null) {
            tokenOwnerMap.remove(token);
        }
    }

}
