package cn.clazs.easymeeting.websocket.messaging;

import cn.clazs.easymeeting.constant.Constants;
import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地消息处理器（单机模式）
 * 直接在本机内存中查找用户并发送消息
 *
 * 当配置 messaging.handle.channel=local 或未配置时启用
 */
@Component
@ConditionalOnProperty(
        name = Constants.MESSAGE_HANDLE_CHANNEL,
        havingValue = "local",
        matchIfMissing = true  // 如果没配置，默认使用这个
)
@Slf4j
@RequiredArgsConstructor
public class LocalMessageHandler implements MessageHandler {

    private final BizChannelContext bizChannelContext;

    @PostConstruct
    public void init() {
        listenMessage();
    }

    @Override
    public void listenMessage() {
        // 单机模式不需要监听外部消息（服务器持有所有连接，无需跨服务器）
        log.info("本地消息处理器已启动（单机模式）");
    }

    @Override
    public void sendMessage(MessageSendDTO messageSendDTO) {
        // 直接在本机发送
        bizChannelContext.sendMessage(messageSendDTO);
    }
}
