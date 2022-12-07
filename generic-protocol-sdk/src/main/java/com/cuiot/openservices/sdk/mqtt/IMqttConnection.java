package com.cuiot.openservices.sdk.mqtt;

/**
 * @author yht
 */
public interface IMqttConnection {
    /**
     * 泛协议服务连接成功
     */
    void onConnected();

    /**
     * 泛协议服务连接断开
     */
    void onDisconnected();
}
