package cn.clazs.easymeeting.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {

    /**
     * 用户ID，12位字符串
     */
    private String userId;

    /**
     * 邮箱，唯一
     */
    private String email;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 性别：0女，1男，2保密
     */
    private Integer sex;

    /**
     * 密码（MD5加密）
     */
    @JsonIgnore
    private String password;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;

    /**
     * 最后离开时间
     */
    private Long lastOffTime;

    /**
     * 个人会议号，10位字符串
     */
    private String meetingNo;
}