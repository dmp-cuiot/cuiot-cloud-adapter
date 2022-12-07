package com.cuiot.openservices.sdk.constant;

/**
 * 服务端返回码
 *
 * @author unicom
 */
public enum ServerCode {
    /**
     * 设备连接达到上限
     */
    SERVICE_ACCESS_NUMBER_REACH_MAXIMUM("090096", "Service access number has reached maximum "),
    ;

    private String code;
    private String errorMessage;

    public String getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    ServerCode(String code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
