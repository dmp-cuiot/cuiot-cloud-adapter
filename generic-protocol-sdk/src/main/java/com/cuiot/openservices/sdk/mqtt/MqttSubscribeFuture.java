package com.cuiot.openservices.sdk.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.concurrent.Future;

import java.util.List;

/**
 * @author unicom
 */
public interface MqttSubscribeFuture extends Future<MqttQoS[]> {

    /**
     * 订阅主题
     *
     * @return List
     */
    List<MqttSubscribe> subscriptions();

    /**
     * 订阅主题是否都成功，不关注主题qos是否被降级
     *
     * @return boolean
     */
    boolean isAllSuccess();

    /**
     * 订阅主题是否都成功
     *
     * @return boolean
     */
    boolean isCompleteSuccess();

}
