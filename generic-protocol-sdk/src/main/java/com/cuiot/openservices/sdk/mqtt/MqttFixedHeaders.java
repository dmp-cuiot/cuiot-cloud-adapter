package com.cuiot.openservices.sdk.mqtt;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author unicom
 */
public final class MqttFixedHeaders {
    private MqttFixedHeaders() {
    }

    public static final MqttFixedHeader CONNECT_HEADER =
            new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, -1);

    public static final MqttFixedHeader CONNACK_HEADER =
            new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 2);

    public static final MqttFixedHeader PUBACK_HEADER =
            new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 2);

    public static final MqttFixedHeader PUBREC_HEADER =
            new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 2);

    public static final MqttFixedHeader PUBREL_HEADER =
            new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 2);

    public static final MqttFixedHeader PUBCOMP_HEADER =
            new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 2);

    public static final MqttFixedHeader SUBSCRIBE_HEADER =
            new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, -1);

    public static final MqttFixedHeader SUBACK_HEADER =
            new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, -1);

    public static final MqttFixedHeader UNSUBSCRIBE_HEADER =
            new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, -1);

    public static final MqttFixedHeader UNSUBACK_HEADER =
            new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 2);

    public static final MqttFixedHeader PINGREQ_HEADER =
            new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);

    public static final MqttFixedHeader PINGRESP_HEADER =
            new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);

    public static final MqttFixedHeader DISCONNECT_HEADER =
            new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0);
}
