package cn.clazs.easymeeting.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO implements Serializable {

    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotEmpty(message = "密码不能为空")
    @Size(max = 32, message = "密码长度不能超过32个字符")
    private String password;

    @NotEmpty(message = "验证码不能为空")
    private String checkCode;

    @NotEmpty(message = "验证码Key不能为空")
    private String checkCodeKey;
}
