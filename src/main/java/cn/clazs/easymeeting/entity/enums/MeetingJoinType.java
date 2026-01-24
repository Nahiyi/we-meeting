package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会议加入类型枚举：需要密码、不需要密码
 */
@Getter
@AllArgsConstructor
public enum MeetingJoinType {
    NP_PASSWORD(0, "无需密码"),
    PASSWORD(1, "需要密码");

    @JsonValue
    @JSONField
    private final Integer type;
    private final String desc;

    public static MeetingJoinType getByType(Integer type) {
        for (MeetingJoinType item : MeetingJoinType.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }
}
