package cn.clazs.easymeeting.websocket.handler;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.websocket.messaging.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * WebRTC 信令处理器
 * 处理 Offer/Answer/ICE Candidate 等信令类型的消息
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebRTCSignalingHandler implements MessageTypeHandler {

    private final MessageHandler messageHandler;

    @Override
    public List<MessageType> getSupportedTypes() {
        return Arrays.asList(
                MessageType.WEBRTC_OFFER,
                MessageType.WEBRTC_ANSWER,
                MessageType.WEBRTC_ICE_CANDIDATE
        );
    }

    @Override
    public void handle(ChannelHandlerContext ctx, MessageSendDTO message) {
        String targetUserId = message.getReceiveUserId();

        if (targetUserId == null || targetUserId.isEmpty()) {
            log.warn("WebRTC 信令消息缺少目标用户ID, type={}", message.getMessageType());
            return;
        }

        log.debug("转发 [WebRTC] {} -> {}, type={}, meetingId={}",
                message.getSendUserId(), targetUserId, message.getMessageType(), message.getMeetingId());

        // 点对点转发
        messageHandler.sendMessage(message);
        log.info("[WebRTC] 信令已转发到消息处理器");
    }
}
