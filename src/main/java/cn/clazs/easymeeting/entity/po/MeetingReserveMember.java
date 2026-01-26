package cn.clazs.easymeeting.entity.po;

import lombok.Data;

/**
 * 成员预约会议记录实体
 */
@Data
public class MeetingReserveMember {

    /**
     * 会议ID
     */
    private String meetingId;

    /**
     * 被邀请用户ID
     */
    private String inviteUserId;
}
