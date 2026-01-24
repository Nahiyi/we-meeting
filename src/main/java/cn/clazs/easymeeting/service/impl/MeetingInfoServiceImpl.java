package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.context.UserContext;
import cn.clazs.easymeeting.entity.dto.*;
import cn.clazs.easymeeting.entity.enums.*;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.entity.po.MeetingMember;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.exception.BusinessException;
import cn.clazs.easymeeting.mapper.MeetingInfoMapper;
import cn.clazs.easymeeting.mapper.MeetingMemberMapper;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.MeetingInfoService;
import cn.clazs.easymeeting.util.StringUtil;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MeetingInfoServiceImpl implements MeetingInfoService {

    @Resource
    private MeetingInfoMapper meetingInfoMapper;
    @Resource
    private BizChannelContext bizChannelContext;
    @Resource
    private MeetingMemberMapper meetingMemberMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private AsyncMeetingInfoServiceImpl asyncMeetingInfoService;

    @Override
    public MeetingInfo createMeeting(MeetingInfo meetingInfo) {
        meetingInfoMapper.insert(meetingInfo);
        return meetingInfo;
    }

    @Override
    public MeetingInfo updateMeeting(MeetingInfo meetingInfo) {
        meetingInfoMapper.updateById(meetingInfo);
        return meetingInfo;
    }

    @Override
    public MeetingInfo getMeetingById(String meetingId) {
        return meetingInfoMapper.selectById(meetingId);
    }

    @Override
    public MeetingInfo getMeetingByNo(String meetingNo) {
        return meetingInfoMapper.selectByMeetingNo(meetingNo);
    }

    @Override
    public List<MeetingInfo> getMeetingsByUserId(String userId) {
        return meetingInfoMapper.selectByCreateUserId(userId);
    }

    @Override
    public List<MeetingInfo> listMeetings(Integer status) {
        return meetingInfoMapper.selectList(status);
    }

    @Override
    public void deleteMeeting(String meetingId) {
        meetingInfoMapper.deleteById(meetingId);
    }

    @Override
    public PageResult<MeetingInfo> loadAllMeetings(String userId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        Long total = meetingInfoMapper.countAllMeetings(userId);
        List<MeetingInfo> list = meetingInfoMapper.selectAllMeetings(userId, offset, pageSize);
        return PageResult.of(pageNo, pageSize, total, list);
    }

    @Override
    public PageResult<MeetingInfo> loadCreatedMeetings(String userId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        Long total = meetingInfoMapper.countCreatedMeetings(userId);
        List<MeetingInfo> list = meetingInfoMapper.selectCreatedMeetings(userId, offset, pageSize);
        return PageResult.of(pageNo, pageSize, total, list);
    }

    @Override
    public PageResult<MeetingInfo> loadJoinedMeetings(String userId, Integer pageNo, Integer pageSize) {
        int offset = (pageNo - 1) * pageSize;
        Long total = meetingInfoMapper.countJoinedMeetings(userId);
        List<MeetingInfo> list = meetingInfoMapper.selectJoinedMeetings(userId, offset, pageSize);
        return PageResult.of(pageNo, pageSize, total, list);
    }

    /**
     * 会议ID是数据库表记录的主键ID，是唯一
     * 会议号是用户和会议的属性，在会议表中，可能不唯一，因为可选使用用户默认会议号作为会议号
     * 后续加入会议，根据会议号即可，因为根据会议号和“进行中”状态，查询结果必然唯一
     * <p>
     * 会议ID是面向存储、查询；会议号仅面向用户感知到的加入会议
     * @param nickName 可自定义入会昵称
     */
    @Override
    public void quickMeeting(MeetingInfo meetingInfo, String nickName) {
        meetingInfo.setCreateTime(LocalDateTime.now());
        meetingInfo.setMeetingId(StringUtil.generateMeetingNo());
        // double-check: 如果没有设置 meetingNo，自动生成一个（预约会议开始时会用到）
        if (StringUtil.isEmpty(meetingInfo.getMeetingNo())) {
            meetingInfo.setMeetingNo(StringUtil.generateMeetingNo());
        }
        // 创建会议并开始会议
        meetingInfo.setStartTime(LocalDateTime.now());
        // 会议状态：进行中
        meetingInfo.setStatus(MeetingStatus.RUNNING.getStatus());
        meetingInfoMapper.insert(meetingInfo);

        UserTokenInfoDTO currentUser = UserContext.getCurrentUser();
        currentUser.setCurrentMeetingId(meetingInfo.getMeetingId());
        currentUser.setCurrentNickName(currentUser.getNickName());
        // 更新缓存中用户信息（会议中+会议ID）
        redisComponent.updateUserTokenInfo(currentUser);
    }

    /**
     * 用户必须先通过 preJoinMeeting 验证，才能调用此接口
     * 用户敏感信息（userId、nickName、sex）从 token 获取，不信任前端传入
     * meetingId 必须与 token 中的 currentMeetingId 一致，防止绕过 preJoin
     */
    @Override
    public void joinMeeting(JoinMeetingDTO joinMeetingDTO) {
        // 再验证 meetingId 与 token 中记录的一致（防止绕过 preJoin）
        String requestMeetingId = joinMeetingDTO.getMeetingId();
        if (StringUtil.isNotEmpty(requestMeetingId)
                && !requestMeetingId.equals(UserContext.getCurrentMeetingId())) {
            throw new BusinessException("会议ID不匹配，请重新加入会议");
        }

        String meetingId = joinMeetingDTO.getMeetingId();
        String userId = joinMeetingDTO.getUserId();

        // 基本参数校验
        if (StringUtil.isEmpty(meetingId)) {
            throw new BusinessException("会议ID不能为空");
        }
        // 轻量级检查：只检查会议是否存在和状态
        // 注意：不再调用 checkMeetingJoin()，因为 preJoinMeeting 已经校验过了
        MeetingInfo meetingInfo = meetingInfoMapper.selectById(meetingId);
        if (meetingInfo == null) {
            throw new BusinessException("会议不存在");
        }

        if (MeetingStatus.FINISHED.getStatus().equals(meetingInfo.getStatus())) {
            throw new BusinessException("会议已结束");
        }

        // 检查加入者是不是创建者，是则其“会议身份”为“主持人”
        MemberType memberType = meetingInfo.getCreateUserId().equals(userId)
                ? MemberType.COMPERE : MemberType.NORMAL;

        // 幂等：插入或更新db一条会议成员记录
        addMeetingMember(meetingId, userId, joinMeetingDTO.getNickName(), memberType);

        // 幂等：加入或更新会议（Redis）
        addToMeeting(meetingId, userId, joinMeetingDTO, memberType);

        // 加入 WebSocket 房间（底层ChannelGroup是Set实现，所以add天然也幂等）
        bizChannelContext.joinMeetingRoom(meetingId, bizChannelContext.getChannel(userId));

        // 延迟发送 WebSocket 消息，确保前端有足够时间注册消息监听器
        // 这解决了新加入用户收不到 ADD_MEETING_ROOM 消息的问题
        MemberJoinMeetingDTO memberJoinMeetingDTO = new MemberJoinMeetingDTO();
        memberJoinMeetingDTO.setNewMember(redisComponent.getMeetingMember(meetingId, userId));
        memberJoinMeetingDTO.setMeetingMemberList(redisComponent.getMeetingMemberList(meetingId));

        // 使用异步方法延迟发送消息，避免阻塞主线程
        asyncMeetingInfoService.delayedSendMemberJoinMessage(meetingId, memberJoinMeetingDTO);
    }

    /**
     * 指定用户以指定身份，加入指定会议（根据传入会议ID定位）
     * 插入同一条数据库用户入会表后，会议成员表记录
     */
    private void addMeetingMember(String meetingId, String userId, String nickName, MemberType memberType) {
        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setMeetingId(meetingId);
        meetingMember.setUserId(userId);
        meetingMember.setNickName(nickName);
        meetingMember.setLastJoinTime(LocalDateTime.now());
        meetingMember.setStatus(MeetingMemberStatus.NORMAL.getStatus());
        meetingMember.setMemberType(memberType.getType());
        meetingMember.setMeetingStatus(MeetingStatus.RUNNING.getStatus());
        if (meetingMemberMapper.selectByMeetingIdAndUserId(meetingId, userId) == null) {
            meetingMemberMapper.insert(meetingMember);
        } else {
            // 存在则更新数据（可重复入会）
            meetingMemberMapper.updateByMeetingIdAndUserId(meetingMember);
        }
    }

    /**
     * 插入用户入会记录到redis会议缓存中
     */
    private void addToMeeting(String meetingId, String userId, JoinMeetingDTO joinMeetingDTO, MemberType memberType) {
        MeetingMemberDTO meetingMemberDTO = new MeetingMemberDTO();
        meetingMemberDTO.setUserId(userId);
        meetingMemberDTO.setNickName(joinMeetingDTO.getNickName());
        meetingMemberDTO.setJoinTime(System.currentTimeMillis());
        meetingMemberDTO.setMemberType(memberType);
        meetingMemberDTO.setStatus(MeetingMemberStatus.NORMAL);
        meetingMemberDTO.setVideoOpen(joinMeetingDTO.getVideoOpen());
        meetingMemberDTO.setSex(joinMeetingDTO.getSex());

        redisComponent.addToMeeting(meetingId, meetingMemberDTO);
    }

    @Override
    public String preJoinMeeting(String meetingNo, String nickName, String password) {
        UserTokenInfoDTO currentUser = UserContext.getCurrentUser();
        currentUser.setNickName(nickName);
        // 根据会议号，查询到正在进行中的会议（会议号+状态限制唯一结果，如果有）
        MeetingInfo meetingInfo = meetingInfoMapper.selectRunningMeetingByMeetingNo(meetingNo);
        if (meetingInfo == null) {
            throw new BusinessException("会议不存在");
        }
        // 缓存检查用户当前可能已存在的会议信息
        String currentMeetingId = currentUser.getCurrentMeetingId();
        if (StringUtil.isNotEmpty(currentMeetingId) &&
                !meetingInfo.getMeetingId().equals(currentMeetingId)) {
            throw new BusinessException("你有未结束的会议");
        }
        // 严格校验合法性
        checkMeetingJoin(meetingInfo.getMeetingId(), currentUser.getUserId());
        // 需要密码的话，还要校验入会密码；不需要密码可跳过此步校验
        if (MeetingJoinType.PASSWORD.getType().equals(meetingInfo.getJoinType()) &&
                !meetingInfo.getJoinPassword().equals(password)) {
            throw new BusinessException("密码错误");
        }
        // 更新缓存中用户的入会状态以及对应的会议（更新缓存用户入会状态、入会ID等数据，具有幂等性）
        currentUser.setCurrentMeetingId(meetingInfo.getMeetingId());
        redisComponent.updateUserTokenInfo(currentUser);
        return meetingInfo.getMeetingId();
    }

    // 缓存与db：双重检查指定用户相对于指定会议的状态是否合法（黑名单检查）
    private void checkMeetingJoin(String meetingId, String userId) {
        // 1. 先检查 Redis 中的状态（如果用户还在会议中）
        MeetingMemberDTO meetingMemberDTO = redisComponent.getMeetingMember(meetingId, userId);
        if (meetingMemberDTO != null &&
                MeetingMemberStatus.BLACKLIST == meetingMemberDTO.getStatus()) {
            throw new BusinessException("已经被拉黑");
        }

        // 2. 检查数据库中的状态（用户被拉黑后会从 Redis 移除，但数据库有记录）
        MeetingMember meetingMember = meetingMemberMapper.selectByMeetingIdAndUserId(meetingId, userId);
        if (meetingMember != null &&
                MeetingMemberStatus.BLACKLIST.getStatus().equals(meetingMember.getStatus())) {
            throw new BusinessException("已经被拉黑");
        }
    }

    /**
     * 用户退出会议
     */
    @Override
    public void exitMeetingRoom() {
        UserTokenInfoDTO currentUser = UserContext.getCurrentUser();
        String meetingId = currentUser.getCurrentMeetingId();
        if (StringUtil.isEmpty(meetingId)) {
            return;
        }
        String userId = currentUser.getUserId();

        // 1. 从 Redis 会议成员列表中移除
        Boolean exit = redisComponent.exitMeeting(meetingId, userId);

        // 2. 清除用户的当前会议ID（Redis 中的 token 信息），使不与后续入会冲突
        currentUser.setCurrentMeetingId(null);
        redisComponent.updateUserTokenInfo(currentUser);

        if (!exit) {
            return;
        }

        // 3. 如果被拉黑，更新数据库中的成员状态
        /* if (MeetingMemberStatus.BLACKLIST == status) {
            MeetingMember meetingMember = meetingMemberMapper.selectByMeetingIdAndUserId(meetingId, userId);
            if (meetingMember != null) {
                meetingMember.setStatus(MeetingMemberStatus.BLACKLIST.getStatus());
                meetingMemberMapper.updateByMeetingIdAndUserId(meetingMember);
            }
        } */

        // 4. 构建退出消息
        List<MeetingMemberDTO> meetingMemberDtoList = redisComponent.getMeetingMemberList(meetingId);
        MeetingExitDTO meetingExitDTO = new MeetingExitDTO();
        meetingExitDTO.setMeetingMemberList(meetingMemberDtoList);
        meetingExitDTO.setExitUserId(userId);
        // meetingExitDTO.setExitStatus(status);

        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setMessageType(MessageType.EXIT_MEETING_ROOM);
        messageSendDTO.setMeetingId(meetingId);
        messageSendDTO.setMessageSendToType(MessageSendToType.GROUP);
        messageSendDTO.setSendUserId(userId);
        messageSendDTO.setMessageContent(meetingExitDTO); // 消息内容为用户退出DTO

        // 5. 发送消息并清理 Channel（从会议房间移除）
        bizChannelContext.sendExitMessageAndCleanup(messageSendDTO, userId);

        // 6. 如果被拉黑或被踢出，可选择强制断开 WebSocket 连接
        // 注意：这会导致用户需要重新建立 WebSocket 连接
        // 如果不需要强制断开，可以注释掉这段代码
        /* if (MeetingMemberStatus.BLACKLIST == status ||
                MeetingMemberStatus.KICK_OUT == status) {
            // 强制关闭用户的 WebSocket 连接，同时清理 USER_CONTEXT_MAP
            bizChannelContext.closeContext(userId);
        } */

        // 7. 检查会议是否还有人，没人则自动结束会议
        if (meetingMemberDtoList == null || meetingMemberDtoList.isEmpty()) {
            // 自动结束会议时传入 null，跳过权限检查
            // TODO 切面获取代理对象执行保障事务性
            finishMeeting(meetingId, null);
        }
    }

    @Override
    public void forceExitMeetingRoom(UserTokenInfoDTO userTokenInfoDTO, String userId, MeetingMemberStatus MeetingMemberStatus) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishMeeting(String meetingId, String userId) {
        MeetingInfo meetingInfo = meetingInfoMapper.selectById(meetingId);
        if (meetingInfo == null) {
            throw new BusinessException("会议不存在");
        }
        if (MeetingStatus.FINISHED.getStatus().equals(meetingInfo.getStatus())) {
            // 会议已结束，直接返回，避免重复处理
            return;
        }
        // 只有主持人可以主动结束会议，自动结束（userId=null）时跳过权限检查
        if (userId != null && !meetingInfo.getCreateUserId().equals(userId)) {
            throw new BusinessException("你没有权限");
        }

        // 1. 更新 MeetingInfo 表状态
        meetingInfo.setStatus(MeetingStatus.FINISHED.getStatus());
        meetingInfo.setEndTime(LocalDateTime.now());
        meetingInfoMapper.updateById(meetingInfo);

        // 2. 获取会议中的所有成员（从 Redis）
        List<MeetingMemberDTO> memberList = redisComponent.getMeetingMemberList(meetingId);

        // 3. 构建并发送会议结束消息（只有还有成员时才发送）
        if (memberList != null && !memberList.isEmpty()) {
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            messageSendDTO.setMessageType(MessageType.FINIS_MEETING);
            messageSendDTO.setMeetingId(meetingId);
            messageSendDTO.setMessageSendToType(MessageSendToType.GROUP);
            messageSendDTO.setSendUserId(userId);
            messageSendDTO.setMessageContent("会议已结束");
            bizChannelContext.sendMessage(messageSendDTO);
        }

        // 4. 批量更新 MeetingMember 表（更新会议状态为已结束）
        List<MeetingMember> dbMembers = meetingMemberMapper.selectByMeetingId(meetingId);
        for (MeetingMember member : dbMembers) {
            member.setMeetingStatus(MeetingStatus.FINISHED.getStatus());
            meetingMemberMapper.updateByMeetingIdAndUserId(member);
        }

        // 4.1 更新预约会议状态为已结束（如果该会议是从预约会议开始的）
       /*  MeetingReserve meetingReserve = meetingReserveMapper.selectByRealMeetingId(meetingId);
        if (meetingReserve != null) {
            meetingReserve.setStatus(MeetingReserveStatus.FINISHED.getStatus());
            meetingReserveMapper.updateById(meetingReserve);
        } */

        // 5. 批量更新 TokenUserInfo（清除 currentMeetingId）并清理 WebSocket 房间
        if (memberList != null) {
            for (MeetingMemberDTO member : memberList) {
                String memberUserId = member.getUserId();
                String token = redisComponent.getTokenByUserId(memberUserId);
                if (token != null) {
                    UserTokenInfoDTO userInfo = redisComponent.getUserTokenInfo(token);
                    if (userInfo != null && meetingId.equals(userInfo.getCurrentMeetingId())) {
                        userInfo.setCurrentMeetingId(null);
                        redisComponent.updateUserTokenInfo(userInfo);
                    }
                }
                // 从 WebSocket 房间移除（需要检查 channel 是否存在）
                Channel channel = bizChannelContext.getChannel(memberUserId);
                if (channel != null) {
                    bizChannelContext.leaveMeetingRoom(meetingId, channel);
                }
            }
        }

        // 6. 清理 Redis 中的会议成员数据
        redisComponent.removeMeetingMembers(meetingId);
    }

    @Override
    public void reserveJoinMeeting(String meetingId, UserTokenInfoDTO userTokenInfoDTO, String password) {

    }

    @Override
    public void inviteContact(UserTokenInfoDTO userTokenInfoDTO, List<String> contactsId) {

    }

    @Override
    public void acceptInvite(UserTokenInfoDTO userTokenInfoDTO, String meetingId) {

    }
}
