package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会议本身状态的枚举：进行中、已结束
 */
@Getter
@AllArgsConstructor
public enum MeetingStatus {
    RUNNING(0, "会议进行中"),
    FINISHED(1, "会议已结束"),
    SCHEDULED(2, "会议待开始");

    @JSONField
    @JsonValue
    private final Integer status;
    private final String desc;

    public static MeetingStatus getMeetingStatusEnum(Integer status) {
        for (MeetingStatus item : MeetingStatus.values()) {
            if (item.status.equals(status)) {
                return item;
            }
        }
        return null;

    }
}
