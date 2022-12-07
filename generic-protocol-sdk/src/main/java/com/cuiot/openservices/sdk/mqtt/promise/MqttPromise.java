package com.cuiot.openservices.sdk.mqtt.promise;

import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.TimeUnit;

/**
 * @author unicom
 */
public abstract class MqttPromise<V> extends DefaultPromise<V> implements TimerTask {

    public MqttPromise() {
    }

    protected MqttPromise(EventExecutor executor) {
        super(executor);
    }

    /**
     * 消息类型
     *
     * @return MqttMessageType
     */
    public abstract MqttMessageType messageType();

    public Timeout createTimeout(Timer timer) {
        return timer.newTimeout(this, 10, TimeUnit.SECONDS);
    }

}
