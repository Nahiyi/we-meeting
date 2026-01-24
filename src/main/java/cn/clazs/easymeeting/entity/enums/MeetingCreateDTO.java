package cn.clazs.easymeeting.entity.enums;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MeetingCreateDTO {
    // 会议号类型：0，使用创建用户的会议号；1，使用系统生成一个会议号
    @NotNull
    Integer meetingNoType;

    @NotEmpty
    @Size(min = 1, max = 100)
    String meetingName;

    @NotNull
    Integer joinType;

    @Size(max = 5, message = "会议密码最多5位")
    String joinPassword;
}
