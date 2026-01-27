package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatMessageStatus {
    SENDING(0,"正在发送"),
    SENDEND(1,"发送完毕");

    @JsonValue
    @JSONField
    private final Integer status;
    private final String desc;

    public static ChatMessageStatus getEnumByStatus(Integer status) {
        for (ChatMessageStatus e : ChatMessageStatus.values()) {
            if (e.getStatus().equals(status)) {
                return e;
            }
        }
        return null;
    }
}
