package com.cuiot.openservices.sdk.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author unicom
 */
public class MqttPingHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = LoggerFactory.getLogger(MqttPingHandler.class);

    private final int keepaliveSeconds;

    private ScheduledFuture<?> pingRespTimeout;

    public MqttPingHandler(int keepaliveSeconds) {
        this.keepaliveSeconds = keepaliveSeconds;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof MqttMessage)) {
            ctx.fireChannelRead(msg);
            return;
        }
        MqttMessage message = (MqttMessage) msg;
        if (message.fixedHeader().messageType() == MqttMessageType.PINGREQ) {
            // 下行，平台发送PINGREQ
            handlePingReq(ctx.channel());
        } else if (message.fixedHeader().messageType() == MqttMessageType.PINGRESP) {
            // 下行，平台发送PINGRESP
            handlePingResp();
        } else {
            // 下行，其他类型消息不处理，由后续Handler处理
            ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case WRITER_IDLE:
                    sendPingReq(ctx.channel());
                    break;
                case READER_IDLE:
                default:
                    break;
            }
        }
    }

    private void sendPingReq(Channel channel) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);
        channel.writeAndFlush(new MqttMessage(fixedHeader));

        if (pingRespTimeout != null) {
            pingRespTimeout = channel.eventLoop().schedule(() -> {
                MqttFixedHeader fixedHeader2 = new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0);
                channel.writeAndFlush(new MqttMessage(fixedHeader2)).addListener(ChannelFutureListener.CLOSE);
            }, keepaliveSeconds, TimeUnit.SECONDS);
        }
    }

    private void handlePingReq(Channel channel) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        channel.writeAndFlush(new MqttMessage(fixedHeader));
    }

    private void handlePingResp() {
        if (pingRespTimeout != null
                && !pingRespTimeout.isCancelled()
                && !pingRespTimeout.isDone()) {
            pingRespTimeout.cancel(true);
            pingRespTimeout = null;
        }
    }
}
