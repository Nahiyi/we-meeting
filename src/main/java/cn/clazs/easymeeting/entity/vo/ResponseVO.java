package cn.clazs.easymeeting.entity.vo;

import lombok.Data;

@Data
public class ResponseVO<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public ResponseVO() {
        this.timestamp = System.currentTimeMillis();
    }

    // 成功响应 - 带数据
    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setCode(200);
        vo.setMessage("success");
        vo.setData(data);
        return vo;
    }

    // 成功响应 - 无数据
    public static <T> ResponseVO<T> success() {
        return success(null);
    }

    // 成功响应 - 自定义消息
    public static <T> ResponseVO<T> success(String message, T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setCode(200);
        vo.setMessage(message);
        vo.setData(data);
        return vo;
    }

    // 失败响应 - 自定义code和message
    public static <T> ResponseVO<T> fail(Integer code, String message) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setCode(code);
        vo.setMessage(message);
        return vo;
    }

    // 失败响应 - 默认500
    public static <T> ResponseVO<T> fail(String message) {
        return fail(500, message);
    }

    // error 方法 - 兼容 GlobalExceptionHandler 的调用
    public static <T> ResponseVO<T> error(Integer code, String message) {
        return fail(code, message);
    }

    // error 方法 - 默认500
    public static <T> ResponseVO<T> error(String message) {
        return fail(500, message);
    }
}
