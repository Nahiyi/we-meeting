package cn.clazs.easymeeting.entity.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private String userId;
    private String nickName;
    private Integer sex;
    private String token;
    private String meetingNo;
    private Boolean admin;
    private Boolean success;
}
