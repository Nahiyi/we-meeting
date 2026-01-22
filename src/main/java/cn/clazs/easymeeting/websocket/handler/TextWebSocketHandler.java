package cn.clazs.easymeeting.websocket.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于TextWebSocketFrame处理text文本消息的自定义websocket下一级处理器
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class TextWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接断开...");
        // TODO 连接断开的逻辑
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String text = textWebSocketFrame.text();
        log.error("用户发送: {}", text);
    }
}
