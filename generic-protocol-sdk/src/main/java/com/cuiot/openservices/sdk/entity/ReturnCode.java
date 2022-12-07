package com.cuiot.openservices.sdk.entity;

/**
 * 返回Code TODO 要与平台返回错误区分开
 *
 * @author unicom
 */
public enum ReturnCode {
    /**
     * 成功
     */
    SUCCESS("000000", "成功"),

    /**
     * 通用错误
     */
    SDK_ERROR("000001", "SDK内部错误"),
    DATA_ILLEGAL("000002", "数据格式错误"),
    REPEATED_REQUEST("000003", "重复请求"),

    /**
     * 泛协议服务错误
     */
    SERVICE_ERROR("100000", "泛协议服务错误"),
    START_ERROR("100001", "泛协议服务启动失败"),
    SERVICE_IN_CONNECTING("100002", "泛协议服务正在连接中"),
    SERVICE_IN_CONNECTED("100003", "泛协议服务已连接"),
    SERVICE_IN_DISCONNECTING("100004", "泛协议服务正在断开中"),
    SERVICE_IN_DISCONNECTED("100005", "泛协议服务已断开"),
    SERVICE_CONNECT_ERROR("100006", "泛协议服务连接平台失败"),
    SERVICE_SUBSCRIBE_ERROR("100007", "泛协议服务订阅主题失败"),
    SERVICE_NOT_CONNECTED("100008", "泛协议服务未连接"),
    SERVICE_UNSUBSCRIBE_ERROR("100009", "泛协议服务设备取消订阅主题失败"),

    /**
     * 设备错误
     */
    DEVICE_NOT_LOGIN("200000", "设备没有登录"),
    DEVICE_ALREADY_LOGIN("200001", "设备已登录"),
    DEVICE_LOGOUT_FAILED("200002", "设备登出失败"),

    ;

    private final String code;
    private final String msg;

    ReturnCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
