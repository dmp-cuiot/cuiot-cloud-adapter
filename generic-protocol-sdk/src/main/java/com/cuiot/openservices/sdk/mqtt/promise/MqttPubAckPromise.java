package com.cuiot.openservices.sdk.mqtt.promise;

import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.util.Timeout;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author unicom
 */
public class MqttPubAckPromise extends MqttPromise<Void> {

    private final int packetId;

    public MqttPubAckPromise(EventExecutor executor, long timeout, TimeUnit unit, int packetId) {
        super(executor);
        this.packetId = packetId;
    }

    @Override
    public final MqttMessageType messageType() {
        return MqttMessageType.PUBACK;
    }

    public int packetId() {
        return packetId;
    }

    @Override
    public void run(Timeout timeout) {
        tryFailure(new TimeoutException("PubAck timeout"));
    }
}
