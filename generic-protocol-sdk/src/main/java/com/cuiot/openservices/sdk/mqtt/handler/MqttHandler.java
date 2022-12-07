package com.cuiot.openservices.sdk.mqtt.handler;

import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.exception.MqttDuplicatePacketException;
import com.cuiot.openservices.sdk.exception.MqttSubscribeException;
import com.cuiot.openservices.sdk.exception.MqttUnexpectedPacketException;
import com.cuiot.openservices.sdk.exception.MqttUnexpectedQosException;
import com.cuiot.openservices.sdk.mqtt.*;
import com.cuiot.openservices.sdk.mqtt.promise.*;
import com.cuiot.openservices.sdk.mqtt.result.MqttConnectResult;
import com.cuiot.openservices.sdk.mqtt.result.MqttPingResult;
import com.cuiot.openservices.sdk.mqtt.result.MqttPublishResult;
import com.cuiot.openservices.sdk.util.ScheduledExecutorTimer;
import com.cuiot.openservices.sdk.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author unicom
 */
public class MqttHandler extends ChannelDuplexHandler {
    private static Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    private final MqttUnexpectedPacketHandler unexpectedPacketHandler = new MqttUnexpectedPacketHandler();

    private boolean connected = false;
    private Timer timer;
    private final ChannelFutureListener writeListener = new LastWriteTimeUpdater();

    private final MqttPacketId subscribeId = new MqttPacketId();
    private final MqttPacketId unsubscribeId = new MqttPacketId();

    private final AtomicReference<MqttConnectPromise> connectPromise = new AtomicReference<>();
    private final ConcurrentMap<Integer, MqttPublishPromise> publishPromises = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, MqttPubAckPromise> pubAckPromises = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, MqttSubscribePromise> subscribePromises = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Promise<MqttUnsubAckMessage>> unsubscribePromises = new ConcurrentHashMap<>();
    private final Queue<Promise<MqttPingResult>> pingPromises = new ConcurrentLinkedQueue<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (timer == null) {
            timer = new ScheduledExecutorTimer(ctx.executor());
        }
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        new PromiseBreaker(new ClosedChannelException())
                .renege(connectPromise.getAndSet(null))
                .renege(publishPromises.values())
                .renege(subscribePromises.values())
                .renege(unsubscribePromises.values());
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof MqttMessage) {
                MqttMessage message = (MqttMessage) msg;
                switch (message.fixedHeader().messageType()) {
                    case CONNACK:
                        connAckRead(ctx, (MqttConnAckMessage) msg);
                        break;
                    case PUBLISH:
                        // 平台下行publish
                        ctx.fireChannelRead(msg);
                        break;
                    case PUBACK:
                        pubAckRead(ctx, message);
                        break;
                    case SUBACK:
                        subAckRead(ctx, (MqttSubAckMessage) msg);
                        break;
                    case UNSUBACK:
                        unsubAckRead(ctx, (MqttUnsubAckMessage) msg);
                        break;
                    case PINGRESP:
                        pingRespRead(ctx, message);
                        break;
                    default:
                        final MqttMessageType type = message.fixedHeader().messageType();
                        ctx.fireExceptionCaught(new MqttUnexpectedPacketException(type));
                        if (type == MqttMessageType.PINGREQ) {
                            ctx.channel().writeAndFlush(new MqttMessage(MqttFixedHeaders.PINGRESP_HEADER));
                        } else {
                            ctx.close();
                        }
                        break;
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 上行
        if (msg instanceof MqttPromise<?>) {
            switch (((MqttPromise<?>) msg).messageType()) {
                case CONNECT:
                    write(ctx, (MqttConnectPromise) msg, promise);
                    break;
                case PUBLISH:
                    write(ctx, (MqttPublishPromise) msg, promise);
                    break;
                case PUBACK:
                    write(ctx, (MqttPubAckPromise) msg, promise);
                    break;
                case SUBSCRIBE:
                    write(ctx, (MqttSubscribePromise) msg, promise);
                    break;
                case UNSUBSCRIBE:
                    write(ctx, (MqttUnsubscribePromise) msg, promise);
                    break;
                default:
                    ctx.write(msg, promise);
                    break;
            }
        } else if (msg instanceof MqttMessage) {
            writeAndTouch(ctx, msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void write(ChannelHandlerContext ctx, MqttConnectPromise msg, ChannelPromise channel) {
        // CONNECT
        if (isConnected()) {
            // TODO 改造
            msg.setFailure(new AlreadyConnectedException());
        } else if (!connectPromise.compareAndSet(null, msg)) {
            // TODO 改造；并发，保证之前没有connect，当前compareAndSet成功后，走到最后的else分支
            msg.setFailure(new ConnectionPendingException());
        } else {
            final MqttConnectPromise promise = setTimer(msg);
            final MqttConnectMessage message;
            // channel(cancel, failure) -> promise
            channel.addListener(new PromiseCanceller<>(promise));
            // 创建mqtt connect消息
            message = new MqttConnectMessage(
                    MqttFixedHeaders.CONNECT_HEADER,
                    new MqttConnectVariableHeader(
                            "MQTT",
                            4,
                            true,
                            true,
                            false,
                            0,
                            false,
                            true,
                            CommonConstant.MQTT_KEEPALIVE_TIME),
                    new MqttConnectPayload(
                            msg.clientId(),
                            null,
                            null,
                            msg.username(),
                            msg.password()));
            promise.addListener(new ConnectStateUpdater());
            writeAndTouch(ctx, message, channel);
        }
    }

    private void write(ChannelHandlerContext ctx, MqttPublishPromise msg, ChannelPromise channel) {
        // PUBLISH
        if (!isConnected()) {
            msg.setFailure(new NotYetConnectedException());
        } else {
            final MqttArticle article = msg.article();
            final int packetId = msg.packetId();
            final ByteBuf payload;
            final MqttPublishPromise promise;
            final MqttPublishMessage message;
            // QoS 0
            payload = article.payload();
            promise = msg;
            channel.addListener(new PromiseNotifier(promise));
            // 创建mqtt publish消息
            message = new MqttPublishMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH,
                            promise.isDuplicate(),
                            MqttQoS.AT_MOST_ONCE,
                            false,
                            0),
                    new MqttPublishVariableHeader(article.topic(), packetId),
                    payload);
            writeAndTouch(ctx, message, channel);
        }
    }

    private void write(ChannelHandlerContext ctx, MqttPubAckPromise msg, ChannelPromise channel) {
        // PUBACK
        if (!isConnected()) {
            msg.setFailure(new NotYetConnectedException());
        } else {
            final int packetId = msg.packetId();
            if (null != pubAckPromises.putIfAbsent(packetId, msg)) {
                msg.setFailure(new MqttDuplicatePacketException(MqttMessageType.PUBACK, packetId));
            } else {
                final MqttPubAckPromise promise = setTimer(msg);
                final MqttMessage message;
                promise.addListener(new PromiseRemover<>(pubAckPromises, packetId, promise));
                // channel(cancel, failure) -> promise
                channel.addListener(new PromiseCanceller<>(promise));
                // 创建mqtt puback消息
                message = new MqttMessage(MqttFixedHeaders.PUBACK_HEADER, MqttMessageIdVariableHeader.from(packetId));
                writeAndTouch(ctx, message, channel);
                promise.trySuccess(null);
            }
        }
    }

    private void write(ChannelHandlerContext ctx, MqttSubscribePromise msg, ChannelPromise channel) {
        if (!isConnected()) {
            msg.setFailure(new NotYetConnectedException());
        } else {
            final int packetId;
            if (msg.getPacketId() != null) {
                packetId = msg.getPacketId();
            } else {
                packetId = subscribeId.getAndIncrement();
            }
            if (null != subscribePromises.putIfAbsent(packetId, msg)) {
                msg.setFailure(new MqttDuplicatePacketException(MqttMessageType.SUBSCRIBE, packetId));
            } else {
                final MqttSubscribePromise promise = setTimer(msg);
                final MqttSubscribeMessage message;
                promise.addListener(new PromiseRemover<>(subscribePromises, packetId, promise));
                // channel(cancel, failure) -> promise
                channel.addListener(new PromiseCanceller<>(promise));
                // 创建mqtt subscribe消息
                List<MqttTopicSubscription> subscriptions = new ArrayList<>();
                for (MqttSubscribe subscription : msg.subscriptions()) {
                    if (Utils.isEmpty(subscription.topicFilter())) {
                        continue;
                    }
                    subscriptions.add(new MqttTopicSubscription(subscription.topicFilter(), subscription.qos()));
                }
                MqttFixedHeader fixHeader;
                if (msg.getQosLevel() != null) {
                    fixHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, msg.getQosLevel(), false, -1);
                } else {
                    fixHeader = MqttFixedHeaders.SUBSCRIBE_HEADER;
                }
                message = new MqttSubscribeMessage(
                        fixHeader,
                        MqttMessageIdVariableHeader.from(packetId),
                        new MqttSubscribePayload(subscriptions));
                writeAndTouch(ctx, message, channel);
            }
        }
    }

    private void write(ChannelHandlerContext ctx, MqttUnsubscribePromise msg, ChannelPromise channel) {
        if (!isConnected()) {
            msg.setFailure(new NotYetConnectedException());
        } else {
            final int packetId = unsubscribeId.getAndIncrement();
            if (null != unsubscribePromises.putIfAbsent(packetId, msg)) {
                msg.setFailure(new MqttDuplicatePacketException(MqttMessageType.UNSUBSCRIBE, packetId));
            } else {
                final Promise<MqttUnsubAckMessage> promise = setTimer(msg);
                final MqttUnsubscribeMessage message;
                promise.addListener(new PromiseRemover<>(unsubscribePromises, packetId, promise));
                // channel(cancel, failure) -> promise
                channel.addListener(new PromiseCanceller<>(promise));
                // 创建mqtt unsubscribe消息
                message = new MqttUnsubscribeMessage(
                        MqttFixedHeaders.UNSUBSCRIBE_HEADER,
                        MqttMessageIdVariableHeader.from(packetId),
                        new MqttUnsubscribePayload(msg.topicFilters()));
                writeAndTouch(ctx, message, channel);
            }
        }
    }

    private void writeAndTouch(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(msg, promise.unvoid()).addListener(writeListener);
    }

    private static class LastWriteTimeUpdater implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private void connAckRead(ChannelHandlerContext ctx, MqttConnAckMessage msg) throws Exception {
        if (isConnected()) {
            // TODO
            unexpectedPacketHandler.connAck(ctx, new AlreadyConnectedException());
        } else {
            final MqttConnectPromise promise = connectPromise.getAndSet(null);
            if (promise == null) {
                unexpectedPacketHandler.connAck(ctx, new NoSuchElementException("No promise"));
            } else {
                final MqttConnAckVariableHeader variableHeader = msg.variableHeader();
                final MqttConnectReturnCode returnCode = variableHeader.connectReturnCode();
                promise.trySuccess(new MqttConnectResult(variableHeader.isSessionPresent(), returnCode));
            }
        }
    }

    private void pubAckRead(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        final int packetId = ((MqttMessageIdVariableHeader) msg.variableHeader()).messageId();
        if (!isConnected()) {
            unexpectedPacketHandler.pubAck(ctx, packetId, new NotYetConnectedException());
        } else {
            final MqttPublishPromise promise = publishPromises.remove(packetId);
            if (promise == null) {
                unexpectedPacketHandler.pubAck(ctx, packetId, new NoSuchElementException("No promise"));
            } else {
                final MqttArticle article = promise.article();
                final MqttQoS qos = article.qos();
                if (qos != MqttQoS.AT_LEAST_ONCE) {
                    promise.tryFailure(new MqttUnexpectedQosException(MqttMessageType.PUBACK, packetId, qos));
                } else if (promise.trySuccess(new MqttPublishResult(MqttMessageType.PUBACK, packetId)) || promise.isSuccess()) {
                    promise.article().release();
                }
            }
        }
    }

    private void subAckRead(ChannelHandlerContext ctx, MqttSubAckMessage msg) throws Exception {
        final int packetId = msg.variableHeader().messageId();
        if (!isConnected()) {
            unexpectedPacketHandler.subAck(ctx, packetId, new NotYetConnectedException());
        } else {
            final MqttSubAckPayload payload = msg.payload();
            final MqttSubscribePromise promise = subscribePromises.remove(packetId);
            if (promise == null) {
                unexpectedPacketHandler.subAck(ctx, packetId, new NoSuchElementException("No promise"));
            } else {
                final List<Integer> results = payload.grantedQoSLevels();
                final int actual = results.size();
                final int expected = promise.subscriptions().size();
                final MqttQoS[] returnCodes = new MqttQoS[actual];
                for (int index = 0; index < actual; index++) {
                    returnCodes[index] = MqttQoS.valueOf(results.get(index));
                }
                if (actual != expected) {
                    promise.tryFailure(new MqttSubscribeException(
                            "Number of return codes do not match: " + actual + " (expected: " + expected + ")",
                            returnCodes));
                } else {
                    promise.trySuccess(returnCodes);
                }
            }
        }
    }

    private void unsubAckRead(ChannelHandlerContext ctx, MqttUnsubAckMessage msg) throws Exception {
        final int packetId = msg.variableHeader().messageId();
        if (!isConnected()) {
            unexpectedPacketHandler.unsubAck(ctx, packetId, new NotYetConnectedException());
        } else {
            final Promise<MqttUnsubAckMessage> promise = unsubscribePromises.remove(packetId);
            if (promise == null) {
                unexpectedPacketHandler.unsubAck(ctx, packetId, new NoSuchElementException("No promise"));
            } else {
                promise.trySuccess(new MqttUnsubAckMessage(msg.fixedHeader(), msg.variableHeader()));
            }
        }
    }

    private void pingRespRead(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        final Promise<MqttPingResult> promise = pingPromises.poll();
        DecoderResult decoderResult = msg.decoderResult();
        MqttFixedHeader mqttFixedHeader = msg.fixedHeader();
        if (promise != null && mqttFixedHeader != null && decoderResult.isSuccess()) {
            promise.trySuccess(new MqttPingResult(mqttFixedHeader.messageType(), mqttFixedHeader.isDup(), mqttFixedHeader.qosLevel(), mqttFixedHeader.isRetain(), mqttFixedHeader.remainingLength()));
        }
    }

    private class ConnectStateUpdater implements FutureListener<MqttConnectResult> {
        @Override
        public void operationComplete(Future<MqttConnectResult> connect) throws Exception {
            connected = connect.isSuccess();
            connectPromise.set(null);
        }
    }

    private <P extends MqttPromise<V>, V> P setTimer(P promise) {
        final Timeout timeout = promise.createTimeout(timer());
        if (timeout != null) {
            promise.addListener(new TimeoutCanceller<>(timeout));
        }
        return promise;
    }

    private Timer timer() {
        return timer;
    }

    private boolean isConnected() {
        return connected;
    }

}
