package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.enums.ChatMessageStatus;
import cn.clazs.easymeeting.entity.enums.MessageSendToType;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.entity.enums.UserContactStatus;
import cn.clazs.easymeeting.entity.po.PrivateChatMessage;
import cn.clazs.easymeeting.entity.po.PrivateChatUnread;
import cn.clazs.easymeeting.entity.po.UserContact;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.PrivateChatMessageMapper;
import cn.clazs.easymeeting.mapper.PrivateChatUnreadMapper;
import cn.clazs.easymeeting.service.PrivateChatService;
import cn.clazs.easymeeting.service.UserContactService;
import cn.clazs.easymeeting.util.SessionIdUtil;
import cn.clazs.easymeeting.util.StringUtil;
import cn.clazs.easymeeting.util.TableSplitUtil;
import cn.clazs.easymeeting.websocket.messaging.MessageHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会议外 - 私聊消息服务实现类
 * 处理好友之间的一对一私聊消息
 */
@Service
public class PrivateChatServiceImpl implements PrivateChatService {

    /**
     * 私聊消息分表前缀
     */
    private static final String PRIVATE_CHAT_TABLE_PREFIX = "private_chat_message";

    /**
     * 消息内容最大长度
     */
    private static final int MAX_MESSAGE_LENGTH = 500;

    @Resource
    private PrivateChatMessageMapper privateChatMessageMapper;

    @Resource
    private PrivateChatUnreadMapper privateChatUnreadMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private MessageHandler messageHandler;

    /**
     * 雪花算法相关参数
     */
    private static long lastTimestamp = -1L;
    private static long sequence = 0L;
    private static final long SEQUENCE_MASK = 4095L;


    @Override
    public PrivateChatMessage sendMessage(PrivateChatMessage message) {
        // 参数校验
        validateSendMessage(message);

        // 好友关系验证
        validateFriendship(message.getSendUserId(), message.getReceiveUserId());

        // 生成 sessionId
        String sessionId = SessionIdUtil.generateSessionId(message.getSendUserId(), message.getReceiveUserId());
        message.setSessionId(sessionId);

        // 生成消息ID
        if (message.getMessageId() == null) {
            message.setMessageId(generateMessageId());
        }

        // 设置发送时间
        if (message.getSendTime() == null) {
            message.setSendTime(System.currentTimeMillis());
        }

        // 设置消息状态
        MessageType messageTypeEnum = MessageType.getByType(message.getMessageType());
        if (messageTypeEnum == MessageType.CHAT_TEXT_MESSAGE) {
            message.setStatus(ChatMessageStatus.SENDEND.getStatus());
        } else if (messageTypeEnum == MessageType.CHAT_MEDIA_MESSAGE) {
            if (StringUtil.isEmpty(message.getFileName()) || message.getFileSize() == null || message.getFileType() == null) {
                throw new BusinessException("媒体消息文件信息不完整");
            }
            message.setFileSuffix(StringUtil.getFileSuffix(message.getFileName()));
            message.setStatus(ChatMessageStatus.SENDING.getStatus());
        }

        // 获取分表名并保存消息
        String tableName = getPrivateChatTableName(sessionId);
        privateChatMessageMapper.insert(tableName, message);

        // 增加接收者的未读计数
        String messagePreview = getMessagePreview(message);
        privateChatUnreadMapper.incrementUnreadCount(
                message.getReceiveUserId(),
                message.getSendUserId(),
                message.getSendTime(),
                messagePreview
        );

        // 通过 WebSocket 推送消息
        sendWebSocketMessage(message);

        return message;
    }

    /**
     * 验证发送消息参数
     */
    private void validateSendMessage(PrivateChatMessage message) {
        if (StringUtil.isEmpty(message.getSendUserId())) {
            throw new BusinessException("发送者ID不能为空");
        }
        if (StringUtil.isEmpty(message.getReceiveUserId())) {
            throw new BusinessException("接收者ID不能为空");
        }
        if (message.getSendUserId().equals(message.getReceiveUserId())) {
            throw new BusinessException("不能给自己发送消息");
        }
        if (message.getMessageType() == null) {
            throw new BusinessException("消息类型不能为空");
        }

        // 验证消息类型
        MessageType messageTypeEnum = MessageType.getByType(message.getMessageType());
        if (messageTypeEnum != MessageType.CHAT_TEXT_MESSAGE
                && messageTypeEnum != MessageType.CHAT_MEDIA_MESSAGE) {
            throw new BusinessException("不支持的消息类型");
        }

        // 文本消息内容校验
        if (messageTypeEnum == MessageType.CHAT_TEXT_MESSAGE) {
            if (StringUtil.isEmpty(message.getMessageContent())) {
                throw new BusinessException("消息内容不能为空");
            }
            if (message.getMessageContent().length() > MAX_MESSAGE_LENGTH) {
                throw new BusinessException("消息内容不能超过" + MAX_MESSAGE_LENGTH + "字符");
            }
        }
    }

    /**
     * 验证好友关系
     */
    private void validateFriendship(String sendUserId, String receiveUserId) {
        // 检查发送者是否有接收者为好友
        UserContact senderContact = userContactService.getByUserIdAndContactId(sendUserId, receiveUserId);
        if (senderContact == null || !UserContactStatus.NORMAL.getStatus().equals(senderContact.getStatus())) {
            throw new BusinessException("只能给好友发送消息");
        }

        // 检查接收者是否将发送者拉黑
        UserContact receiverContact = userContactService.getByUserIdAndContactId(receiveUserId, sendUserId);
        if (receiverContact != null && UserContactStatus.BLACK.getStatus().equals(receiverContact.getStatus())) {
            throw new BusinessException("对方已将你拉黑，无法发送消息");
        }
        if (receiverContact != null && UserContactStatus.DEL.getStatus().equals(receiverContact.getStatus())) {
            throw new BusinessException("对方已将你删除，无法发送消息");
        }
    }

    /**
     * 获取消息预览（用于未读消息显示）
     */
    private String getMessagePreview(PrivateChatMessage message) {
        if (MessageType.CHAT_MEDIA_MESSAGE.getType().equals(message.getMessageType())) {
            return "[文件] " + message.getFileName();
        }
        String content = message.getMessageContent();
        if (content != null && content.length() > 50) {
            return content.substring(0, 50) + "...";
        }
        return content;
    }

    /**
     * 发送 WebSocket 消息
     */
    private void sendWebSocketMessage(PrivateChatMessage message) {
        MessageSendDTO dto = new MessageSendDTO();
        dto.setMessageId(message.getMessageId());
        dto.setMessageType(MessageType.getByType(message.getMessageType()));
        dto.setSendUserId(message.getSendUserId());
        dto.setSendUserNickName(message.getSendUserNickName());
        dto.setMessageContent(message.getMessageContent());
        dto.setSendTime(message.getSendTime());
        dto.setStatus(message.getStatus());
        dto.setMessageSendToType(MessageSendToType.USER);
        dto.setReceiveUserId(message.getReceiveUserId());

        // 媒体消息附加文件信息
        if (MessageType.CHAT_MEDIA_MESSAGE.getType().equals(message.getMessageType())) {
            dto.setFileName(message.getFileName());
            dto.setFileType(message.getFileType());
            dto.setFileSize(message.getFileSize());
        }

        messageHandler.sendMessage(dto);
    }

    @Override
    public PageResult<PrivateChatMessage> getChatHistory(String userId, String contactId, Integer pageNo, Integer pageSize) {
        if (StringUtil.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        if (StringUtil.isEmpty(contactId)) {
            throw new BusinessException("联系人ID不能为空");
        }
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        String sessionId = SessionIdUtil.generateSessionId(userId, contactId);
        String tableName = getPrivateChatTableName(sessionId);
        int offset = (pageNo - 1) * pageSize;

        Long total = privateChatMessageMapper.countBySessionId(tableName, sessionId);
        List<PrivateChatMessage> list = privateChatMessageMapper.selectBySessionId(tableName, sessionId, offset, pageSize);

        return PageResult.of(pageNo, pageSize, total, list);
    }

    @Override
    public PageResult<PrivateChatMessage> getMessagesBeforeId(String userId, String contactId, Long maxMessageId, Integer pageSize) {
        if (StringUtil.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        if (StringUtil.isEmpty(contactId)) {
            throw new BusinessException("联系人ID不能为空");
        }
        if (maxMessageId == null) {
            throw new BusinessException("消息ID不能为空");
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        String sessionId = SessionIdUtil.generateSessionId(userId, contactId);
        String tableName = getPrivateChatTableName(sessionId);

        List<PrivateChatMessage> list = privateChatMessageMapper.selectBeforeMessageId(tableName, sessionId, maxMessageId, pageSize);
        Long total = privateChatMessageMapper.countBySessionId(tableName, sessionId);

        return PageResult.of(1, pageSize, total, list);
    }


    @Override
    public Integer getUnreadCount(String userId, String contactId) {
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(contactId)) {
            return 0;
        }
        Integer count = privateChatUnreadMapper.getUnreadCount(userId, contactId);
        return count != null ? count : 0;
    }

    @Override
    public void markAsRead(String userId, String contactId) {
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(contactId)) {
            return;
        }
        privateChatUnreadMapper.clearUnreadCount(userId, contactId);
    }

    @Override
    public Map<String, Integer> getAllUnreadCounts(String userId) {
        Map<String, Integer> result = new HashMap<>();
        if (StringUtil.isEmpty(userId)) {
            return result;
        }

        List<PrivateChatUnread> unreadList = privateChatUnreadMapper.selectByUserId(userId);
        for (PrivateChatUnread unread : unreadList) {
            result.put(unread.getContactId(), unread.getUnreadCount());
        }
        return result;
    }

    @Override
    public List<PrivateChatUnread> getUnreadList(String userId) {
        if (StringUtil.isEmpty(userId)) {
            return List.of();
        }
        return privateChatUnreadMapper.selectByUserId(userId);
    }

    /**
     * 获取私聊消息分表名
     */
    private String getPrivateChatTableName(String sessionId) {
        return TableSplitUtil.getTableName(PRIVATE_CHAT_TABLE_PREFIX, TableSplitUtil.DEFAULT_TABLE_COUNT, sessionId);
    }

    /**
     * 生成消息ID（简化版雪花算法）
     */
    private synchronized long generateMessageId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;
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
