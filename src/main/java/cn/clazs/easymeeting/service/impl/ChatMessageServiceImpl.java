package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.context.UserContext;
import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.enums.ChatMessageStatus;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.entity.enums.ReceiveType;
import cn.clazs.easymeeting.entity.po.ChatMessage;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.ChatMessageMapper;
import cn.clazs.easymeeting.service.ChatMessageService;
import cn.clazs.easymeeting.util.MessageSendDTOBuilder;
import cn.clazs.easymeeting.util.StringUtil;
import cn.clazs.easymeeting.util.TableSplitUtil;
import cn.clazs.easymeeting.websocket.messaging.MessageHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天消息服务实现类
 * 自动处理分表路由、消息ID生成、时间戳设置等
 */
@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private MessageHandler messageHandler;

    /**
     * 雪花算法相关参数
     */
    private static long lastTimestamp = -1L;
    private static long sequence = 0L;
    private static final long SEQUENCE_MASK = 4095L; // 12位序列号掩码

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        // 参数校验
        if (message.getMeetingId() == null || message.getMeetingId().isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }

        // 验证消息类型
        validateMessageType(message.getMessageType());

        // 接收类型校验
        validateReceiveType(message);

        // 生成消息ID
        if (message.getMessageId() == null) {
            message.setMessageId(generateMessageId());
        }

        // 设置发送时间
        if (message.getSendTime() == null) {
            message.setSendTime(System.currentTimeMillis());
        }

        // 设置默认状态
        MessageType messageTypeEnum = MessageType.getByType(message.getMessageType());
        if (messageTypeEnum == MessageType.CHAT_TEXT_MESSAGE) {
            if (StringUtil.isEmpty(message.getMessageContent())) {
                throw new BusinessException("文本消息内容不能为空");
            }
            message.setStatus(ChatMessageStatus.SENDEND.getStatus());
        } else if (messageTypeEnum == MessageType.CHAT_MEDIA_MESSAGE) {
            if (StringUtil.isEmpty(message.getFileName()) || message.getFileSize() == null || message.getFileType() == null) {
                throw new BusinessException("媒体消息文件信息不完整");
            }
            message.setFileSuffix(StringUtil.getFileSuffix(message.getFileName()));
            message.setStatus(ChatMessageStatus.SENDING.getStatus());
        }

        // 获取分表名并插入
        String tableName = TableSplitUtil.getTableName(message.getMeetingId());
        chatMessageMapper.insert(tableName, message);

        // 构建 WebSocket 推送 DTO 并发送消息
        MessageSendDTO messageSendDTO = MessageSendDTOBuilder.fromChatMessage(message);
        messageHandler.sendMessage(messageSendDTO);

        return message;
    }

    /**
     * 验证接收类型
     * - 当 receiveType 为 null 时默认设置为群发(0)
     * - 当 receiveType 为私聊(1)但 receiveUserId 为空时抛出异常
     * - 当 receiveType 为非法值时抛出异常
     */
    private void validateReceiveType(ChatMessage message) {
        // 默认设置为群发
        if (message.getReceiveType() == null) {
            message.setReceiveType(ReceiveType.ALL.getType());
            return;
        }

        // 验证接收类型是否合法
        ReceiveType receiveTypeEnum = ReceiveType.getByType(message.getReceiveType());
        if (receiveTypeEnum == null) {
            throw new BusinessException("不支持的接收类型");
        }

        // 私聊消息必须指定接收者
        if (receiveTypeEnum == ReceiveType.USER) {
            if (StringUtil.isEmpty(message.getReceiveUserId())) {
                throw new BusinessException("私聊消息必须指定接收者");
            }
        }
    }

    @Override
    public ChatMessage getMessageById(String meetingId, Long messageId) {
        if (meetingId == null || meetingId.isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }
        if (messageId == null) {
            throw new BusinessException("消息ID不能为空");
        }

        String tableName = TableSplitUtil.getTableName(meetingId);
        return chatMessageMapper.selectById(tableName, messageId);
    }

    @Override
    public PageResult<ChatMessage> getMessagesByMeetingId(String meetingId, Integer pageNo, Integer pageSize) {
        if (meetingId == null || meetingId.isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        String tableName = TableSplitUtil.getTableName(meetingId);
        int offset = (pageNo - 1) * pageSize;

        Long total = chatMessageMapper.countByMeetingId(tableName, meetingId);
        List<ChatMessage> list = chatMessageMapper.selectByMeetingId(tableName, meetingId, offset, pageSize);

        /* list.forEach(chatMessage -> {
            log.debug("【原list中】：{}", chatMessage.debugField());
        }); */

        String currentUserId = UserContext.getCurrentUserId();
        // 从结果集筛选当前用户可见的消息（可也额外定义mapper方法再db层过滤）
        List<ChatMessage> finalList = list.stream().filter(chatMessage -> {
            // 先检查群聊类型
            String receiveUserId = chatMessage.getReceiveUserId();
            if (receiveUserId.equals("0") || chatMessage.getReceiveType() == 0) {
                return true;
            }
            // 再检查私聊类型
            String sendUserId = chatMessage.getSendUserId();
            if (currentUserId.equals(receiveUserId) || currentUserId.equals(sendUserId)) {
                return true;
            }
            return false;
        }).toList();

        /* finalList.forEach(chatMessage -> {
            log.debug("【新list中】：{}", chatMessage.debugField());
        }); */

        return PageResult.of(pageNo, pageSize, total, finalList);
    }

    @Override
    public ChatMessage updateMessage(ChatMessage message) {
        if (message.getMeetingId() == null || message.getMeetingId().isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }
        if (message.getMessageId() == null) {
            throw new BusinessException("消息ID不能为空");
        }

        String tableName = TableSplitUtil.getTableName(message.getMeetingId());
        chatMessageMapper.updateById(tableName, message);

        return message;
    }

    @Override
    public void deleteMessage(String meetingId, Long messageId) {
        if (meetingId == null || meetingId.isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }
        if (messageId == null) {
            throw new BusinessException("消息ID不能为空");
        }

        String tableName = TableSplitUtil.getTableName(meetingId);
        chatMessageMapper.deleteById(tableName, messageId);
    }

    @Override
    public PageResult<ChatMessage> getMessagesBeforeId(String meetingId, Long maxMessageId, Integer pageSize) {
        if (meetingId == null || meetingId.isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }
        if (maxMessageId == null) {
            throw new BusinessException("消息ID不能为空");
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        String tableName = TableSplitUtil.getTableName(meetingId);
        List<ChatMessage> list = chatMessageMapper.selectBeforeMessageId(tableName, meetingId, maxMessageId, pageSize);
        Long total = chatMessageMapper.countByMeetingId(tableName, meetingId);

        return PageResult.of(1, pageSize, total, list);
    }

    @Override
    public PageResult<ChatMessage> getMessagesByMeetingId(String meetingId, String currentUserId, Integer pageNo, Integer pageSize) {
        if (meetingId == null || meetingId.isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }
        if (currentUserId == null || currentUserId.isEmpty()) {
            throw new BusinessException("当前用户ID不能为空");
        }
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        String tableName = TableSplitUtil.getTableName(meetingId);
        int offset = (pageNo - 1) * pageSize;

        Long total = chatMessageMapper.countByMeetingIdWithPrivateFilter(tableName, meetingId, currentUserId);
        List<ChatMessage> list = chatMessageMapper.selectByMeetingIdWithPrivateFilter(tableName, meetingId, currentUserId, offset, pageSize);

        return PageResult.of(pageNo, pageSize, total, list);
    }

    @Override
    public PageResult<ChatMessage> getMessagesBeforeId(String meetingId, String currentUserId, Long maxMessageId, Integer pageSize) {
        if (meetingId == null || meetingId.isEmpty()) {
            throw new BusinessException("会议ID不能为空");
        }
        if (currentUserId == null || currentUserId.isEmpty()) {
            throw new BusinessException("当前用户ID不能为空");
        }
        if (maxMessageId == null) {
            throw new BusinessException("消息ID不能为空");
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        String tableName = TableSplitUtil.getTableName(meetingId);
        List<ChatMessage> list = chatMessageMapper.selectBeforeMessageIdWithPrivateFilter(tableName, meetingId, currentUserId, maxMessageId, pageSize);
        Long total = chatMessageMapper.countByMeetingIdWithPrivateFilter(tableName, meetingId, currentUserId);

        return PageResult.of(1, pageSize, total, list);
    }

    /**
     * 验证消息类型
     * 只允许聊天消息类型（文本消息和媒体消息）
     */
    private void validateMessageType(Integer messageType) {
        if (messageType == null) {
            throw new BusinessException("消息类型不能为空");
        }
        // 只允许聊天消息类型：5-文本消息，6-媒体消息
        if (!MessageType.CHAT_TEXT_MESSAGE.getType().equals(messageType)
                && !MessageType.CHAT_MEDIA_MESSAGE.getType().equals(messageType)) {
            throw new BusinessException("不支持的消息类型");
        }
    }

    /**
     * 生成消息ID（简化版雪花算法）
     * 生产环境建议使用 Hutool 或专门的分布式 ID 生成器
     */
    private synchronized long generateMessageId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 当前毫秒序列号用完，等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 简化版：时间戳左移22位 + 序列号
        return (timestamp << 22) | sequence;
    }

    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
