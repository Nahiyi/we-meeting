package cn.clazs.easymeeting.mapper;

import cn.clazs.easymeeting.entity.po.MeetingMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingMemberMapper {

    int insert(MeetingMember meetingMember);

    int updateByMeetingIdAndUserId(MeetingMember meetingMember);

    MeetingMember selectByUserId(@Param("userId") String userId);

    MeetingMember selectByMeetingIdAndUserId(@Param("meetingId") String meetingId, @Param("userId") String userId);

    List<MeetingMember> selectByMeetingId(@Param("meetingId") String meetingId);

    int deleteByUserId(@Param("userId") String userId);

    int deleteByMeetingId(@Param("meetingId") String meetingId);
}
