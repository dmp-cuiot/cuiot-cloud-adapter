package com.cuiot.openservices.sdk.exception;

import com.cuiot.openservices.sdk.entity.ReturnCode;

/**
 * 异常基类
 *
 * @author yht
 */
public final class AdapterException extends RuntimeException {
    private String code;
    private String message;

    public AdapterException() {
    }

    public AdapterException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public AdapterException(ReturnCode returnCode) {
        super(returnCode.getMsg());
        this.code = returnCode.getCode();
        this.message = returnCode.getMsg();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
