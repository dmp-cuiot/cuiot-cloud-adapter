package com.cuiot.openservices.sdk.exception;


import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * MQTT包异常
 *
 * @author unicom
 */
public class MqttPacketException extends IllegalStateException {

    private static final long serialVersionUID = 8859800976034950713L;

    private final MqttMessageType messageType;
    private final int packetId;

    MqttPacketException(String message, MqttMessageType type, int packetId) {
        super(message);
        this.messageType = type;
        this.packetId = packetId;
    }

    MqttPacketException(String message, MqttMessageType type, int packetId, Throwable cause) {
        super(message, cause);
        this.messageType = type;
        this.packetId = packetId;
    }

    public MqttMessageType messageType() {
        return messageType;
    }

    public int packetId() {
        return packetId;
    }
}
