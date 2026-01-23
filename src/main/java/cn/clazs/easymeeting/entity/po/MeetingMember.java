package cn.clazs.easymeeting.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议成员信息表实体，会议id和用户id为联合主键
 * 表示一条会议成员记录
 */
@Data
public class MeetingMember {

    /**
     * 会议ID
     */
    private String meetingId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 最后加入时间
     */
    private LocalDateTime lastJoinTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 成员类型
     */
    private Integer memberType;

    /**
     * 会议状态
     */
    private Integer meetingStatus;
}
