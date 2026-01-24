package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会议成员身份类型枚举：主持人 or 普通成员
 */
@Getter
@AllArgsConstructor
public enum MemberType {
    NORMAL(0, "普通成员"),
    COMPERE(1, "主持人");

    @JsonValue
    @JSONField
    private final Integer type;
    private final String desc;

    public static MemberType getMemberTypeEnum(Integer type) {
        for (MemberType memberTypeEnum : MemberType.values()) {
            if (memberTypeEnum.type.equals(type)) {
                return memberTypeEnum;
            }
        }
        return null;
    }

}
