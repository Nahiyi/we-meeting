package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MeetingReserveStatus {
    NO_START(0, "待开始"),
    RUNNING(1, "进行中"),
    FINISHED(2, "已结束"),
    CANCELLED(3, "已取消");

    @JsonValue
    @JSONField
    private final Integer status;
    private final String desc;

    public static MeetingReserveStatus getMeetingStatusEnum(Integer status) {
        for (MeetingReserveStatus item : MeetingReserveStatus.values()) {
            if (item.status.equals(status)) {
                return item;
            }
        }
        return null;
    }
}
