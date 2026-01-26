package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.dto.MessageSendDTO;
import cn.clazs.easymeeting.entity.enums.MessageSendToType;
import cn.clazs.easymeeting.entity.enums.MessageType;
import cn.clazs.easymeeting.entity.enums.UserContactApplyStatus;
import cn.clazs.easymeeting.entity.enums.UserContactStatus;
import cn.clazs.easymeeting.entity.po.UserContact;
import cn.clazs.easymeeting.entity.po.UserContactApply;
import cn.clazs.easymeeting.entity.vo.UserContactApplyVO;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.UserContactApplyMapper;
import cn.clazs.easymeeting.mapper.UserContactMapper;
import cn.clazs.easymeeting.service.UserContactApplyService;
import cn.clazs.easymeeting.websocket.messaging.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserContactApplyServiceImpl implements UserContactApplyService {

    private final UserContactApplyMapper userContactApplyMapper;
    private final UserContactMapper userContactMapper;
    private final MessageHandler messageHandler;

    @Override
    public UserContactApply createApply(UserContactApply userContactApply) {
        // 检查是否已存在待处理的申请
        UserContactApply existing = userContactApplyMapper.selectByApplyUserIdAndReceiveUserId(
                userContactApply.getApplyUserId(), userContactApply.getReceiveUserId());
        if (existing != null && existing.getStatus().equals(UserContactApplyStatus.INIT.getStatus())) {
            throw new BusinessException("已存在待处理的申请");
        }

        userContactApply.setStatus(UserContactApplyStatus.INIT.getStatus()); // 待处理
        // last_apply_time由数据库自动设置
        userContactApplyMapper.insert(userContactApply);
        log.info("创建联系人申请成功: applyUserId={}, receiveUserId={}",
                userContactApply.getApplyUserId(), userContactApply.getReceiveUserId());
        return userContactApply;
    }

    @Override
    public UserContactApply createOrUpdateApply(UserContactApply userContactApply) {
        userContactApply.setStatus(UserContactApplyStatus.INIT.getStatus()); // 待处理
        // 利用唯一索引，插入或更新
        userContactApplyMapper.insertOrUpdate(userContactApply);
        log.info("创建或更新联系人申请成功: applyUserId={}, receiveUserId={}",
                userContactApply.getApplyUserId(), userContactApply.getReceiveUserId());
        // 返回最新数据
        return userContactApplyMapper.selectByApplyUserIdAndReceiveUserId(
                userContactApply.getApplyUserId(), userContactApply.getReceiveUserId());
    }

    @Override
    public UserContactApply updateApply(UserContactApply userContactApply) {
        UserContactApply existing = userContactApplyMapper.selectById(userContactApply.getApplyId());
        if (existing == null) {
            throw new BusinessException("申请不存在");
        }
        userContactApplyMapper.updateById(userContactApply);
        return userContactApplyMapper.selectById(userContactApply.getApplyId());
    }

    @Override
    public void deleteById(Integer applyId) {
        userContactApplyMapper.deleteById(applyId);
        log.info("删除联系人申请成功: applyId={}", applyId);
    }

    @Override
    public UserContactApply getById(Integer applyId) {
        return userContactApplyMapper.selectById(applyId);
    }

    @Override
    public List<UserContactApply> getAll() {
        return userContactApplyMapper.selectAll();
    }

    @Override
    public List<UserContactApply> getByApplyUserId(String applyUserId) {
        return userContactApplyMapper.selectByApplyUserId(applyUserId);
    }

    @Override
    public List<UserContactApply> getByReceiveUserId(String receiveUserId) {
        return userContactApplyMapper.selectByReceiveUserId(receiveUserId);
    }

    @Override
    public List<UserContactApply> getByStatus(Integer status) {
        return userContactApplyMapper.selectByStatus(status);
    }

    @Override
    public UserContactApply getByApplyUserIdAndReceiveUserId(String applyUserId, String receiveUserId) {
        return userContactApplyMapper.selectByApplyUserIdAndReceiveUserId(applyUserId, receiveUserId);
    }

    @Override
    public void updateStatus(Integer applyId, Integer status) {
        UserContactApply existing = userContactApplyMapper.selectById(applyId);
        if (existing == null) {
            throw new BusinessException("申请不存在");
        }
        userContactApplyMapper.updateStatus(applyId, status);
        log.info("更新联系人申请状态: applyId={}, status={}", applyId, status);
    }

    @Override
    public void updateStatusByApplyUserIdAndReceiveUserId(String applyUserId, String receiveUserId, Integer status) {
        userContactApplyMapper.updateStatusByApplyUserIdAndReceiveUserId(applyUserId, receiveUserId, status);
        log.info("更新联系人申请状态: applyUserId={}, receiveUserId={}, status={}", applyUserId, receiveUserId, status);
    }

    @Override
    public List<UserContactApply> getPendingByReceiveUserId(String receiveUserId) {
        return userContactApplyMapper.selectPendingByReceiveUserId(receiveUserId);
    }

    @Override
    public List<UserContactApply> getSentByApplyUserId(String applyUserId) {
        return userContactApplyMapper.selectSentByApplyUserId(applyUserId);
    }

    @Override
    public boolean existsPendingApply(String applyUserId, String receiveUserId) {
        UserContactApply apply = userContactApplyMapper.selectByApplyUserIdAndReceiveUserId(applyUserId, receiveUserId);
        return apply != null && apply.getStatus().equals(UserContactApplyStatus.INIT.getStatus());
    }

    @Override
    public Integer saveUserContactApply(UserContactApply userContactApply) {
        String applyUserId = userContactApply.getApplyUserId();
        String receiveUserId = userContactApply.getReceiveUserId();

        // 检查对方是否已将我拉黑
        UserContact opponentContact = userContactMapper.selectByUserIdAndContactId(receiveUserId, applyUserId);
        if (opponentContact != null && UserContactStatus.BLACK.getStatus().equals(opponentContact.getStatus())) {
            throw new BusinessException("对方已将你拉黑");
        }

        // 对方已有我为好友（NORMAL状态），直接互加好友，无需审批
        if (opponentContact != null && UserContactStatus.NORMAL.getStatus().equals(opponentContact.getStatus())) {
            // 使用insertOrUpdate，处理我方记录可能存在但状态为DEL的情况
            UserContact myContact = new UserContact();
            myContact.setUserId(applyUserId);
            myContact.setContactId(receiveUserId);
            myContact.setStatus(UserContactStatus.NORMAL.getStatus());
            userContactMapper.insertOrUpdate(myContact);
            log.info("对方已是好友，直接互加成功: applyUserId={}, receiveUserId={}", applyUserId, receiveUserId);
            return UserContactApplyStatus.PASS.getStatus(); // 返回已通过状态
        }

        // 否则创建或更新申请记录
        userContactApply.setStatus(UserContactApplyStatus.INIT.getStatus());
        userContactApplyMapper.insertOrUpdate(userContactApply);
        log.info("创建好友申请: applyUserId={}, receiveUserId={}", applyUserId, receiveUserId);

        // 发送消息通知对方（需要注入MessageHandler）
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setMessageSendToType(MessageSendToType.USER);  // 发给个人
        messageSendDTO.setMessageType(MessageType.USER_CONTACT_APPLY); // 好友申请类型
        messageSendDTO.setSendUserId(applyUserId);
        messageSendDTO.setReceiveUserId(receiveUserId);  // 接收者
        messageSendDTO.setMessageContent(userContactApply);  // 申请详情

        messageHandler.sendMessage(messageSendDTO);  // 发送消息

        return UserContactApplyStatus.INIT.getStatus(); // 返回待处理状态
    }

    /**
     * 处理好友申请
     * @param applyUserId 申请人ID
     * @param userId 处理人ID（当前用户）
     * @param nickName 处理人昵称
     * @param status 处理状态：1-同意，2-拒绝，3-拉黑
     */
    @Override
    public void dealWithApply(String applyUserId, String userId, String nickName, Integer status) {
        // 1. 校验状态参数
        UserContactApplyStatus statusEnum = UserContactApplyStatus.getUserContactApplyStatus(status);
        if (statusEnum == null || UserContactApplyStatus.INIT.getStatus().equals(statusEnum.getStatus())) {
            throw new BusinessException("无效的处理状态");
        }

        // 2. 查询申请记录
        UserContactApply userContactApply = userContactApplyMapper.selectByApplyUserIdAndReceiveUserId(applyUserId, userId);
        if (userContactApply == null) {
            throw new BusinessException("申请记录不存在");
        }

        // 3. 检查申请是否已处理
        if (!UserContactApplyStatus.INIT.getStatus().equals(userContactApply.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        // 4. 如果同意，双向添加好友
        if (UserContactApplyStatus.PASS.equals(statusEnum)) {
            // 申请人添加处理人为好友
            UserContact contact1 = new UserContact();
            contact1.setUserId(applyUserId);
            contact1.setContactId(userId);
            contact1.setStatus(UserContactStatus.NORMAL.getStatus());
            userContactMapper.insertOrUpdate(contact1);

            // 处理人添加申请人为好友
            UserContact contact2 = new UserContact();
            contact2.setUserId(userId);
            contact2.setContactId(applyUserId);
            contact2.setStatus(UserContactStatus.NORMAL.getStatus());
            userContactMapper.insertOrUpdate(contact2);

            log.info("好友添加成功: {} <-> {}", applyUserId, userId);
        }

        // 5. 如果拉黑，更新联系人状态为拉黑
        if (UserContactApplyStatus.BLACKLIST.equals(statusEnum)) {
            UserContact blackContact = new UserContact();
            blackContact.setUserId(userId);
            blackContact.setContactId(applyUserId);
            blackContact.setStatus(UserContactStatus.BLACK.getStatus());
            userContactMapper.insertOrUpdate(blackContact);
            log.info("已拉黑用户: {} -> {}", userId, applyUserId);
        }

        // 6. 更新申请状态
        userContactApplyMapper.updateStatusByApplyUserIdAndReceiveUserId(applyUserId, userId, status);
        log.info("处理好友申请: applyUserId={}, userId={}, status={}", applyUserId, userId, statusEnum.getDesc());

        // 7. 发送消息通知申请人（处理结果）
        // 即使对方不在线，前端联系人页面刷新时会显示最新状态
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setMessageSendToType(MessageSendToType.USER);
        messageSendDTO.setMessageType(MessageType.USER_CONTACT_APPLY);
        messageSendDTO.setSendUserId(userId);
        messageSendDTO.setSendUserNickName(nickName);
        messageSendDTO.setReceiveUserId(applyUserId);  // 通知申请人

        // 更新申请对象的状态后发送
        userContactApply.setStatus(status);
        messageSendDTO.setMessageContent(userContactApply);

        messageHandler.sendMessage(messageSendDTO);
    }

    @Override
    public List<UserContactApplyVO> getReceivedAppliesWithNickName(String receiveUserId) {
        return userContactApplyMapper.selectReceivedAppliesWithNickName(receiveUserId);
    }

}
