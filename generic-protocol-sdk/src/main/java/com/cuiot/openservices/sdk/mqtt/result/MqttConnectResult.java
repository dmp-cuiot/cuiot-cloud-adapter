package com.cuiot.openservices.sdk.mqtt.result;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

/**
 * @author unicom
 */
public class MqttConnectResult {

    private final boolean sessionPresent;

    private final MqttConnectReturnCode returnCode;

    public MqttConnectResult(boolean sessionPresent, MqttConnectReturnCode returnCode) {
        this.sessionPresent = sessionPresent;
        this.returnCode = returnCode;
    }

    public boolean isSessionPresent() {
        return sessionPresent;
    }

    public MqttConnectReturnCode returnCode() {
        return returnCode;
    }

    @Override
    public String toString() {
        return "MqttConnectResult{" +
                "sessionPresent=" + sessionPresent +
                ", returnCode=" + returnCode +
                '}';
    }

}
