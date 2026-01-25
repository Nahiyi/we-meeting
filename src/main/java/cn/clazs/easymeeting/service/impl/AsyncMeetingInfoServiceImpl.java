package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.websocket.messaging.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service("asyncMeetingInfoService")
@RequiredArgsConstructor
public class AsyncMeetingInfoServiceImpl {

    private final MessageHandler messageHandler;

    /**
     * 异步延迟发送成员加入消息
     * 使用 @Async 注解，Spring 会自动在线程池中执行此方法
     * 延迟 1 秒确保前端已完成 WebSocket 初始化并注册消息监听器
     *
     * @param messageSendDTO 封装了成员加入消息DTO的作为发送消息的DTO
     */
    @Async("asyncTaskExecutor")
    public void delayedSendMemberJoinMessage(MessageSendDTO messageSendDTO) {
        try {
            Thread.sleep(1000);
            messageHandler.sendMessage(messageSendDTO);
            log.debug("延迟发送的 ADD_MEETING_ROOM 消息已发送，会议ID: {}", messageSendDTO.getMeetingId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("延迟发送消息被中断，会议ID: {}", messageSendDTO.getMeetingId(), e);
        }
    }
}
