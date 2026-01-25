package cn.clazs.easymeeting.websocket.handler;

import cn.clazs.easymeeting.entity.enums.MessageType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理器注册表
 * 自动发现并注册所有 MessageTypeHandler 实现
 */
@Component
@Slf4j
public class HandlerRegistry {

    /** 注册过的handler的bean会被自动装配进来 */
    private final List<MessageTypeHandler> handlers;
    /** 核心的消息类型和对应的具体处理器的映射，适用于策略模式拓展 */
    private final Map<MessageType, MessageTypeHandler> handlerMap = new ConcurrentHashMap<>();

    private MessageTypeHandler defaultHandler;

    public HandlerRegistry(List<MessageTypeHandler> handlers) {
        this.handlers = handlers;
    }

    @PostConstruct
    public void init() {
        // 遍历每个消息处理器实现
        for (MessageTypeHandler handler : handlers) {
            // 遍历当前消息处理器处理的所有类型，如果该类型已经有消息处理器了，就覆盖之
            for (MessageType type : handler.getSupportedTypes()) {
                MessageTypeHandler oldVal = handlerMap.put(type, handler);
                if (oldVal != null) {
                    log.warn("消息类型 {} 已有旧处理器 {}，被 {} 覆盖",
                            type, oldVal.getClass().getSimpleName(),
                            handler.getClass().getSimpleName());
                }
                log.info("注册消息处理器: {} -> {}", type, handler.getClass().getSimpleName());
            }

            // 检查是否是默认处理器
            /* if (handler instanceof DefaultForwardHandler) {
                this.defaultHandler = handler;
            } */
        }
        log.info("消息处理器注册完成，共 {} 个处理器，{} 种消息类型",
                handlers.size(), handlerMap.size());
    }

    /**
     * 根据消息类型获取处理器
     */
    public Optional<MessageTypeHandler> getHandler(MessageType messageType) {
        return Optional.ofNullable(handlerMap.get(messageType));
    }

    /**
     * 获取默认处理器
     */
    public MessageTypeHandler getDefaultHandler() {
        return defaultHandler;
    }

    /**
     * 获取已注册的处理器数量
     */
    public int getHandlerCount() {
        return handlerMap.size();
    }
}
