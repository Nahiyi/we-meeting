package cn.clazs.easymeeting.exception;

import cn.clazs.easymeeting.entity.enums.ResponseCode;
import lombok.Getter;

public class BusinessException extends RuntimeException {

    @Getter
    private ResponseCode codeEnum;

    @Getter
    private Integer code;

    private String message;

    public BusinessException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(Throwable e) {
        super(e);
    }

    public BusinessException(ResponseCode codeEnum) {
        super(codeEnum.getMsg());
        this.codeEnum = codeEnum;
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMsg();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 重写fillInStackTrace方法，避免打印堆栈信息，提高效率
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
