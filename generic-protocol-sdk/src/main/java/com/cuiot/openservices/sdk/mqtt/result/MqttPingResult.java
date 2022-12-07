package com.cuiot.openservices.sdk.mqtt.result;

import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author unicom
 */
public class MqttPingResult {

    private final MqttMessageType messageType;
    private final boolean isDup;
    private final MqttQoS qosLevel;
    private final boolean isRetain;
    private final int remainingLength;

    public MqttPingResult(MqttMessageType messageType, boolean isDup, MqttQoS qosLevel, boolean isRetain, int remainingLength) {
        this.messageType = messageType;
        this.isDup = isDup;
        this.qosLevel = qosLevel;
        this.isRetain = isRetain;
        this.remainingLength = remainingLength;
    }

    public MqttMessageType getMessageType() {
        return messageType;
    }

    public boolean isDup() {
        return isDup;
    }

    public MqttQoS getQosLevel() {
        return qosLevel;
    }

    public boolean isRetain() {
        return isRetain;
    }

    public int getRemainingLength() {
        return remainingLength;
    }
}
