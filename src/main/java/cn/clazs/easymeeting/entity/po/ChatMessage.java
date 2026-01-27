package cn.clazs.easymeeting.entity.po;

import lombok.Data;

/**
 * 聊天消息实体类
 * 对应分表 message_chat_message_XX (XX = 01-32)
 * 使用 meeting_id 作为分表键，通过 MurmurHash 算法路由到对应分表
 */
@Data
public class ChatMessage {

    /**
     * 消息ID，主键，使用雪花算法生成
     */
    private Long messageId;

    /**
     * 会议ID，分表键，10位字符串
     */
    private String meetingId;

    /**
     * 消息类型
     * 5 - 文本消息 (CHAT_TEXT_MESSAGE)
     * 6 - 媒体消息 (CHAT_MEDIA_MESSAGE)
     * @see cn.clazs.easymeeting.entity.enums.MessageType
     */
    private Integer messageType;

    /**
     * 消息内容，最大500字符
     */
    private String messageContent;

    /**
     * 发送者用户ID
     */
    private String sendUserId;

    /**
     * 发送者昵称
     */
    private String sendUserNickName;

    /**
     * 发送时间戳（毫秒）
     */
    private Long sendTime;

    /**
     * 接收类型
     * 0 - 群发（发送给会议中所有人）
     * 1 - 私聊（发送给指定用户）
     */
    private Integer receiveType;

    /**
     * 接收者用户ID（私聊时使用）
     */
    private String receiveUserId;

    /**
     * 文件大小（字节，媒体消息时使用）
     */
    private Long fileSize;

    /**
     * 文件名（媒体消息时使用）
     */
    private String fileName;

    /**
     * 文件类型（媒体消息时使用）
     */
    private Integer fileType;

    /**
     * 文件后缀（媒体消息时使用，如 jpg、mp4）
     */
    private String fileSuffix;

    /**
     * 消息状态
     * 0 - 正在发送
     * 1 - 发送完毕
     * @see cn.clazs.easymeeting.entity.enums.ChatMessageStatus
     */
    private Integer status;

    /**
     * 调试方法，返回核心字段的格式化字符串
     * 格式：某人（某人id）发送了一条 什么类型的消息，接受者为某人（某人id）
     */
    @Deprecated
    public String debugField() {
        // 处理消息类型显示

        // 处理接收者显示
        String receiverStr;
        if (receiveType != null && receiveType == 0) {
            receiverStr = "所有人";
        } else {
            receiverStr = receiveUserId != null ? receiveUserId : "未知用户";
        }

        return String.format("%s（%s）发送了 [%s]，接受者为%s（%s）",
                sendUserNickName != null ? sendUserNickName : "未知用户",
                sendUserId != null ? sendUserId : "未知ID",
                messageContent,
                receiverStr,
                receiveUserId != null ? receiveUserId : "未知ID");
    }
}
