package cn.clazs.easymeeting.service;

import cn.clazs.easymeeting.entity.dto.UserTokenInfoDTO;
import cn.clazs.easymeeting.entity.po.MeetingReserve;
import cn.clazs.easymeeting.entity.po.MeetingReserveMember;
import cn.clazs.easymeeting.entity.vo.PageResult;

import java.util.List;

public interface MeetingReserveService {

    /**
     * 创建预约会议
     */
    MeetingReserve createReserve(MeetingReserve meetingReserve, List<String> inviteUserIds);

    /**
     * 更新预约会议
     */
    MeetingReserve updateReserve(MeetingReserve meetingReserve);

    /**
     * 根据ID获取预约会议
     */
    MeetingReserve getReserveById(String meetingId);

    /**
     * 删除预约会议
     */
    void deleteReserve(String meetingId, String userId);

    /**
     * 取消预约会议
     */
    void cancelReserve(String meetingId, String userId);

    /**
     * 获取用户创建的预约会议（分页）
     */
    PageResult<MeetingReserve> loadCreatedReserves(String userId, Integer pageNo, Integer pageSize);

    /**
     * 获取用户被邀请的预约会议（分页）
     */
    PageResult<MeetingReserve> loadInvitedReserves(String userId, Integer pageNo, Integer pageSize);

    /**
     * 添加会议邀请成员
     */
    void addInviteMembers(String meetingId, List<String> inviteUserIds);

    /**
     * 移除会议邀请成员
     */
    void removeInviteMember(String meetingId, String inviteUserId);

    /**
     * 获取会议邀请成员列表
     */
    List<MeetingReserveMember> getInviteMembers(String meetingId);

    /**
     * 获取用户创建的指定状态的预约会议
     */
    List<MeetingReserve> loadCreatedReservesByStatus(String userId, Integer status);

    /**
     * 获取用户今天的预约会议（创建的 + 被邀请的）
     */
    List<MeetingReserve> loadTodayReserves(String userId);

    /**
     * 开始预约会议
     * 1. 验证是否为创建人
     * 2. 复用 MeetingInfoService.quickMeeting() 创建实际会议
     * 3. 更新预约会议状态和 realMeetingId
     * @return 实际会议ID
     */
    String startReserveMeeting(String reserveId, UserTokenInfoDTO currentUser);

    /**
     * 加入预约会议
     * 1. 验证会议是否已开始
     * 2. 设置 currentMeetingId 到 token
     * 3. 返回实际会议ID供用户加入
     * @return 实际会议ID
     */
    String joinReserveMeeting(String reserveId, UserTokenInfoDTO currentUser);
}
