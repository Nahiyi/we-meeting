package cn.clazs.easymeeting.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议本身信息实体类
 */
@Data
public class MeetingInfo {

    /**
     * 会议表的主键：会议ID
     */
    private String meetingId;

    /**
     * 会议号（可用户自定义，也可使用用户默认会议号）
     */
    private String meetingNo;

    /**
     * 会议名称
     */
    private String meetingName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建用户ID
     */
    private String createUserId;

    /**
     * 加入类型
     */
    private Integer joinType;

    /**
     * 加入密码
     */
    private String joinPassword;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态
     */
    private Integer status;
}
