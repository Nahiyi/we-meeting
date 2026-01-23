package cn.clazs.easymeeting.websocket.netty.handler;

import cn.clazs.easymeeting.websocket.BizChannelContext;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 是读写闲置超时事件消息
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    String userId = ctx.channel().attr(BizChannelContext.USER_ID_KEY).get();
                    log.info("用户: {} 没有发送心跳，断开连接", userId);
                    ctx.close();
                    break;
                case WRITER_IDLE:
                    ctx.writeAndFlush("pong");
                    break;
                case ALL_IDLE:
                    break;
            }
        }
        // 透传下去处理后续可能的handler处理事件
        super.userEventTriggered(ctx, evt);
    }
}
