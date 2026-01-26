package cn.clazs.easymeeting.entity.dto;

import lombok.Data;

/**
 * 会议邀请信息DTO
 */
@Data
public class MeetingInviteDTO {
    /** 会议名称 */
    private String meetingName;
    /**  */
    private String inviteUserName;
    private String meetingId;
}
