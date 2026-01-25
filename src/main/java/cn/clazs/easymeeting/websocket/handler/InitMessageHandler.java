package cn.clazs.easymeeting.websocket.handler;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * INIT 消息处理器
 * 处理前端请求刷新会议成员列表的消息
 * 主要用于页面刷新或离开会议后重新加入时获取成员列表
 * ps.用户加入接口，做了降级的异步处理
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InitMessageHandler implements MessageTypeHandler {

    private final BizChannelContext bizChannelContext;
    private final RedisComponent redisComponent;

    @Override
    public List<MessageType> getSupportedTypes() {
        return Collections.singletonList(MessageType.INIT);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, MessageSendDTO message) {
        String userId = message.getSendUserId();
        String meetingIdFromMessage = message.getMeetingId();

        log.info("收到 INIT 消息: userId={}, meetingId={}", userId, meetingIdFromMessage);

        // 获取 Channel 上的用户信息
        UserTokenInfoDTO channelUserInfo = bizChannelContext.getUserInfo(ctx.channel());
        if (channelUserInfo == null) {
            log.warn("无法获取用户信息，userId={}", userId);
            return;
        }

        // 注意：从 Redis 获取最新的用户信息
        // 因为 Channel 上的 UserTokenInfoDTO 可能是旧的（用户离开会议后重新加入时）
        // HTTP API 更新的是 Redis 中的 UserTokenInfoDTO，而不是 Channel 上的
        String token = redisComponent.getTokenByUserId(userId);
        UserTokenInfoDTO redisUserInfo = token != null ? redisComponent.getUserTokenInfo(token) : null;

        // 优先使用 Redis 中的 currentMeetingId（最新状态）
        String currentMeetingId = null;
        if (redisUserInfo != null && redisUserInfo.getCurrentMeetingId() != null) {
            currentMeetingId = redisUserInfo.getCurrentMeetingId();
            log.info("从 Redis 获取到 currentMeetingId: {}", currentMeetingId);

            // 同步更新 Channel 上的用户信息
            channelUserInfo.setCurrentMeetingId(currentMeetingId);

            // 确保用户在 WebSocket 房间中
            Channel channel = ctx.channel();
            bizChannelContext.joinMeetingRoom(currentMeetingId, channel);
        } else if (channelUserInfo.getCurrentMeetingId() != null) {
            // 如果 Redis 中没有，使用 Channel 上的（兼容旧逻辑）
            currentMeetingId = channelUserInfo.getCurrentMeetingId();
            log.info("从 Channel 获取到 currentMeetingId: {}", currentMeetingId);
        }

        if (currentMeetingId != null) {
            log.info("用户 {} 请求刷新会议 {} 的成员列表", channelUserInfo.getNickName(), currentMeetingId);

            // 重新广播成员列表给房间内所有用户
            // 这里将 newMember 设置为当前用户，这样前端可以正确识别自己是"新成员"
            bizChannelContext.broadcastMeetingMemberUpdate(currentMeetingId, userId, channelUserInfo.getNickName());
        } else {
            log.info("用户 {} 不在任何会议中，忽略 INIT 消息", userId);
        }
    }
}