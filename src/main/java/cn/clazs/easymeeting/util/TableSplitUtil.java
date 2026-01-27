package cn.clazs.easymeeting.util;

import java.nio.charset.StandardCharsets;

/**
 * 分表工具类
 * 使用 MurmurHash3 算法实现均匀分布，确保数据在各分表间均匀分配
 */
public class TableSplitUtil {

    /**
     * 聊天消息分表前缀
     */
    public static final String SPLIT_TABLE_CHAT_MESSAGE = "message_chat_message";

    /**
     * 私聊消息分表前缀
     */
    public static final String SPLIT_TABLE_PRIVATE_CHAT_MESSAGE = "private_chat_message";

    /**
     * 默认分表数量
     */
    public static final Integer DEFAULT_TABLE_COUNT = 32;

    /**
     * 创建分表 SQL 模板
     */
    private static final String CREATE_TABLE_TEMPLATE = "CREATE TABLE IF NOT EXISTS %s LIKE %s;";


    /**
     * 根据 meeting_id 获取分表名（使用默认配置）
     *
     * @param meetingId 会议ID
     * @return 分表名，如 message_chat_message_15
     */
    public static String getTableName(String meetingId) {
        return getTableName(SPLIT_TABLE_CHAT_MESSAGE, DEFAULT_TABLE_COUNT, meetingId);
    }

    /**
     * 根据 sessionId 获取私聊消息分表名
     *
     * @param sessionId 会话ID（两个用户ID按字典序拼接）
     * @return 分表名，如 private_chat_message_15
     */
    public static String getPrivateChatTableName(String sessionId) {
        return getTableName(SPLIT_TABLE_PRIVATE_CHAT_MESSAGE, DEFAULT_TABLE_COUNT, sessionId);
    }

    /**
     * 根据两个用户ID获取私聊消息分表名
     *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 分表名，如 private_chat_message_15
     */
    public static String getPrivateChatTableName(String userId1, String userId2) {
        String sessionId = generateSessionId(userId1, userId2);
        return getPrivateChatTableName(sessionId);
    }

    /**
     * 生成会话ID（两个用户ID按字典序拼接）
     *
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @return 会话ID
     */
    private static String generateSessionId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    /**
     * 根据分表键获取表名
     *
     * @param prefix     表名前缀
     * @param tableCount 分表总数
     * @param key        分表键（如 meeting_id）
     * @return 分表名，带零填充后缀
     */
    public static String getTableName(String prefix, Integer tableCount, String key) {
        int hashCode = Math.abs(murmurHash3(key, 0));
        // 处理 Integer.MIN_VALUE 的特殊情况
        if (hashCode == Integer.MIN_VALUE) {
            hashCode = 0;
        }
        int tableNum = hashCode % tableCount + 1;
        int padLen = String.valueOf(tableCount).length();
        return prefix + "_" + String.format("%0" + padLen + "d", tableNum);
    }

    /**
     * 根据整数 key 获取表名（兼容旧接口）
     *
     * @param prefix     表名前缀
     * @param tableCount 分表总数
     * @param key        整数分表键
     * @return 分表名
     */
    public static String getTableName(String prefix, Integer tableCount, Integer key) {
        return getTableName(prefix, tableCount, String.valueOf(key));
    }

    /**
     * 生成创建单个分表的 SQL
     *
     * @param templateTableName 模板表名
     * @param tableIndex        分表索引（从1开始）
     * @param tableCount        分表总数
     * @return CREATE TABLE SQL 语句
     */
    public static String getCreateTableSql(String templateTableName, Integer tableIndex, Integer tableCount) {
        int padLen = String.valueOf(tableCount).length();
        String tableName = templateTableName + "_" + String.format("%0" + padLen + "d", tableIndex);
        return String.format(CREATE_TABLE_TEMPLATE, tableName, templateTableName);
    }

    /**
     * 生成所有分表的创建 SQL
     *
     * @param templateTableName 模板表名
     * @param tableCount        分表总数
     * @return 所有分表的 CREATE TABLE SQL 数组
     */
    public static String[] getAllCreateTableSql(String templateTableName, Integer tableCount) {
        String[] sqls = new String[tableCount];
        for (int i = 1; i <= tableCount; i++) {
            sqls[i - 1] = getCreateTableSql(templateTableName, i, tableCount);
        }
        return sqls;
    }

    /**
     * 生成聊天消息分表的所有创建 SQL（使用默认配置）
     *
     * @return 32个分表的 CREATE TABLE SQL 数组
     */
    public static String[] getChatMessageCreateTableSql() {
        return getAllCreateTableSql(SPLIT_TABLE_CHAT_MESSAGE, DEFAULT_TABLE_COUNT);
    }

    /**
     * 生成私聊消息分表的所有创建 SQL（使用默认配置）
     *
     * @return 32个分表的 CREATE TABLE SQL 数组
     */
    public static String[] getPrivateChatMessageCreateTableSql() {
        return getAllCreateTableSql(SPLIT_TABLE_PRIVATE_CHAT_MESSAGE, DEFAULT_TABLE_COUNT);
    }

    /**
     * MurmurHash3 32-bit 实现
     * 基于 Austin Appleby 的 MurmurHash3 算法，具有良好的分布性和性能
     *
     * @param key  输入字符串
     * @param seed 种子值
     * @return 32位哈希值
     */
    public static int murmurHash3(String key, int seed) {
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        int len = data.length;
        int h1 = seed;

        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;

        int roundedEnd = (len & 0xfffffffc); // round down to 4 byte block

        for (int i = 0; i < roundedEnd; i += 4) {
            int k1 = (data[i] & 0xff)
                    | ((data[i + 1] & 0xff) << 8)
                    | ((data[i + 2] & 0xff) << 16)
                    | (data[i + 3] << 24);

            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // tail
        int k1 = 0;
        switch (len & 0x03) {
            case 3:
                k1 = (data[roundedEnd + 2] & 0xff) << 16;
                // fall through
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xff) << 8;
                // fall through
            case 1:
                k1 |= (data[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = Integer.rotateLeft(k1, 15);
                k1 *= c2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= len;
        h1 = fmix32(h1);

        return h1;
    }

    /**
     * MurmurHash3 finalization mix
     */
    private static int fmix32(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }
}
