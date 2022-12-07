package com.cuiot.openservices.sdk.entity;

import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author yht
 */
public final class SubscribeTopic {
    private String topic;
    /**
     * 默认qos=0
     */
    private MqttQoS mqttQoS = MqttQoS.AT_MOST_ONCE;

    public SubscribeTopic() {
    }

    public SubscribeTopic(String topic) {
        this.topic = topic;
    }

    public SubscribeTopic(String topic, MqttQoS mqttQoS) {
        this.topic = topic;
        this.mqttQoS = mqttQoS;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MqttQoS getMqttQoS() {
        return mqttQoS;
    }

    public void setMqttQoS(MqttQoS mqttQoS) {
        this.mqttQoS = mqttQoS;
    }
}
