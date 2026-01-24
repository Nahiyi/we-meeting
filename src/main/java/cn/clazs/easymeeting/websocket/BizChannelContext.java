package cn.clazs.easymeeting.websocket;

import cn.clazs.easymeeting.entity.dto.MeetingMemberDTO;
import cn.clazs.easymeeting.entity.dto.MemberJoinMeetingDTO;
import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MessageSendToType;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.util.StringUtil;
import com.alibaba.fastjson2.JSON;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Biz: User 和 Meeting
 * 用户、会议房间和Channel的上下文管理类
 * 存储三者相互映射的核心Map等
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BizChannelContext {

    /** 全局唯一的AttributeKey，用于作不同Channel上存储的用户ID时的键 */
    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("USER_ID");

    /** 全局唯一的AttributeKey，用于作不同Channel上存储的用户Token信息的键 */
    public static final AttributeKey<UserTokenInfoDTO> USER_TOKEN_INFO_KEY = AttributeKey.valueOf("USER_TOKEN_INFO");

    /** userId与Channel的映射 userId -> Channel */
    public static final Map<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    /** 会议房间（会议号）和channelGroup（多个Channel的集合）的映射 meetingId -> ChannelGroup（ -> Channel） */
    public static final Map<String, ChannelGroup> MEETING_ROOM_CONTEXT_MAP = new ConcurrentHashMap<>();

    private final RedisComponent redisComponent;

    /**
     * 用户建立连接，处理建立连接后端初始化，并建立如下三个绑定c
     * 1. 将用户ID和Channel进行绑定：userId -> Channel
     * 2. Channel与userId绑定：通过attr(全局key)拿到Attribute<用户id> -> userId
     * 3. Channel与用户Token信息的绑定：通过attr(全局key)拿到Attribute<用户Token> -> UserTokenInfoDTO
     */
    public void addToContext(String userId, Channel channel, UserTokenInfoDTO userInfo) {
        Channel oldChannel = USER_CONTEXT_MAP.get(userId);
        if (oldChannel != null && oldChannel != channel) {
            // 存在旧连接，清除旧连接；清除旧连接的属性，防止channelInactive时发送用户退出消息
            UserTokenInfoDTO oldUserInfo = oldChannel.attr(USER_TOKEN_INFO_KEY).get();
            // 如果旧连接在会议中，退出会议
            if (oldUserInfo != null && oldUserInfo.getCurrentMeetingId() != null) {
                leaveMeetingRoom(oldUserInfo.getCurrentMeetingId(), oldChannel);
            }
            oldChannel.attr(USER_ID_KEY).set(null);
            oldChannel.attr(USER_TOKEN_INFO_KEY).set(null);
            oldChannel.close();
        }

        // 如果userInfo为null，根据userId从Redis获取token信息
        if (userInfo == null) {
            String token = redisComponent.getTokenByUserId(userId);
            if (token != null) {
                userInfo = redisComponent.getUserTokenInfo(token);
            }
        }

        // 建立三向映射
        channel.attr(USER_ID_KEY).set(userId);              // Channel -> UserId
        channel.attr(USER_TOKEN_INFO_KEY).set(userInfo);    // Channel -> UserTokenInfoDTO
        USER_CONTEXT_MAP.put(userId, channel);              // UserId -> Channel

        log.info("用户: {} 已添加到在线列表，ChannelId: {}", userId, channel.id().asShortText());

        // 如果用户正在会议中，自动加入会议房间
        if (userInfo != null && userInfo.getCurrentMeetingId() != null) {
            String meetingId = userInfo.getCurrentMeetingId();
            joinMeetingRoom(meetingId, channel);
            log.info("用户: {} 自动加入会议房间: {}", userId, meetingId);
        }
    }

    /**
     * 添加用户连接（重载版，只传userId，会自动从Redis获取userInfo）
     */
    public void addToContext(String userId, Channel channel) {
        addToContext(userId, channel, null);
    }

    /**
     * 根据Channel移除连接（连接断开时使用）
     * 注意：这里不更新lastOffTime，因为断开可能只是退出会议而非退出登录
     */
    public void removeByChannel(Channel channel) {
        String userId = channel.attr(USER_ID_KEY).get();
        if (userId == null) {
            // 已被清理过，直接返回（幂等性保护）
            return;
        }

        // 清除Channel->userId绑定，防止重复处理
        channel.attr(USER_ID_KEY).set(null);

        // 从会议房间移除
        UserTokenInfoDTO userInfo = channel.attr(USER_TOKEN_INFO_KEY).get();
        if (userInfo != null && userInfo.getCurrentMeetingId() != null) {
            leaveMeetingRoom(userInfo.getCurrentMeetingId(), channel);
        }
        // 清除Channel->userToken绑定
        channel.attr(USER_TOKEN_INFO_KEY).set(null);

        // 只有当前Channel匹配时才移除，防止误删新连接
        USER_CONTEXT_MAP.remove(userId, channel);

        log.info("用户: {} 连接已断开，ChannelId: {}", userId, channel.id().asShortText());
    }

    /**
     * 根据 userId 获取 Channel
     */
    public Channel getChannel(String userId) {
        return USER_CONTEXT_MAP.get(userId);
    }

    /**
     * 根据Channel获取userId
     */
    public String getUserId(Channel channel) {
        return channel.attr(USER_ID_KEY).get();
    }

    /**
     * 根据Channel获取用户完整信息
     */
    public UserTokenInfoDTO getUserInfo(Channel channel) {
        return channel.attr(USER_TOKEN_INFO_KEY).get();
    }

    // =========================== 消息发送相关方法 ===========================

    /**
     * 发送消息（根据类型路由到群组或个人）
     */
    public void sendMessage(MessageSendDTO messageSendDTO) {
        if (messageSendDTO == null) {
            return;
        }
        MessageSendToType sendToType = messageSendDTO.getMessageSendToType();
        if (MessageSendToType.GROUP == sendToType) {
            sendMsgToGroup(messageSendDTO);
        } else if (MessageSendToType.USER == sendToType) {
            sendMsgToUser(messageSendDTO);
        }
    }

    /**
     * 发送消息到群组（会议房间）
     */
    private void sendMsgToGroup(MessageSendDTO messageSendDTO) {
        String meetingId = messageSendDTO.getMeetingId();
        if (StringUtil.isEmpty(meetingId)) {
            log.warn("发送群组消息失败：meetingId 为空");
            return;
        }
        ChannelGroup channelGroup = MEETING_ROOM_CONTEXT_MAP.get(meetingId);
        if (channelGroup == null || channelGroup.isEmpty()) {
            log.warn("发送群组消息失败：会议房间 {} 不存在或为空", meetingId);
            return;
        }
        // 序列化消息并发送给房间内所有用户
        String messageJson = JSON.toJSONString(messageSendDTO);
        // 封装为纯文本的websocket帧发送，后续被WebSocket协议处理器转为ByteBuf
        channelGroup.writeAndFlush(new TextWebSocketFrame(messageJson));
        log.info("消息已发送到会议房间: {}，房间在线人数: {}", meetingId, channelGroup.size());
    }

    /**
     * 发送消息到指定用户
     */
    private void sendMsgToUser(MessageSendDTO messageSendDTO) {
        String receiveUserId = messageSendDTO.getReceiveUserId();
        if (StringUtil.isEmpty(receiveUserId)) {
            log.warn("发送个人消息失败：receiveUserId 为空");
            return;
        }
        // 获取目标用户的Channel
        Channel channel = USER_CONTEXT_MAP.get(receiveUserId);
        if (channel == null || !channel.isActive()) {
            log.warn("发送个人消息失败：用户 {} 不在线", receiveUserId);
            return;
        }
        // 序列化消息并发送
        String messageJson = JSON.toJSONString(messageSendDTO);
        channel.writeAndFlush(new TextWebSocketFrame(messageJson));
        log.info("消息已发送给用户: {}", receiveUserId);
    }

    // =========================== 会议房间相关方法 ===========================

    /**
     * 加入会议房间
     * 注意：channel可能为null（用户通过HTTP API加入会议但尚未建立WebSocket连接）
     * 此时跳过WebSocket房间加入，用户建立WebSocket连接后会通过addToContext()自动加入
     */
    public void joinMeetingRoom(String meetingId, Channel channel) {
        if (channel == null) {
            log.warn("尝试加入会议房间: {} 但channel为null，长连接尚未建立，跳过房间加入（用户将在建立 WebSocket 连接后自动加入）", meetingId);
            return;
        }
        // 若不存在则一并创建会议的ChannelGroup
        ChannelGroup channelGroup = MEETING_ROOM_CONTEXT_MAP.computeIfAbsent(
                meetingId,
                k -> new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
        );
        channelGroup.add(channel);
        log.info("Channel: {} 加入会议房间: {}", channel.id().asShortText(), meetingId);
    }

    /**
     * 离开会议房间：从指定的会议对应的ChannelGroup中移除当前Channel
     * meetingId 一对一-> ChannelGroup 一对多-> Channel
     */
    public void leaveMeetingRoom(String meetingId, Channel channel) {
        ChannelGroup channelGroup = MEETING_ROOM_CONTEXT_MAP.get(meetingId);
        if (channelGroup != null) {
            channelGroup.remove(channel);
            log.info("Channel: {} 离开会议房间: {}", channel.id().asShortText(), meetingId);
            // 如果房间为空，移除房间
            if (channelGroup.isEmpty()) {
                MEETING_ROOM_CONTEXT_MAP.remove(meetingId);
                log.info("会议房间: {} 已清空并销毁", meetingId);
            }
        }
    }

    /**
     * 发送退出会议消息并清理 Channel
     * 先发送消息（确保用户还在房间内能收到），再从房间移除 Channel
     *
     * @param messageSendDTO 退出消息
     * @param userId 退出的用户ID
     */
    public void sendExitMessageAndCleanup(MessageSendDTO messageSendDTO, String userId) {
        String meetingId = messageSendDTO.getMeetingId();
        if (StringUtil.isEmpty(meetingId) || StringUtil.isEmpty(userId)) {
            log.warn("发送退出消息失败：meetingId 或 userId 为空");
            return;
        }

        // 1. 先发送消息（此时用户还在房间内）
        sendMessage(messageSendDTO);

        // 2. 再从 WebSocket 房间移除 Channel
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel != null) {
            leaveMeetingRoom(meetingId, channel);
            // 更新 Channel 上的用户信息：置空
            UserTokenInfoDTO channelUserInfo = channel.attr(USER_TOKEN_INFO_KEY).get();
            if (channelUserInfo != null) {
                channelUserInfo.setCurrentMeetingId(null);
            }
            log.info("用户 {} 已从会议房间 {} 移除", userId, meetingId);
        }
    }

    /**
     * 在本会议房间内，广播会议成员更新消息
     * 当用户通过 WebSocket 连接并自动加入会议房间时调用
     * 发送成员列表给房间内所有用户
     */
    public void broadcastMeetingMemberUpdate(String meetingId, String newUserId, String newUserNickName) {
        // 获取会议成员列表
        MeetingMemberDTO newMember = redisComponent.getMeetingMember(meetingId, newUserId);
        List<MeetingMemberDTO> memberList = redisComponent.getMeetingMemberList(meetingId);

        // 构建消息内容
        MemberJoinMeetingDTO meetingJoinDTO = new MemberJoinMeetingDTO();
        meetingJoinDTO.setNewMember(newMember);
        meetingJoinDTO.setMeetingMemberList(memberList);

        // 构建消息
        MessageSendDTO messageSendDto = new MessageSendDTO();
        messageSendDto.setMessageType(MessageType.ADD_MEETING_ROOM);
        messageSendDto.setMeetingId(meetingId);
        messageSendDto.setMessageSendToType(MessageSendToType.GROUP);
        messageSendDto.setMessageContent(meetingJoinDTO); // 设置用户加入消息
        messageSendDto.setSendUserId(newUserId);
        messageSendDto.setSendUserNickName(newUserNickName);

        // 广播给房间内所有用户
        sendMessage(messageSendDto);
        log.info("在房间 {} 广播会议成员更新消息，新成员: {}", meetingId, newUserNickName);
    }

    /**
     * 关闭用户连接（主动踢人时使用）
     * 注意：这里不更新 lastOffTime，单设备登录通过 Redis token 机制实现
     */
    public void closeContext(String userId) {
        if (StringUtil.isEmpty(userId)) {
            return;
        }
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel != null) {
            // 先从会议房间移除
            UserTokenInfoDTO userInfo = channel.attr(USER_TOKEN_INFO_KEY).get();
            if (userInfo != null && userInfo.getCurrentMeetingId() != null) {
                leaveMeetingRoom(userInfo.getCurrentMeetingId(), channel);
            }
            // 清理映射
            channel.attr(USER_ID_KEY).set(null);
            channel.attr(USER_TOKEN_INFO_KEY).set(null);
            USER_CONTEXT_MAP.remove(userId);

            // 关闭连接
            channel.close();
            log.info("用户 {} 连接已关闭", userId);
        }
    }
}
