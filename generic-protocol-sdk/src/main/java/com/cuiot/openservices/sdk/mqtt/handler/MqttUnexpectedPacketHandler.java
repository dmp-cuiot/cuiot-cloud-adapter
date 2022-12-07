package com.cuiot.openservices.sdk.mqtt.handler;

import com.cuiot.openservices.sdk.exception.MqttUnexpectedPacketException;
import com.cuiot.openservices.sdk.mqtt.MqttFixedHeaders;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * @author unicom
 */
public class MqttUnexpectedPacketHandler {

    void connAck(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(
                new MqttUnexpectedPacketException(MqttMessageType.CONNACK, cause));
    }

    void pubAck(ChannelHandlerContext ctx, int packetId, Throwable cause) {
        ctx.fireExceptionCaught(
                new MqttUnexpectedPacketException(MqttMessageType.PUBACK, packetId, cause));
    }

    void subAck(ChannelHandlerContext ctx, int packetId, Throwable cause) {
        ctx.fireExceptionCaught(
                new MqttUnexpectedPacketException(MqttMessageType.SUBACK, packetId, cause));
    }

    void unsubAck(ChannelHandlerContext ctx, int packetId, Throwable cause) {
        ctx.fireExceptionCaught(
                new MqttUnexpectedPacketException(MqttMessageType.UNSUBACK, packetId, cause));
    }

    public void unsupported(ChannelHandlerContext ctx, MqttMessage msg) {
        final MqttMessageType type = msg.fixedHeader().messageType();
        ctx.fireExceptionCaught(new MqttUnexpectedPacketException(type));
        if (type == MqttMessageType.PINGREQ) {
            ctx.channel().writeAndFlush(new MqttMessage(MqttFixedHeaders.PINGRESP_HEADER));
        } else {
            ctx.close();
        }
    }

}
