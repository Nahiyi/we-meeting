package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MeetingMemberStatus {
    DEL_MEETING(0, "删除会议"),
    NORMAL(1, "正常"),
    EXIT_MEETING(2, "退出会议"),
    KICK_OUT(3, "被踢出会议"),
    BLACKLIST(4, "被拉黑");

    @JSONField
    @JsonValue
    private final Integer status;
    private final String desc;

    public static MeetingMemberStatus getByStatus(Integer status) {
        for (MeetingMemberStatus item : MeetingMemberStatus.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}
