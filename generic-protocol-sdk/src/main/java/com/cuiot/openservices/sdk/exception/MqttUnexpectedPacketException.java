package com.cuiot.openservices.sdk.exception;

import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * MQTT 包异常
 *
 * @author unicom
 */
public final class MqttUnexpectedPacketException extends MqttPacketException {

    private static final long serialVersionUID = -4106467930672049143L;

    public MqttUnexpectedPacketException(MqttMessageType type) {
        super("Unexpected packet: type=" + type, type, 0);
    }

    public MqttUnexpectedPacketException(MqttMessageType type, Throwable cause) {
        super("Unexpected packet: type=" + type, type, 0, cause);
    }

    public MqttUnexpectedPacketException(MqttMessageType type, int packetId) {
        super("Unexpected packet: type=" + type + ", packetId=" + packetId, type, packetId);
    }

    public MqttUnexpectedPacketException(MqttMessageType type, int packetId, Throwable cause) {
        super("Unexpected packet: type=" + type + ", packetId=" + packetId, type, packetId, cause);
    }
}
