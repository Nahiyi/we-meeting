package cn.clazs.easymeeting.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 成员入会成功DTO：有本次入会的新成员信息+该会议已有的成员列表
 */
@Data
public class MemberJoinMeetingDTO {
    private MeetingMemberDTO newMember;
    private List<MeetingMemberDTO> meetingMemberList;
}
