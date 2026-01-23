package cn.clazs.easymeeting.entity.dto;

import lombok.Data;

/**
 * 加入会议请求DTO
 */
@Data
public class JoinMeetingDTO {
    /** 加入的会议ID */
    String meetingId;
    /** 用户ID */
    String userId;
    String nickName;
    Integer sex;
    Boolean videoOpen;
}
