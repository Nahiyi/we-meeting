package cn.clazs.easymeeting.websocket.handler;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.websocket.messaging.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 默认转发处理器
 * 处理不需要特殊业务逻辑的消息，直接转发
 */
@Component
@Order // 默认最低优先级，作为默认处理器
@Slf4j
@RequiredArgsConstructor
public class DefaultForwardHandler implements MessageTypeHandler {

    private final MessageHandler messageHandler;

    @Override
    public List<MessageType> getSupportedTypes() {
        return Arrays.asList(
                MessageType.CHAT_TEXT_MESSAGE,
                MessageType.CHAT_MEDIA_MESSAGE,
                MessageType.CHAT_MEDIA_MESSAGE_UPDATE,
                MessageType.MEETING_USER_VIDEO_CHANGE,
                MessageType.INVITE_MESSAGE_MEETING,
                MessageType.USER_CONTACT_APPLY,
                MessageType.ADD_MEETING_ROOM,
                MessageType.PEER
        );
    }

    @Override
    public void handle(ChannelHandlerContext ctx, MessageSendDTO message) {
        log.debug("默认转发消息: type={}", message.getMessageType());
        messageHandler.sendMessage(message);
    }
}
