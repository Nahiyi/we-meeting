package cn.clazs.easymeeting.entity.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDto implements Serializable {

    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotEmpty(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickName;

    @NotEmpty(message = "密码不能为空")
    @Size(max = 20, message = "密码长度不能超过20个字符")
    private String password;

    @NotEmpty(message = "验证码不能为空")
    private String checkCode;

    @NotEmpty(message = "验证码Key不能为空")
    private String checkCodeKey;
}
