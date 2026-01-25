package cn.clazs.easymeeting.websocket.netty.handler;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MessageSendToType;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import cn.clazs.easymeeting.websocket.handler.MessageDispatcher;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于TextWebSocketFrame处理text文本消息的自定义websocket下一级处理器
 */
@Component
@ChannelHandler.Sharable
@Slf4j
@RequiredArgsConstructor
public class TextBizWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final BizChannelContext bizChannelContext;
    private final MessageDispatcher messageDispatcher;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入，ChannelId: {}", ctx.channel().id().asShortText());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 监听 WebSocket 握手完成事件
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("WebSocket 握手完成，ChannelId: {}", ctx.channel().id().asShortText());

            // 获取用户信息（在 TokenValidationHandler 中已设置）
            UserTokenInfoDTO userInfo = bizChannelContext.getUserInfo(ctx.channel());
            if (userInfo != null && userInfo.getCurrentMeetingId() != null) {
                String meetingId = userInfo.getCurrentMeetingId();
                String userId = userInfo.getUserId();
                String nickName = userInfo.getNickName();

                log.info("用户 {} 在会议 {} 中，广播成员列表", nickName, meetingId);

                // 广播成员列表给房间内所有用户（包括新加入的用户）
                bizChannelContext.broadcastMeetingMemberUpdate(meetingId, userId, nickName);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接断开...");
        // 获取用户信息，用于发送离开通知
        UserTokenInfoDTO userInfo = bizChannelContext.getUserInfo(ctx.channel());
        String userId = bizChannelContext.getUserId(ctx.channel());
        String meetingId = userInfo != null ? userInfo.getCurrentMeetingId() : null;
        String nickName = userInfo != null ? userInfo.getNickName() : null;

        // 清理连接
        bizChannelContext.removeByChannel(ctx.channel());
        log.info("连接已断开，用户: {}, ChannelId: {}", userId, ctx.channel().id().asShortText());

        // 如果用户在会议中，通知其他成员
        if (meetingId != null && userId != null) {
            MessageSendDTO exitMessage = new MessageSendDTO();
            exitMessage.setMessageType(MessageType.EXIT_MEETING_ROOM);
            exitMessage.setMeetingId(meetingId);
            exitMessage.setMessageSendToType(MessageSendToType.GROUP);
            exitMessage.setSendUserId(userId);
            exitMessage.setSendUserNickName(nickName);
            bizChannelContext.sendMessage(exitMessage);
            log.info("已发送用户 {} 离开会议 {} 的通知", nickName, meetingId);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String message = textWebSocketFrame.text();
        // 心跳消息直接响应，不经过分发器
        if ("ping".equals(message)) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("pong"));
            return;
        }

        log.info("收到消息：{}", message);

        try {
            // 解析消息，封装websocket文本帧为MessageSendDTO对象用于后续发送
            MessageSendDTO messageSendDTO = JSON.parseObject(message, MessageSendDTO.class);
            if (messageSendDTO == null) {
                log.warn("无法解析消息: {}", message);
                return;
            }

            // 解耦于消息调度器执行调度，因为消息类型、发送相关都已封装
            messageDispatcher.dispatch(ctx, messageSendDTO);
        } catch (Exception e) {
            log.error("处理消息失败: {}", message, e);
        }
    }
}
