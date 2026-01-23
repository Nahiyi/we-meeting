package cn.clazs.easymeeting.service.impl;

import cn.clazs.easymeeting.entity.po.MeetingMember;
import cn.clazs.easymeeting.mapper.MeetingMemberMapper;
import cn.clazs.easymeeting.service.MeetingMemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MeetingMemberServiceImpl implements MeetingMemberService {

    @Resource
    private MeetingMemberMapper meetingMemberMapper;

    @Override
    public MeetingMember addMember(MeetingMember meetingMember) {
        meetingMemberMapper.insert(meetingMember);
        return meetingMember;
    }

    @Override
    public MeetingMember updateMember(MeetingMember meetingMember) {
        meetingMemberMapper.updateByMeetingIdAndUserId(meetingMember);
        return meetingMember;
    }

    @Override
    public MeetingMember getMemberByUserId(String userId) {
        return meetingMemberMapper.selectByUserId(userId);
    }

    @Override
    public MeetingMember getMember(String meetingId, String userId) {
        return meetingMemberMapper.selectByMeetingIdAndUserId(meetingId, userId);
    }

    @Override
    public List<MeetingMember> getMembersByMeetingId(String meetingId) {
        return meetingMemberMapper.selectByMeetingId(meetingId);
    }

    @Override
    public void removeMemberByUserId(String userId) {
        meetingMemberMapper.deleteByUserId(userId);
    }

    @Override
    public void removeMembersByMeetingId(String meetingId) {
        meetingMemberMapper.deleteByMeetingId(meetingId);
    }
}
