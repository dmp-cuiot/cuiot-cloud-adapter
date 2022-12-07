package com.cuiot.openservices.sdk.exception;

import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * MQTT 重复包异常
 *
 * @author unicom
 */
public final class MqttDuplicatePacketException extends MqttPacketException {

    private static final long serialVersionUID = -5651519169441099046L;

    public MqttDuplicatePacketException(MqttMessageType type, int packetId) {
        super("Duplicate packet: type=" + type + ", packetId=" + packetId, type, packetId);
    }
}
