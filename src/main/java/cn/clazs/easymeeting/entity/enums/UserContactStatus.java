package cn.clazs.easymeeting.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserContactStatus {
    NORMAL(0, "好友"),
    DEL(1, "删除"),
    BLACK(2, "拉黑");

    private final Integer status;
    private final String desc;

    public static UserContactStatus getUserContactStatus(Integer status) {
        UserContactStatus[] values = UserContactStatus.values();
        for (UserContactStatus value : values) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
