package com.cuiot.openservices.sdk.exception;

import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * MQTT 订阅异常
 *
 * @author unicom
 */
public final class MqttSubscribeException extends IllegalStateException {

    private static final long serialVersionUID = -5095305821092199572L;

    private final MqttQoS[] returnCodes;

    public MqttSubscribeException(String message, MqttQoS... returnCodes) {
        super(message);
        this.returnCodes = returnCodes;
    }

    public int returnCodeLength() {
        return returnCodes.length;
    }

    public MqttQoS returnCode(int index) {
        return returnCodes[index];
    }
}
