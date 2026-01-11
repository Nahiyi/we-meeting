package cn.clazs.easymeeting.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserTokenInfoDTO implements Serializable {

    private static final long serialVersionUID = -3244262035649152692L;

    private String token;
    private String userId;
    private String nickName;
    private Integer sex;
    private String meetingNo;

    /** 当前所在会议ID */
    private String currentMeetingId;

    /** 当前在会议中使用的昵称 */
    private String currentNickName;

    /** 是否是管理员 */
    private Boolean admin;
}
