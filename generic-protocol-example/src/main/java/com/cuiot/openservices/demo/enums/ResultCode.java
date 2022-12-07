package com.cuiot.openservices.demo.enums;

/**
 * @author zhh
 * @date 2021/8/27 16:07
 * @description 错误码枚举类
 */
public enum ResultCode {

    /**
     * 请求接口时token是无效的
     */
    TOKEN_INVALID("100001", "token无效"),

    /**
     * 读取配置文件无法找到对应的设备信息
     */
    DEVICE_EXISTENT("100002", "设备不存在"),

    /**
     * 设备数据上行时数据格式问题
     */
    DATA_ILLEGAL("100003", "数据格式错误"),

    /**
     * 非上行类型的一种
     */
    TYPE_EXISTENT("100004", "类型不存在"),

    /**
     * 上行数据的key或者value为空
     */
    KEY_VALUE_EXISTENT("100005", "key或者value为空");

    /**
     * 状态码
     */
    private String code;
    /**
     * 状态信息
     */
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 私有构造函数
     *
     * @param code    状态码
     * @param message 状态信息
     */
    private ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
