package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.dto.JoinMeetingDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.entity.vo.PageResult;

import java.util.List;

public interface MeetingInfoService {

    MeetingInfo createMeeting(MeetingInfo meetingInfo);

    MeetingInfo updateMeeting(MeetingInfo meetingInfo);

    MeetingInfo getMeetingById(String meetingId);

    MeetingInfo getMeetingByNo(String meetingNo);

    List<MeetingInfo> getMeetingsByUserId(String userId);

    List<MeetingInfo> listMeetings(Integer status);

    void deleteMeeting(String meetingId);

    /**
     * 加载所有历史会议（我创建的 + 我参加的）
     */
    PageResult<MeetingInfo> loadAllMeetings(String userId, Integer pageNo, Integer pageSize);

    /**
     * 加载我创建的会议
     */
    PageResult<MeetingInfo> loadCreatedMeetings(String userId, Integer pageNo, Integer pageSize);

    /**
     * 加载我参加过的会议（不包括我创建的）
     */
    PageResult<MeetingInfo> loadJoinedMeetings(String userId, Integer pageNo, Integer pageSize);

    void quickMeeting(MeetingInfo meetingInfo, String nickName);

    void joinMeeting(JoinMeetingDTO joinMeetingDto);

    String preJoinMeeting(String meetingNo, UserTokenInfoDTO UserTokenInfoDTO, String password);

    void exitMeetingRoom(UserTokenInfoDTO UserTokenInfoDTO, MeetingMemberStatus statusEnum);

    void forceExitMeetingRoom(UserTokenInfoDTO UserTokenInfoDTO, String userId, MeetingMemberStatus meetingMemberStatusEnum);

    void finishMeeting(String meetingId, String userId);

    void reserveJoinMeeting(String meetingId, UserTokenInfoDTO UserTokenInfoDTO, String password);

    void inviteContact(UserTokenInfoDTO UserTokenInfoDTO, List<String> contactsId);

    void acceptInvite(UserTokenInfoDTO UserTokenInfoDTO, String meetingId);
}
