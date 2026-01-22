package cn.clazs.easymeeting.websocket;

import cn.clazs.easymeeting.websocket.netty.WebSocketServerStarter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitRun implements ApplicationRunner {

    private final WebSocketServerStarter webSocketServerStarter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("准备启动Netty-WebSocket服务器...");
        // 实测run方法的执行是由main线程进行，所以为了不阻塞springboot服务器这里异步启动netty服务器
        new Thread(webSocketServerStarter, "thread-websocket-starter").start();
    }
}
