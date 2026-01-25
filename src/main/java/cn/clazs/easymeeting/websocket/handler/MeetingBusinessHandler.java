package cn.clazs.easymeeting.websocket.handler;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import cn.clazs.easymeeting.entity.enums.MeetingStatus;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.MeetingInfoService;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import cn.clazs.easymeeting.websocket.messaging.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 会议业务处理器
 * 处理需要业务逻辑的会议相关消息
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MeetingBusinessHandler implements MessageTypeHandler {

    private final MessageHandler messageHandler;
    private final RedisComponent redisComponent;
    private final MeetingInfoService meetingInfoService;
    private final BizChannelContext bizChannelContext;

    @Override
    public List<MessageType> getSupportedTypes() {
        return Arrays.asList(
                MessageType.FINIS_MEETING,
                MessageType.EXIT_MEETING_ROOM
        );
    }

    @Override
    public void handle(ChannelHandlerContext ctx, MessageSendDTO message) {
        MessageType type = message.getMessageType();
        if (type == null) {
            log.warn("未知的消息类型");
            return;
        }

        switch (type) {
            case FINIS_MEETING:
                handleFinishMeeting(ctx, message);
                break;
            case EXIT_MEETING_ROOM:
                handleExitMeeting(ctx, message);
                break;
            default:
                log.warn("未处理的会议消息类型: {}", type);
        }
    }

    /**
     * 处理结束会议
     */
    private void handleFinishMeeting(ChannelHandlerContext ctx, MessageSendDTO message) {
        String meetingId = message.getMeetingId();
        log.info("处理结束会议: meetingId={}, userId={}", meetingId, message.getSendUserId());

        // 更新数据库中的会议状态
        MeetingInfo meeting = meetingInfoService.getMeetingById(meetingId);
        if (meeting != null) {
            meeting.setStatus(MeetingStatus.FINISHED.getStatus());
            meetingInfoService.updateMeeting(meeting);
        }

        // 清理 Redis 中的会议数据
        redisComponent.removeMeetingMembers(meetingId);

        // 广播给所有参会者
        messageHandler.sendMessage(message);
    }

    /**
     * 处理退出会议（通过 WebSocket 消息触发）
     * 复用 Service 层逻辑，确保完整处理
     */
    private void handleExitMeeting(ChannelHandlerContext ctx, MessageSendDTO message) {
        String meetingId = message.getMeetingId();
        String userId = message.getSendUserId();
        log.info("处理退出会议(WebSocket): meetingId={}, userId={}", meetingId, userId);

        // 从 Channel 获取用户信息
        UserTokenInfoDTO tokenUserInfo = bizChannelContext.getUserInfo(ctx.channel());
        if (tokenUserInfo == null) {
            log.warn("无法获取用户信息，userId={}", userId);
            // 降级处理：直接从 Redis 移除
            redisComponent.removeMeetingMember(meetingId, userId);
            messageHandler.sendMessage(message);
            return;
        }

        // 复用 Service 层的退出逻辑（包含完整的清理操作）
        meetingInfoService.exitMeetingRoom(tokenUserInfo, MeetingMemberStatus.EXIT_MEETING);
    }
}
