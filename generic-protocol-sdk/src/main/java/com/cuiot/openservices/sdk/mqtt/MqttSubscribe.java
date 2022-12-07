package com.cuiot.openservices.sdk.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author unicom
 */
public class MqttSubscribe {

    private final MqttQoS qos;
    private final String topicFilter;

    public MqttSubscribe(MqttQoS qos, String topicFilter) {
        this.qos = qos;
        this.topicFilter = topicFilter;
    }

    public MqttQoS qos() {
        return qos;
    }

    public String topicFilter() {
        return topicFilter;
    }

    public static MqttSubscribe qos0(String topicFilter) {
        return new MqttSubscribe(MqttQoS.AT_MOST_ONCE, topicFilter);
    }

    public static MqttSubscribe qos1(String topicFilter) {
        return new MqttSubscribe(MqttQoS.AT_LEAST_ONCE, topicFilter);
    }

    public static MqttSubscribe qos2(String topicFilter) {
        return new MqttSubscribe(MqttQoS.EXACTLY_ONCE, topicFilter);
    }
}
