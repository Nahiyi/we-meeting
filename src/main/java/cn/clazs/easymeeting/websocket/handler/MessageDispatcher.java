package cn.clazs.easymeeting.websocket.handler;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 消息分发器
 * 根据消息类型路由到对应的处理器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MessageDispatcher {

    private final HandlerRegistry handlerRegistry;
    private final BizChannelContext bizChannelContext;

    /**
     * 分发消息到对应的处理器
     */
    public void dispatch(ChannelHandlerContext ctx, MessageSendDTO message) {
        // 设置发送者信息（基于当前websocket <--> channel --> userInfo）
        UserTokenInfoDTO userInfo = bizChannelContext.getUserInfo(ctx.channel());
        if (userInfo != null) {
            message.setSendUserId(userInfo.getUserId());
            message.setSendUserNickName(userInfo.getNickName());
        }

        // 根据发送的消息类型，查找对应的处理器（策略模式）
        MessageType messageType = message.getMessageType();
        MessageTypeHandler handler = handlerRegistry
                .getHandler(messageType)
                .orElse(handlerRegistry.getDefaultHandler());

        if (handler != null) {
            log.debug("分发消息类型 {} 到处理器 {}", messageType, handler.getClass().getSimpleName());
            handler.handle(ctx, message);
        } else {
            log.warn("未找到消息类型 {} 的处理器，且无默认处理器", messageType);
        }
    }
}
