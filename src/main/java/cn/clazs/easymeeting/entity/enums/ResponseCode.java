package cn.clazs.easymeeting.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    CODE_200(200, "请求成功"),
    CODE_404(404, "请求地址不能存在"),
    CODE_500(500, "服务器返回错误，请联系管理员"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已经存在"),
    CODE_602(602, "文件不存在"),
    CODE_901(901, "登录超时"),
    CODE_902(902, "您不是对方的好友，请先向对方发送朋友验证申请"),
    CODE_903(903, "您已经不在群聊，请重新加入群聊");

    private final Integer code;

    private final String msg;
}
