package cn.clazs.easymeeting.redis;

import cn.clazs.easymeeting.constant.Constants;
import cn.clazs.easymeeting.entity.dto.MeetingMemberDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    // ==================== 会议相关 ====================

    /**
     * 用户入会，添加一条缓存记录
     * 缓存结构：Hash
     * key        ----->    hashKey    ----->      field
     * 会议号(房间号)         入会用户ID             对应用户详情
     */
    public void addToMeeting(String meetingId, MeetingMemberDTO meetingMemberDTO) {
        redisUtil.hSet(Constants.REDIS_KEY_MEETING_ROOM + meetingId, meetingMemberDTO.getUserId(), meetingMemberDTO);
    }

    public List<MeetingMemberDTO> getMeetingMemberList(String meetingId) {
        // 获取会议室所有成员
        Map<Object, Object> members = redisUtil.hGetAll(Constants.REDIS_KEY_MEETING_ROOM + meetingId);

        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为 List
        return members.values().stream()
                .map(obj -> (MeetingMemberDTO) obj)
                .collect(Collectors.toList());
    }

    public MeetingMemberDTO getMeetingMember(String meetingId, String userId) {
        return (MeetingMemberDTO) redisUtil.hGet(Constants.REDIS_KEY_MEETING_ROOM + meetingId, userId);
    }

    /**
     * 从会议中移除单个成员
     */
    public void removeMeetingMember(String meetingId, String userId) {
        redisUtil.hDelete(Constants.REDIS_KEY_MEETING_ROOM + meetingId, userId);
    }

    /**
     * 清理整个会议的成员数据（结束会议时使用）
     */
    public void removeMeetingMembers(String meetingId) {
        redisUtil.delete(Constants.REDIS_KEY_MEETING_ROOM + meetingId);
    }

    /**
     * 指定用户退出指定会议，即删除哈希结构中的一条hashkey-field
     */
    public Boolean exitMeeting(String meetingId, String userId) {
        MeetingMemberDTO meetingMemberDTO = getMeetingMember(meetingId, userId);
        if (meetingMemberDTO == null) {
            return false;
        }
        // 从会议成员列表中移除该用户
        removeMeetingMember(meetingId, userId);
        return true;
    }

    /**
     * 添加会议邀请信息
     * @param meetingId 会议ID
     * @param userId 被邀请用户ID
     */
    public void addInviteInfo(String meetingId, String userId) {
        redisUtil.set(Constants.REDIS_KEY_MEETING_INVITE + userId + ":" + meetingId,
                meetingId, Constants.REDIS_EXPIRE_INVITE, TimeUnit.SECONDS);
    }

    /**
     * 获取会议邀请信息
     * @param meetingId 会议ID
     * @param userId 被邀请用户ID
     * @return 会议ID（如果邀请存在）
     */
    public String getInviteInfo(String meetingId, String userId) {
        return (String) redisUtil.get(Constants.REDIS_KEY_MEETING_INVITE + userId + ":" + meetingId);
    }

    /**
     * 删除会议邀请信息（接受邀请后删除）
     * @param meetingId 会议ID
     * @param userId 被邀请用户ID
     */
    public void removeInviteInfo(String meetingId, String userId) {
        redisUtil.delete(Constants.REDIS_KEY_MEETING_INVITE + userId + ":" + meetingId);
    }
}
