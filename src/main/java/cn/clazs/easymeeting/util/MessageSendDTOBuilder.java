package cn.clazs.easymeeting.util;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.enums.MessageSendToType;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.entity.enums.ReceiveType;
import cn.clazs.easymeeting.entity.po.ChatMessage;

/**
 * 消息发送DTO构建器
 * 统一构建 WebSocket 推送的消息对象
 */
public class MessageSendDTOBuilder {

    /**
     * 从会议消息构建推送DTO
     * 根据 receiveType 设置正确的 messageSendToType：
     * - receiveType=0(群发) -> messageSendToType=1(GROUP)
     * - receiveType=1(私聊) -> messageSendToType=0(USER)
     *
     * @param message 会议消息实体
     * @return WebSocket 推送 DTO
     */
    public static MessageSendDTO fromChatMessage(ChatMessage message) {
        MessageSendDTO dto = new MessageSendDTO();

        // 设置基本消息信息
        dto.setMessageId(message.getMessageId());
        dto.setMeetingId(message.getMeetingId());
        dto.setMessageType(MessageType.getByType(message.getMessageType()));
        dto.setSendUserId(message.getSendUserId());
        dto.setSendUserNickName(message.getSendUserNickName());
        dto.setMessageContent(message.getMessageContent());
        dto.setSendTime(message.getSendTime());
        dto.setStatus(message.getStatus());

        // 根据接收类型设置推送目标
        if (ReceiveType.USER.getType().equals(message.getReceiveType())) {
            // 私聊消息：发送给指定用户
            dto.setMessageSendToType(MessageSendToType.USER);
            dto.setReceiveUserId(message.getReceiveUserId());
        } else {
            // 群发消息：发送给会议房间所有成员
            dto.setMessageSendToType(MessageSendToType.GROUP);
        }

        // 媒体消息附加文件信息
        if (MessageType.CHAT_MEDIA_MESSAGE.getType().equals(message.getMessageType())) {
            dto.setFileName(message.getFileName());
            dto.setFileType(message.getFileType());
            dto.setFileSize(message.getFileSize());
        }

        return dto;
    }
}
