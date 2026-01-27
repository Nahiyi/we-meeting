package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReceiveType {
    ALL(0, "全员"),
    USER(1, "个人");

    @JsonValue
    @JSONField
    private final Integer type;
    private final String desc;

    public static ReceiveType getByType(Integer type) {
        for (ReceiveType receiveTypeEnum : ReceiveType.values()) {
            if (receiveTypeEnum.getType().equals(type)) {
                return receiveTypeEnum;
            }
        }
        return null;
    }
}
