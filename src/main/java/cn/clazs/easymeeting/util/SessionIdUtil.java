package cn.clazs.easymeeting.util;

/**
 * 会话ID工具类
 * 用于生成和解析私聊会话ID
 */
public class SessionIdUtil {

    /**
     * 生成会话ID
     * 规则：两个用户ID按字典序排序后用下划线拼接
     * 确保 A→B 和 B→A 的消息存储在同一会话中
     *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 会话ID，格式：smallerId_largerId
     * @throws IllegalArgumentException 如果任一用户ID为空
     */
    public static String generateSessionId(String userId1, String userId2) {
        if (userId1 == null || userId1.isEmpty()) {
            throw new IllegalArgumentException("用户ID1不能为空");
        }
        if (userId2 == null || userId2.isEmpty()) {
            throw new IllegalArgumentException("用户ID2不能为空");
        }
        
        // 按字典序排序，确保 A→B 和 B→A 生成相同的 sessionId
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    /**
     * 从会话ID中解析出两个用户ID
     *
     * @param sessionId 会话ID
     * @return 用户ID数组 [smallerId, largerId]
     * @throws IllegalArgumentException 如果会话ID格式无效
     */
    public static String[] parseSessionId(String sessionId) {
        if (sessionId == null || !sessionId.contains("_")) {
            throw new IllegalArgumentException("无效的会话ID");
        }
        String[] parts = sessionId.split("_", 2);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("无效的会话ID格式");
        }
        return parts;
    }

    /**
     * 检查用户是否属于该会话
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 如果用户属于该会话返回 true
     */
    public static boolean isUserInSession(String sessionId, String userId) {
        if (sessionId == null || userId == null) {
            return false;
        }
        String[] userIds = parseSessionId(sessionId);
        return userId.equals(userIds[0]) || userId.equals(userIds[1]);
    }

    /**
     * 获取会话中的另一个用户ID
     *
     * @param sessionId 会话ID
     * @param userId    当前用户ID
     * @return 另一个用户ID，如果当前用户不在会话中返回 null
     */
    public static String getOtherUserId(String sessionId, String userId) {
        if (sessionId == null || userId == null) {
            return null;
        }
        String[] userIds = parseSessionId(sessionId);
        if (userId.equals(userIds[0])) {
            return userIds[1];
        } else if (userId.equals(userIds[1])) {
            return userIds[0];
        }
        return null;
    }
}
