package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.dto.JoinMeetingDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.entity.vo.PageResult;
import cn.clazs.easymeeting.mapper.MeetingInfoMapper;
import cn.clazs.easymeeting.mapper.MeetingMemberMapper;
import cn.clazs.easymeeting.redis.RedisComponent;
import cn.clazs.easymeeting.service.MeetingInfoService;
import cn.clazs.easymeeting.util.StringUtil;
import cn.clazs.easymeeting.websocket.BizChannelContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public void quickMeeting(MeetingInfo meetingInfo, String nickName) {

    }


    private void addToMeeting(String meetingId, String userId, String nickName, Integer sex, Integer memberType, Boolean videoOpen) {


    }

    private void checkMeetingJoin(String meetingId, String userId) {

    }

    @Override
    public void joinMeeting(JoinMeetingDTO joinMeetingDTO) {

    }

    @Override
    public String preJoinMeeting(String meetingNo, UserTokenInfoDTO UserTokenInfoDTO, String password) {
        return null;
    }

    @Override
    public void exitMeetingRoom(UserTokenInfoDTO UserTokenInfoDTO, MeetingMemberStatus statusEnum) {
        String meetingId = UserTokenInfoDTO.getCurrentMeetingId();
        if (StringUtil.isEmpty(meetingId)) {
            return;
        }

    }

    @Override
    public void forceExitMeetingRoom(UserTokenInfoDTO UserTokenInfoDTO, String userId, MeetingMemberStatus MeetingMemberStatus) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishMeeting(String meetingId, String userId) {

    }

    @Override
    public void reserveJoinMeeting(String meetingId, UserTokenInfoDTO UserTokenInfoDTO, String password) {

    }

    @Override
    public void inviteContact(UserTokenInfoDTO UserTokenInfoDTO, List<String> contactsId) {

    }

    @Override
    public void acceptInvite(UserTokenInfoDTO UserTokenInfoDTO, String meetingId) {

    }
}
