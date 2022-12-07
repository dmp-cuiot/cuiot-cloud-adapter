package com.cuiot.openservices.sdk.config;

/**
 * 泛协议接入服务配置接口类
 *
 * @author yht
 */
public interface IAdapterConfig {

    /**
     * 平台连接地址
     *
     * @return String
     */
    String getConnectionHost();

    /**
     * 平台连接端口
     *
     * @return int
     */
    int getConnectionPort();

    /**
     * 泛协议服务ID
     *
     * @return String
     */
    String getServiceId();

    /**
     * 泛协议服务实例Key
     *
     * @return String
     */
    String getServiceKey();

    /**
     * 泛协议服务实例Secret
     *
     * @return String
     */
    String getServiceSecret();

    /**
     * 泛协议服务实例签名方法
     *
     * @return String
     */
    String getSignMethod();

    /**
     * 和平台之间是否使用加密传输，默认为true
     *
     * @return Boolean
     */
    Boolean enableTls();

    /**
     * 和平台之间的控制连接断开后是否重连，默认为false
     *
     * @return Boolean
     */
    Boolean enableReconnect();

    /**
     * 和平台之间的控制连接异常断开后的初始重连等待间隔（默认值：30，单位：秒）
     * 重连失败后等待间隔会呈指数级逐渐增加，若重连成功，则等待间隔重置为初始重连等待间隔
     * 当enableReconnect为true时生效
     *
     * @return Long
     */
    Long getReconnectInterval();

    /**
     * 重连最大等待间隔，单位秒，达到上限后，自动从初始重连等待间隔开始
     *
     * @return Long
     */
    Long getMaxReconnectInterval();

    /**
     * 获取redis连接地址
     *
     * @return
     */
    String getRedisServer();

    /**
     * 获取redis连接密码
     *
     * @return
     */
    String getRedisPd();

    /**
     * 获取文件加载方式
     *
     * @return
     */
    String getConfigType();

    /**
     * 获取处理上下行消息线程数
     *
     * @return Integer
     */
    Integer getHandlerThreads();
}

