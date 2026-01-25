package cn.clazs.easymeeting.websocket.messaging;

import cn.clazs.easymeeting.constant.Constants;
import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static cn.clazs.easymeeting.constant.Constants.REDIS_CHANNEL_MESSAGE;

/**
 * Redis 消息处理器（集群模式）
 * 使用 Redis Pub/Sub 实现跨服务器消息传递
 * 视频会议信令对实时性要求高，Redis 延迟更低
 * 信令消息丢失可以通过 WebRTC 的 ICE 重连机制恢复
 *
 * 当配置 messaging.handle.channel=redis 时启用
 */
@Component
@ConditionalOnProperty(
        name = Constants.MESSAGE_HANDLE_CHANNEL,
        havingValue = Constants.MESSAGE_CHANNEL_REDIS
)
@Slf4j
@RequiredArgsConstructor
public class RedisMessageHandler implements MessageHandler {

    private final RedissonClient redissonClient;
    private final BizChannelContext bizChannelContext;

    private int listenerId;

    /**
     * 应用启动后自动开始监听
     */
    @PostConstruct
    public void init() {
        listenMessage();
        log.info("Redis 消息监听器已启动，Topic: {}", REDIS_CHANNEL_MESSAGE);
    }

    @Override
    public void listenMessage() {
        RTopic topic = redissonClient.getTopic(REDIS_CHANNEL_MESSAGE);

        // 添加监听器
        listenerId = topic.addListener(
                MessageSendDTO.class,
                (channel, messageSendDTO) -> {
                    log.info("Redis 收到消息：{}", JSONObject.toJSONString(messageSendDTO));
                    // 在本机尝试发送消息
                    bizChannelContext.sendMessage(messageSendDTO);
                }
        );
    }

    @Override
    public void sendMessage(MessageSendDTO messageSendDTO) {
        RTopic topic = redissonClient.getTopic(REDIS_CHANNEL_MESSAGE);
        long receiversCount = topic.publish(messageSendDTO);
        log.info("消息已发布到 Redis，订阅者数量: {}", receiversCount);
    }

    @PreDestroy
    public void destroy() {
        RTopic topic = redissonClient.getTopic(REDIS_CHANNEL_MESSAGE);
        topic.removeListener(listenerId);
        log.info("Redis 消息监听器已关闭");
    }
}
