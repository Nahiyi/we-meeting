package cn.clazs.easymeeting.websocket.messaging;

import cn.clazs.easymeeting.constant.Constants;
import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ 消息处理器（集群模式）
 * 使用 RabbitMQ Fanout Exchange 实现跨服务器消息广播
 * 
 * 当配置 messaging.handle.channel=rabbitmq 时启用
 */
@Component
@ConditionalOnProperty(
        name = Constants.MESSAGE_HANDLE_CHANNEL,
        havingValue = Constants.MESSAGE_CHANNEL_RABBITMQ
)
@Slf4j
@RequiredArgsConstructor
public class RabbitMQMessageHandler implements MessageHandler {

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final BizChannelContext bizChannelContext;

    private SimpleMessageListenerContainer container;
    private volatile String currentQueueName;

    /**
     * 应用启动后自动开始监听
     */
    @PostConstruct
    public void init() {
        listenMessage();
        log.info("RabbitMQ 消息监听器初始化完成");
    }

    @Override
    public void listenMessage() {
        // 1. 声明 Fanout 交换机
        FanoutExchange exchange = new FanoutExchange(Constants.RABBITMQ_EXCHANGE_MESSAGE, true, false);
        // 创建交换机是幂等的，只会存在一个
        amqpAdmin.declareExchange(exchange);

        // 2. 声明匿名队列 (Exclusive, Auto-Delete, Non-Durable)
        // AnonymousQueue 会自动生成随机名称，并在连接关闭时自动删除
        Queue queue = new AnonymousQueue();
        // 显式声明队列以获取生成的名称
        currentQueueName = amqpAdmin.declareQueue(queue);
        
        if (currentQueueName == null) {
            log.error("RabbitMQ 匿名队列声明失败");
            return;
        }

        // 3. 将队列绑定到交换机
        Binding binding = BindingBuilder.bind(queue).to(exchange);
        amqpAdmin.declareBinding(binding);

        log.info("已创建 RabbitMQ 临时广播队列: {} 并绑定到交换机: {}", currentQueueName, Constants.RABBITMQ_EXCHANGE_MESSAGE);

        // 4. 创建并启动监听容器
        container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(currentQueueName);
        container.setMessageListener(message -> {
            try {
                String body = new String(message.getBody(), StandardCharsets.UTF_8);
                log.debug("RabbitMQ 收到消息: {}", body);
                
                MessageSendDTO messageSendDTO = JSON.parseObject(body, MessageSendDTO.class);
                if (messageSendDTO != null) {
                    // 在本机尝试发送消息
                    bizChannelContext.sendMessage(messageSendDTO);
                }
            } catch (Exception e) {
                log.error("RabbitMQ 消息处理异常", e);
            }
        });
        
        container.start();
        log.info("RabbitMQ 监听容器已启动");
    }

    @Override
    public void sendMessage(MessageSendDTO messageSendDTO) {
        if (messageSendDTO == null) {
            return;
        }
        try {
            String jsonMessage = JSON.toJSONString(messageSendDTO);
            // 发送到 Fanout 交换机，RoutingKey 为空
            rabbitTemplate.convertAndSend(Constants.RABBITMQ_EXCHANGE_MESSAGE, "", jsonMessage);
            log.info("消息已发布到 RabbitMQ Exchange: {}", Constants.RABBITMQ_EXCHANGE_MESSAGE);
        } catch (Exception e) {
            log.error("发送消息到 RabbitMQ 失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (container != null) {
            container.stop();
        }
        // 队列是 Auto-Delete 的，通常连接关闭后会自动删除
        log.info("RabbitMQ 消息监听器已关闭");
    }
}
