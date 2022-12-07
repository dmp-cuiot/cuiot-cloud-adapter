package com.cuiot.openservices.sdk.exception;

import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * MQTT QoS 异常
 *
 * @author unicom
 */
public final class MqttUnexpectedQosException extends MqttPacketException {

    private static final long serialVersionUID = 6352002242649565825L;

    private final MqttQoS qos;

    public MqttUnexpectedQosException(MqttMessageType messageType, int packetId, MqttQoS qos) {
        super("Unexpected packet: type=" + messageType + ", packetId=" + packetId + ", qos=" + qos,
                messageType, packetId);
        this.qos = qos;
    }

    public MqttQoS qos() {
        return qos;
    }
}
