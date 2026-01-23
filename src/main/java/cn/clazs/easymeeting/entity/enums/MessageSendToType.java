package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息发送目标类型枚举：发送给个人、发送给群
 */
@AllArgsConstructor
public enum MessageSendToType {
    USER(0, "个人"),
    GROUP(1, "群");

    @JSONField
    @JsonValue
    private final Integer type;

    @Getter
    private final String desc;

    @JSONField
    public Integer getType() {
        return type;
    }
}
