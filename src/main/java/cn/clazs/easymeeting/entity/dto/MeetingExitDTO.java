package cn.clazs.easymeeting.entity.dto;

import cn.clazs.easymeeting.entity.enums.MeetingMemberStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户退出会议DTO，指定用户以某状态退出当前的会议列表
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MeetingExitDTO implements Serializable {
    public String exitUserId;
    // 当前exitUserId用户退出后的会议用户列表
    private List<MeetingMemberDTO> meetingMemberList;
    private MeetingMemberStatus exitStatus;
}
