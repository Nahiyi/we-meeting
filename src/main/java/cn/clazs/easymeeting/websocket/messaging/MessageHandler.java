package cn.clazs.easymeeting.websocket.messaging;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;

/**
 * 消息处理器接口
 * 支持多种实现：Local、Redis、RabbitMQ、Kafka
 */
public interface MessageHandler {

    /**
     * 开始监听消息
     * 应用启动时调用
     */
    void listenMessage();

    /**
     * 发送消息
     * @param messageSendDTO 消息内容
     */
    void sendMessage(MessageSendDTO messageSendDTO);
}
