package com.leungyh.apiclient.exception;

/**
 * 自定义异常类
 *
 * @author leungyh
 */
public class LeungyhApiException extends RuntimeException {

    private final int code;

    public LeungyhApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public LeungyhApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public LeungyhApiException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
