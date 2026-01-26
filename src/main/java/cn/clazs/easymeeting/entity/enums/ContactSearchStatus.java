package cn.clazs.easymeeting.entity.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContactSearchStatus {
    SELF(-1, "自己"),
    NOT_FRIEND(0, "非好友，可申请"),
    FRIEND(1, "已是好友"),
    PENDING(2, "申请待处理"),
    BLACKLISTED(3, "已被拉黑"),
    BE_FRIEND(4, "对方已添加我");

    @JsonValue
    @JSONField
    private final Integer status;
    private final String desc;
}
