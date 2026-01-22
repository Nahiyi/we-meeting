package cn.clazs.easymeeting.websocket.netty;

import cn.clazs.easymeeting.config.AppConfig;
import cn.clazs.easymeeting.websocket.handler.HeartBeatHandler;
import cn.clazs.easymeeting.websocket.handler.TextWebSocketHandler;
import cn.clazs.easymeeting.websocket.handler.TokenValidationHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于Netty搭建的WebSocket服务器启动类
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketServerStarter implements Runnable {

    // boss线程组，处理客户端连接
    private final EventLoopGroup boss = new NioEventLoopGroup();
    // worker线程组，处理读写IO消息
    private final EventLoopGroup workers = new NioEventLoopGroup();

    private final AppConfig appConfig;
    private final TokenValidationHandler tokenValidationHandler;
    private final TextWebSocketHandler textWebSocketHandler;

    @Override
    public void run() {
        log.info("WebSocket服务器启动引导中...");

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, workers);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    // HTTP协议下编解码器
                    pipeline.addLast(new HttpServerCodec());
                    // HTTP消息聚合器
                    pipeline.addLast(new HttpObjectAggregator(65536));
                    // 三个参数：读超时阈值（客户端心跳）、写超时阈值
                    pipeline.addLast(new IdleStateHandler(10, 0, 0));
                    pipeline.addLast(new HeartBeatHandler());
                    pipeline.addLast(tokenValidationHandler);
                    // WebSocket协议处理器
                    pipeline.addLast(new WebSocketServerProtocolHandler("/ws", 10000L));
                    // 自定义处理WebSocket文本帧的处理器
                    pipeline.addLast(textWebSocketHandler);
                }
            });

            Channel channel = serverBootstrap.bind(appConfig.getWsPort()).sync().channel();
            log.info("WebSocket服务器启动成功, port: [{}]", appConfig.getWsPort());

            // 获取一个对于连接关闭的监听future，等待连接关闭事件发生
            ChannelFuture closedFuture = channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("Netty-WebSocket服务器失败", e);
        } finally {
            log.debug("资源释放...");
            close();
        }
    }

    @PreDestroy
    private void close() {
        boss.shutdownGracefully();
        workers.shutdownGracefully();
    }
}
