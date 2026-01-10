package cn.clazs.easymeeting.entity.enums;

public enum UserStatusEnum {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");;
    private Integer code;
    private String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatus(Integer code) {
        for (UserStatusEnum item : UserStatusEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
