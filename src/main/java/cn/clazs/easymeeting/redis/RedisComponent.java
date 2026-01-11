package cn.clazs.easymeeting.redis;

import cn.clazs.easymeeting.constant.Constants;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisComponent {

    private final RedisUtil redisUtil;

    // ==================== 验证码相关 ====================

    public String saveCheckCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtil.set(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_EXPIRE_CHECK_CODE, TimeUnit.SECONDS);
        return checkCodeKey;
    }

    public String getCheckCode(String checkCodeKey) {
        Object value = redisUtil.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        return value == null ? null : value.toString();
    }

    public void clearCheckCode(String checkCodeKey) {
        redisUtil.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    // ==================== Token相关 ====================

    /**
     * 保存用户Token信息到Redis（双向映射）
     * token -> TokenUserInfoDTO
     * userId -> token
     */
    public void saveUserTokenInfo(UserTokenInfoDTO tokenUserInfo) {
        String token = tokenUserInfo.getToken();
        String userId = tokenUserInfo.getUserId();

        // 先删除该用户之前的token（实现单设备登录）
        String oldToken = getTokenByUserId(userId);
        if (oldToken != null) {
            removeToken(oldToken);
        }

        // 保存 token -> TokenUserInfoDTO
        redisUtil.set(Constants.REDIS_KEY_TOKEN + token, tokenUserInfo, Constants.REDIS_EXPIRE_TOKEN, TimeUnit.SECONDS);
        // 保存 userId -> token
        redisUtil.set(Constants.REDIS_KEY_USER_TOKEN + userId, token, Constants.REDIS_EXPIRE_TOKEN, TimeUnit.SECONDS);
    }

    /**
     * 根据Token获取用户会话信息
     */
    public UserTokenInfoDTO getUserTokenInfo(String token) {
        return (UserTokenInfoDTO) redisUtil.get(Constants.REDIS_KEY_TOKEN + token);
    }

    /**
     * 根据userId获取token
     */
    public String getTokenByUserId(String userId) {
        Object value = redisUtil.get(Constants.REDIS_KEY_USER_TOKEN + userId);
        return value == null ? null : value.toString();
    }

    /**
     * 更新Token用户信息（如进入会议时更新currentMeetingId）
     */
    public void updateUserTokenInfo(UserTokenInfoDTO tokenUserInfo) {
        String token = tokenUserInfo.getToken();
        // 获取剩余过期时间，保持原有过期时间
        Long ttl = redisUtil.getExpire(Constants.REDIS_KEY_TOKEN + token);
        if (ttl != null && ttl > 0) {
            redisUtil.set(Constants.REDIS_KEY_TOKEN + token, tokenUserInfo, ttl, TimeUnit.SECONDS);
        }
    }

    /**
     * 删除Token（退出登录/踢人下线）
     */
    public void removeToken(String token) {
        UserTokenInfoDTO tokenUserInfo = getUserTokenInfo(token);
        if (tokenUserInfo != null) {
            // 删除 userId -> token
            redisUtil.delete(Constants.REDIS_KEY_USER_TOKEN + tokenUserInfo.getUserId());
        }
        // 删除 token -> TokenUserInfoDTO
        redisUtil.delete(Constants.REDIS_KEY_TOKEN + token);
    }

    /**
     * 根据userId踢人下线
     */
    public void removeTokenByUserId(String userId) {
        String token = getTokenByUserId(userId);
        if (token != null) {
            redisUtil.delete(Constants.REDIS_KEY_TOKEN + token);
        }
        redisUtil.delete(Constants.REDIS_KEY_USER_TOKEN + userId);
    }

    /**
     * 验证Token是否存在
     */
    public boolean hasToken(String token) {
        return redisUtil.hasKey(Constants.REDIS_KEY_TOKEN + token);
    }

    /**
     * Token 自动续期
     * 当 Token 剩余有效期小于阈值时，自动延长有效期
     * @param token Token
     * @param renewThresholdSeconds 续期阈值（秒），剩余时间小于此值时触发续期
     * @return true 表示已续期，false 表示未续期
     */
    public boolean renewTokenIfNeeded(String token, long renewThresholdSeconds) {
        String tokenKey = Constants.REDIS_KEY_TOKEN + token;
        Long ttl = redisUtil.getExpire(tokenKey);

        if (ttl == null || ttl <= 0) {
            return false;
        }

        // 如果剩余时间小于阈值，则续期
        if (ttl < renewThresholdSeconds) {
            UserTokenInfoDTO userInfo = getUserTokenInfo(token);
            if (userInfo != null) {
                // 续期 token -> UserInfo
                redisUtil.expire(tokenKey, Constants.REDIS_EXPIRE_TOKEN, TimeUnit.SECONDS);
                // 续期 userId -> token
                redisUtil.expire(Constants.REDIS_KEY_USER_TOKEN + userInfo.getUserId(),
                        Constants.REDIS_EXPIRE_TOKEN, TimeUnit.SECONDS);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取 Token 剩余有效期（秒）
     */
    public Long getTokenTTL(String token) {
        return redisUtil.getExpire(Constants.REDIS_KEY_TOKEN + token);
    }
}
