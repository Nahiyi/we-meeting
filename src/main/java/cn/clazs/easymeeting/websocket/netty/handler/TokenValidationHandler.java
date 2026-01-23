package cn.clazs.easymeeting.websocket.netty.handler;

import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.util.StringUtil;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class TokenValidationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final RedisComponent redisComponent;
    private final BizChannelContext bizChannelContext;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 从ws的请求路径uri中解析获取token
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        Map<String, List<String>> parameters = decoder.parameters();
        // 并非数组类型，取第一个即可
        List<String> tokens = parameters.get("token");
        if (tokens == null) {
            sendErrorResponse(ctx, request);
            return;
        }
        String token = tokens.getFirst();
        if (StringUtil.isEmpty(token)) {
            sendErrorResponse(ctx, request);
            return;
        }
        UserTokenInfoDTO userTokenInfo = redisComponent.getUserTokenInfo(token);
        if (userTokenInfo == null) {
            log.error("校验Token失败: {}", token);
            sendErrorResponse(ctx, request);
            return;
        }

        log.info("用户: {} ({}) 验证通过，准备ws握手", userTokenInfo.getNickName(), userTokenInfo.getUserId());

        // 重写 URI，欺骗后续的 WebSocketServerProtocolHandler
        // 原始是 /ws?token=xxx，必须改成 /ws，否则握手处理器不认
        request.setUri("/ws");

        // 连接校验通过，添加映射到上下文中维护
        bizChannelContext.addToContext(userTokenInfo.getUserId(), ctx.channel(), userTokenInfo);

        log.debug("上下文添加完毕：{}", bizChannelContext.getUserId(ctx.channel()));
        // SCIH会自动release一次，防止非法参数，需要手动retain引用+1
        ctx.fireChannelRead(request.retain());
    }

    /**
     * 校验不通过时，返回错误的Response
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                request.protocolVersion(), HttpResponseStatus.UNAUTHORIZED);
        String errorMessage = "Token验证失败";
        response.content().writeBytes(errorMessage.getBytes());
        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        headers.set(HttpHeaderNames.CONNECTION, "close");
        headers.setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        // 响应完毕成功后关闭channel
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
