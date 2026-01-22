package cn.clazs.easymeeting.websocket.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 是读写闲置超时事件消息
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                // 是客户端超时未发送心跳（服务器读事件）
                Channel channel = ctx.channel();
                // 基于Attribute获取绑定到channel的userId
                Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
                String userId = attribute.get();
                log.info("用户: {} 未发送心跳，超时断开", userId);
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                // 服务器超时为发出心跳（服务器写超时），暂无配置，可忽略
                log.debug("IdleStateEvent - WRITER_IDLE");
            }
        }
        // 透传下去处理后续可能的handler处理事件
        super.userEventTriggered(ctx, evt);
    }
}
