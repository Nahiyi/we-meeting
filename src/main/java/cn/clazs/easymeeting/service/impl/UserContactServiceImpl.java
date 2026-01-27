package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.enums.ContactSearchStatus;
import cn.clazs.easymeeting.entity.enums.UserContactApplyStatus;
import cn.clazs.easymeeting.entity.enums.UserContactStatus;
import cn.clazs.easymeeting.entity.po.PrivateChatUnread;
import cn.clazs.easymeeting.entity.po.UserContact;
import cn.clazs.easymeeting.entity.po.UserContactApply;
import cn.clazs.easymeeting.entity.po.UserInfo;
import cn.clazs.easymeeting.entity.vo.UserContactVO;
import cn.clazs.easymeeting.entity.vo.UserInfoVoForSearch;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.PrivateChatUnreadMapper;
import cn.clazs.easymeeting.mapper.UserContactApplyMapper;
import cn.clazs.easymeeting.mapper.UserContactMapper;
import cn.clazs.easymeeting.mapper.UserMapper;
import cn.clazs.easymeeting.service.UserContactService;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserContactServiceImpl implements UserContactService {

    private final UserContactMapper userContactMapper;
    private final UserMapper userMapper;
    private final UserContactApplyMapper userContactApplyMapper;
    private final BizChannelContext bizChannelContext;
    private final PrivateChatUnreadMapper privateChatUnreadMapper;

    @Override
    public UserContact addContact(UserContact userContact) {
        // 检查是否已存在
        UserContact existing = userContactMapper.selectByUserIdAndContactId(
                userContact.getUserId(), userContact.getContactId());
        if (existing != null) {
            // 如果已存在且状态为正常，抛出异常
            if (UserContactStatus.NORMAL.getStatus().equals(existing.getStatus())) {
                throw new BusinessException("联系人已存在");
            }
            // 如果已存在但状态为删除或拉黑，更新状态为正常
            userContactMapper.updateStatus(userContact.getUserId(), userContact.getContactId(),
                    UserContactStatus.NORMAL.getStatus());
            log.info("恢复联系人成功: userId={}, contactId={}", userContact.getUserId(), userContact.getContactId());
            return userContactMapper.selectByUserIdAndContactId(userContact.getUserId(), userContact.getContactId());
        }
        userContactMapper.insert(userContact);
        log.info("添加联系人成功: userId={}, contactId={}", userContact.getUserId(), userContact.getContactId());
        return userContact;
    }

    @Override
    public UserContact updateContact(UserContact userContact) {
        UserContact existing = userContactMapper.selectByUserId(userContact.getUserId());
        if (existing == null) {
            throw new BusinessException("联系人不存在");
        }
        userContactMapper.updateByUserId(userContact);
        return userContactMapper.selectByUserId(userContact.getUserId());
    }

    @Override
    public void deleteByUserId(String userId) {
        userContactMapper.deleteByUserId(userId);
        log.info("删除联系人成功: userId={}", userId);
    }

    @Override
    public void deleteByContactId(String contactId) {
        userContactMapper.deleteByContactId(contactId);
        log.info("删除联系人成功: contactId={}", contactId);
    }

    @Override
    public UserContact getByUserId(String userId) {
        return userContactMapper.selectByUserId(userId);
    }

    @Override
    public UserContact getByContactId(String contactId) {
        return userContactMapper.selectByContactId(contactId);
    }

    @Override
    public UserContact getByUserIdAndContactId(String userId, String contactId) {
        return userContactMapper.selectByUserIdAndContactId(userId, contactId);
    }

    @Override
    public List<UserContact> getContactsByUserId(String userId) {
        return userContactMapper.selectContactsByUserId(userId);
    }

    @Override
    public List<UserContact> getNormalContactsByUserId(String userId) {
        return userContactMapper.selectNormalContactsByUserId(userId);
    }

    @Override
    public List<UserContact> getByStatus(Integer status) {
        return userContactMapper.selectByStatus(status);
    }

    @Override
    public List<UserContact> getAll() {
        return userContactMapper.selectAll();
    }

    @Override
    public void updateStatus(String userId, String contactId, Integer status) {
        userContactMapper.updateStatus(userId, contactId, status);
        log.info("更新联系人状态: userId={}, contactId={}, status={}", userId, contactId, status);
    }

    @Override
    public boolean isContact(String userId, String contactId) {
        UserContact contact = userContactMapper.selectByUserIdAndContactId(userId, contactId);
        return contact != null && UserContactStatus.NORMAL.getStatus().equals(contact.getStatus());
    }

    @Override
    public UserInfoVoForSearch searchContact(String userId, String contactId) {
        UserInfo userInfo = userMapper.selectById(contactId);
        if (userInfo == null) {
            return null;
        }

        UserInfoVoForSearch result = new UserInfoVoForSearch();
        result.setUserId(userInfo.getUserId());
        result.setNickName(userInfo.getNickName());

        // 1. 搜索自己
        if (userId.equals(contactId)) {
            result.setStatus(ContactSearchStatus.SELF.getStatus());
            return result;
        }

        UserContactApply userContactApply = userContactApplyMapper
                .selectByApplyUserIdAndReceiveUserId(userId, contactId);
        UserContact userContact = userContactMapper
                .selectByUserIdAndContactId(contactId, userId); // 对方是否有我
        UserContact myContact = userContactMapper
                .selectByUserIdAndContactId(userId, contactId); // 我是否有对方

        // 2. 被拉黑
        boolean isApplyBlacklisted = userContactApply != null
                && UserContactApplyStatus.BLACKLIST.getStatus().equals(userContactApply.getStatus());
        boolean isContactBlacklisted = userContact != null
                && UserContactStatus.BLACK.getStatus().equals(userContact.getStatus());
        if (isApplyBlacklisted || isContactBlacklisted) {
            result.setStatus(ContactSearchStatus.BLACKLISTED.getStatus());
            return result;
        }

        // 3. 已是好友（双向）
        boolean iAmHisFriend = userContact != null
                && UserContactStatus.NORMAL.getStatus().equals(userContact.getStatus());
        boolean heIsMyFriend = myContact != null
                && UserContactStatus.NORMAL.getStatus().equals(myContact.getStatus());
        if (iAmHisFriend && heIsMyFriend) {
            result.setStatus(ContactSearchStatus.FRIEND.getStatus());
            return result;
        }

        // 4. 对方已添加我但我没添加对方（单向好友，可直接添加）
        if (iAmHisFriend && !heIsMyFriend) {
            result.setStatus(ContactSearchStatus.BE_FRIEND.getStatus());
            return result;
        }

        // 5. 申请待处理
        if (userContactApply != null
                && UserContactApplyStatus.INIT.getStatus().equals(userContactApply.getStatus())) {
            result.setStatus(ContactSearchStatus.PENDING.getStatus());
            return result;
        }

        // 6. 默认：非好友，可申请
        result.setStatus(ContactSearchStatus.NOT_FRIEND.getStatus());
        return result;
    }

    @Override
    public List<UserContactVO> getNormalContactsWithNickName(String userId) {
        List<UserContactVO> contacts = userContactMapper.selectNormalContactsWithNickName(userId);

        // 批量设置实时在线状态（优化：一次性获取所有在线用户ID）
        Set<String> onlineUserIds = bizChannelContext.getOnlineUserIds();

        // 获取所有未读消息记录
        List<PrivateChatUnread> unreadList = privateChatUnreadMapper.selectByUserId(userId);
        Map<String, PrivateChatUnread> unreadMap = unreadList.stream()
                .collect(Collectors.toMap(PrivateChatUnread::getContactId, u -> u, (a, b) -> a));

        for (UserContactVO contact : contacts) {
            contact.setOnline(onlineUserIds.contains(contact.getContactId()));

            // 设置未读消息信息
            PrivateChatUnread unread = unreadMap.get(contact.getContactId());
            if (unread != null) {
                contact.setUnreadCount(unread.getUnreadCount());
                contact.setLastMessageTime(unread.getLastMessageTime());
                contact.setLastMessageContent(unread.getLastMessageContent());
            } else {
                contact.setUnreadCount(0);
            }
        }
        return contacts;
    }
}
