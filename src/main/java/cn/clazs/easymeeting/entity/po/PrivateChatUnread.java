package cn.clazs.easymeeting.entity.po;

import lombok.Data;

/**
 * 私聊未读消息计数实体类
 * 对应表 private_chat_unread
 */
@Data
public class PrivateChatUnread {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（消息接收者）
     */
    private String userId;

    /**
     * 联系人ID（消息发送者）
     */
    private String contactId;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 最后消息时间戳
     */
    private Long lastMessageTime;

    /**
     * 最后消息内容预览
     */
    private String lastMessageContent;
}
