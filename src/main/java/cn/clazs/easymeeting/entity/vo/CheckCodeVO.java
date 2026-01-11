package cn.clazs.easymeeting.entity.vo;

import lombok.Data;

/**
 * 验证码生成结果返回VO
 */
@Data
public class CheckCodeVO {
    // 验证码问题（图片的base64文本）
    private String checkCode;
    // 验证码唯一id
    private String checkCodeKey;
}
