package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.po.ChatMessage;
import cn.clazs.easymeeting.entity.vo.PageResult;

/**
 * 聊天消息服务接口
 * 自动处理分表路由，调用方无需关心分表逻辑
 */
public interface ChatMessageService {

    /**
     * 保存消息
     * 自动生成 messageId 和 sendTime，自动路由到正确的分表
     *
     * @param message 消息实体
     * @return 保存后的消息（包含生成的 messageId）
     */
    ChatMessage saveMessage(ChatMessage message);

    /**
     * 根据ID获取消息
     *
     * @param meetingId 会议ID（用于确定分表）
     * @param messageId 消息ID
     * @return 消息实体，不存在返回 null
     */
    ChatMessage getMessageById(String meetingId, Long messageId);

    /**
     * 获取会议消息列表（分页）
     *
     * @param meetingId 会议ID
     * @param pageNo    页码（从1开始）
     * @param pageSize  每页数量
     * @return 分页结果
     */
    PageResult<ChatMessage> getMessagesByMeetingId(String meetingId, Integer pageNo, Integer pageSize);

    /**
     * 更新消息
     *
     * @param message 消息实体（必须包含 meetingId 和 messageId）
     * @return 更新后的消息
     */
    ChatMessage updateMessage(ChatMessage message);

    /**
     * 删除消息（逻辑删除）
     *
     * @param meetingId 会议ID（用于确定分表）
     * @param messageId 消息ID
     */
    void deleteMessage(String meetingId, Long messageId);

    /**
     * 获取指定消息ID之前的消息（用于加载更多历史消息）
     *
     * @param meetingId    会议ID
     * @param maxMessageId 最大消息ID（查询比这个ID更早的消息）
     * @param pageSize     每页数量
     * @return 分页结果
     */
    PageResult<ChatMessage> getMessagesBeforeId(String meetingId, Long maxMessageId, Integer pageSize);

    /**
     * 获取会议消息列表（带私聊消息过滤）
     * 返回群发消息 + 与当前用户相关的私聊消息
     *
     * @param meetingId     会议ID
     * @param currentUserId 当前用户ID
     * @param pageNo        页码（从1开始）
     * @param pageSize      每页数量
     * @return 分页结果
     */
    PageResult<ChatMessage> getMessagesByMeetingId(String meetingId, String currentUserId, Integer pageNo, Integer pageSize);

    /**
     * 获取指定消息ID之前的消息（带私聊消息过滤）
     *
     * @param meetingId     会议ID
     * @param currentUserId 当前用户ID
     * @param maxMessageId  最大消息ID
     * @param pageSize      每页数量
     * @return 分页结果
     */
    PageResult<ChatMessage> getMessagesBeforeId(String meetingId, String currentUserId, Long maxMessageId, Integer pageSize);
}
