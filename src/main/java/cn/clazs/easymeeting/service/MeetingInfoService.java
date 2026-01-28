package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.dto.JoinMeetingDTO;
import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import cn.clazs.easymeeting.entity.po.MeetingInfo;
import cn.clazs.easymeeting.entity.vo.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MeetingInfoService {

    MeetingInfo createMeeting(MeetingInfo meetingInfo);

    MeetingInfo updateMeeting(MeetingInfo meetingInfo);

    MeetingInfo getMeetingById(String meetingId);

    /**
     * 批量查询会议ID和会议号的映射关系
     * @param meetingIds 会议ID集合
     * @return Map<会议ID, 会议号>
     */
    Map<String, String> getMeetingIdAndNoMapByIds(Collection<String> meetingIds);

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

    void createScheduledMeeting(MeetingInfo meetingInfo);

    void joinMeeting(JoinMeetingDTO joinMeetingDto);

    String preJoinMeeting(String meetingNo, String nickName, String password);

    void exitMeetingRoom(UserTokenInfoDTO currentUser, MeetingMemberStatus status);

    void forceExitMeetingRoom(UserTokenInfoDTO UserTokenInfoDTO, String userId, MeetingMemberStatus meetingMemberStatusEnum);

    void finishMeeting(String meetingId, String userId);

    @Deprecated
    void reserveJoinMeeting(String meetingId, UserTokenInfoDTO UserTokenInfoDTO, String password);

    void inviteContact(UserTokenInfoDTO UserTokenInfoDTO, List<String> contactsId);

    void acceptInvite(UserTokenInfoDTO UserTokenInfoDTO, String meetingId);
}
