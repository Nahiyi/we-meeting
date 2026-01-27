package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息 Mapper 接口
 * 支持动态表名，用于分表查询
 */
@Mapper
public interface ChatMessageMapper {

    /**
     * 插入消息
     *
     * @param tableName 分表名
     * @param message   消息实体
     * @return 影响行数
     */
    int insert(@Param("tableName") String tableName,
               @Param("message") ChatMessage message);

    /**
     * 根据消息ID查询
     *
     * @param tableName 分表名
     * @param messageId 消息ID
     * @return 消息实体
     */
    ChatMessage selectById(@Param("tableName") String tableName,
                           @Param("messageId") Long messageId);

    /**
     * 根据会议ID查询消息列表（分页，按发送时间倒序）
     *
     * @param tableName 分表名
     * @param meetingId 会议ID
     * @param offset    偏移量
     * @param limit     每页数量
     * @return 消息列表
     */
    List<ChatMessage> selectByMeetingId(@Param("tableName") String tableName,
                                        @Param("meetingId") String meetingId,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    /**
     * 统计会议消息数量
     *
     * @param tableName 分表名
     * @param meetingId 会议ID
     * @return 消息数量
     */
    Long countByMeetingId(@Param("tableName") String tableName,
                          @Param("meetingId") String meetingId);

    /**
     * 更新消息
     *
     * @param tableName 分表名
     * @param message   消息实体
     * @return 影响行数
     */
    int updateById(@Param("tableName") String tableName,
                   @Param("message") ChatMessage message);

    /**
     * 删除消息（逻辑删除，将 status 设为 1）
     *
     * @param tableName 分表名
     * @param messageId 消息ID
     * @return 影响行数
     */
    int deleteById(@Param("tableName") String tableName,
                   @Param("messageId") Long messageId);

    /**
     * 查询指定消息ID之前的消息（用于加载更多历史消息）
     *
     * @param tableName    分表名
     * @param meetingId    会议ID
     * @param maxMessageId 最大消息ID（查询比这个ID更早的消息）
     * @param limit        数量限制
     * @return 消息列表
     */
    List<ChatMessage> selectBeforeMessageId(@Param("tableName") String tableName,
                                            @Param("meetingId") String meetingId,
                                            @Param("maxMessageId") Long maxMessageId,
                                            @Param("limit") Integer limit);

    /**
     * 根据会议ID查询消息列表（带私聊消息过滤）
     * 返回群发消息 + 与当前用户相关的私聊消息（发送者或接收者为当前用户）
     *
     * @param tableName     分表名
     * @param meetingId     会议ID
     * @param currentUserId 当前用户ID
     * @param offset        偏移量
     * @param limit         每页数量
     * @return 消息列表
     */
    List<ChatMessage> selectByMeetingIdWithPrivateFilter(@Param("tableName") String tableName,
                                                         @Param("meetingId") String meetingId,
                                                         @Param("currentUserId") String currentUserId,
                                                         @Param("offset") Integer offset,
                                                         @Param("limit") Integer limit);

    /**
     * 统计会议消息数量（带私聊消息过滤）
     *
     * @param tableName     分表名
     * @param meetingId     会议ID
     * @param currentUserId 当前用户ID
     * @return 消息数量
     */
    Long countByMeetingIdWithPrivateFilter(@Param("tableName") String tableName,
                                           @Param("meetingId") String meetingId,
                                           @Param("currentUserId") String currentUserId);

    /**
     * 查询指定消息ID之前的消息（带私聊消息过滤）
     *
     * @param tableName     分表名
     * @param meetingId     会议ID
     * @param currentUserId 当前用户ID
     * @param maxMessageId  最大消息ID
     * @param limit         数量限制
     * @return 消息列表
     */
    List<ChatMessage> selectBeforeMessageIdWithPrivateFilter(@Param("tableName") String tableName,
                                                             @Param("meetingId") String meetingId,
                                                             @Param("currentUserId") String currentUserId,
                                                             @Param("maxMessageId") Long maxMessageId,
                                                             @Param("limit") Integer limit);
}
