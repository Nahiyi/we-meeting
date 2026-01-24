package cn.clazs.easymeeting.entity.dto;

import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import cn.clazs.easymeeting.entity.enums.MemberType;
import lombok.Data;

/**
 * 向Redis中缓存一条 会议房间->用户信息 的哈希记录的DTO
 */
@Data
public class MeetingMemberDTO {
    private String userId;
    private String nickName;
    private long joinTime;
    private MemberType memberType;
    private MeetingMemberStatus status;
    private Boolean videoOpen;
    private Integer sex;
}
