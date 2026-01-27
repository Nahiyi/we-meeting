package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.po.PrivateChatMessage;
import cn.clazs.easymeeting.entity.po.PrivateChatUnread;
import cn.clazs.easymeeting.entity.vo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 私聊消息服务接口
 * 处理好友之间的一对一私聊消息
 */
public interface PrivateChatService {

    /**
     * 发送私聊消息
     * 自动处理：好友关系验证、Session_Id生成、消息保存、WebSocket推送、未读计数
     *
     * @param message 消息实体（需包含 sendUserId、receiveUserId、messageType、messageContent）
     * @return 保存后的消息（包含生成的 messageId 和 sessionId）
     */
    PrivateChatMessage sendMessage(PrivateChatMessage message);

    /**
     * 获取与好友的聊天记录（分页）
     *
     * @param userId    当前用户ID
     * @param contactId 联系人ID
     * @param pageNo    页码（从1开始）
     * @param pageSize  每页数量
     * @return 分页结果
     */
    PageResult<PrivateChatMessage> getChatHistory(String userId, String contactId, Integer pageNo, Integer pageSize);

    /**
     * 获取指定消息ID之前的消息（加载更多历史消息）
     *
     * @param userId       当前用户ID
     * @param contactId    联系人ID
     * @param maxMessageId 最大消息ID
     * @param pageSize     每页数量
     * @return 分页结果
     */
    PageResult<PrivateChatMessage> getMessagesBeforeId(String userId, String contactId, Long maxMessageId, Integer pageSize);

    /**
     * 获取单个会话的未读消息数
     *
     * @param userId    用户ID
     * @param contactId 联系人ID
     * @return 未读消息数
     */
    Integer getUnreadCount(String userId, String contactId);

    /**
     * 标记消息已读（清零未读计数）
     *
     * @param userId    用户ID
     * @param contactId 联系人ID
     */
    void markAsRead(String userId, String contactId);

    /**
     * 获取用户所有会话的未读消息数量
     *
     * @param userId 用户ID
     * @return Map<联系人ID, 未读数>
     */
    Map<String, Integer> getAllUnreadCounts(String userId);

    /**
     * 获取用户所有会话的未读消息记录
     *
     * @param userId 用户ID
     * @return 未读消息记录列表
     */
    List<PrivateChatUnread> getUnreadList(String userId);
}
