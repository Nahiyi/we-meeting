package cn.clazs.easymeeting.entity.enums;

import cn.clazs.easymeeting.util.StringUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserContactApplyStatus {
    INIT(0, "待处理"),
    PASS(1, "已同意"),
    REJECT(2, "已拒绝"),
    BLACKLIST(3, "已拉黑");

    @JsonValue
    @JSONField
    private final Integer status;
    private final String desc;


    public static UserContactApplyStatus getUserContactApplyStatus(String status) {
        try {
            if (StringUtil.isEmpty(status)) {
                return null;
            }
            return UserContactApplyStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static UserContactApplyStatus getUserContactApplyStatus(Integer status) {
        UserContactApplyStatus[] userContactApplyStatusEnums = UserContactApplyStatus.values();
        for (UserContactApplyStatus userContactApplyStatusEnum : userContactApplyStatusEnums) {
            if (userContactApplyStatusEnum.getStatus().equals(status)) {
                return userContactApplyStatusEnum;
            }
        }
        return null;
    }
}
