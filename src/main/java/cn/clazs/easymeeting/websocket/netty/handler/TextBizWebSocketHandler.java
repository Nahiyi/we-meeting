package cn.clazs.easymeeting.websocket.netty.handler;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MessageSendToType;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入，ChannelId: {}", ctx.channel().id().asShortText());
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
        log.info("收到消息：{}", message);

        // 心跳消息直接响应，不经过分发器
        if ("ping".equals(message)) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("pong"));
            return;
        }

        try {
            // 解析消息
            MessageSendDTO messageSendDTO = JSON.parseObject(message, MessageSendDTO.class);
            if (messageSendDTO == null) {
                log.warn("无法解析消息: {}", message);
                return;
            }
        } catch (Exception e) {
            log.error("处理消息失败: {}", message, e);
        }
    }
}
