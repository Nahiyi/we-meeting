package cn.clazs.easymeeting.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 预约会议实体
 */
@Data
public class MeetingReserve {

    /**
     * alias：预约会议ID，并不是真实会议ID
     * 会议ID，10位字符串
     */
    private String meetingId;

    /**
     * 会议名称
     */
    private String meetingName;

    /**
     * 加入类型
     */
    private Integer joinType;

    /**
     * 加入密码
     */
    private String joinPassword;

    /**
     * 会议时长（分钟）
     */
    private Integer duration;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 创建用户ID
     */
    private String createUserId;

    /**
     * 状态：0-待开始，1-进行中，2-已结束，3-已取消
     */
    private Integer status;

    /**
     * 实际会议ID，预约会议开始后关联的 MeetingInfo.meetingId
     */
    private String realMeetingId;
}
